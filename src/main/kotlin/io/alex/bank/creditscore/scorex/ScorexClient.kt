package io.alex.bank.creditscore.scorex

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.alex.bank.creditscore.scorex.ScorexMapper.toScorexCreditScoreRequest
import io.alex.bank.creditscore.scorex.ScorexMapper.toThirdPartyCreditScore
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.ThirdPartyCreditScore
import io.alex.bank.error.Failure
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class ScorexClient(
    private val sorexWebClient: WebClient,
) {
    suspend fun getCreditScore(customer: Customer): Either<Failure, ThirdPartyCreditScore> =
        sorexWebClient
            .post()
            .uri("/creditscore")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(customer.toScorexCreditScoreRequest())
            .exchangeToMono<Either<Failure, ThirdPartyCreditScore>> { response ->
                when {
                    response.statusCode().is2xxSuccessful ->
                        response.bodyToMono<ScorexCreditScoreResponse>().map { res ->
                            res.toThirdPartyCreditScore().right()
                        }

                    response.statusCode().is4xxClientError ->
                        response.bodyToMono<String>().map { msg ->
                            Failure
                                .ThirdPartyCreditScoreRetrievalFailure(
                                    providerName = "Scorex",
                                    msg = msg ?: "Client error",
                                ).left()
                        }

                    else ->
                        response.bodyToMono<String>().map { msg ->
                            Failure
                                .ThirdPartyCreditScoreRetrievalFailure(
                                    providerName = "Scorex",
                                    msg = msg ?: "Server error",
                                ).left()
                        }
                }
            }.awaitSingle()
}
