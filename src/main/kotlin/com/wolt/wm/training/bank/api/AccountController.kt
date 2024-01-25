package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.account.models.Account
import com.wolt.wm.training.bank.account.models.AccountDepositRequest
import com.wolt.wm.training.bank.account.models.AccountStatus
import com.wolt.wm.training.bank.account.models.AccountWithdrawRequest
import com.wolt.wm.training.bank.account.models.ApiAccount
import com.wolt.wm.training.bank.account.models.ApiAccountListPage
import com.wolt.wm.training.bank.account.models.ApiCustomerAccountList
import com.wolt.wm.training.bank.account.models.CreateAccountRequest
import com.wolt.wm.training.bank.account.services.AccountService
import com.wolt.wm.training.bank.customer.services.CustomerService
import com.wolt.wm.training.bank.utils.parseUuidFromString
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/accounts")
class AccountController(private val accountService: AccountService, private val customerService: CustomerService) {
    @GetMapping
    fun getAccounts(
        @RequestParam query: String?,
        @RequestParam pageSize: Int?,
        @RequestParam page: Int?,
    ): ResponseEntity<ApiAccountListPage> {
        val page = page ?: 1
        val pageSize = pageSize ?: 50
        val accounts = accountService.getAccounts(query = query, pageSize = pageSize, page = page)

        return ResponseEntity.ok(ApiAccountListPage(accounts = accounts, page = page, pageSize = pageSize))
    }

    @GetMapping("/{accountId}")
    fun getAccountById(
        @PathVariable accountId: String,
    ): ResponseEntity<ApiAccount> {
        val accountId = UUID.fromString(accountId)

        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        val customer =
            customerService.getCustomer(account.customerId)
                ?: throw NoSuchElementException("Customer with id ${account.customerId} not found")

        return ResponseEntity.ok(ApiAccount(account = account, customer = customer))
    }

    @GetMapping("/customers/{customerId}")
    fun getAccountsByCustomerId(
        @PathVariable customerId: String,
    ): ResponseEntity<ApiCustomerAccountList> {
        val customerId = UUID.fromString(customerId)

        val customer =
            customerService.getCustomer(customerId)
                ?: throw NoSuchElementException("Customer with id $customerId not found")

        val accounts = accountService.getAccountsByCustomerId(customerId)

        return ResponseEntity.ok(ApiCustomerAccountList(customer = customer, accounts = accounts))
    }

    @PostMapping("/create")
    fun createAccount(
        @RequestBody accountRequest: CreateAccountRequest,
    ): ResponseEntity<ApiAccount> {
        val newAccountId = UUID.randomUUID()
        val customerId = parseUuidFromString(accountRequest.customerId, "Invalid customer id format")

        val customer =
            customerService.getCustomer(customerId)
                ?: throw NoSuchElementException("Customer with id ${accountRequest.customerId} not found")

        val account =
            Account(
                id = newAccountId,
                customerId = customerId,
                balance = 0.toBigDecimal(),
                currency = accountRequest.currency,
                type = accountRequest.type,
                status = AccountStatus.ACTIVE,
                createdAt = LocalDate.now(),
                updatedAt = null,
            )

        accountService.createAccount(account)

        return ResponseEntity.ok(ApiAccount(account = account, customer = customer))
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAccount(
        @PathVariable accountId: String,
    ) {
        val accountId = UUID.fromString(accountId)
        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        accountService.deleteAccount(accountId)
    }

    @PostMapping("/deposit")
    fun depositMoney(
        @RequestBody depositRequest: AccountDepositRequest,
    ): ResponseEntity<ApiAccount> {
        val accountId = UUID.fromString(depositRequest.accountId)

        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        val customer =
            customerService.getCustomer(account.customerId)
                ?: throw NoSuchElementException("Customer with id ${account.customerId} not found")

        if (account.currency != depositRequest.currency) {
            throw IllegalArgumentException("Deposit currency must match account currency")
        }

        if (depositRequest.amount <= 0.toBigDecimal()) {
            throw IllegalArgumentException("Deposit amount must be positive")
        }

        val amount: BigDecimal = depositRequest.amount

        val updatedAccount =
            account.copy(
                balance = account.balance + amount,
                updatedAt = LocalDate.now(),
            )

        accountService.updateAccount(updatedAccount)

        return ResponseEntity.ok(ApiAccount(account = updatedAccount, customer = customer))
    }

    @PostMapping("/withdraw")
    fun withdrawMoney(
        @RequestBody withdrawRequest: AccountWithdrawRequest,
    ): ResponseEntity<ApiAccount> {
        val accountId = UUID.fromString(withdrawRequest.accountId)

        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        val customer =
            customerService.getCustomer(account.customerId)
                ?: throw NoSuchElementException("Customer with id ${account.customerId} not found")

        if (account.currency != withdrawRequest.currency) {
            throw IllegalArgumentException("Withdraw currency must match account currency")
        }

        val amount: BigDecimal = withdrawRequest.amount

        if (amount <= 0.toBigDecimal()) {
            throw IllegalArgumentException("Withdraw amount must be positive")
        }

        if (account.balance < amount) {
            throw IllegalArgumentException("Account balance is less than requested amount")
        }

        val updatedAccount =
            account.copy(
                balance = account.balance - amount,
                updatedAt = LocalDate.now(),
            )

        accountService.updateAccount(updatedAccount)

        return ResponseEntity.ok(ApiAccount(account = updatedAccount, customer = customer))
    }
}
