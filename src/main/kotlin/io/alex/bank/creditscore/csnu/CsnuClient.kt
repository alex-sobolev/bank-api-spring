package io.alex.bank.creditscore.csnu

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.alex.bank.creditscore.csnu.CsnuMapper.toCsnuCreditScoreRequest
import io.alex.bank.creditscore.csnu.CsnuMapper.toThirdPartyCreditScore
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.ThirdPartyCreditScore
import io.alex.bank.error.Failure
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class CsnuClient(
    private val csnuWebClient: WebClient,
) {
    suspend fun getCreditScore(customer: Customer): Either<Failure, ThirdPartyCreditScore> =
        csnuWebClient
            .post()
            .uri("/credit-score")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(customer.toCsnuCreditScoreRequest())
            .exchangeToMono<Either<Failure, ThirdPartyCreditScore>> { response ->
                when {
                    response.statusCode().is2xxSuccessful ->
                        response.bodyToMono<CsnuCreditScoreResponse>().map { res ->
                            res.toThirdPartyCreditScore().right()
                        }

                    response.statusCode().is4xxClientError ->
                        response.bodyToMono<String>().map { msg ->
                            Failure
                                .ThirdPartyCreditScoreRetrievalFailureClient(
                                    providerName = "CSNU",
                                    msg = msg ?: "Client error",
                                ).left()
                        }

                    else ->
                        response.bodyToMono<String>().map { msg ->
                            Failure
                                .ThirdPartyCreditScoreRetrievalFailureServer(
                                    providerName = "CSNU",
                                    msg = msg ?: "Server error",
                                ).left()
                        }
                }
            }.awaitSingle()
            .onLeft {
                logger.error("Failed to retrieve credit score from CSNU: $it")
            }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CsnuClient::class.java)
    }
}
