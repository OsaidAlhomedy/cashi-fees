package com.cashi.fees.services

import com.cashi.fees.domain.BigDecimalSerializer
import com.cashi.fees.domain.FeeQuote
import com.cashi.fees.domain.fees.FeeRuleRegistry
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.Service
import dev.restate.sdk.common.TerminalException
import dev.restate.sdk.springboot.RestateComponent
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@RestateComponent
@Service
class FeeCalculationService(
    private val registry: FeeRuleRegistry,
) {

    @Serializable
    data class CalculationRequest(
        val transactionType: String,
        @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
        val asset: String,
    )

    @Handler
    suspend fun calculate(request: CalculationRequest): FeeQuote {
        return try {
            registry.ruleFor(request.transactionType).quote(request.amount)
        } catch (e: NullPointerException) {
            throw TerminalException(TerminalException.BAD_REQUEST_CODE, e.message)
        }
    }

}

