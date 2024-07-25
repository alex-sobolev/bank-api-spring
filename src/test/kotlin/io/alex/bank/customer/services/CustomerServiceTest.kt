package io.alex.bank.customer.services

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.SpykBean
import io.alex.bank.IntegrationBaseTest
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.creditscore.csnu.CsnuClient
import io.alex.bank.creditscore.scorex.ScorexClient
import io.alex.bank.customer.models.CustomerStatus
import io.alex.bank.customer.models.LoanRecommendation
import io.alex.bank.customer.models.ThirdPartyCreditScore
import io.alex.bank.customer.repositories.CustomerRepository
import io.alex.bank.error.Failure
import io.alex.bank.fixtures.CustomerFixtures.testCustomer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

class CustomerServiceTest(
    @SpykBean @Autowired private val customerRepository: CustomerRepository,
    @SpykBean @Autowired private val accountRepository: AccountRepository,
    @SpykBean @Autowired private val csnuClient: CsnuClient,
    @SpykBean @Autowired private val scorexClient: ScorexClient,
) : IntegrationBaseTest() {
    private val transactionTemplate: TransactionTemplate = mockk()
    private val customerService: CustomerService =
        CustomerService(customerRepository, accountRepository, transactionTemplate, csnuClient, scorexClient)

    @Test
    fun `getCustomers calls repository`() {
        // Given
        val name = "John"
        val pageSize = 10
        val page = 1

        val customer1 = testCustomer()

        val expectedCustomers = listOf(customer1)
        every { customerRepository.getCustomers(name, pageSize, page) } returns expectedCustomers

        // When
        val actualCustomers = customerService.getCustomers(name, pageSize, page)

        // Then
        actualCustomers shouldBe expectedCustomers
        verify(exactly = 1) { customerRepository.getCustomers(name, pageSize, page) }
    }

    @Test
    fun `getCustomer calls repository`() {
        // Given
        val id = UUID.randomUUID()
        val expectedCustomer = testCustomer(customerId = id)
        every { customerRepository.findCustomer(id) } returns expectedCustomer

        // When
        val actualCustomer =
            customerService.getCustomer(id).fold(
                ifLeft = { null },
                ifRight = { it },
            )

        // Then
        actualCustomer shouldBe expectedCustomer
        verify(exactly = 1) { customerRepository.findCustomer(id) }
    }

    @Test
    fun `getCustomer calls repository and fails with CustomerNotFound`() {
        // Given
        val id = UUID.randomUUID()
        every { customerRepository.findCustomer(id) } returns null

        // When
        val result = customerService.getCustomer(id)

        // Then
        result shouldBe Failure.CustomerNotFound(id).left()
        verify(exactly = 1) { customerRepository.findCustomer(id) }
    }

    @Test
    fun `updateCustomer calls repository`() {
        // Prepare customer data
        val id = UUID.randomUUID()
        val customerToAdd = testCustomer(customerId = id)
        every { customerRepository.createCustomer(customerToAdd) } returns customerToAdd

        val addedCustomer =
            customerService.createCustomer(customerToAdd).fold(
                ifLeft = { null },
                ifRight = { it },
            )

        addedCustomer shouldBe customerToAdd
        verify(exactly = 1) { customerRepository.createCustomer(customerToAdd) }

        // Given
        val customerToUpdate = addedCustomer!!.copy(email = "john.doe.updated@example.com")
        every { customerRepository.findCustomer(customerToUpdate.id) } returns addedCustomer
        every { customerRepository.updateCustomer(customerToUpdate) } returns customerToUpdate

        // When
        val updatedCustomer = customerService.updateCustomer(customerToUpdate)

        // Then
        updatedCustomer shouldBe customerToUpdate.right()
        verify(exactly = 1) { customerRepository.updateCustomer(customerToUpdate) }
    }

    @Test
    fun `deleteCustomer calls repository`() {
        // Given
        val id = UUID.randomUUID()

        every { customerRepository.deleteCustomer(id) } returns 1
        every { accountRepository.getAccountsByCustomerId(id) } returns emptyList()
        every { accountRepository.deleteAccountsByCustomerId(id) } returns 1

        val transactionStatus: TransactionStatus = mockk()

        every { transactionTemplate.execute<Unit>(any()) } answers {
            firstArg<TransactionCallback<Unit>>().doInTransaction(transactionStatus)
            Unit
        }

        // When
        customerService.deleteCustomer(id)

        // Then
        verify(exactly = 1) { customerRepository.deleteCustomer(id) }
    }

    @Test
    fun `should anonymize a customer`() {
        // Given
        val id = UUID.randomUUID()
        val customer = testCustomer(customerId = id, status = CustomerStatus.INACTIVE)
        val expectedCustomer = customer.anonymize()

        // Add customer to DB
        customerRepository.createCustomer(customer)

        // When
        val result = customerService.anonymizeCustomer(id)

        // Then
        result shouldBe expectedCustomer.right()
        verify(exactly = 1) { customerRepository.findCustomer(id, true) }
        verify(exactly = 1) { customerRepository.updateCustomer(expectedCustomer) }
    }

    @Test
    fun `should not anonymize an active customer`() {
        // Given
        val id = UUID.randomUUID()
        val customer = testCustomer(customerId = id, status = CustomerStatus.ACTIVE)
        val anonymizedCustomer = customer.anonymize().copy(status = CustomerStatus.ACTIVE)

        // Add customer to DB
        customerRepository.createCustomer(customer)

        // When
        val result = customerService.anonymizeCustomer(id)

        // Then
        result shouldBe Failure.ActiveCustomerAnonymization(id).left()
    }

    @Test
    fun `should return average credit score for a customer`() {
        // Given
        val customerId = UUID.randomUUID()
        val customer = testCustomer(customerId = customerId)

        // Add customer to DB
        customerRepository.createCustomer(customer)

        val csnuCreditScore = 80
        val scorexCreditScore = 90
        val averageScore = (csnuCreditScore + scorexCreditScore) / 2 // 85

        every { runBlocking { csnuClient.getCreditScore(customer) } } returns ThirdPartyCreditScore(score = csnuCreditScore).right()
        every { runBlocking { scorexClient.getCreditScore(customer) } } returns ThirdPartyCreditScore(score = scorexCreditScore).right()

        // When
        val result =
            customerService.getCustomerCreditScore(customer.id).fold(
                ifLeft = { null },
                ifRight = { it },
            )

        // Then
        result?.score shouldBe averageScore
        result?.recommendation shouldBe LoanRecommendation.APPROVE
    }
}
