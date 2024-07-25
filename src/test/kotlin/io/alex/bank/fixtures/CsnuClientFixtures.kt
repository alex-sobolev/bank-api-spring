package io.alex.bank.fixtures

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.alex.bank.creditscore.csnu.CsnuCreditScoreResponse

object CsnuClientFixtures {
    fun testCsnuCreditScoreResponse(
        score: Int? = 80,
        customerName: String? = "Customer 1",
    ): String {
        val payload =
            CsnuCreditScoreResponse(
                score = score!!,
                customerName = customerName!!,
            )

        val objectMapper = jacksonObjectMapper()

        return objectMapper.writeValueAsString(payload)
    }

    fun testCsnuCreditScoreResponseError(error: String? = "Bad request"): String =
        jacksonObjectMapper().writeValueAsString(mapOf("error" to error))
}
