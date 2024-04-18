package io.alex.bank.account.repositories

import io.alex.bank.account.models.Account
import io.alex.bank.account.models.AccountStatus
import io.alex.bank.account.models.AccountType
import io.alex.bank.account.models.Currency
import io.alex.bank.db.tables.records.AccountRecord
import io.alex.bank.db.tables.references.ACCOUNT
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountRepository(private val ctx: DSLContext) {
    private fun AccountRecord.toDomain(): Account =
        Account(
            id = id!!,
            customerId = customerId!!,
            balance = balance!!,
            currency = Currency.valueOf(currency!!),
            type = AccountType.valueOf(type!!),
            status = AccountStatus.valueOf(status!!),
            createdAt = createdAt!!,
            updatedAt = updatedAt,
            version = version!!,
        )

    private fun Account.toRecord(increaseVersion: Boolean = false): AccountRecord =
        AccountRecord().also {
            it.id = id
            it.customerId = customerId
            it.balance = balance
            it.currency = currency.name
            it.type = type.name
            it.status = status.name
            it.createdAt = createdAt
            it.updatedAt = updatedAt
            it.version = if (increaseVersion) version + 1 else version
        }

    private fun upsertAccount(account: Account): Account? {
        val result =
            ctx.insertInto(ACCOUNT)
                .set(account.toRecord())
                .onConflict(ACCOUNT.ID)
                .doUpdate()
                .set(account.toRecord(true))
                .where(ACCOUNT.VERSION.eq(account.version))
                .returning()
                .fetchOne()

        return result?.toDomain()
    }

    fun getAccounts(
        pageSize: Int,
        page: Int,
    ): List<Account> {
        val offset = (page - 1) * pageSize
        val query = ctx.selectFrom(ACCOUNT).where(ACCOUNT.STATUS.eq(AccountStatus.ACTIVE.name)).offset(offset).limit(pageSize)
        val records = query.fetch()

        return records.map { it.toDomain() }
    }

    fun getAccountsByCustomerId(customerId: UUID): List<Account> {
        val accountRecords = ctx.selectFrom(ACCOUNT).where(ACCOUNT.CUSTOMER_ID.eq(customerId)).fetch()

        return accountRecords.map { it.toDomain() }
    }

    fun findAccount(accountId: UUID): Account? {
        val record =
            ctx.selectFrom(
                ACCOUNT,
            ).where(ACCOUNT.ID.eq(accountId)).and(ACCOUNT.STATUS.eq(AccountStatus.ACTIVE.name)).fetchOne() ?: return null

        return record.toDomain()
    }

    fun createAccount(account: Account): Account? = upsertAccount(account)

    fun updateAccount(account: Account): Account? = upsertAccount(account)

    fun deleteAccount(accountId: UUID): Int {
        return ctx.update(ACCOUNT).set(ACCOUNT.STATUS, AccountStatus.INACTIVE.name).where(ACCOUNT.ID.eq(accountId)).execute()
    }

    fun deleteAccountsByCustomerId(customerId: UUID): Int {
        return ctx.update(ACCOUNT).set(ACCOUNT.STATUS, AccountStatus.INACTIVE.name).where(ACCOUNT.CUSTOMER_ID.eq(customerId)).execute()
    }
}
