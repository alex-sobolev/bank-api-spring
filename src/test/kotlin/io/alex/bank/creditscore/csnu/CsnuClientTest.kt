package io.alex.bank.creditscore.csnu

import arrow.core.right
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.alex.bank.IntegrationBaseTest
import io.alex.bank.customer.models.ThirdPartyCreditScore
import io.alex.bank.fixtures.CsnuClientFixtures.testCsnuCreditScoreResponse
import io.alex.bank.fixtures.CsnuClientFixtures.testCsnuCreditScoreResponseError
import io.alex.bank.fixtures.CustomerFixtures.testCustomer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CsnuClientTest(
    @Autowired private val csnuClient: CsnuClient,
) : IntegrationBaseTest() {
    @Test
    fun `getCreditScore should return credit score from CSNU provider`() {
        val customer = testCustomer()

        stubFor(
            post(urlEqualTo("/credit-score"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(testCsnuCreditScoreResponse(score = 80)),
                ),
        )

        val creditScore = runBlocking { csnuClient.getCreditScore(customer) }

        creditScore shouldBe ThirdPartyCreditScore(score = 80).right()
    }

    @Test
    fun `getCreditScore should return failure when CSNU returns 4xx`() {
        val customer = testCustomer()

        stubFor(
            post(urlEqualTo("/credit-score"))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(testCsnuCreditScoreResponseError(error = "Bad request")),
                ),
        )

        val creditScore = runBlocking { csnuClient.getCreditScore(customer) }

        creditScore.isLeft() shouldBe true
    }
}
