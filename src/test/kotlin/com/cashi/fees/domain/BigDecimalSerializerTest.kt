package com.cashi.fees.domain

import com.cashi.fees.workflow.FeeWorkFlow
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class BigDecimalSerializerTest {
    @Test
    fun `money is encoded as a plain string keeping its scale`() {
        val json = Json.encodeToString(FeeQuote(BigDecimal("1.50"), BigDecimal("0.0015"), "0.15%"))

        assertEquals("""{"fee":"1.50","rate":"0.0015","description":"0.15%"}""", json)
    }

    @Test
    fun `FeeResult survives a round trip unchanged`() {
        val original = FeeWorkFlow.FeeResult(
            transactionId = "txn_001",
            quote = FeeQuote(BigDecimal("1.50"), BigDecimal("0.0015"), "0.15%"),
            charge = FeeCharge("chg_abc12345", BigDecimal("1.50"), "USD"),
            state = TransactionState.SETTLED,
        )

        val decoded = Json.decodeFromString<FeeWorkFlow.FeeResult>(Json.encodeToString(original))

        assertEquals(original, decoded)
        assertEquals(2, decoded.quote.fee.scale())
    }
}