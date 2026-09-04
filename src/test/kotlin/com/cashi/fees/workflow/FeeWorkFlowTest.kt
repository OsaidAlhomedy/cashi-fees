package com.cashi.fees.workflow

import com.cashi.fees.domain.*
import com.cashi.fees.domain.fees.FeeRuleRegistry
import com.cashi.fees.domain.fees.PercentageFeeRule
import com.cashi.fees.persistence.FeeRecordRepository
import com.cashi.fees.services.FeeCalculationService
import com.cashi.fees.services.FeeChargeService
import com.cashi.fees.services.FeeRecordService
import dev.restate.client.Client
import dev.restate.client.IngressException
import dev.restate.client.kotlin.attachSuspend
import dev.restate.client.kotlin.response
import dev.restate.client.kotlin.workflow
import dev.restate.client.kotlin.workflowHandle
import dev.restate.sdk.common.TerminalException
import dev.restate.sdk.testing.BindService
import dev.restate.sdk.testing.RestateTest
import dev.restate.sdk.testing.RestateURL
import dev.restate.serde.kotlinx.KotlinSerializationSerdeFactory
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Timeout
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

@RestateTest(containerImage = "docker.restate.dev/restatedev/restate:latest")
class FeeWorkFlowTest {

    private val records = FakeFeeRecordService()

    private lateinit var client: Client

    @BeforeAll
    fun setUp(@RestateURL url: String) {
        client = Client.connect(url, KotlinSerializationSerdeFactory())
    }

    @BindService
    val workflow = FeeWorkFlow(records)

    @BindService
    val calculation = FeeCalculationService(
        FeeRuleRegistry(mapOf("Mobile Top Up" to PercentageFeeRule(BigDecimal("0.0015"), "0.15%")))
    )

    @BindService
    val charges = FeeChargeService()

    @BeforeEach
    fun resetFake() = records.reset()

    private fun txn(id: String, type: String = "Mobile Top Up", amount: String = "1000") =
        Transaction(
            id, BigDecimal(amount), "USD", AssetType.FIAT, type,
            TransactionState.PENDING_FEE, "2026-09-04 10:00:00"
        )


    @Test // happy path
    @Timeout(60)
    fun `prices, charges and records the fee`() = runBlocking {
        val result = client.workflow<FeeWorkFlow>("txn-happy").run(txn("txn-happy"))

        assertEquals(BigDecimal("1.50"), result.quote.fee)
        assertEquals(BigDecimal("0.0015"), result.quote.rate)
        assertTrue(result.charge.chargeId.startsWith("chg_"))
        assertEquals(TransactionState.SETTLED, result.state)

        // The four steps ran in order, and no failure path was touched.
        assertEquals(listOf("recordQuote", "markCharged"), records.calls)
    }

    @Test
    @Timeout(60)
    fun `resubmitting the same transaction id conflicts, and attaching returns the original result`() = runBlocking {
        val id = "txn-duplicate"
        val first = client.workflow<FeeWorkFlow>(id).run(txn(id))

        // Second submit of the same workflow key: Restate rejects it.
        val conflict = assertFailsWith<IngressException> {
            client.workflow<FeeWorkFlow>(id).run(txn(id))
        }
        assertEquals(409, conflict.statusCode)

        // ...and this is exactly what the controller does in its catch block.
        val attached = client.workflowHandle<FeeWorkFlow.FeeResult>("FeeWorkFlow", id)
            .attachSuspend().response

        assertEquals(first.charge.chargeId, attached.charge.chargeId)
        assertEquals(first, attached)

        // The fee was recorded exactly once despite two submissions.
        assertEquals(listOf("recordQuote", "markCharged"), records.calls)


    }

    @Test
    @Timeout(60)
    fun `an unpriced transaction type is rejected without recording anything`() = runBlocking {
        val id = "txn-unpriced"

        val e = assertFailsWith<IngressException> {
            client.workflow<FeeWorkFlow>(id).run(txn(id, type = "Crypto Swap"))
        }
        assertEquals(400, e.statusCode)   // TerminalException(BAD_REQUEST_CODE) surfaces as 400

        // Step 1 threw, so nothing was quoted...
        assertTrue(records.calls.none { it == "recordQuote" })
        // ...but the catch block still ran, with no charge to reference.
        assertEquals(listOf<String?>(null), records.markFailedChargeIds)
    }

    @Test
    @Timeout(60)
    fun `a terminal failure after charging rolls back and marks the record failed`() = runBlocking {
        val id = "txn-saga"
        records.markChargedFailure = TerminalException(500, "ledger down")

        assertFailsWith<IngressException> { client.workflow<FeeWorkFlow>(id).run(txn(id)) }

        // markFailed received a real charge id, which proves the catch block:
        //   1. ran the compensation list (refund) without throwing, THEN
        //   2. read state().get(CHARGE) - so step 3 had completed.
        // markFailed is unreachable if refund() had failed.
        assertEquals(1, records.markFailedChargeIds.size)
        assertTrue(records.markFailedChargeIds.single()!!.startsWith("chg_"))
        assertEquals(listOf("recordQuote", "markFailed"), records.calls)

    }

    @Test
    @Timeout(60)
    fun `a transient failure in a durable step is retried, not surfaced`() = runBlocking {
        val id = "txn-retry"
        records.transientRecordQuoteFailures.set(2)   // fail twice, succeed on the third

        val result = client.workflow<FeeWorkFlow>(id).run(txn(id))

        assertEquals(BigDecimal("1.50"), result.quote.fee)
        assertEquals(3, records.recordQuoteAttempts.get())
        // ~600ms of backoff: 200ms + 400ms, per STEP_RETRY
    }

}

class FakeFeeRecordService : FeeRecordService(mock(FeeRecordRepository::class.java)) {
    val calls = CopyOnWriteArrayList<String>()
    val recordQuoteAttempts = AtomicInteger()
    val markFailedChargeIds = CopyOnWriteArrayList<String?>()

    val transientRecordQuoteFailures = AtomicInteger(0)

    @Volatile
    var markChargedFailure: RuntimeException? = null

    fun reset() {
        calls.clear()
        recordQuoteAttempts.set(0)
        markFailedChargeIds.clear()
        transientRecordQuoteFailures.set(0)
        markChargedFailure = null
    }

    override fun recordQuote(txn: Transaction, quote: FeeQuote, at: Instant) {
        recordQuoteAttempts.incrementAndGet()
        if (transientRecordQuoteFailures.getAndDecrement() > 0) {
            throw RuntimeException("transient db blip")   // non-terminal -> Restate retries
        }
        calls += "recordQuote"
    }

    override fun markCharged(txn: Transaction, quote: FeeQuote, charge: FeeCharge, at: Instant) {
        markChargedFailure?.let { throw it }
        calls += "markCharged"
    }

    override fun markFailed(txnId: String, chargeId: String?) {
        calls += "markFailed"
        markFailedChargeIds += chargeId
    }
}
