package com.cashi.fees.api.dto

import com.cashi.fees.api.validation.EnumValue
import com.cashi.fees.api.validation.KnownTransactionType
import com.cashi.fees.domain.AssetType
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionFeeRequest(

    @field:NotBlank
    val transactionId: String,

    @field:NotNull
    @field:DecimalMin(value = "0", inclusive = false, message = "must be greater than zero")
    @field:Digits(integer = 18, fraction = 4, message = "supports at most 4 decimal places")
    var amount: BigDecimal,

    @field:NotBlank
    val asset: String,

    @field:NotBlank
    @field:EnumValue(AssetType::class)
    val assetType: String,

    @field:NotBlank
    @field:KnownTransactionType
    val type: String,

    val state: String? = null,

    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSS][.SSS]")
    val createdAt: LocalDateTime? = null,

    )