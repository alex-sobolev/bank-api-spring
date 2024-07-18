package io.alex.bank.creditscore.scorex

import io.alex.bank.creditscore.csnu.CsnuCreditScoreResponse
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.ThirdPartyCreditScore

object CsnuMapper {
    fun Customer.toScorexCreditScoreRequest() =
        ScorexCreditScoreRequest(
            firstName = firstName,
            lastName = lastName,
            birthdate = birthdate,
            address = "${address.street}, ${address.postalCode}, ${address.city}, ${address.country}",
            email = email ?: "",
            phone = phone ?: "",
        )

    fun CsnuCreditScoreResponse.toThirdPartyCreditScore() = ThirdPartyCreditScore(score = score)
}
