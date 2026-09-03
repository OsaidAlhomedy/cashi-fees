package com.cashi.fees.workflow

import com.cashi.fees.domain.FeeCharge
import com.cashi.fees.domain.FeeQuote
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import com.cashi.fees.services.FeeCalculationService
import com.cashi.fees.services.FeeChargeService
import com.cashi.fees.services.FeeRecordService
import dev.restate.sdk.annotation.Shared
import dev.restate.sdk.annotation.Workflow
import dev.restate.sdk.kotlin.runBlock
import dev.restate.sdk.kotlin.service
import dev.restate.sdk.kotlin.state
import dev.restate.sdk.kotlin.stateKey
import dev.restate.sdk.kotlin.virtualObject
import dev.restate.sdk.springboot.RestateComponent
import kotlinx.serialization.Serializable
import java.time.Instant

@RestateComponent
@Workflow
class FeeWorkFlow (private val feeRecordService: FeeRecordService) {

    @Serializable
    data class FeeResult(
        val transactionId: String,
        val quote: FeeQuote,
        val charge: FeeCharge,
        val state: String,
    )

    @Workflow
    suspend fun run(transaction: Transaction): FeeResult {
        val quote = service<FeeCalculationService>()
            .calculate(
                FeeCalculationService.CalculationRequest(
                    transaction.type,
                    transaction.amount,
                    transaction.asset
                )
            )

        state().set(QUOTE, quote)

        val quotedAt = Instant.now();
        runBlock("record-quote") { feeRecordService.recordQuote(transaction, quote, quotedAt) }

        val charge = virtualObject<FeeChargeService>(transaction.transactionId).charge(
            FeeChargeService.ChargeRequest(transaction.transactionId, quote.fee, transaction.asset)
        )
        state().set(CHARGE, charge)

        val chargedAt = Instant.now();
        runBlock("mark-charged") {
            feeRecordService.markCharged(transaction.transactionId, charge, chargedAt)
        }

        // TODO if more time :  maybe here I can raise an event to down stream services for analytics, reconsilation ... etc

        return FeeResult(transaction.transactionId, quote, charge, TransactionState.SETTLED)

    }

    companion object {
        private val QUOTE = stateKey<FeeQuote>("quote")
        private val CHARGE = stateKey<FeeCharge>("charge")
    }

    @Shared
    suspend fun quote(): FeeQuote? = state().get(QUOTE)

}