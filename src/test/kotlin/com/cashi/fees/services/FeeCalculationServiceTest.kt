package com.cashi.fees.services

import com.cashi.fees.domain.fees.FeeRuleRegistry
import com.cashi.fees.domain.fees.PercentageFeeRule
import dev.restate.sdk.common.TerminalException
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeeCalculationServiceTest {

    private val service = FeeCalculationService(
        FeeRuleRegistry(mapOf("Mobile Top Up" to PercentageFeeRule(BigDecimal("0.0015"), "0.15%")))
    )

    @Test
    fun `quotes a priced transaction type`() = runBlocking {
        val quote = service.calculate(
            FeeCalculationService.CalculationRequest("Mobile Top Up", BigDecimal("1000"), "USD")
        )

        assertEquals(BigDecimal("1.50"), quote.fee)

    }

    @Test
    fun `un-priced type becomes a terminal 400 so Restate does not retry forever`() = runBlocking {
        val e = assertFailsWith<TerminalException> {
            service.calculate(
                FeeCalculationService.CalculationRequest("Crypto Swap", BigDecimal("1000"), "USD")
            )
        }

        assertEquals(TerminalException.BAD_REQUEST_CODE, e.code)
    }
}