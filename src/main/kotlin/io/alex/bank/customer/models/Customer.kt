package io.alex.bank.customer.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.util.UUID

enum class CustomerStatus {
    ACTIVE,
    INACTIVE,
}

data class Customer(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val gender: String?,
    val address: Address,
    val email: String?,
    val phone: String?,
    val status: CustomerStatus,
)

data class Address(
    val street: String,
    val city: String,
    val country: String,
    val postalCode: String?,
)

data class ApiCustomerListPage(
    val customers: List<Customer>,
    val page: Int,
    val pageSize: Int,
)

data class CustomerRequest(
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val gender: String?,
    val address: Address,
    val email: String?,
    val phone: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KafkaGdprAnonymizeCustomerEvent(
    val customerId: UUID,
)
