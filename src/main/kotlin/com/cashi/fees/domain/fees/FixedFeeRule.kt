package com.cashi.fees.domain.fees

import com.cashi.fees.domain.FeeQuote
import java.math.BigDecimal

class FixedFeeRule(val rate: BigDecimal, val description: String) : FeeRule {
    override fun quote(amount: BigDecimal): FeeQuote = FeeQuote(
        fee = rate,
        rate = rate,
        description = description,
    )
}