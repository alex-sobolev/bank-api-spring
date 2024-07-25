package io.alex.bank.creditscore.scorex

import java.time.LocalDate

data class ScorexCreditScoreRequest(
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val address: String,
    val email: String,
    val phone: String,
)

data class ScorexCreditScoreResponse(
    val customer: String,
    val creditScore: Int,
)
