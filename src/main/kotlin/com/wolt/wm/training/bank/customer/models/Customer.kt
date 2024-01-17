package com.wolt.wm.training.bank.customer.models

import java.time.LocalDate
import java.util.*

data class Customer(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val gender: String?,
    val address: Address,
    val email: String?,
    val phone: String?,
)

data class Address(
    val street: String,
    val city: String,
    val country: String,
    val postalCode: String?
)

data class ApiCustomerListPage(
    val customers: List<Customer>,
    val page: Int,
    val pageSize: Int,
)

data class CreateCustomerRequest(
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val gender: String?,
    val address: Address,
    val email: String?,
    val phone: String?,
)

data class UpdateCustomerRequest(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthdate: LocalDate,
    val gender: String?,
    val address: Address,
    val email: String?,
    val phone: String?,
)
