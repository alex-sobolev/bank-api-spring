package com.wolt.wm.training.bank.customer.models

import java.util.*

data class Customer(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthdate: Date,
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

data class ApiCustomerPage(
    val customers: List<Customer>,
    val page: Int,
    val pageSize: Int,
)
