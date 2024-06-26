package io.alex.bank.customer.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.alex.bank.customer.models.KafkaGdprAnonymizeCustomerEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.temporal.ChronoUnit

inline fun <reified T> ObjectMapper.readValue(bytes: ByteArray): T = this.readValue(bytes, T::class.java)

@Service
class GdprCustomerConsumer(
    val objectMapper: ObjectMapper,
    val customerService: CustomerService,
) {
    @KafkaListener(topics = ["gdpr.customer.v1"])
    fun consume(
        bytes: ByteArray,
        ack: Acknowledgment,
    ) {
        try {
            val gdprCustomerEvent = objectMapper.readValue<KafkaGdprAnonymizeCustomerEvent>(bytes)

            // Anonymize customer data
            println("Anonymizing customer with id ${gdprCustomerEvent.customerId}")
            customerService.anonymizeCustomer(gdprCustomerEvent.customerId)

            println("Successfully consumed GDPR event for customer with id: ${gdprCustomerEvent.customerId}")
            ack.acknowledge()
        } catch (t: Throwable) {
            println("Failed to consume GDPR event: $t")
            ack.nack(Duration.of(1, ChronoUnit.SECONDS))
        }
    }
}
