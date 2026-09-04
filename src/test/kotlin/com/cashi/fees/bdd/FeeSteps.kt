package com.cashi.fees.bdd

import com.cashi.fees.persistence.FeeRecordRepository
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeeSteps {
    @Autowired
    lateinit var rest: TestRestTemplate
    @Autowired
    lateinit var repository: FeeRecordRepository

    private lateinit var transactionId: String
    private var amount: String = "1000"
    private var type: String = "Mobile Top Up"
    private var assetType: String = "FIAT"
    private lateinit var response: ResponseEntity<String>
    private var firstBody: String? = null

    @Before
    fun setUp() {
        RestateTestEnvironment.registerDeployment()
        repository.deleteAll()
        // Restate keeps completed workflow keys forever - a reused id silently takes the
        // 409-attach path and returns the PREVIOUS scenario's result. Always use a fresh one.
        transactionId = "txn-" + UUID.randomUUID()
    }

    @Given("a settled transaction of {word} USD of type {string}")
    fun aSettledTransaction(amount: String, type: String) {
        this.amount = amount
        this.type = type
    }

    @Given("the asset type is {string}")
    fun theAssetTypeIs(assetType: String) {
        this.assetType = assetType
    }

    @Given("the transaction id is empty")
    fun theTransactionIdIsEmpty() {
        this.transactionId = ""
    }


    @When("the fee is requested")
    fun theFeeIsRequested() {
        response = rest.postForEntity(
            "/transaction/fee",
            HttpEntity(
                """                                                                                                                                                                                                               
                  {"transaction_id":"$transactionId","amount":$amount,"asset":"USD",                                                                                                                                                
                   "asset_type":"$assetType","type":"$type","state":"SETTLED - PENDING FEE",                                                                                                                                        
                   "created_at":"2023-08-30 15:42:17.610059"}                                                                                                                                                                       
                  """.trimIndent(),
                HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON },
            ),
            String::class.java,
        )
    }

    @When("the same transaction is submitted again")
    fun theSameTransactionIsSubmittedAgain() {
        firstBody = response.body
        theFeeIsRequested()
    }

    @Then("the response is {int}")
    fun theResponseIs(status: Int) = assertEquals(status, response.statusCode.value())

    @Then("the quoted fee is {word}")
    fun theQuotedFeeIs(fee: String) =
        assertTrue(response.body!!.contains("\"fee\":$fee"), "body was ${response.body}")

    @Then("the error code is {word}")
    fun theErrorCodeIs(code: String) =
        assertTrue(response.body!!.contains("\"error\":\"$code\""), "body was ${response.body}")

    @Then("the message mentions {string}")
    fun theMessageMentions(text: String) =
        assertTrue(response.body!!.contains(text), "body was ${response.body}")

    @Then("the fee record is settled with a charge id")
    fun theFeeRecordIsSettled() {
        val record = repository.findById(transactionId).orElseThrow()
        assertEquals("SETTLED", record.state)
        assertTrue(record.chargeId.startsWith("chg_"))
    }

    @Then("both responses are identical")
    fun bothResponsesAreIdentical() {
        assertEquals(200, response.statusCode.value())
        assertEquals(firstBody, response.body)
    }

    @Then("exactly one fee record exists")
    fun exactlyOneFeeRecordExists() = assertEquals(1, repository.count())
}