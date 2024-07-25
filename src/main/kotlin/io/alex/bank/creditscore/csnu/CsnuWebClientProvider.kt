package io.alex.bank.creditscore.csnu

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class CsnuWebClientProvider(
    @Value("\${csnu.base.url}") private val baseUrl: String,
) {
    private val authToken = "csnu-ertg"

    @Bean
    fun csnuWebClient(): WebClient =
        WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer $authToken")
            .defaultHeader("Content-Type", "application/json")
            .build()
}
