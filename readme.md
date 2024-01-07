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



