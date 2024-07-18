package io.alex.bank.creditscore.csnu

import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.ThirdPartyCreditScore

object CsnuMapper {
    fun Customer.toCsnuCreditScoreRequest() =
        CsnuCreditScoreRequest(
            firstName = firstName,
            lastName = lastName,
            birthdate = birthdate,
            address = "${address.street}, ${address.postalCode}, ${address.city}, ${address.country}",
            email = email ?: "",
            phone = phone ?: "",
        )

    fun CsnuCreditScoreResponse.toThirdPartyCreditScore() = ThirdPartyCreditScore(score = score)
}
