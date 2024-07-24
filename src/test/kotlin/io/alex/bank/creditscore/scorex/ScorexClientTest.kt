package io.alex.bank.creditscore.scorex

import arrow.core.right
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.alex.bank.IntegrationBaseTest
import io.alex.bank.customer.models.ThirdPartyCreditScore
import io.alex.bank.fixtures.CustomerFixtures.testCustomer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ScorexClientTest(
    @Autowired private val scorexClient: ScorexClient,
) : IntegrationBaseTest() {
    @Test
    fun `getCreditScore should return credit score`() {
        val customer = testCustomer()

        stubFor(
            post(urlEqualTo("/creditscore"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"creditScore": 80, "customer": "Customer 1"}"""),
                ),
        )

        val creditScore = runBlocking { scorexClient.getCreditScore(customer) }

        creditScore shouldBe ThirdPartyCreditScore(score = 80).right()
    }

    @Test
    fun `getCreditScore should return failure when Scorex returns 4xx`() {
        val customer = testCustomer()

        stubFor(
            post(urlEqualTo("/creditscore"))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error": "Bad request"}"""),
                ),
        )

        val creditScore = runBlocking { scorexClient.getCreditScore(customer) }

        creditScore.isLeft() shouldBe true
    }
}
