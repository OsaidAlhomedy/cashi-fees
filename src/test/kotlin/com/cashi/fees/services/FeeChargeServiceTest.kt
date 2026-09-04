package com.cashi.fees.services

import dev.restate.client.Client
import dev.restate.client.kotlin.virtualObject
import dev.restate.sdk.testing.BindService
import dev.restate.sdk.testing.RestateTest
import dev.restate.sdk.testing.RestateURL
import dev.restate.serde.kotlinx.KotlinSerializationSerdeFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RestateTest(containerImage = "docker.restate.dev/restatedev/restate:latest")
class FeeChargeServiceTest {

    @BindService
    val charges = FeeChargeService()

    private lateinit var client: Client

    @BeforeAll
    fun setUp(@RestateURL url: String) {
        client = Client.connect(url, KotlinSerializationSerdeFactory())
    }

    private fun request(txnId: String) =
        FeeChargeService.ChargeRequest(txnId, BigDecimal("1.50"), "USD")

    @Test
    @Timeout(60)
    fun `charging mints a charge id`() = runBlocking {
        val charge = client.virtualObject<FeeChargeService>("txn-mint").charge(request("txn-mint"))

        assertTrue(charge.chargeId.startsWith("chg_"), "got ${charge.chargeId}")
        assertEquals(BigDecimal("1.50"), charge.amount)
        assertEquals("USD", charge.asset)
    }

    @Test
    @Timeout(60)
    fun `charging twice returns the same charge, never a second one`() = runBlocking {
        val obj = client.virtualObject<FeeChargeService>("txn-twice")

        val first = obj.charge(request("txn-twice"))
        val second = obj.charge(request("txn-twice"))

        assertEquals(first.chargeId, second.chargeId)
    }

    @Test
    @Timeout(60)
    fun `refunding an untouched object is a no-op`() = runBlocking {
        client.virtualObject<FeeChargeService>("txn-never-charged").refund()
    }

    @Test
    @Timeout(60)
    fun `refunding twice is safe`() = runBlocking {
        val obj = client.virtualObject<FeeChargeService>("txn-refund")

        obj.charge(request("txn-refund"))
        obj.refund()
        obj.refund()
    }

}