package io.alex.bank.customer.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.right
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.creditscore.csnu.CsnuClient
import io.alex.bank.creditscore.scorex.ScorexClient
import io.alex.bank.customer.models.Address
import io.alex.bank.customer.models.CreditScore
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.CustomerStatus
import io.alex.bank.customer.models.LoanRecommendation
import io.alex.bank.customer.repositories.CustomerRepository
import io.alex.bank.error.Failure
import io.alex.bank.error.Failure.CustomerNotFound
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.util.UUID

fun Customer.anonymize(): Customer =
    Customer(
        id = this.id,
        firstName = "Anonymized",
        lastName = "Anonymized",
        birthdate = LocalDate.of(1900, 1, 1),
        gender = null,
        address =
            Address(
                street = "Anonymized",
                city = "Anonymized",
                country = "Anonymized",
                postalCode = null,
            ),
        email = null,
        phone = null,
        status = CustomerStatus.INACTIVE,
    )

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val accountRepository: AccountRepository,
    private val transactionTemplate: TransactionTemplate,
    private val csnuClient: CsnuClient,
    private val scorexClient: ScorexClient,
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

    fun getCustomerCreditScore(customerId: UUID): Either<Failure, CreditScore> =
        either {
            val customer = customerRepository.findCustomer(customerId)
            ensureNotNull(customer) { CustomerNotFound(customerId) }

            val averageScore =
                runBlocking {
                    val csnuScore = async { csnuClient.getCreditScore(customer).bind() }
                    val scorexScore = async { scorexClient.getCreditScore(customer).bind() }

                    (csnuScore.await().score + scorexScore.await().score) / 2
                }

            val recommendation =
                when (averageScore) {
                    in 80..100 -> LoanRecommendation.APPROVE
                    in 60..79 -> LoanRecommendation.MAYBE
                    else -> LoanRecommendation.REJECT
                }

            return CreditScore(score = averageScore, recommendation = recommendation).right()
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
            val customer = customerRepository.findCustomer(customerId, true)
            ensureNotNull(customer) { CustomerNotFound(customerId) }
            ensure(customer.status == CustomerStatus.INACTIVE) { Failure.ActiveCustomerAnonymization(customerId) }

            val anonymizedCustomer = customer.anonymize()
            val result = customerRepository.updateCustomer(anonymizedCustomer)
            ensureNotNull(result) { CustomerNotFound(customerId) }

            return result.right()
        }
}
