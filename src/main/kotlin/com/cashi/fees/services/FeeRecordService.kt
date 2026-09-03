package com.cashi.fees.services

import com.cashi.fees.domain.FeeCharge
import com.cashi.fees.domain.FeeQuote
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import com.cashi.fees.persistence.FeeRecord
import com.cashi.fees.persistence.FeeRecordRepository
import com.cashi.fees.shared.Utils
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class FeeRecordService(private val repository: FeeRecordRepository) {

    @Transactional
    fun recordQuote(txn: Transaction, quote: FeeQuote, at: Instant) {

        val existing = repository.findById(txn.transactionId).orElse(null)
        if (existing != null) return

        repository.save(
            FeeRecord(
                transactionId = txn.transactionId,
                amount = txn.amount,
                asset = txn.asset,
                transactionType = Utils.normalize(txn.type),
                fee = quote.fee,
                rate = quote.rate,
                description = quote.description,
                state = TransactionState.PENDING_FEE,
                recordedAt = at,
            )
        )
    }

    @Transactional
    fun markCharged(transactionId: String, charge: FeeCharge, at: Instant) {
        val record = repository.findById(transactionId)
            .orElseThrow { IllegalStateException("No fee record for $transactionId") }
        record.chargeId = charge.chargeId
        record.state = TransactionState.SETTLED
        record.chargedAt = at
    }

}