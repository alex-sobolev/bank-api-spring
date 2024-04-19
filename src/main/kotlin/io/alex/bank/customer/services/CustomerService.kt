package io.alex.bank.customer.services

import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.repositories.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val accountRepository: AccountRepository,
    private val transactionTemplate: TransactionTemplate,
) {
    fun getCustomers(
        name: String?,
        pageSize: Int,
        page: Int,
    ): List<Customer> = customerRepository.getCustomers(name = name, pageSize = pageSize, page = page)

    fun getCustomer(customerId: UUID): Customer? = customerRepository.findCustomer(customerId)

    fun createCustomer(customer: Customer) = customerRepository.createCustomer(customer)

    fun updateCustomer(customer: Customer) = customerRepository.updateCustomer(customer)

    fun deleteCustomer(customerId: UUID) {
        transactionTemplate.execute {
            val accounts = accountRepository.getAccountsByCustomerId(customerId)

            if (accounts.isNotEmpty()) {
                accountRepository.deleteAccountsByCustomerId(customerId)
            }

            accountRepository.deleteAccountsByCustomerId(customerId)

            val archiveResult = customerRepository.deleteCustomer(customerId)

            if (archiveResult == 0) {
                throw NoSuchElementException("Customer with id $customerId not found")
            }
        }
    }
}
