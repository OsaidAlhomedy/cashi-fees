package com.cashi.fees.services

import com.cashi.fees.domain.FeeCharge
import com.cashi.fees.domain.FeeQuote
import com.cashi.fees.domain.Transaction
import com.cashi.fees.domain.TransactionState
import com.cashi.fees.persistence.FeeRecord
import com.cashi.fees.persistence.FeeRecordRepository
import com.cashi.fees.shared.Utils
import dev.restate.sdk.common.TerminalException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.time.Instant
import kotlin.time.toJavaInstant


@Service
class FeeRecordService(private val repository: FeeRecordRepository) {

    @Transactional
    fun recordQuote(txn: Transaction, quote: FeeQuote, at: Instant) {
        if(repository.existsById(txn.transactionId)) return
        repository.save(newRecord(txn, quote,at))
    }

    @Transactional
    fun markCharged(txn: Transaction,quote: FeeQuote, charge: FeeCharge, at: Instant) {
        val record = repository.findById(txn.transactionId)
            .orElseGet { newRecord(txn,quote,at) }
        record.chargeId = charge.chargeId
        record.state = TransactionState.SETTLED
        record.chargedAt = at.toJavaInstant()
    }

    private fun newRecord(txn: Transaction,quote: FeeQuote,at: Instant) = FeeRecord(
        transactionId = txn.transactionId,
        amount = txn.amount,
        asset = txn.asset,
        transactionType = Utils.normalize(txn.type),
        fee = quote.fee,
        rate = quote.rate,
        description = quote.description,
        state = TransactionState.PENDING_FEE,
        recordedAt = at.toJavaInstant(),
    )

}