package io.alex.bank.creditscore.csnu

import arrow.core.Either
import arrow.core.right
import io.alex.bank.creditscore.csnu.CsnuMapper.toCsnuCreditScoreRequest
import io.alex.bank.creditscore.csnu.CsnuMapper.toThirdPartyCreditScore
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.ThirdPartyCreditScore
import io.alex.bank.error.Failure
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class CsnuClient(
    private val webClient: WebClient,
) {
    suspend fun getCreditScore(customer: Customer): Either<Failure, ThirdPartyCreditScore> =
        Either
            .catch {
                val requestPayload = customer.toCsnuCreditScoreRequest()

                val res =
                    webClient
                        .post()
                        .uri("https://api.csnu.com/creditscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestPayload)
                        .retrieve()
                        .onStatus({ status -> status.is4xxClientError }) { clientResponse ->
                            throw ClientErrorException(clientResponse.statusCode().toString())
                        }.onStatus({ status -> status.is5xxServerError }) { clientResponse ->
                            throw ServerErrorException(clientResponse.statusCode().toString())
                        }.awaitBody<CsnuCreditScoreResponse>()

                return res.toThirdPartyCreditScore().right()
            }.mapLeft { e ->
                when (e) {
                    is ClientErrorException ->
                        Failure.ThirdPartyCreditScoreRetrievalFailure(
                            providerName = "CSNU",
                            msg =
                                e.message ?: "Client error",
                        )
                    is ServerErrorException ->
                        Failure.ThirdPartyCreditScoreRetrievalFailure(
                            providerName = "CSNU",
                            msg =
                                e.message ?: "Server error",
                        )
                    else -> Failure.ThirdPartyCreditScoreRetrievalFailure(providerName = "CSNU", msg = e.message ?: "Unknown error")
                }
            }
}

class ClientErrorException(
    message: String,
) : Exception(message)

class ServerErrorException(
    message: String,
) : Exception(message)
