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

#### Step-4

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
#### Step-5

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

#### Step-6

Create account table with Flyway migration

Use JOOQ to implement account repository with following methods.

- Create an account
- Get accounts by customer id
- Deposit money to an account
- Withdraw money from an account

Relationships
- Customer id should exist in Customer table

**Note:** Check how foreign keys works in Postgresql [here](https://www.postgresql.org/docs/current/tutorial-fk.html)

#### Step-7

Write unit tests for your service. With unit tests, you can test your business logic isolated from external dependencies which would run faster than integration tests.

Use [mockk](https://mockk.io/) to mock the dependencies.


```kotlin
class ExampleServiceTest() {

    private val exampleRepository: ExampleRepository = mockk()
    val exampleService = ExampleService(exampleRepository)

    fun `test example`() {
        // Given
        every { exampleRepository.get() } returns Example("some example")

        ...
    }
}
```

**Bonus:** Consider using `value` class for domain model ids, so that we can leverage more from compile time type check.
```kotlin
@JvmInline
value class AccountId(val value: String)
```
See the value class docs [here](https://kotlinlang.org/docs/inline-classes.html).

- Use upsert for your insert and update operations in which you can update the record if it already exists.
  You can use sql [ON CONFLICT](https://www.postgresql.org/docs/current/sql-insert.html) for it.
  ```kotlin
  fun upsert(example :Example) =
        context.insertInto(EXAMPLES)
            .set(example.toInsertRecord())
            .onConflict(EXAMPLES.ID)
            .doUpdate()
            .set(example.toUpdateRecord())
            .where(EXAMPLES.ID.eq(id))
            .returning()
            .fetchOne()
  ```

#### Step-8

We got a feedback from users that when they search with customer name that it takes too long.
Try to improve search by customer name by using index.

Consider using GIN index explained [here](https://www.postgresql.org/docs/9.1/textsearch-indexes.html)

Use `explain analyze` to check the execution plan for your query.
See https://www.postgresql.org/docs/current/using-explain.html#USING-EXPLAIN-ANALYZE

To generate data for your `Customer` table you can use [mockaroo](https://www.mockaroo.com/):
1. Add your table fields with related types for them;
2. Specify how many rows of data you need (max: 1000 row);
3. Select format to `SQL`
4. Add your table name (`CUSTOMER`)
5. Click on `Generate Data` button.


#### Step-9

Last week, two users withdrew money from the same bank account simultaneously and the account balance went to negative.
This is a financial risk for our bank, and we want to prevent it happening again.

Consider using optimistic locking described [here](https://en.wikipedia.org/wiki/Optimistic_concurrency_control) to handle concurrent operations.

A simple approach to implement optimistic locking is
- Add a `version` column to your table.
- Increment `version` with each update.
- Get `version` before each update.
- Execute the update query with `where` condition version equals gathered version.
- When the update query does not return anything, version is changed. Either inform the user or retry.

#### Step-10

Last week, a customer was able to withdraw money from an account even though the customer is archived (soft deleted).
When we investigated, we found that the customer was archived, but his accounts were not archived.
Make sure that when a customer is archived, all of their accounts are also archived.

- Implement archive for customer and account.
- Execute customer and account archiving in a single transaction.

Consider using [TransactionTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionTemplate.html) to handle transactions in Spring.
```kotlin
class SomeService(private val transactionTemplate: TransactionTemplate) {
    
    override fun someFunction() {
        transactionTemplate.execute { 
            doDbStuff()
            doMoreDbStuff()
        }
    }
}
```
- Implement an integration test for the customer and account archiving where customer archiving is successful but account archiving fails.
  Verify that the customer is not archived in this case. Use [@MockkBean](https://github.com/Ninja-Squad/springmockk) or [@SpyBean](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/mock/mockito/SpyBean.html) to fail the account archiving in the integration test.

#### Step-11

We would like leverage functional programming in our project. Arrow-kt is a library that provides functional programming in Kotlin.
Here are the docs for Arrow-kt: https://arrow-kt.io/docs/.

- Replace throwing exceptions on the service layer with Arrow's `Either` type. See the docs [here](https://apidocs.arrow-kt.io/arrow-core/arrow.core/-either/index.html).
- On the API layer, you need to throw an exception when the `Either` is `Left`, so that the response can be mapped correctly.

### Step-12

We got a request from our users that they want to delete their personal data in the context of GDPR.
The requests will be sent to a kafka topic. The bank application should consume from it and anonymize the personal data.

- Implement a Kafka consumer that listens to the topic `gdpr.customer.v1`.
- The kafka payload contains the customer email address.
- Anonymization should happen for all personal data including customer and account.

Consider using @KafkaListener to consume messages from Kafka. See the docs [here](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html).

Here is an example usage.

```kotlin
@Service
class GdprCustomerConsumer(
    val objectMapper: ObjectMapper,
) {

    @KafkaListener(topics = ["gdpr.customer.v1"])
    fun consume(bytes: ByteArray, ack: Acknowledgment) {
        val gdprCustomerEvent = objectMapper.readValue<KafkaGdprCustomerEvent>(bytes)

        // Anonymize customer data
    }
}
```

Write an integration test to verify the anonymization process. You can use `KafkaTemplate<String, ByteArray>` to send a message to the topic.

### Step-13

A customer came to us last week, and they asked for a loan.
We want to implement a check on their credit score from third-party providers to determine if they are eligible for a loan in a bank.
There are extra risks for the bank to give a loan to a customer without knowing their credit history.

We are going to call two major third-party providers `Credit Score National Union (CSNU)` and `SCOREX` to get the credit score of the customer.

Tasks:
- Add an endpoint to get the credit score of the customer by customer id (GET or POST)
- Use HTTP client to call the third-party providers' related endpoints.
- The external providers' endpoints will return a numeric score between 0 and 100.
- Because there are two scores, we will take the average of them.
- Return the average score and also a recommendation to a bank manager in regard to should they approve loan or not.
- If the average score is:
  - less than 60: the recommendation should be `Reject`
  - between 60 and 79: `Maybe`
  - between 80 and 100: `Approve`

Technical details:
- Consider using [WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html) to make HTTP requests.
- For integration tests, in order to mock a response from a third-party provider, you can use [WireMock](https://wiremock.org/docs/getting-started/).
- Consider using Kotlin coroutine [async](https://kotlinlang.org/docs/composing-suspending-functions.html#concurrent-using-async) to make parallel requests to the third-party providers.
