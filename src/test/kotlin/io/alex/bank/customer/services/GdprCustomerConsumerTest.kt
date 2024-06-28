package io.alex.bank.customer.services
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import io.alex.bank.IntegrationBaseTest
import io.alex.bank.customer.models.CustomerStatus
import io.alex.bank.customer.models.KafkaGdprAnonymizeCustomerEvent
import io.alex.bank.customer.repositories.CustomerRepository
import io.alex.bank.fixtures.CustomerFixtures.testCustomer
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class GdprCustomerConsumerTest(
    @SpykBean @Autowired private val customerRepository: CustomerRepository,
    @Autowired private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    @Autowired private val objectMapper: ObjectMapper,
) : IntegrationBaseTest() {
    @Test
    fun `should consume kafka customer GDPR event`() {
        // Given
        val id = UUID.randomUUID()
        val customer = testCustomer(customerId = id, status = CustomerStatus.INACTIVE)
        val expectedCustomer = customer.anonymize()

        // Add customer to DB
        customerRepository.createCustomer(customer)

        // When
        // Send a message to the topic
        kafkaTemplate.send("gdpr.customer.v1", id.toString(), objectMapper.writeValueAsBytes(KafkaGdprAnonymizeCustomerEvent(id)))

        // Then
        // Verify that the customer is anonymized
        runBlocking {
            eventually(3.seconds) { customerRepository.findCustomer(id, true) shouldBe expectedCustomer }
        }
    }
}
