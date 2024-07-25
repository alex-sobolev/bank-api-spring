package io.alex.bank.creditscore.csnu

import java.time.LocalDate

data class CsnuCreditScoreRequest(
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val address: String,
    val email: String,
    val phone: String,
)

data class CsnuCreditScoreResponse(
    val customerName: String,
    val score: Int,
)
