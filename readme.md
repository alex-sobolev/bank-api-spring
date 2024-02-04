# Backend Learning Training - Bank

This is a training project to learn how to implement backend by using Kotlin, Spring Boot and Postgresql.

You will build a bank application in the end of this training.

A bank can have customers and accounts. A customer can have multiple accounts.

## Pre-requisites

This project requires basic Kotlin and Postgresql knowledge.

Use [Kotlin Koans](https://kotlinlang.org/docs/koans.html) to learn Kotlin.

## How to start

Run `BankApplication.kt` to start the application.
An alternative way is to run `./gradlew bootRun` in the terminal.

### Steps

#### Step-1

Implement API specs for customer. The endpoints can return dummy data for now.

Use Spring's [RestController](https://spring.io/guides/gs/rest-service/) to implement the API.

A customer has a name, birthdate, address, phone number and email address.

APIs:
- Create a customer
- Get customer by id
- Search customers by name

#### Step-2

Implement API specs for account. The endpoints can return dummy data for now.

An account has a name, balance, currency and customer id.

APIs:
- Create an account
- Get accounts by customer id
- Deposit money to an account
- Withdraw money from an account

#### Step-3

Implement validations for the API request. Investigate how to use [@ExceptionHandler](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc/#using-exceptionhandler)
and [@ControllerAdvice](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc/#using-controlleradvice-classes).

Customer validations
- Name should not be blank
- Birthdate should be before now
- Address should not be blank
- Email should have a valid format
- Phone number should have a valid format

Account validations
- Currency should be a valid currency
- Customer id should exist
- Deposit account should exist
- Deposit amount should be bigger than 0
- Deposit currency should be same as account currency
- Withdraw account should exist
- Withdraw amount should be bigger than 0
- Withdraw amount should be less or equal to the account balance
- Withdraw currency should be same as account currency

### Step-4

Test the validations that you have implemented in Step-3.

Consider using [JUnit](https://junit.org/junit5/docs/current/user-guide/#writing-tests), [WebTestClient](https://docs.spring.io/spring-framework/reference/testing/webtestclient.html#webtestclient-tests)
and [Kotest assertions](https://kotest.io/docs/assertions/assertions.html).
WebTestClient is already configured in `IntegrationBaseTest`.

Example usage:
```kotlin
class HelloControllerTest(
    @Autowired private val webTestClient: WebTestClient,
): IntegrationBaseTest() {

    @Test
    fun `returns hello world`() {
        // TODO
    }
}
```
### Step-5

Create customer table with Flyway migration.
Add your SQL script to `/src/main/resources/db/migration/`, it will be executed during application startup.

Use JOOQ to implement customer repository with following methods.
- Create customer
- Get customer by id
- Get customers by name

An example repository:
```
@Repository
class CustomerRepository(private val context: DSLContext) {

}
```

JOOQ classes are generated into `/build/generated-jooq/`.
See JOOQ docs [here](https://www.jooq.org/doc/latest/manual/getting-started/jooq-and-kotlin/)

**Note:** Uncomment `cleanUpDatabase` in `IntegrationBaseTest` after creating your first migration script, so that tables are cleaned up between integration tests.
