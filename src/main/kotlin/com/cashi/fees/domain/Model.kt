package com.cashi.fees.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
enum class AssetType { FIAT }

@Serializable
data class Transaction(
    val transactionId: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val asset: String,
    val assetType: AssetType,
    val type: String,
    val state: String,
    val createdAt: String,
)

object TransactionState {
    const val PENDING_FEE = "SETTLED - PENDING FEE"
    const val SETTLED = "SETTLED"
    const val FEE_FAILED = "SETTLED - FEE FAILED"
}

@Serializable
data class FeeQuote(
    @Serializable(with = BigDecimalSerializer::class) val fee: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val rate: BigDecimal,
    val description: String,
)

@Serializable
data class FeeCharge(
    val chargeId: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val asset: String,
)