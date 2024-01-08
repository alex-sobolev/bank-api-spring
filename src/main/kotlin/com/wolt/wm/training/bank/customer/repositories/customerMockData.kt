package com.wolt.wm.training.bank.customer.repositories

import com.wolt.wm.training.bank.customer.models.Address
import com.wolt.wm.training.bank.customer.models.Customer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun convertCsvRowToCustomer(row: String): Customer {
    val columns = row.split(",")
    val id = columns[0]
    val firstName = columns[1]
    val lastName = columns[2]
    val birthdate = columns[3]
    val gender = columns[4]
    val email = columns[5]
    val phone = columns[6]
    val streetAddress = columns[7]
    val city = columns[8]
    val country = columns[9]
    val postalCode = columns[10]
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    return Customer(
        id = UUID.fromString(id),
        firstName = firstName,
        lastName = lastName,
        birthdate = dateFormat.parse(birthdate),
        gender = gender.ifBlank { null },
        email = email.ifBlank { null },
        phone = phone.ifBlank { null },
        address = Address(
            street = streetAddress,
            city = city,
            country = country,
            postalCode = postalCode.ifBlank { null },
        )
    )
}

fun csvToCustomers(): List<Customer> {
    val rows = File("src/main/resources/customer-mock-data.csv").readLines()
    val customers = rows.drop(1).map { convertCsvRowToCustomer(it) }

    return customers
}
