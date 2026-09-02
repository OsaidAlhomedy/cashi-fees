package com.cashi.fees.domain.fees

import com.cashi.fees.domain.FeeQuote
import com.cashi.fees.domain.fees.FeeRule.Companion.ROUNDING
import com.cashi.fees.domain.fees.FeeRule.Companion.SCALE
import java.math.BigDecimal

class PercentageFeeRule(val rate: BigDecimal, val description: String,) : FeeRule {
    override fun quote(amount: BigDecimal): FeeQuote = FeeQuote(
        fee = (amount * rate).setScale(SCALE, ROUNDING),
        rate = rate,
        description = description,
    )
}