package com.cashi.fees.domain

import java.math.BigDecimal

enum class AssetType { FIAT }

data class Transaction(
    val transactionId: String,
    val amount: BigDecimal,
    val asset: String,
    val assetType: AssetType,
    val type: String,
    val state: String,
    val createdAt: String,
    val accountId: String,
)

object TransactionState {
    const val PENDING_FEE = "SETTLED - PENDING FEE"
    const val SETTLED = "SETTLED"
    const val FEE_FAILED = "SETTLED - FEE FAILED"
}

data class FeeQuote(
    val fee: BigDecimal,
    val rate: BigDecimal,
    val description: String,
)