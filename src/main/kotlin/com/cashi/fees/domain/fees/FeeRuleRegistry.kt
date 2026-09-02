package com.cashi.fees.domain.fees

class FeeRuleRegistry(rules: Map<String, FeeRule>) {

    private val byType: Map<String, FeeRule> =
        rules.mapKeys { (type, _) -> normalise(type) }

    fun ruleFor(transactionType: String): FeeRule =
        byType[normalise(transactionType)]
            ?: throw NullPointerException("No fee rule for $transactionType")

    fun supportedTypes(): Set<String> = byType.keys

    private fun normalise(type: String) = type.trim().lowercase()
}