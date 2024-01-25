package com.wolt.wm.training.bank.customer.repositories

import com.wolt.wm.training.bank.customer.models.Customer
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomerRepository {
    private val customers: MutableList<Customer> = csvToCustomers().toMutableList()

    fun getCustomers(query: String?, pageSize: Int, page: Int): List<Customer> {
        val offset = (page - 1) * pageSize
        val limit = pageSize

        val filteredCustomers = when {
            query.isNullOrBlank() -> customers
            else -> customers.filter {
                it.firstName.contains(query, ignoreCase = true) ||
                    it.lastName.contains(query, ignoreCase = true) ||
                    "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true) ||
                    it.address.street.contains(query, ignoreCase = true) ||
                    it.address.city.contains(query, ignoreCase = true) ||
                    it.address.country.contains(query, ignoreCase = true) ||
                    it.address.postalCode?.contains(query, ignoreCase = true) ?: false ||
                    it.email?.contains(query, ignoreCase = true) ?: false
            }
        }

        val result = when {
            filteredCustomers.size >= offset + limit -> filteredCustomers.subList(offset, offset + limit)
            filteredCustomers.size - offset > 0 -> filteredCustomers.subList(offset, filteredCustomers.size)
            else -> emptyList()
        }

        return result
    }

    fun getCustomer(customerId: UUID): Customer? {
        return customers.find { it.id == customerId }
    }

    fun createCustomer(customer: Customer) {
        customers.add(customer)
    }

    fun updateCustomer(customer: Customer) {
        val prev = customers.find { it.id == customer.id }
        customers.remove(prev)
        customers.add(customer)
    }

    fun deleteCustomer(customerId: UUID) {
        val customer = customers.find { it.id == customerId }
        customers.remove(customer)
    }
}
