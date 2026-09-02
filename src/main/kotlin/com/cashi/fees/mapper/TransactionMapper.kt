package com.cashi.fees.mapper

import com.cashi.fees.api.dto.TransactionFeeRequest
import com.cashi.fees.domain.AssetType
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import org.springframework.stereotype.Component

@Component
class TransactionMapper {

    fun TransactionFeeRequest.toDomain(accountPrefix: String) = Transaction(
        transactionId = transactionId,
        amount = amount,
        asset = asset,
        assetType = AssetType.valueOf(assetType.uppercase()),
        type = type,
        state = state ?: TransactionState.PENDING_FEE,
        createdAt = createdAt?.toString() ?: "",
        accountId = accountId ?: "${accountPrefix}_${asset.lowercase()}",
    )
}