package com.cashi.fees.api

import com.cashi.fees.api.dto.TransactionFeeRequest
import com.cashi.fees.api.dto.TransactionFeeResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class TransactionFeeController {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/transaction/fee")
    fun chargeFee(@Valid @RequestBody request : TransactionFeeRequest) : ResponseEntity<TransactionFeeResponse> {
        log.info("Submitting fee workflow for {}", request)

        // TODO : map the request, already validated

        // TODO : execute the Restate flow

        // TODO : return the response with 200 status code

        return ResponseEntity.ok().body(TransactionFeeResponse("tx-123", BigDecimal(1000),"USD","Mobile Top Up", BigDecimal(1.5), BigDecimal(0.0015),"Standard fee rate of\n" +
                "0.15%"));
    }
}