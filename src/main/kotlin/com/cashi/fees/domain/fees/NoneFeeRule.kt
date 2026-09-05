package com.cashi.fees.domain.fees

import com.cashi.fees.domain.FeeQuote
import java.math.BigDecimal

class NoneFeeRule (val description: String) : FeeRule {
    override fun quote(amount: BigDecimal): FeeQuote = FeeQuote(
        rate = BigDecimal.ZERO,
        fee = BigDecimal.ZERO,
        description = description
    )
}