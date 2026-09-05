package com.cashi.fees.services

import com.cashi.fees.domain.*
import com.cashi.fees.persistence.FeeRecord
import com.cashi.fees.persistence.FeeRecordRepository
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FeeRecordServiceTest {

    private val repository = mock(FeeRecordRepository::class.java)
    private val service = FeeRecordService(repository)

    private val at = Instant.parse("2026-09-04T10:00:00Z")
    private val txn = Transaction(
        "txn_1", BigDecimal("1000"), "USD", AssetType.FIAT,
        "  mobile top up  ", TransactionState.PENDING_FEE, ""
    )
    private val quote = FeeQuote(BigDecimal("1.50"), BigDecimal("0.0015"), "0.15%")
    private val charge = FeeCharge("chg_abc12345", BigDecimal("1.50"), "USD")
    private val record = FeeRecord(
        "txn_1",
        BigDecimal("1000"),
        "USD",
        "MOBILE TOP UP",
        BigDecimal("1.50"),
        BigDecimal("0.0015"),
        "Standard fee rate of 0.15%",
        "chg_abc12345",
        TransactionState.SETTLED
    )

    private fun captor(): ArgumentCaptor<FeeRecord> = ArgumentCaptor.forClass(FeeRecord::class.java)


    // quote recording

    @Test
    fun `recordQuote is a no-op when the record already exists`() {
        `when`(repository.findById("txn_1")).thenReturn(Optional.of(record))

        service.recordQuote(txn, quote, at)

        verify(repository, never()).save(any())
    }

    @Test
    fun `recordQuote stores a pending record with the type normalized`() {
        `when`(repository.existsById("txn_1")).thenReturn(false)

        service.recordQuote(txn, quote, at)

        val saved = captor()
        verify(repository).save(saved.capture())
        assertEquals(TransactionState.PENDING_FEE, saved.value.state)
        assertEquals("MOBILE TOP UP", saved.value.transactionType)
        assertEquals(BigDecimal("1.50"), saved.value.fee)
    }

    // charge marking
    @Test
    fun `markCharged settles an existing record`() {
        val existing = FeeRecord(transactionId = "txn_1", state = TransactionState.PENDING_FEE)
        `when`(repository.findById("txn_1")).thenReturn(Optional.of(existing))

        service.markCharged(txn, quote, charge, at)

        val saved = captor()
        verify(repository).save(saved.capture())
        assertEquals(TransactionState.SETTLED, saved.value.state)
        assertEquals("chg_abc12345", saved.value.chargeId)
    }

    @Test
    fun `markCharged recreates the record when the quote row is missing`() {
        `when`(repository.findById("txn_1")).thenReturn(Optional.empty())

        service.markCharged(txn, quote, charge, at)

        val saved = captor()
        verify(repository).save(saved.capture())
        assertEquals(TransactionState.SETTLED, saved.value.state)
        assertEquals(BigDecimal("1.50"), saved.value.fee)   // rebuilt from the quote
    }

    // failed marking

    @Test
    fun `markFailed does nothing when there is no record`() {
        `when`(repository.findById("txn_1")).thenReturn(Optional.empty())

        service.markFailed("txn_1", "chg_abc12345")

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `markFailed keeps the existing chargeId when none is supplied`() {
        val existing =
            FeeRecord(transactionId = "txn_1", chargeId = "chg_original", state = TransactionState.PENDING_FEE)
        `when`(repository.findById("txn_1")).thenReturn(Optional.of(existing))

        service.markFailed("txn_1", null)

        val saved = captor()
        verify(repository).save(saved.capture())
        assertEquals(TransactionState.FEE_FAILED, saved.value.state)
        assertEquals("chg_original", saved.value.chargeId)
    }
}