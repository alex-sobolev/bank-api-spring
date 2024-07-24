package io.alex.bank.creditscore.scorex

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientProvider(
    @Value("\${scorex.base.url}") private val baseUrl: String,
) {
    private val authToken = "eyJ"

    @Bean
    fun scorexWebClient(): WebClient =
        WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer $authToken")
            .defaultHeader("Content-Type", "application/json")
            .build()
}
