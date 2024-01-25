package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.api.mappings.CustomerMapper.toDomain
import com.wolt.wm.training.bank.customer.models.ApiCustomerListPage
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.models.CustomerRequest
import com.wolt.wm.training.bank.customer.services.CustomerService
import com.wolt.wm.training.bank.utils.parseUuidFromString
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/customers")
class CustomerController(private val customerService: CustomerService) {
    private fun String.isValidEmail(): Boolean {
        return emailRegex.matches(this)
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumberRegex.matches(phoneNumber)
    }

    private fun validateCustomerRequest(customerRequest: CustomerRequest) {
        if (customerRequest.firstName.isBlank()) throw IllegalArgumentException("First name is required")
        if (customerRequest.lastName.isBlank()) throw IllegalArgumentException("Last name is required")
        if (customerRequest.birthdate > LocalDate.now()) throw IllegalArgumentException("Birthdate cannot be in the future")

        val minBirthdate = LocalDate.of(1910, 1, 1)

        if (customerRequest.birthdate < minBirthdate) throw IllegalArgumentException("Birthdate cannot be before $minBirthdate")
        if (customerRequest.address.street.isBlank()) throw IllegalArgumentException("Street is required")
        if (customerRequest.address.city.isBlank()) throw IllegalArgumentException("City is required")
        if (customerRequest.address.country.isBlank()) throw IllegalArgumentException("Country is required")

        if (customerRequest.address.postalCode != null && customerRequest.address.postalCode.isBlank()) {
            throw IllegalArgumentException(
                "Postal code cannot be blank",
            )
        }

        if (customerRequest.email != null && !customerRequest.email.isValidEmail()) {
            throw IllegalArgumentException(
                "Invalid email address",
            )
        }

        if (customerRequest.phone != null && !isValidPhoneNumber(customerRequest.phone)) {
            throw IllegalArgumentException(
                "Invalid phone number",
            )
        }
    }

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
    fun getCustomer(
        @PathVariable customerId: String,
    ): ResponseEntity<Customer> {
        val customerId = parseUuidFromString(customerId, "Invalid customer id format: $customerId")

        val customer =
            customerService.getCustomer(customerId)
                ?: throw NoSuchElementException("Customer with id $customerId not found")

        return ResponseEntity.ok(customer)
    }

    @PostMapping
    fun createCustomer(
        @RequestBody customerRequest: CustomerRequest,
    ): ResponseEntity<Customer> {
        validateCustomerRequest(customerRequest)

        val newCustomerId = UUID.randomUUID()
        val customer = customerRequest.toDomain(newCustomerId)

        customerService.createCustomer(customer)

        return ResponseEntity.ok(customer)
    }

    @PutMapping("/{customerId}")
    fun updateCustomer(
        @PathVariable customerId: String,
        @RequestBody customerRequest: CustomerRequest,
    ): ResponseEntity<Customer> {
        val customerId = parseUuidFromString(customerId, "Invalid customer id format: $customerId")

        customerService.getCustomer(customerId)
            ?: throw NoSuchElementException("Customer with id $customerId not found")

        validateCustomerRequest(customerRequest)

        val customerUpdate = customerRequest.toDomain(customerId)

        customerService.updateCustomer(customerUpdate)

        return ResponseEntity.ok(customerUpdate)
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(
        @PathVariable customerId: String,
    ) {
        val customerId = parseUuidFromString(customerId, "Invalid customer id format")

        customerService.getCustomer(customerId)
            ?: throw NoSuchElementException("Customer with id $customerId not found")

        customerService.deleteCustomer(customerId)
    }

    companion object {
        private val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()

        // Valid phone format: "+48 461 956 4063" or "+484619564063":
        private val phoneNumberRegex = "^\\+[0-9 ]*$".toRegex()
    }
}
