package com.cashi.fees.config

import com.cashi.fees.domain.fees.FeeRule
import com.cashi.fees.domain.fees.FeeRuleRegistry
import com.cashi.fees.domain.fees.PercentageFeeRule
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@ConfigurationProperties(prefix = "cashi.fees")
data class FeeProperties(
    val rules: Map<String, RuleConfig> = emptyMap(),
) {
    data class RuleConfig(
        val type: RuleType,
        val rate: BigDecimal,
        val description: String,
    )

    enum class RuleType { PERCENTAGE }
}

@Configuration
@EnableConfigurationProperties(FeeProperties::class)
class FeeConfiguration {

    @Bean
    fun feeRuleRegistry(props: FeeProperties): FeeRuleRegistry =
        FeeRuleRegistry(props.rules.mapValues { (type, cfg) -> cfg.toRule(type) })

    private fun FeeProperties.RuleConfig.toRule(type: String): FeeRule = when (this.type) {
        FeeProperties.RuleType.PERCENTAGE -> PercentageFeeRule(
            rate = requireNotNull(rate) { "rule '$type' is PERCENTAGE but has no rate" },
            description = description,
        )
    }

    // TODO : MORE RULES CAN BE ADDED LATER ( FIXED .... )
}