package com.cashi.fees.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
   data class FeeStatusResponse(
    val transactionId: String,
    val fee: BigDecimal?,
    val rate: BigDecimal?,
    val description: String?,
    val chargeId: String?,
    val refunded: Boolean?,
    val state: String?,
   )  