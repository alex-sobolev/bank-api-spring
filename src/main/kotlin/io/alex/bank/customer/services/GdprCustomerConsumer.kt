package io.alex.bank.customer.services

import arrow.core.left
import com.fasterxml.jackson.databind.ObjectMapper
import io.alex.bank.customer.models.KafkaGdprAnonymizeCustomerEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

            logger.info("Anonymizing customer with id ${gdprCustomerEvent.customerId}")

            val result = customerService.anonymizeCustomer(gdprCustomerEvent.customerId)

            if (result.isLeft()) {
                logger.warn("Failed to anonymize customer with id ${gdprCustomerEvent.customerId}: ${result.left()}")
            }

            logger.info("Successfully consumed GDPR event for customer with id: ${gdprCustomerEvent.customerId}")
            ack.acknowledge()
        } catch (t: Throwable) {
            logger.error("Failed to consume GDPR event: $t")
            ack.nack(Duration.of(1, ChronoUnit.SECONDS))
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(GdprCustomerConsumer::class.java)
    }
}
