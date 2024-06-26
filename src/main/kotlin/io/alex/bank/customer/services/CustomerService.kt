package io.alex.bank.customer.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.CustomerStatus
import io.alex.bank.customer.repositories.CustomerRepository
import io.alex.bank.error.Failure
import io.alex.bank.error.Failure.CustomerNotFound
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

    fun getCustomer(customerId: UUID): Either<Failure, Customer> =
        either {
            val customer = customerRepository.findCustomer(customerId)
            ensureNotNull(customer) { CustomerNotFound(customerId) }
        }

    fun createCustomer(customer: Customer): Either<Failure, Customer> =
        either {
            return customerRepository.createCustomer(customer).right()
        }

    fun updateCustomer(customer: Customer): Either<Failure, Customer> =
        either {
            val customerToUpdate = customerRepository.findCustomer(customer.id)
            ensureNotNull(customerToUpdate) { CustomerNotFound(customer.id) }

            return customerRepository.updateCustomer(customer).right()
        }

    fun deleteCustomer(customerId: UUID) {
        transactionTemplate.execute {
            val accounts = accountRepository.getAccountsByCustomerId(customerId)

            if (accounts.isNotEmpty()) {
                accountRepository.deleteAccountsByCustomerId(customerId)
            }

            val archiveCustomerResult = customerRepository.deleteCustomer(customerId)

            if (archiveCustomerResult == 0) {
                throw NoSuchElementException("Customer with id $customerId not found")
            }
        }
    }

    fun anonymizeCustomer(customerId: UUID): Either<Failure, Customer> =
        either {
            val customer = customerRepository.findCustomer(customerId)
            ensureNotNull(customer) { CustomerNotFound(customerId) }
            ensure(customer.status == CustomerStatus.INACTIVE) { Failure.ActiveCustomerAnonymization(customerId) }

            val result = customerRepository.anonymizeCustomer(customer)
            ensureNotNull(result) { CustomerNotFound(customerId) }

            return result.right()
        }
}
