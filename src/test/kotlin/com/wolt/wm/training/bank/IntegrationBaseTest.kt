package com.wolt.wm.training.bank

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(
    classes = [BankApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureWebTestClient(timeout = "PT2M")
abstract class IntegrationBaseTest {

    companion object {
        private val postgresqlContainer = PostgreSQLContainer(DockerImageName.parse("postgres:15.2"))
            .withReuse(true)

        init {
            postgresqlContainer.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresqlContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresqlContainer.username }
            registry.add("spring.datasource.password") { postgresqlContainer.password }
        }
    }
}
