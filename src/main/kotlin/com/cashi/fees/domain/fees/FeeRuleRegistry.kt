package com.cashi.fees.domain.fees

import com.cashi.fees.shared.Utils

class FeeRuleRegistry(rules: Map<String, FeeRule>) {

    private val byType: Map<String, FeeRule> =
        rules.mapKeys { (type, _) -> Utils.normalize(type) }

    fun ruleFor(transactionType: String): FeeRule =
        byType[Utils.normalize(transactionType)]
            ?: throw NullPointerException("No fee rule for $transactionType")

    fun supportedTypes(): Set<String> = byType.keys
}