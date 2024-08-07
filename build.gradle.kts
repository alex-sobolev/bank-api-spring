import dev.monosoul.jooq.RecommendedVersions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("dev.monosoul.jooq-docker") version "6.0.3"
}

group = "io.alex"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    jooqCodegen("org.postgresql:postgresql:42.7.3")
    runtimeOnly("org.postgresql:postgresql:42.7.3")
    implementation("org.flywaydb:flyway-core:${RecommendedVersions.FLYWAY_VERSION}")
    implementation("org.flywaydb:flyway-database-postgresql:${RecommendedVersions.FLYWAY_VERSION}")
    implementation("org.jooq:jooq:${RecommendedVersions.JOOQ_VERSION}")
    implementation("io.arrow-kt:arrow-core:2.0.0-alpha.2")
    implementation("io.arrow-kt:arrow-fx-coroutines:2.0.0-alpha.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.2")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:4.1.3")
}

val jooqGeneratedClassesDirName = "generated-jooq"
ktlint {
    filter {
        exclude { it.file.path.contains("/$jooqGeneratedClassesDirName/") }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    generateJooqClasses {
        withContainer {
            image {
                name = "postgres:15.2"
            }
        }
        schemas.set(setOf("public"))
        outputSchemaToDefault.set(setOf("public"))
        basePackageName.set("io.alex.bank.db")
        usingJavaConfig {
            name = "org.jooq.codegen.KotlinGenerator"
        }
    }
}
