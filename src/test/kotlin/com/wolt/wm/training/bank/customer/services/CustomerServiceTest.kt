package com.wolt.wm.training.bank.customer.services

import com.wolt.wm.training.bank.customer.repositories.CustomerRepository
import com.wolt.wm.training.bank.fixtures.CustomerFixtures.testCustomer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.UUID

class CustomerServiceTest {
    private val customerRepository: CustomerRepository = mockk()
    private val customerService: CustomerService = CustomerService(customerRepository)

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
        val actualCustomer = customerService.getCustomer(id)

        // Then
        actualCustomer shouldBe expectedCustomer
        verify(exactly = 1) { customerRepository.findCustomer(id) }
    }

    @Test
    fun `updateCustomer calls repository`() {
        // Prepare customer data
        val id = UUID.randomUUID()
        val customerToAdd = testCustomer(customerId = id)
        every { customerRepository.createCustomer(customerToAdd) } returns customerToAdd

        val addedCustomer = customerService.createCustomer(customerToAdd)

        addedCustomer shouldBe customerToAdd
        verify(exactly = 1) { customerRepository.createCustomer(customerToAdd) }

        // Given
        val customerToUpdate = addedCustomer.copy(email = "john.doe.updated@example.com")
        every { customerRepository.updateCustomer(customerToUpdate) } returns customerToUpdate

        // When
        val updatedCustomer = customerService.updateCustomer(customerToUpdate)

        // Then
        updatedCustomer shouldBe customerToUpdate
        verify(exactly = 1) { customerRepository.updateCustomer(customerToUpdate) }
    }

    @Test
    fun `deleteCustomer calls repository`() {
        // Given
        val id = UUID.randomUUID()
        every { customerRepository.deleteCustomer(id) } returns Unit

        // When
        customerService.deleteCustomer(id)

        // Then
        verify(exactly = 1) { customerRepository.deleteCustomer(id) }
    }
}
