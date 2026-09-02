package com.cashi.fees.domain.fees

import com.cashi.fees.domain.FeeQuote
import java.math.BigDecimal
import java.math.RoundingMode

interface FeeRule {

    // this is used instead of declaring static fields
    companion object {
        val ROUNDING: RoundingMode = RoundingMode.HALF_UP
        const val SCALE: Int = 2
    }

    fun quote(amount: BigDecimal): FeeQuote
}