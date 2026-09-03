package com.cashi.fees.mapper

import com.cashi.fees.api.dto.TransactionFeeRequest
import com.cashi.fees.api.dto.TransactionFeeResponse
import com.cashi.fees.domain.AssetType
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import com.cashi.fees.workflow.FeeWorkFlow.FeeResult
import org.springframework.stereotype.Component

@Component
class TransactionMapper {

    fun toDomain(request : TransactionFeeRequest) = Transaction(
        transactionId = request.transactionId,
        amount = request.amount,
        asset = request.asset,
        assetType = AssetType.valueOf(request.assetType.uppercase()),
        type = request.type,
        state = request.state ?: TransactionState.PENDING_FEE,
        createdAt = request.createdAt?.toString() ?: "",
    )

    fun toResponse(txn :Transaction, result : FeeResult) : TransactionFeeResponse {
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
}