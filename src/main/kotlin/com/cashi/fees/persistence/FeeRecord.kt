package com.cashi.fees.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "fee_records")
class FeeRecord(
    @Id
    @Column(name = "transaction_id")
    var transactionId: String = "",

    @Column(nullable = false, precision = 20, scale = 4)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var asset: String = "",

    @Column(name = "transaction_type", nullable = false)
    var transactionType: String = "",

    @Column(nullable = false, precision = 20, scale = 4)
    var fee: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 12, scale = 6)
    var rate: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var description: String = "",

    @Column(name = "charge_id", nullable = true)
    var chargeId: String? = null,

    @Column(nullable = false)
    var state: String = "",

    @Column(name = "recorded_at", nullable = false)
    var recordedAt: Instant = Instant.EPOCH,

    @Column(name = "charged_at", nullable = false)
    var chargedAt: Instant = Instant.EPOCH,
)