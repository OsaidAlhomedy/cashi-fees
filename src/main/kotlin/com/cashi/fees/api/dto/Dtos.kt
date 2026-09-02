package com.cashi.fees.api.dto

import com.cashi.fees.api.validation.EnumValue
import com.cashi.fees.api.validation.KnownTransactionType
import com.cashi.fees.domain.AssetType
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionFeeRequest(
    @field:NotBlank val transactionId: String,
    @field:NotNull var amount: BigDecimal,
    @field:NotBlank val asset: String,
    @field:NotBlank @field:EnumValue(AssetType::class) val assetType: String,
    @field:NotBlank @field:KnownTransactionType val type: String,
    val state: String? = null,
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSS][.SSS]")
    val createdAt: LocalDateTime? = null,
    val accountId: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TransactionFeeResponse(
    val transactionId: String,
    val amount: BigDecimal,
    val asset: String,
    val type: String,
    val fee: BigDecimal,
    val rate: BigDecimal,
    val description: String,
)

data class ApiError(
    val error: String,
    val message: String,
    val transactionId: String? = null,
)