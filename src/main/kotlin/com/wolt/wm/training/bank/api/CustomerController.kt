package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.customer.models.ApiCustomerListPage
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.models.CustomerRequest
import com.wolt.wm.training.bank.customer.services.CustomerService
import com.wolt.wm.training.bank.utils.parseUuidFromString
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return emailRegex.matches(email)
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    // Valid phone format: "+48 461 956 4063" or "+484619564063":
    val phoneNumberRegex = "^\\+[0-9 ]*$".toRegex()
    return phoneNumberRegex.matches(phoneNumber)
}

fun validateCustomerRequest(customerRequest: CustomerRequest) {
    if (customerRequest.firstName.isBlank()) throw IllegalArgumentException("First name is required")
    if (customerRequest.lastName.isBlank()) throw IllegalArgumentException("Last name is required")
    if (customerRequest.birthdate > LocalDate.now()) throw IllegalArgumentException("Birthdate cannot be in the future")

    val minBirthdate = LocalDate.of(1910, 1, 1)

    if (customerRequest.birthdate < minBirthdate) throw IllegalArgumentException("Birthdate cannot be before $minBirthdate")
    if (customerRequest.address.street.isBlank()) throw IllegalArgumentException("Street is required")
    if (customerRequest.address.city.isBlank()) throw IllegalArgumentException("City is required")
    if (customerRequest.address.country.isBlank()) throw IllegalArgumentException("Country is required")

    if (customerRequest.address.postalCode != null && customerRequest.address.postalCode.isBlank()) throw IllegalArgumentException(
        "Postal code cannot be blank"
    )

    if (customerRequest.email != null && !isValidEmail(customerRequest.email)) throw IllegalArgumentException(
        "Invalid email address"
    )

    if (customerRequest.phone != null && !isValidPhoneNumber(customerRequest.phone)) throw IllegalArgumentException(
        "Invalid phone number"
    )
}

@RestController
@RequestMapping("/api/customers")
class CustomerController(private val customerService: CustomerService) {
    @GetMapping
    fun getCustomers(
        @RequestParam query: String?,
        @RequestParam pageSize: Int?,
        @RequestParam page: Int?,
    ): ResponseEntity<ApiCustomerListPage> {
        val pageSize = pageSize ?: 50
        val page = page ?: 1
        val customers = customerService.getCustomers(query = query, pageSize = pageSize, page = page)
        val payload = ApiCustomerListPage(customers = customers, page = page, pageSize = pageSize)

        return ResponseEntity(payload, HttpStatus.OK)
    }

    @GetMapping("/{customerId}")
    fun getCustomer(@PathVariable customerId: String): ResponseEntity<Customer> {
        val customerId = parseUuidFromString(customerId, "Invalid customer id format")

        val customer = customerService.getCustomer(customerId)
            ?: throw NoSuchElementException("Customer with id $customerId not found")

        return ResponseEntity.ok(customer)
    }

    @PostMapping
    fun createCustomer(@RequestBody customerRequest: CustomerRequest): ResponseEntity<Customer> {
        validateCustomerRequest(customerRequest)

        val newCustomerId = UUID.randomUUID()

        val customer = Customer(
            id = newCustomerId,
            firstName = customerRequest.firstName,
            lastName = customerRequest.lastName,
            birthdate = customerRequest.birthdate,
            gender = customerRequest.gender,
            address = customerRequest.address,
            email = customerRequest.email,
            phone = customerRequest.phone,
        )

        customerService.createCustomer(customer)

        return ResponseEntity.ok(customer)
    }

    @PutMapping("/{customerId}")
    fun updateCustomer(
        @PathVariable customerId: String,
        @RequestBody customerRequest: CustomerRequest
    ): ResponseEntity<Customer> {
        val customerId = parseUuidFromString(customerId, "Invalid customer id format")

        val customer = customerService.getCustomer(customerId)
            ?: throw NoSuchElementException("Customer with id $customerId not found")

        validateCustomerRequest(customerRequest)

        val customerUpdate = Customer(
            id = customerId,
            firstName = customerRequest.firstName,
            lastName = customerRequest.lastName,
            birthdate = customerRequest.birthdate,
            gender = customerRequest.gender,
            address = customerRequest.address,
            email = customerRequest.email,
            phone = customerRequest.phone,
        )

        customerService.updateCustomer(customerUpdate)

        return ResponseEntity.ok(customerUpdate)
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable customerId: String) {
        val customerId = parseUuidFromString(customerId, "Invalid customer id format")

        val customer = customerService.getCustomer(customerId)
            ?: throw NoSuchElementException("Customer with id $customerId not found")

        customerService.deleteCustomer(customerId)
    }
}
