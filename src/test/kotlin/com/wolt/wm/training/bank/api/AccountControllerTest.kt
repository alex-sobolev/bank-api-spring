package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.IntegrationBaseTest
import com.wolt.wm.training.bank.account.models.AccountDepositRequest
import com.wolt.wm.training.bank.account.models.AccountType
import com.wolt.wm.training.bank.account.models.AccountWithdrawRequest
import com.wolt.wm.training.bank.account.models.ApiAccount
import com.wolt.wm.training.bank.account.models.ApiAccountListPage
import com.wolt.wm.training.bank.account.models.ApiCustomerAccountList
import com.wolt.wm.training.bank.account.models.CreateAccountRequest
import com.wolt.wm.training.bank.account.models.Currency
import com.wolt.wm.training.bank.customer.models.Address
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.db.tables.references.CUSTOMER
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class AccountControllerTest(
    @Autowired private val webTestClient: WebTestClient,
) : IntegrationBaseTest() {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    companion object {
        val customerId: UUID = UUID.fromString("7752c457-0f07-47d1-bc78-e714cceebed2")

        val newCustomer =
            Customer(
                id = customerId,
                firstName = "John",
                lastName = "Doe",
                birthdate = LocalDate.of(1990, 1, 1),
                gender = "Male",
                address =
                    Address(
                        street = "123 Test Street",
                        city = "Test City",
                        country = "Test Country",
                        postalCode = "12345",
                    ),
                email = "john.doe@example.com",
                phone = "+123 456 7890",
            )

        val createAccountRequest =
            CreateAccountRequest(
                customerId = customerId.toString(),
                type = AccountType.SAVINGS,
                currency = Currency.EUR,
            )

        val notFoundUuid: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    private fun addNewCustomer(customer: Customer) {
        context.insertInto(CUSTOMER)
            .set(CUSTOMER.ID, customer.id)
            .set(CUSTOMER.FIRST_NAME, customer.firstName)
            .set(CUSTOMER.LAST_NAME, customer.lastName)
            .set(CUSTOMER.BIRTHDATE, customer.birthdate)
            .set(CUSTOMER.GENDER, customer.gender)
            .set(CUSTOMER.STREET_ADDRESS, customer.address.street)
            .set(CUSTOMER.CITY, customer.address.city)
            .set(CUSTOMER.COUNTRY, customer.address.country)
            .set(CUSTOMER.POSTAL_CODE, customer.address.postalCode)
            .set(CUSTOMER.EMAIL, customer.email)
            .set(CUSTOMER.PHONE, customer.phone)
            .execute()
    }

    private fun getAccountsList(): ApiAccountListPage =
        webTestClient.get()
            .uri("/api/accounts")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ApiAccountListPage::class.java)
            .returnResult().responseBody!!

    private fun getAccountById(accountId: UUID): ApiAccount =
        webTestClient.get()
            .uri("/api/accounts/$accountId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ApiAccount::class.java)
            .returnResult().responseBody!!

    private fun addNewAccount(createAccountRequest: CreateAccountRequest): ApiAccount =
        webTestClient.post()
            .uri("/api/accounts/create")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(createAccountRequest)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ApiAccount::class.java)
            .returnResult().responseBody!!

    private fun depositMoney(depositRequest: AccountDepositRequest): ApiAccount =
        webTestClient.post()
            .uri("/api/accounts/deposit")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(depositRequest)
            .exchange()
            .expectStatus().isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(ApiAccount::class.java)
            .returnResult().responseBody!!

    private fun withdrawMoney(withdrawRequest: AccountWithdrawRequest): ApiAccount =
        webTestClient.post()
            .uri("/api/accounts/withdraw")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(withdrawRequest)
            .exchange()
            .expectStatus().isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody(ApiAccount::class.java)
            .returnResult().responseBody!!

    @Test
    fun `get accounts list`() {
        // Initially there are no accounts
        val initialRes = getAccountsList()

        initialRes.accounts shouldBe emptyList()
        initialRes.page shouldBe 1

        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // Add an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // Get accounts list again
        val res = getAccountsList()

        res.page shouldBe 1
        res.pageSize shouldBe 50
        res.accounts.size shouldBe 1
        res.accounts[0].id shouldBe accountId
    }

    @Test
    fun `get account by id`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // Add an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // Get account by id
        val res = getAccountById(accountId)

        res.account.id shouldBe accountId
        res.customer.id shouldBe customerId
    }

    @Test
    fun `get account by id that is not found`() {
        webTestClient.get()
            .uri("/api/accounts/$notFoundUuid")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `get accounts by customer id`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // Add an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // Get accounts by customer id
        val res =
            webTestClient.get()
                .uri("/api/accounts/search?customerId=$customerId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ApiCustomerAccountList::class.java)
                .returnResult().responseBody!!

        res.accounts.size shouldBe 1
        res.accounts[0].id shouldBe accountId
        res.customer.id shouldBe customerId
    }

    @Test
    fun `get accounts by customer id that is not found`() {
        webTestClient.get()
            .uri("/api/accounts/customers/$notFoundUuid")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `create an account`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // Add an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // Get created account by its id
        val res = getAccountById(accountId)

        res.account.id shouldBe accountId
        res.account.currency shouldBe createAccountRequest.currency
        res.account.type shouldBe createAccountRequest.type
        res.customer.id shouldBe customerId
    }

    @Test
    fun `delete an existing account`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // Add an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // Check that the account exists
        val createdAccountRes = getAccountById(accountId)

        createdAccountRes.account.id shouldBe accountId
        createdAccountRes.customer.id shouldBe customerId

        // Delete the account
        webTestClient.delete()
            .uri("/api/accounts/$accountId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent

        // Check that the account is deleted
        val res =
            webTestClient.get()
                .uri("/api/accounts/$accountId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun `delete an account that is not found`() {
        webTestClient.delete()
            .uri("/api/accounts/$notFoundUuid")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `deposit money to an account`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // deposit money
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = accountId.toString(),
                amount = 100.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        val res = depositMoney(depositMoneyRequest)

        res.account.balance shouldBe BigDecimal("100.00")
        res.account.currency shouldBe createAccountRequest.currency
    }

    @Test
    fun `deposit money to an account that is not found`() {
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = notFoundUuid.toString(),
                amount = 100.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post()
            .uri("/api/accounts/deposit")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(depositMoneyRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `deposit money with a negative amount`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // deposit money
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = accountId.toString(),
                amount = (-100).toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post()
            .uri("/api/accounts/deposit")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(depositMoneyRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `deposit money with 0 amount`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // deposit money
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = accountId.toString(),
                amount = 0.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post()
            .uri("/api/accounts/deposit")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(depositMoneyRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `deposit money with a different currency`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // deposit money
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = accountId.toString(),
                amount = 100.toBigDecimal(),
                currency = Currency.USD,
            )

        webTestClient.post()
            .uri("/api/accounts/deposit")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(depositMoneyRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `withdraw money from an account`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // deposit money
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = accountId.toString(),
                amount = 100.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        val depositMoneyRes = depositMoney(depositMoneyRequest)

        depositMoneyRes.account.currency shouldBe createAccountRequest.currency
        depositMoneyRes.account.balance shouldBe BigDecimal("100.00")

        // withdraw money
        val withdrawMoneyRequest =
            AccountWithdrawRequest(
                accountId = accountId.toString(),
                amount = 50.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        val withdrawMoneyRes = withdrawMoney(withdrawMoneyRequest)

        withdrawMoneyRes.account.balance shouldBe BigDecimal("50.00")
    }

    @Test
    fun `withdraw money from an account that is not found`() {
        val withdrawMoneyRequest =
            AccountWithdrawRequest(
                accountId = notFoundUuid.toString(),
                amount = 100.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post()
            .uri("/api/accounts/withdraw")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(withdrawMoneyRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `withdraw money with a negative amount`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // withdraw money
        val withdrawMoneyRequest =
            AccountWithdrawRequest(
                accountId = accountId.toString(),
                amount = (-100).toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post().uri("/api/accounts/withdraw").accept(MediaType.APPLICATION_JSON)
            .bodyValue(withdrawMoneyRequest).exchange().expectStatus().isBadRequest
    }

    @Test
    fun `withdraw money with 0 amount`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // withdraw money
        val withdrawMoneyRequest =
            AccountWithdrawRequest(
                accountId = accountId.toString(),
                amount = 0.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post()
            .uri("/api/accounts/withdraw")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(withdrawMoneyRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `withdraw money with a different currency`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        createAccountRes.account.currency shouldBe createAccountRequest.currency
        createAccountRes.account.balance shouldBe 0.toBigDecimal()

        // withdraw money
        val withdrawMoneyRequest =
            AccountWithdrawRequest(
                accountId = accountId.toString(),
                amount = 100.toBigDecimal(),
                currency = Currency.USD,
            )

        webTestClient.post()
            .uri("/api/accounts/withdraw")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(withdrawMoneyRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `withdraw more money than account balance`() {
        // Add customer to Customer table
        addNewCustomer(newCustomer)

        // create an account
        val createAccountRes = addNewAccount(createAccountRequest)
        val accountId = createAccountRes.account.id

        // deposit money
        val depositMoneyRequest =
            AccountDepositRequest(
                accountId = accountId.toString(),
                amount = 100.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        val depositMoneyRes = depositMoney(depositMoneyRequest)

        depositMoneyRes.account.currency shouldBe createAccountRequest.currency
        depositMoneyRes.account.balance shouldBe BigDecimal("100.00")

        // withdraw money
        val withdrawMoneyRequest =
            AccountWithdrawRequest(
                accountId = accountId.toString(),
                amount = 150.toBigDecimal(),
                currency = createAccountRequest.currency,
            )

        webTestClient.post()
            .uri("/api/accounts/withdraw")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(withdrawMoneyRequest)
            .exchange()
            .expectStatus().isBadRequest
    }
}
