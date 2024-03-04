package io.alex.bank.account.services

import io.alex.bank.account.models.Currency
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.fixtures.AccountFixtures.testAccount
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class AccountServiceTest {
    private val accountRepository: AccountRepository = mockk()
    private val accountService: AccountService = AccountService(accountRepository)

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
        every { accountRepository.getAccountsByCustomerId(testCustomerId) } returns expectedAccounts

        // When
        val actualAccounts = accountService.getAccountsByCustomerId(testCustomerId)

        // Then
        verify(exactly = 1) { accountRepository.getAccountsByCustomerId(testCustomerId) }
        actualAccounts shouldBe expectedAccounts
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
        actualAccount shouldBe expectedAccount
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
        every { accountRepository.updateAccount(updatedAccount) } returns updatedAccount

        // When
        val actualAccount = accountService.updateAccount(updatedAccount)

        // Then
        verify(exactly = 1) { accountRepository.updateAccount(updatedAccount) }
        actualAccount shouldBe updatedAccount
    }

    @Test
    fun `deleteAccount calls repository with correct parameters`() {
        // Given
        val testAccountId = UUID.randomUUID()
        every { accountRepository.deleteAccount(testAccountId) } just Runs

        // When
        accountService.deleteAccount(testAccountId)

        // Then
        verify(exactly = 1) { accountRepository.deleteAccount(testAccountId) }
    }
}
