package com.cashi.fees.workflow

import com.cashi.fees.domain.FeeCharge
import com.cashi.fees.domain.FeeQuote
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import com.cashi.fees.services.FeeCalculationService
import com.cashi.fees.services.FeeChargeService
import com.cashi.fees.services.FeeRecordService
import dev.restate.sdk.annotation.Workflow
import dev.restate.sdk.common.TerminalException
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateComponent
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@RestateComponent
@Workflow
class FeeWorkFlow(private val feeRecordService: FeeRecordService) {

    @Workflow
    @OptIn(ExperimentalTime::class)
    suspend fun run(transaction: Transaction): FeeResult {

        val compensations = mutableListOf<suspend () -> Unit>()

        try {

            // step 1 : get the quote ( if fail throw terminal )
            val quote = service<FeeCalculationService>()
                .calculate(
                    FeeCalculationService.CalculationRequest(
                        transaction.type,
                        transaction.amount,
                        transaction.asset
                    )
                )

            state().set(QUOTE, quote)

            // step 2 : record the quote

            val quotedAt =
                Clock.Restate.now() // this is needed in case of replays ( if failed after configured retries then terminal)
            runBlock("record-quote", STEP_RETRY) { feeRecordService.recordQuote(transaction, quote, quotedAt) }

            compensations.add { virtualObject<FeeChargeService>(transaction.transactionId).refund() }

            // step 3 : charge the fees ( if failed after retries then mark the record as failed TransactionState.FEE_FAILED then throw the terminal )
            val charge = virtualObject<FeeChargeService>(transaction.transactionId).charge(
                FeeChargeService.ChargeRequest(transaction.transactionId, quote.fee, transaction.asset)
            )

            // this state id different from the state in the virtual object
            state().set(CHARGE, charge)

            // step 4 : mark the fee as charged ( if this failed after configured retries then do saga pattern and un-charge the transaction
            val chargedAt = Clock.Restate.now();
            runBlock("mark-charged", STEP_RETRY) {
                feeRecordService.markCharged(transaction, quote, charge, chargedAt)
            }

            return FeeResult(transaction.transactionId, quote, charge, TransactionState.SETTLED)
        } catch (e: TerminalException) {

            compensations.asReversed().forEach { it() }
            val chargeId = state().get(CHARGE)?.chargeId
            runCatching {
                runBlock("mark-failed", STEP_RETRY) {
                    feeRecordService.markFailed(
                        transaction.transactionId,
                        chargeId
                    )
                }
            }

            throw e
        }

    }

    companion object {
        private val QUOTE = stateKey<FeeQuote>("quote")
        private val CHARGE = stateKey<FeeCharge>("charge")

        private val STEP_RETRY = RetryPolicy(
            initialDelay = 200.milliseconds,
            exponentiationFactor = 2.0f,
            maxDelay = 5.seconds,
            maxAttempts = 8,
            maxDuration = 30.seconds
        )
    }

    @Serializable
    data class FeeResult(
        val transactionId: String,
        val quote: FeeQuote,
        val charge: FeeCharge,
        val state: String,
    )

}