package io.alex.bank.api

import io.alex.bank.account.models.Account
import io.alex.bank.account.models.AccountDepositRequest
import io.alex.bank.account.models.AccountStatus
import io.alex.bank.account.models.AccountWithdrawRequest
import io.alex.bank.account.models.ApiAccount
import io.alex.bank.account.models.ApiAccountListPage
import io.alex.bank.account.models.ApiCustomerAccountList
import io.alex.bank.account.models.CreateAccountRequest
import io.alex.bank.account.services.AccountService
import io.alex.bank.customer.services.CustomerService
import io.alex.bank.error.Failure.CustomerNotFound
import io.alex.bank.error.handleFailure
import io.alex.bank.utils.parseUuidFromString
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
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/accounts")
class AccountController(private val accountService: AccountService, private val customerService: CustomerService) {
    @GetMapping
    fun getAccounts(
        @RequestParam pageSize: Int?,
        @RequestParam page: Int?,
    ): ResponseEntity<ApiAccountListPage> {
        val pageNumber = page ?: 1
        val pageLimit = pageSize ?: 50
        val accounts = accountService.getAccounts(pageSize = pageLimit, page = pageNumber)

        return ResponseEntity.ok(ApiAccountListPage(accounts = accounts, page = pageNumber, pageSize = pageLimit))
    }

    @GetMapping("/{accountId}")
    fun getAccountById(
        @PathVariable accountId: String,
    ): ResponseEntity<ApiAccount> {
        val accountUuid = UUID.fromString(accountId)

        val account =
            accountService.getAccount(accountUuid) ?: throw NoSuchElementException("Account with id $accountUuid not found")

        val customer =
            customerService.getCustomer(account.customerId).fold(
                ifLeft = { handleFailure(CustomerNotFound("Customer with id ${account.customerId} not found")) },
                ifRight = { it },
            )

        return ResponseEntity.ok(ApiAccount(account = account, customer = customer))
    }

    @GetMapping("/search")
    fun getAccountsByCustomerId(
        @RequestParam(required = true) customerId: String,
    ): ResponseEntity<ApiCustomerAccountList> {
        val customerUuid = parseUuidFromString(customerId, "Invalid customer id format: $customerId")

        val customer =
            customerService.getCustomer(customerUuid)
                .fold(
                    ifLeft = { handleFailure(CustomerNotFound("Customer with id $customerUuid not found")) },
                    ifRight = { it },
                )

        val accounts = accountService.getAccountsByCustomerId(customerUuid)

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
                .fold(
                    ifLeft = { handleFailure(CustomerNotFound("Customer with id $customerId not found")) },
                    ifRight = { it },
                )

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
                version = 0,
            )

        accountService.createAccount(account)

        return ResponseEntity.ok(ApiAccount(account = account, customer = customer))
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAccount(
        @PathVariable accountId: String,
    ) {
        val accountUuid = UUID.fromString(accountId)

        if (accountService.getAccount(accountUuid) == null) {
            throw NoSuchElementException("Account with id $accountUuid not found")
        }

        accountService.deleteAccount(accountUuid)
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
                .fold(
                    ifLeft = { handleFailure(CustomerNotFound("Customer with id ${account.customerId} not found")) },
                    ifRight = { it },
                )

        val updatedAccount =
            accountService.deposit(
                accountId = accountId,
                amount = depositRequest.amount,
                currency = depositRequest.currency,
                version = depositRequest.version,
            )

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
                .fold(
                    ifLeft = { handleFailure(CustomerNotFound("Customer with id ${account.customerId} not found")) },
                    ifRight = { it },
                )

        val updatedAccount =
            accountService.withdraw(
                accountId = accountId,
                amount = withdrawRequest.amount,
                currency = withdrawRequest.currency,
                version = withdrawRequest.version,
            )

        return ResponseEntity.ok(ApiAccount(account = updatedAccount, customer = customer))
    }
}
