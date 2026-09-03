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
import dev.restate.sdk.common.TerminalException
import dev.restate.sdk.kotlin.*
import dev.restate.sdk.springboot.RestateComponent
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@RestateComponent
@Workflow
class FeeWorkFlow(private val feeRecordService: FeeRecordService) {

    @Workflow
    @OptIn(ExperimentalTime::class)
    suspend fun run(transaction: Transaction): FeeResult {

        // step 1 : get the quote
        val quote = service<FeeCalculationService>()
            .calculate(
                FeeCalculationService.CalculationRequest(
                    transaction.type,
                    transaction.amount,
                    transaction.asset
                )
            )

        state().set(QUOTE, quote)

        // step 2 : record the quote

        val quotedAt = Clock.Restate.now() // this is needed in case of replays
        runBlock("record-quote") { feeRecordService.recordQuote(transaction, quote, quotedAt) }

        // step 3 : charge the fees ( in this step I would debit the wallet but the contract doesn't mention a wallet or account id )
        val charge = try {
            virtualObject<FeeChargeService>(transaction.transactionId).charge(
                FeeChargeService.ChargeRequest(transaction.transactionId, quote.fee, transaction.asset))
        }catch (e: TerminalException){
            // TODO : I need to handle Terminal error by marking the charge failed
            throw e
        }

        state().set(CHARGE, charge)

        // step 4 : mark the fee as charged
        val chargedAt = Clock.Restate.now();
        runBlock("mark-charged") {
            feeRecordService.markCharged(transaction, quote, charge, chargedAt)
        }

        // TODO if more time :  maybe here I can raise an event to down stream services for analytics, reconsilation ... etc

        return FeeResult(transaction.transactionId, quote, charge, TransactionState.SETTLED)

    }

    companion object {
        private val QUOTE = stateKey<FeeQuote>("quote")
        private val CHARGE = stateKey<FeeCharge>("charge")
    }

    @Serializable
    data class FeeResult(
        val transactionId: String,
        val quote: FeeQuote,
        val charge: FeeCharge,
        val state: String,
    )

    @Shared
    suspend fun quote(): FeeQuote? = state().get(QUOTE)

}