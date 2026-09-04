package com.cashi.fees.domain

import com.cashi.fees.domain.fees.FeeRuleRegistry
import com.cashi.fees.domain.fees.PercentageFeeRule
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeeRuleRegistryTest {
    private val rule = PercentageFeeRule(BigDecimal("0.0015"),"0.15%")
    private val registry = FeeRuleRegistry(mapOf("Mobile Top Up" to rule))

    @ParameterizedTest
    @ValueSource(strings = ["Mobile Top Up", "mobile top up", "MOBILE TOP UP", "  Mobile Top Up  "])
    fun `lookup ignores case and surrounding whitespace`(input: String) {
        assertEquals(rule, registry.ruleFor(input))
    }

    @Test
    fun `unknown type throws with the original input in the message`() {
        val e = assertFailsWith<NoSuchElementException> { registry.ruleFor("Crypto Swap") }
        assertTrue(e.message!!.contains("Crypto Swap"))
    }

    @Test
    fun `supported types are exposed normalized to uppercase`() {
        assertEquals(setOf("MOBILE TOP UP"), registry.supportedTypes())
    }
}