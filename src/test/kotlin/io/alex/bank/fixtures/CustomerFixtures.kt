package io.alex.bank.fixtures

import io.alex.bank.customer.models.Address
import io.alex.bank.customer.models.Customer
import java.time.LocalDate
import java.util.UUID

object CustomerFixtures {
    fun testCustomer(
        customerId: UUID? = UUID.randomUUID(),
        firstName: String? = "John",
        lastName: String? = "Doe",
        birthdate: LocalDate? = LocalDate.of(1990, 1, 1),
        street: String? = "123 Street",
        city: String? = "City",
        country: String? = "Country",
        postalCode: String? = "12345",
        email: String? = "john.doe@example.com",
        phone: String? = "+1234567890",
    ) = Customer(
        id = customerId!!,
        firstName = firstName!!,
        lastName = lastName!!,
        birthdate = birthdate!!,
        gender = "Male",
        address =
        Address(
            street = street!!,
            city = city!!,
            country = country!!,
            postalCode = postalCode!!,
        ),
        email = email!!,
        phone = phone!!,
    )
}
