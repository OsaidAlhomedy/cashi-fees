package com.cashi.fees.api

import com.cashi.fees.api.dto.TransactionFeeRequest
import com.cashi.fees.api.dto.TransactionFeeResponse
import com.cashi.fees.mapper.TransactionMapper
import com.cashi.fees.workflow.FeeWorkFlow
import dev.restate.client.Client
import dev.restate.client.kotlin.workflow
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionFeeController(private val restateClient: Client,private val mapper: TransactionMapper) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "Calculate and charge the fee for a settled transaction")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(examples = [ExampleObject(name = "Mobile top up", value = """
        {
          "transaction_id": "txn_001",
          "amount": 1000,
          "asset": "USD",
          "asset_type": "FIAT",
          "type": "Mobile Top Up",
          "state": "SETTLED - PENDING FEE",
          "created_at": "2023-08-30 15:42:17.610059"
        }
    """)])]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fee calculated and charged"),
        ApiResponse(responseCode = "400", description = "Validation failed or transaction type not priced"),
        ApiResponse(responseCode = "409", description = "Fee already charged for this transaction"),
    )
    @PostMapping("/transaction/fee")
    fun chargeFee(@Valid @RequestBody request : TransactionFeeRequest) : ResponseEntity<TransactionFeeResponse> {


        val txn = mapper.toDomain(request)

        log.info("Submitting fee workflow for {}", request)

        val response = runBlocking {
            val result = restateClient.workflow<FeeWorkFlow>(txn.transactionId).run(txn)
            mapper.toResponse(txn, result)
        }

        return ResponseEntity.ok(response)
    }
}