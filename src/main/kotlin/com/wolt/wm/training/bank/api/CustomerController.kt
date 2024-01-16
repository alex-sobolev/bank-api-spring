package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.customer.models.ApiCustomerListPage
import com.wolt.wm.training.bank.customer.models.CreateCustomerRequest
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.models.UpdateCustomerRequest
import com.wolt.wm.training.bank.customer.services.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/customers")
class CustomerController(private val customerService: CustomerService) {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(err: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity(err.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(err: IllegalArgumentException): ResponseEntity<String> =
        ResponseEntity(err.message, HttpStatus.BAD_REQUEST)

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
    fun getCustomer(@PathVariable customerId: UUID) = customerService.getCustomer(customerId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@RequestBody customerRequest: CreateCustomerRequest) {
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

        return customerService.createCustomer(customer)
    }

    @PutMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomer(@PathVariable customerId: UUID, @RequestBody customerRequest: UpdateCustomerRequest) {
        val customer: Customer = Customer(
            id = customerId,
            firstName = customerRequest.firstName,
            lastName = customerRequest.lastName,
            birthdate = customerRequest.birthdate,
            gender = customerRequest.gender,
            address = customerRequest.address,
            email = customerRequest.email,
            phone = customerRequest.phone,
        )

        return customerService.updateCustomer(customer)
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable customerId: UUID) = customerService.deleteCustomer(customerId)
}
