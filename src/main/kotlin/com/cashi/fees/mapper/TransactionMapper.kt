package com.cashi.fees.mapper

import com.cashi.fees.api.dto.FeeStatusResponse
import com.cashi.fees.api.dto.TransactionFeeRequest
import com.cashi.fees.api.dto.TransactionFeeResponse
import com.cashi.fees.domain.AssetType
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import com.cashi.fees.workflow.FeeWorkFlow.FeeResult
import com.cashi.fees.workflow.FeeWorkFlow.FeeStatus
import org.springframework.stereotype.Component

@Component
class TransactionMapper {

    fun toDomain(request: TransactionFeeRequest) = Transaction(
        transactionId = request.transactionId,
        amount = request.amount,
        asset = request.asset,
        assetType = AssetType.valueOf(request.assetType.uppercase()),
        type = request.type,
        state = request.state ?: TransactionState.PENDING_FEE,
        createdAt = request.createdAt?.toString() ?: "",
    )

    fun toResponse(txn: Transaction, result: FeeResult): TransactionFeeResponse {
        return TransactionFeeResponse(
            txn.transactionId,
            txn.amount,
            txn.asset,
            txn.type,
            result.quote.fee,
            result.quote.rate,
            result.quote.description
        )
    }

    fun toStatusResponse(status: FeeStatus): FeeStatusResponse {
        return FeeStatusResponse(
            transactionId = status.transactionId,
            fee = status.quote?.fee,
            rate = status.quote?.rate,
            description = status.quote?.description,
            chargeId = status.charge?.chargeId,
            refunded = status.charge?.refunded,
            state = resolveState(status)
        )
    }

    private fun resolveState(status: FeeStatus): String = when {
        status.charge == null -> TransactionState.PENDING_FEE
        status.charge.refunded -> TransactionState.FEE_FAILED
        else -> TransactionState.SETTLED
    }
}