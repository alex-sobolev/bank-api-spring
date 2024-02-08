package com.wolt.wm.training.bank.customer.services

import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.repositories.CustomerRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CustomerService(private val customerRepository: CustomerRepository) {
    fun getCustomers(
        name: String?,
        pageSize: Int,
        page: Int,
    ): List<Customer> = customerRepository.getCustomers(name = name, pageSize = pageSize, page = page)

    fun getCustomer(customerId: UUID): Customer? = customerRepository.findCustomer(customerId)

    fun createCustomer(customer: Customer) = customerRepository.createCustomer(customer)

    fun updateCustomer(customer: Customer) = customerRepository.updateCustomer(customer)

    fun deleteCustomer(customerId: UUID) = customerRepository.deleteCustomer(customerId)
}
