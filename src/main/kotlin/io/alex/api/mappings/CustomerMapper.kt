package io.alex.api.mappings

import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.models.CustomerRequest
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
