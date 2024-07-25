package io.alex.bank.fixtures

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.alex.bank.creditscore.scorex.ScorexCreditScoreResponse

object ScorexClientFixtures {
    fun testScorexCreditScoreResponse(
        score: Int? = 80,
        customer: String? = "Customer 1",
    ): String {
        val payload =
            ScorexCreditScoreResponse(
                customer = customer!!,
                creditScore = score!!,
            )

        val objectMapper = jacksonObjectMapper()

        return objectMapper.writeValueAsString(payload)
    }

    fun testScorexCreditScoreResponseError(error: String? = "Bad request"): String =
        jacksonObjectMapper().writeValueAsString(mapOf("error" to error))
}
