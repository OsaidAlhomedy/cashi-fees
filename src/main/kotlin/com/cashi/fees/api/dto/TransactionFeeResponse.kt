package com.cashi.fees.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

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