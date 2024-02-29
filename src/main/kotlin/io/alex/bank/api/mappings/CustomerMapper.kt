package io.alex.bank.api.mappings

import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.CustomerRequest
import java.util.UUID

object CustomerMapper {
    fun CustomerRequest.toDomain(customerId: UUID) =
        Customer(
            id = customerId,
            firstName = firstName,
            lastName = lastName,
            birthdate = birthdate,
            gender = gender,
            address = address,
            email = email,
            phone = phone,
        )
}
