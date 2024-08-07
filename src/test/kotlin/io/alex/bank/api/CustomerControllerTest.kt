package io.alex.bank.api

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.ninjasquad.springmockk.SpykBean
import io.alex.bank.IntegrationBaseTest
import io.alex.bank.account.models.Account
import io.alex.bank.account.models.AccountStatus
import io.alex.bank.account.models.AccountType
import io.alex.bank.account.models.Currency
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.creditscore.csnu.CsnuClient
import io.alex.bank.creditscore.scorex.ScorexClient
import io.alex.bank.customer.models.Address
import io.alex.bank.customer.models.ApiCustomerCreditScore
import io.alex.bank.customer.models.ApiCustomerListPage
import io.alex.bank.customer.models.CreditScore
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.CustomerRequest
import io.alex.bank.customer.models.CustomerStatus
import io.alex.bank.customer.models.LoanRecommendation.APPROVE
import io.alex.bank.customer.repositories.CustomerRepository
import io.alex.bank.fixtures.CsnuClientFixtures.testCsnuCreditScoreResponse
import io.alex.bank.fixtures.CustomerFixtures.testCustomer
import io.alex.bank.fixtures.ScorexClientFixtures.testScorexCreditScoreResponse
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class CustomerControllerTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val csnuClient: CsnuClient,
    @Autowired private val scorexClient: ScorexClient,
) : IntegrationBaseTest() {
    companion object {
        val expectedFirstCustomer =
            Customer(
                id = UUID.randomUUID(),
                firstName = "Test",
                lastName = "User",
                birthdate = LocalDate.of(1990, 1, 1),
                gender = "Male",
                address = Address(street = "Test Street", city = "Test City", country = "Test Country", postalCode = "Test Postal Code"),
                email = "test@example.com",
                phone = "+49 123 4567 8900",
                status = CustomerStatus.ACTIVE,
            )

        val newCustomerRequest =
            CustomerRequest(
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
    }

    private fun addCustomerToDB(customer: Customer) = customerRepository.createCustomer(customer)

    @Test
    fun `get customer list`() {
        addCustomerToDB(expectedFirstCustomer)

        val res =
            webTestClient
                .get()
                .uri("/api/customers")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(ApiCustomerListPage::class.java)
                .returnResult()
                .responseBody!!

        val actualFirstCustomer = res.customers[0]

        res.customers.size shouldBe 1
        res.page shouldBe 1
        res.pageSize shouldBe 50
        actualFirstCustomer shouldBe expectedFirstCustomer
    }

    @Test
    fun `get a customer by id`() {
        addCustomerToDB(expectedFirstCustomer)

        val testCustomerId = expectedFirstCustomer.id

        val res =
            webTestClient
                .get()
                .uri("/api/customers/$testCustomerId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        res shouldBe expectedFirstCustomer
    }

    @Test
    fun `create a new customer`() {
        val testCustomer = newCustomerRequest

        val res =
            webTestClient
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCustomer)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        res.firstName shouldBe testCustomer.firstName
        res.lastName shouldBe testCustomer.lastName
        res.birthdate shouldBe testCustomer.birthdate
        res.gender shouldBe testCustomer.gender
        res.address shouldBe testCustomer.address
        res.email shouldBe testCustomer.email
        res.phone shouldBe testCustomer.phone
    }

    @Test
    fun `update an existing customer`() {
        // 1. Create new customer
        val testCustomer = newCustomerRequest

        val createResponse =
            webTestClient
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCustomer)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        val createdCustomerId = createResponse.id

        // 2. Update customer
        val updatedCustomerRequest =
            newCustomerRequest.copy(email = "john.doe@gmail.com")

        val updateResponse =
            webTestClient
                .put()
                .uri("/api/customers/$createdCustomerId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCustomerRequest)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        updateResponse.email shouldBe updatedCustomerRequest.email
    }

    @Test
    fun `delete a customer`() {
        // 1. Create new customer
        val testCustomer = newCustomerRequest

        val createResponse =
            webTestClient
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCustomer)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        val createdCustomerId = createResponse.id

        // 2. Delete customer
        webTestClient
            .delete()
            .uri("/api/customers/$createdCustomerId")
            .exchange()
            .expectStatus()
            .isNoContent

        // 3. Verify customer is deleted
        webTestClient
            .get()
            .uri("/api/customers/$createdCustomerId")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    // `customerRequest` validation tests
    @Test
    fun `create a new customer with invalid email`() {
        val testCustomer = newCustomerRequest.copy(email = "invalid-email")

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new customer with invalid phone number`() {
        val testCustomer = newCustomerRequest.copy(phone = "invalid-phone-number")

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a blank first name`() {
        val testCustomer = newCustomerRequest.copy(firstName = "")

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a blank last name`() {
        val testCustomer = newCustomerRequest.copy(lastName = "")

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a birthdate in the future`() {
        val testCustomer = newCustomerRequest.copy(birthdate = LocalDate.now().plusDays(1))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a birthdate before 1910-01-01`() {
        val testCustomer = newCustomerRequest.copy(birthdate = LocalDate.of(1909, 12, 31))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a blank street address`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(street = ""))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a blank city`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(city = ""))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a blank country`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(country = ""))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user with a blank postal code`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(postalCode = ""))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `create a new user without a postal code`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(postalCode = null))

        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `returns credit score for a customer by customerId`() {
        // Given
        val customerId = UUID.randomUUID()
        val customer = testCustomer(customerId = customerId)

        // Add customer to DB
        customerRepository.createCustomer(customer)

        val cnsuCreditScore = 80
        val scorexCreditScore = 90
        val averageCreditScore = (cnsuCreditScore + scorexCreditScore) / 2
        val expectedCreditScore = CreditScore(score = averageCreditScore, recommendation = APPROVE)

        // Use WireMock to stub CsnuClient
        stubFor(
            post(urlEqualTo("/credit-score"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(testCsnuCreditScoreResponse(score = cnsuCreditScore)),
                ),
        )

        // Use WireMock to stub ScorexClient
        stubFor(
            post(urlEqualTo("/creditscore"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(testScorexCreditScoreResponse(score = scorexCreditScore)),
                ),
        )

        // When
        val res =
            webTestClient
                .get()
                .uri("/api/customers/$customerId/credit-score")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(ApiCustomerCreditScore::class.java)
                .returnResult()
                .responseBody!!

        // Then
        res.customer.id shouldBe customerId
        res.creditScore shouldBe expectedCreditScore
    }
}

class ArchiveCustomerTest(
    @Autowired private val webTestClient: WebTestClient,
    @SpykBean @Autowired private val customerRepository: CustomerRepository,
    @SpykBean @Autowired private val accountRepository: AccountRepository,
) : IntegrationBaseTest() {
    @Test
    fun `delete user with accounts`() {
        // 1. Create new customer
        val testCustomer = testCustomer()
        val createdCustomer = customerRepository.createCustomer(testCustomer)
        val createdCustomerId = createdCustomer.id

        // 2. Create new account for the created customer through accountService
        val createdAccount =
            accountRepository.createAccount(
                Account(
                    id = UUID.randomUUID(),
                    customerId = createdCustomerId,
                    balance = BigDecimal(1000),
                    currency = Currency.EUR,
                    type = AccountType.CHECKING,
                    status = AccountStatus.ACTIVE,
                    createdAt = LocalDate.now(),
                    updatedAt = null,
                    version = 0,
                ),
            )

        // 3. Delete customer
        webTestClient
            .delete()
            .uri("/api/customers/$createdCustomerId")
            .exchange()
            .expectStatus()
            .isNoContent

        // 4. Verify customer is deleted via customer repository
        customerRepository.findCustomer(createdCustomerId) shouldBe null

        // 5. Verify account is deleted via account repository
        accountRepository.findAccount(createdAccount!!.id) shouldBe null
    }

    @Test
    fun `archive customer with accounts is rolled back when account archiving fails`() {
        // 1. Create a new customer via customerRepository
        val testCustomer = testCustomer()
        val createdCustomer = customerRepository.createCustomer(testCustomer)

        // 2. Create a new account for the created customer via accountRepository
        val createdAccount =
            accountRepository.createAccount(
                Account(
                    id = UUID.randomUUID(),
                    customerId = createdCustomer.id,
                    balance = BigDecimal(1000),
                    currency = Currency.EUR,
                    type = AccountType.CHECKING,
                    status = AccountStatus.ACTIVE,
                    createdAt = LocalDate.now(),
                    updatedAt = null,
                    version = 0,
                ),
            )

        // 3. Use @SpykBean to fail the account archiving
        every {
            accountRepository.deleteAccountsByCustomerId(
                createdCustomer.id,
            )
        } throws DataIntegrityViolationException("Failed to delete accounts")

        // 4. try to archive customer
        webTestClient
            .delete()
            .uri("/api/customers/${createdCustomer.id}")
            .exchange()
            .expectStatus()
            .is5xxServerError

        // 5. Verify customer is not archived (via customer repository)
        customerRepository.findCustomer(createdCustomer.id) shouldBe createdCustomer

        // 6. Verify account is not archived (via account repository)
        accountRepository.findAccount(createdAccount!!.id) shouldBe createdAccount
    }

    @Test
    fun `archive customer with accounts should be rolled back when customer archiving fails`() {
        // 1. Create a new customer via customerRepository
        val testCustomer = testCustomer()
        val createdCustomer = customerRepository.createCustomer(testCustomer)

        // 2. Create a new account for the created customer via accountRepository
        val createdAccount =
            accountRepository.createAccount(
                Account(
                    id = UUID.randomUUID(),
                    customerId = createdCustomer.id,
                    balance = BigDecimal(1000),
                    currency = Currency.EUR,
                    type = AccountType.CHECKING,
                    status = AccountStatus.ACTIVE,
                    createdAt = LocalDate.now(),
                    updatedAt = null,
                    version = 0,
                ),
            )

        // 3. Use @SpykBean to fail the customer archiving
        every { customerRepository.deleteCustomer(createdCustomer.id) } throws DataIntegrityViolationException("Failed to delete customer")

        // 4. try to archive customer
        webTestClient
            .delete()
            .uri("/api/customers/${createdCustomer.id}")
            .exchange()
            .expectStatus()
            .is5xxServerError

        // 5. Verify customer is not archived (via customer repository)
        customerRepository.findCustomer(createdCustomer.id) shouldBe createdCustomer

        // 6. Verify account is not archived (via account repository)
        accountRepository.findAccount(createdAccount!!.id) shouldBe createdAccount
    }
}
