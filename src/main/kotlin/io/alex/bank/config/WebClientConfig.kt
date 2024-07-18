package io.alex.bank.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    // TODO: add default timeout of 10 seconds
    @Bean
    fun webClient(): WebClient = WebClient.builder().build()
}
