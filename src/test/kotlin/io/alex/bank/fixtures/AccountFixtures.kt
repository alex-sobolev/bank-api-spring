package io.alex.bank.fixtures

import io.alex.bank.account.models.Account
import io.alex.bank.account.models.AccountStatus
import io.alex.bank.account.models.AccountType
import io.alex.bank.account.models.Currency
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

object AccountFixtures {
    fun testAccount(
        accountId: UUID? = UUID.randomUUID(),
        customerId: UUID? = UUID.randomUUID(),
        balance: BigDecimal? = BigDecimal(1000),
        currency: Currency? = Currency.EUR,
        type: AccountType? = AccountType.CHECKING,
        status: AccountStatus? = AccountStatus.ACTIVE,
        createdAt: LocalDate? = LocalDate.now(),
        updatedAt: LocalDate? = null,
        version: Int? = 0,
    ) = Account(
        id = accountId!!,
        customerId = customerId!!,
        balance = balance!!,
        currency = currency!!,
        type = type!!,
        status = status!!,
        createdAt = createdAt!!,
        updatedAt = updatedAt,
        version = version!!,
    )
}
