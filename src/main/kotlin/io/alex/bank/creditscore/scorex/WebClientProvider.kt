package io.alex.bank.creditscore.scorex

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class WebClientProvider(
    @Value("\${scorex.base.url}") private val baseUrl: String,
) {
    private val authToken = "eyJ"

    fun createWebClient(): WebClient =
        WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer $authToken")
            .defaultHeader("Content-Type", "application/json")
            .build()
}
