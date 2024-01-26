package com.wolt.wm.training.bank.api

import com.wolt.wm.training.bank.IntegrationBaseTest
import com.wolt.wm.training.bank.customer.models.Address
import com.wolt.wm.training.bank.customer.models.ApiCustomerListPage
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.customer.models.CustomerRequest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.util.UUID

// Customer data comes from "src/main/resources/customer-mock-data.csv"
// After we switch to Postgresql, we will use a real database instead of mock data
class CustomerControllerTest(
    @Autowired private val webTestClient: WebTestClient,
) : IntegrationBaseTest() {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    companion object {
        val expectedFirstCustomer =
            Customer(
                id = UUID.fromString("7752c457-0f07-47d1-bc78-e714cceebed2"),
                firstName = "Vonny",
                lastName = "Dell Casa",
                gender = "Female",
                birthdate = LocalDate.parse("2004-01-21"),
                email = "vdellcasa0@imageshack.us",
                phone = "+351 491 936 1673",
                address =
                    Address(
                        street = "41512 Clemons Pass",
                        city = "Messejana",
                        country = "Portugal",
                        postalCode = "7600-314",
                    ),
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

    @Test
    fun `get customer list`() {
        val res =
            webTestClient.get()
                .uri("/api/customers")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ApiCustomerListPage::class.java)
                .returnResult()
                .responseBody!!

        val actualFirstCustomer = res.customers[0]

        res.customers.size shouldBe 50
        res.page shouldBe 1
        res.pageSize shouldBe 50
        actualFirstCustomer shouldBe expectedFirstCustomer
    }

    @Test
    fun `get a customer by id`() {
        val testCustomerId = UUID.fromString("7752c457-0f07-47d1-bc78-e714cceebed2")

        val res =
            webTestClient.get()
                .uri("/api/customers/$testCustomerId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        res shouldBe expectedFirstCustomer
    }

    @Test
    fun `create a new customer`() {
        val testCustomer = newCustomerRequest

        val res =
            webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCustomer)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
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
            webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCustomer)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        val createdCustomerId = createResponse.id

        // 2. Update customer
        val updatedCustomerRequest =
            newCustomerRequest.copy(email = "john.doe@gmail.com")

        val updateResponse =
            webTestClient.put()
                .uri("/api/customers/$createdCustomerId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCustomerRequest)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
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
            webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testCustomer)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer::class.java)
                .returnResult()
                .responseBody!!

        val createdCustomerId = createResponse.id

        // 2. Delete customer
        webTestClient.delete()
            .uri("/api/customers/$createdCustomerId")
            .exchange()
            .expectStatus().isNoContent

        // 3. Verify customer is deleted
        webTestClient.get()
            .uri("/api/customers/$createdCustomerId")
            .exchange()
            .expectStatus().isNotFound
    }

    // `customerRequest` validation tests
    @Test
    fun `create a new customer with invalid email`() {
        val testCustomer = newCustomerRequest.copy(email = "invalid-email")

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new customer with invalid phone number`() {
        val testCustomer = newCustomerRequest.copy(phone = "invalid-phone-number")

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a blank first name`() {
        val testCustomer = newCustomerRequest.copy(firstName = "")

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a blank last name`() {
        val testCustomer = newCustomerRequest.copy(lastName = "")

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a birthdate in the future`() {
        val testCustomer = newCustomerRequest.copy(birthdate = LocalDate.now().plusDays(1))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a birthdate before 1910-01-01`() {
        val testCustomer = newCustomerRequest.copy(birthdate = LocalDate.of(1909, 12, 31))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a blank street address`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(street = ""))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a blank city`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(city = ""))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a blank country`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(country = ""))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user with a blank postal code`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(postalCode = ""))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `create a new user without a postal code`() {
        val testCustomer = newCustomerRequest.copy(address = newCustomerRequest.address.copy(postalCode = null))

        webTestClient.post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testCustomer)
            .exchange()
            .expectStatus().isOk
    }
}
