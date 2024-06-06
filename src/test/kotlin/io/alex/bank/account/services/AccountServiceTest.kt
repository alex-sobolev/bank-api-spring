package io.alex.bank.account.services

import arrow.core.right
import io.alex.bank.account.models.Currency
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.customer.services.CustomerService
import io.alex.bank.fixtures.AccountFixtures.testAccount
import io.alex.bank.fixtures.CustomerFixtures.testCustomer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class AccountServiceTest {
    private val accountRepository: AccountRepository = mockk()
    private val customerService: CustomerService = mockk()
    private val accountService: AccountService = AccountService(accountRepository, customerService)

    @Test
    fun `getAccounts calls repository with correct parameters`() {
        // Given
        val testAccount1 = testAccount()
        val testAccount2 = testAccount()
        val expectedAccounts = listOf(testAccount1, testAccount2)
        every { accountRepository.getAccounts(any(), any()) } returns expectedAccounts

        // When
        val actualAccounts = accountService.getAccounts(10, 1)

        // Then
        verify(exactly = 1) { accountRepository.getAccounts(10, 1) }
        actualAccounts shouldBe expectedAccounts
    }

    @Test
    fun `getAccountsByCustomerId calls repository with correct parameters`() {
        // Given
        val testCustomerId = UUID.randomUUID()
        val testAccount1 = testAccount(customerId = testCustomerId, currency = Currency.EUR)
        val testAccount2 = testAccount(customerId = testCustomerId, currency = Currency.USD)
        val expectedAccounts = listOf(testAccount1, testAccount2)

        every { customerService.getCustomer(testCustomerId) } returns testCustomer(customerId = testCustomerId).right()
        every { accountRepository.getAccountsByCustomerId(testCustomerId) } returns expectedAccounts

        // When
        val actualAccounts = accountService.getAccountsByCustomerId(testCustomerId)

        // Then
        verify(exactly = 1) { accountRepository.getAccountsByCustomerId(testCustomerId) }
        actualAccounts shouldBe expectedAccounts.right()
    }

    @Test
    fun `getAccount calls repository with correct parameters`() {
        // Given
        val testAccountId = UUID.randomUUID()
        val expectedAccount = testAccount(accountId = testAccountId)
        every { accountRepository.findAccount(testAccountId) } returns expectedAccount

        // When
        val actualAccount = accountService.getAccount(testAccountId)

        // Then
        verify(exactly = 1) { accountRepository.findAccount(testAccountId) }
        actualAccount shouldBe expectedAccount.right()
    }

    @Test
    fun `createAccount calls repository with correct parameters`() {
        // Given
        val testAccount = testAccount()
        every { accountRepository.createAccount(testAccount) } returns testAccount

        // When
        val actualAccount = accountService.createAccount(testAccount)

        // Then
        verify(exactly = 1) { accountRepository.createAccount(testAccount) }
        actualAccount shouldBe testAccount
    }

    @Test
    fun `updateAccount calls repository with correct parameters`() {
        // Given
        val testAccount = testAccount()
        val updatedAccount = testAccount.copy(balance = BigDecimal(2000))
        every { accountRepository.findAccount(testAccount.id) } returns testAccount
        every { accountRepository.updateAccount(updatedAccount) } returns updatedAccount

        // When
        val actualAccount = accountService.updateAccount(updatedAccount)

        // Then
        verify(exactly = 1) { accountRepository.updateAccount(updatedAccount) }
        actualAccount shouldBe updatedAccount.right()
    }

    @Test
    fun `deposit calls repository with correct parameters`() {
        // Given
        val testAccount = testAccount(balance = BigDecimal(1000))
        val testCurrency = Currency.EUR
        val depositAmount = BigDecimal(500)
        val accountToUpdate = testAccount.copy(balance = testAccount.balance + depositAmount, updatedAt = testAccount.createdAt)
        val expectedAccount = accountToUpdate.copy(version = testAccount.version + 1)

        every { accountRepository.findAccount(testAccount.id) } returns testAccount
        every { accountRepository.updateAccount(accountToUpdate) } returns expectedAccount

        // When
        val actualAccount = accountService.deposit(testAccount.id, depositAmount, testCurrency, testAccount.version)

        // Then
        verify(exactly = 1) { accountRepository.findAccount(testAccount.id) }
        verify(exactly = 1) { accountRepository.updateAccount(accountToUpdate) }
        actualAccount shouldBe expectedAccount.right()
    }

    @Test
    fun `withdraw calls repository with correct parameters`() {
        // Given
        val testAccount = testAccount(balance = BigDecimal(1000))
        val testCurrency = Currency.EUR
        val withdrawAmount = BigDecimal(500)
        val accountToUpdate = testAccount.copy(balance = testAccount.balance - withdrawAmount, updatedAt = testAccount.createdAt)
        val expectedAccount = accountToUpdate.copy(version = testAccount.version + 1)

        every { accountRepository.findAccount(testAccount.id) } returns testAccount
        every { accountRepository.updateAccount(accountToUpdate) } returns expectedAccount

        // When
        val actualAccount = accountService.withdraw(testAccount.id, withdrawAmount, testCurrency, testAccount.version)

        // Then
        verify(exactly = 1) { accountRepository.findAccount(testAccount.id) }
        verify(exactly = 1) { accountRepository.updateAccount(accountToUpdate) }
        actualAccount shouldBe expectedAccount.right()
    }

    @Test
    fun `deleteAccount calls repository with correct parameters`() {
        // Given
        val testAccountId = UUID.randomUUID()
        every { accountRepository.deleteAccount(testAccountId) } returns 1

        // When
        accountService.deleteAccount(testAccountId)

        // Then
        verify(exactly = 1) { accountRepository.deleteAccount(testAccountId) }
    }
}
