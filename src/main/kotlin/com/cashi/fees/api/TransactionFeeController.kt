package com.cashi.fees.api

import com.cashi.fees.api.dto.FeeStatusResponse
import com.cashi.fees.api.dto.TransactionFeeRequest
import com.cashi.fees.api.dto.TransactionFeeResponse
import com.cashi.fees.mapper.TransactionMapper
import com.cashi.fees.workflow.FeeWorkFlow
import dev.restate.client.Client
import dev.restate.client.IngressException
import dev.restate.client.kotlin.attachSuspend
import dev.restate.client.kotlin.response
import dev.restate.client.kotlin.workflow
import dev.restate.client.kotlin.workflowHandle
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.time.Duration.Companion.seconds

@RestController
class TransactionFeeController(private val restateClient: Client, private val mapper: TransactionMapper) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "Calculate and charge the fee for a settled transaction")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [ExampleObject(
                name = "Mobile top up", value = """
        {
          "transaction_id": "txn_001",
          "amount": 1000,
          "asset": "USD",
          "asset_type": "FIAT",
          "type": "Mobile Top Up",
          "state": "SETTLED - PENDING FEE",
          "created_at": "2023-08-30 15:42:17.610059"
        }
    """
            )]
        )]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fee calculated and charged"),
        ApiResponse(responseCode = "400", description = "Validation failed or transaction type not priced"),
        ApiResponse(responseCode = "409", description = "Fee already charged for this transaction"),
    )
    @PostMapping("/transaction/fee")
    fun chargeFee(@Valid @RequestBody request: TransactionFeeRequest): ResponseEntity<TransactionFeeResponse> {


        val txn = mapper.toDomain(request)

        log.info("Submitting fee workflow for {}", request)

        val result = runBlocking {
            withTimeout(30.seconds) {
                try {
                    restateClient.workflow<FeeWorkFlow>(txn.transactionId).run(txn)
                } catch (e: IngressException) {
                    if (e.statusCode != 409) throw e
                    restateClient.workflowHandle<FeeWorkFlow.FeeResult>("FeeWorkFlow", txn.transactionId)
                        .attachSuspend().response
                }
            }
        }

        return ResponseEntity.ok(mapper.toResponse(txn, result))
    }

    @Operation(summary = "Read the current state of a fee workflow")
    @GetMapping("/transaction/{transactionId}/fee")
    fun feeStatus(@PathVariable transactionId: String): ResponseEntity<FeeStatusResponse> {
        val status = runBlocking {
            restateClient.workflow<FeeWorkFlow>(transactionId).status()
        }
        if (status.quote == null && status.charge == null) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapper.toStatusResponse(status))
    }
}
