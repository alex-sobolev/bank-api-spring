package com.wolt.wm.training.bank.api.mappings

import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.models.CustomerRequest
import java.util.*

object CustomerMapper {
    fun CustomerRequest.toDomain(customerId: UUID) = Customer(
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
