package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.account.models.*
import com.wolt.wm.training.bank.account.services.AccountService
import com.wolt.wm.training.bank.common.models.ApiErrorResponseBody
import com.wolt.wm.training.bank.customer.services.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/accounts")
class AccountController(private val accountService: AccountService, private val customerService: CustomerService) {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(err: NoSuchElementException): ResponseEntity<ApiErrorResponseBody> =
        ResponseEntity(ApiErrorResponseBody(error = err.message, status = 404), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(err: IllegalArgumentException): ResponseEntity<ApiErrorResponseBody> =
        ResponseEntity(ApiErrorResponseBody(error = err.message, status = 400), HttpStatus.BAD_REQUEST)

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
    fun getAccountById(@PathVariable accountId: String): ResponseEntity<ApiAccount> {
        val accountId = UUID.fromString(accountId)

        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        val customer = customerService.getCustomer(account.customerId)
            ?: throw NoSuchElementException("Customer with id ${account.customerId} not found")

        return ResponseEntity.ok(ApiAccount(account = account, customer = customer))
    }


    @GetMapping("/customers/{customerId}")
    fun getAccountsByCustomerId(@PathVariable customerId: String): ResponseEntity<ApiCustomerAccountList> {
        val customerId = UUID.fromString(customerId)

        val customer = customerService.getCustomer(customerId)
            ?: throw NoSuchElementException("Customer with id $customerId not found")

        val accounts = accountService.getAccountsByCustomerId(customerId)

        return ResponseEntity.ok(ApiCustomerAccountList(customer = customer, accounts = accounts))
    }

    @PostMapping("/create")
    fun createAccount(@RequestBody accountRequest: CreateAccountRequest): ResponseEntity<ApiAccount> {
        val newAccountId = UUID.randomUUID()

        val customer = customerService.getCustomer(accountRequest.customerId)
            ?: throw NoSuchElementException("Customer with id ${accountRequest.customerId} not found")

        val account = Account(
            id = newAccountId,
            customerId = accountRequest.customerId,
            balance = 0.0,
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
    fun deleteAccount(@PathVariable accountId: String) {
        val accountId = UUID.fromString(accountId)
        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        accountService.deleteAccount(accountId)
    }

    @PostMapping("/deposit")
    fun depositMoney(@RequestBody depositRequest: AccountDepositRequest): ResponseEntity<ApiAccount> {
        val accountId = UUID.fromString(depositRequest.accountId)

        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        val customer = customerService.getCustomer(account.customerId)
            ?: throw NoSuchElementException("Customer with id ${account.customerId} not found")

        val amount: Double = try {
            depositRequest.amount.toDouble()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid number format ('amount'): ${depositRequest.amount}")
        }

        val updatedAccount = account.copy(
            balance = account.balance + amount,
            updatedAt = LocalDate.now(),
        )

        accountService.updateAccount(updatedAccount)

        return ResponseEntity.ok(ApiAccount(account = updatedAccount, customer = customer))
    }

    @PostMapping("/withdraw")
    fun withdrawMoney(@RequestBody withdrawRequest: AccountWithdrawRequest): ResponseEntity<ApiAccount> {
        val accountId = UUID.fromString(withdrawRequest.accountId)

        val account =
            accountService.getAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        val customer = customerService.getCustomer(account.customerId)
            ?: throw NoSuchElementException("Customer with id ${account.customerId} not found")

        val amount: Double = try {
            withdrawRequest.amount.toDouble()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid number format ('amount'): ${withdrawRequest.amount}")
        }

        if (account.balance < amount) {
            throw IllegalArgumentException("Account balance is less than requested amount")
        }

        val updatedAccount = account.copy(
            balance = account.balance - amount,
            updatedAt = LocalDate.now(),
        )

        accountService.updateAccount(updatedAccount)

        return ResponseEntity.ok(ApiAccount(account = updatedAccount, customer = customer))
    }
}
