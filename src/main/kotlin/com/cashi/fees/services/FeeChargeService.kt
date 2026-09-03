package com.cashi.fees.services

import com.cashi.fees.domain.BigDecimalSerializer
import com.cashi.fees.domain.FeeCharge
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.VirtualObject
import dev.restate.sdk.common.TerminalException
import dev.restate.sdk.kotlin.random
import dev.restate.sdk.kotlin.state
import dev.restate.sdk.kotlin.stateKey
import dev.restate.sdk.springboot.RestateComponent
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import kotlin.uuid.ExperimentalUuidApi

@RestateComponent
@VirtualObject
class FeeChargeService {

    @Serializable
    data class ChargeRequest(
        val transactionId: String,
        @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
        val asset: String,
    )

    @Handler
    suspend fun charge(request: ChargeRequest): FeeCharge {
        val s = state()
        if (s.get(ALREADY_CHARGED) == true) {
            throw TerminalException(TerminalException.ABORTED_CODE, "Fee already charged")
        }
        val chargeId = "chg_" + random().nextUUID().toString().take(8)
        s.set(ALREADY_CHARGED, true)
        return FeeCharge(chargeId, request.amount, request.asset)
    }

    companion object {
        private val ALREADY_CHARGED = stateKey<Boolean>("charged")
    }
}