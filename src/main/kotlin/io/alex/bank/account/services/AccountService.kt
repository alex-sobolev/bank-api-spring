package io.alex.bank.account.services

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import io.alex.bank.account.models.Account
import io.alex.bank.account.models.Currency
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.customer.services.CustomerService
import io.alex.bank.error.Failure
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val customerService: CustomerService,
) {
    fun getAccounts(
        pageSize: Int,
        page: Int,
    ) = accountRepository.getAccounts(pageSize = pageSize, page = page)

    fun getAccountsByCustomerId(customerId: UUID): Either<Failure, List<Account>> =
        either {
            customerService.getCustomer(customerId).bind()

            val accounts = accountRepository.getAccountsByCustomerId(customerId)

            return accounts.right()
        }

    fun getAccount(accountId: UUID): Either<Failure, Account> =
        either {
            val account = accountRepository.findAccount(accountId) ?: return Failure.AccountNotFound(accountId).left()

            return account.right()
        }

    fun createAccount(account: Account) = accountRepository.createAccount(account)

    fun updateAccount(account: Account): Either<Failure, Account> =
        either {
            // Check account exists
            getAccount(account.id).bind()

            // update account
            val account = accountRepository.updateAccount(account) ?: return Failure.AccountVersionOutOfDate(account.id).left()

            return account.right()
        }

    fun deposit(
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        version: Int,
    ): Either<Failure, Account> =
        either {
            val account = getAccount(accountId).bind()

            ensure(account.currency == currency) { Failure.MismatchedCurrency(account.currency, currency) }
            ensure(amount > 0.toBigDecimal()) { Failure.InvalidDepositAmount(amount) }

            val accountToUpdate =
                account.copy(
                    balance = account.balance + amount,
                    updatedAt = LocalDate.now(),
                    version = version,
                )

            val updatedAccount = updateAccount(accountToUpdate).bind()

            updatedAccount
        }

    fun withdraw(
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        version: Int,
    ): Either<Failure, Account> =
        either {
            val account = getAccount(accountId).bind()

            ensure(account.currency == currency) { Failure.MismatchedCurrency(account.currency, currency) }
            ensure(amount > 0.toBigDecimal()) { Failure.InvalidWithdrawAmount(amount) }
            ensure(account.balance >= amount) { Failure.InsufficientFunds(accountId) }

            val accountToUpdate =
                account.copy(
                    balance = account.balance - amount,
                    updatedAt = LocalDate.now(),
                    version = version,
                )

            val updatedAccount = updateAccount(accountToUpdate).bind()

            updatedAccount
        }

    fun deleteAccount(accountId: UUID): Either<Failure, Unit> =
        either {
            val deleteResult = accountRepository.deleteAccount(accountId)

            ensure(deleteResult > 0) { Failure.AccountNotFound(accountId) }

            return Unit.right()
        }
}
