package com.cashi.fees.domain

import com.cashi.fees.domain.fees.PercentageFeeRule
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class PercentageFeeRuleTest {

    @ParameterizedTest(name = "{0} at rate {1} -> {2}")
    @CsvSource(
        "1000, 0.0015 ,1.50",
        "1000,    0.0020, 2.00",
        "1000000, 0.0015, 1500.00",
        "0,       0.0015, 0.00",
        "1,       0.0015, 0.00",
        "0.05,    0.10,   0.01",
        "0.04,    0.10,   0.00",
    )
    fun `quotes the fee at the configured rate`(amount: String, rate: String, expected: String) {
        val quote = PercentageFeeRule(BigDecimal(rate), "any").quote(BigDecimal(amount))

        assertEquals(BigDecimal(expected), quote.fee)
    }

    @Test
    fun `fee is always scaled to 2 decimals`() {
        val rule = PercentageFeeRule(BigDecimal("0.0015"), "any")
        assertEquals(2, rule.quote(BigDecimal("7")).fee.scale())
        assertEquals(2, rule.quote(BigDecimal("7.123456")).fee.scale())
    }

    @Test
    fun `rate and description pass through to the quote`() {
        val rule = PercentageFeeRule(BigDecimal("0.0015"), "Standard fee rate of 0.15%")
        val quote = rule.quote(BigDecimal("1000"))

        assertEquals(BigDecimal("0.0015"), quote.rate)
        assertEquals("Standard fee rate of 0.15%", quote.description)
    }
}