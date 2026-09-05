package com.cashi.fees.services

import com.cashi.fees.domain.BigDecimalSerializer
import com.cashi.fees.domain.FeeCharge
import dev.restate.sdk.annotation.Handler
import dev.restate.sdk.annotation.VirtualObject
import dev.restate.sdk.kotlin.random
import dev.restate.sdk.kotlin.state
import dev.restate.sdk.kotlin.stateKey
import dev.restate.sdk.springboot.RestateComponent
import kotlinx.serialization.Serializable
import java.math.BigDecimal

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
        val existing = s.get(CHARGE)
        if (existing != null && !existing.refunded) return existing

        // charging is adding a charge id to the transaction ( no mention for actual charge in the task contract )
        val chargeId = "chg_" + random().nextUUID().toString().take(8)

        val feeCharge = FeeCharge(chargeId, request.amount, request.asset)
        s.set(CHARGE, feeCharge)
        return feeCharge
    }

    @Handler
    suspend fun refund() { // de-charge
        val s = state()
        val charge = s.get(CHARGE) ?: return
        if (charge.refunded) return

        // logic goes here for refunding
        val chargeUpdated = charge.copy(refunded = true)

        s.set(CHARGE, chargeUpdated)
    }

    companion object {
        private val CHARGE = stateKey<FeeCharge>("charge")
    }
}