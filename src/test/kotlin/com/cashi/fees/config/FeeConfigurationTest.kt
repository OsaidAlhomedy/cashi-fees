package com.cashi.fees.config

import com.cashi.fees.domain.fees.FeeRuleRegistry
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class FeeConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withInitializer(ConfigDataApplicationContextInitializer())
        .withUserConfiguration(FeeConfiguration::class.java)

    @Test
    fun `the shipped rules bind with their spaces intact`() {
        runner.run { ctx ->
            val registry = ctx.getBean<FeeRuleRegistry>()

            assertEquals(setOf("MOBILE TOP UP", "BILL PAYMENT"), registry.supportedTypes())
            assertEquals(BigDecimal("0.0015"), registry.ruleFor("Mobile Top Up").quote(BigDecimal("1000")).rate)
            assertEquals(BigDecimal("2.00"), registry.ruleFor("Bill Payment").quote(BigDecimal("1000")).fee)
        }
    }

}