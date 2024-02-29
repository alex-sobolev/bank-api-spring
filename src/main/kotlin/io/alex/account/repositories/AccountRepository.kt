package io.alex.account.repositories

import com.wolt.wm.training.bank.account.models.Account
import com.wolt.wm.training.bank.account.models.AccountStatus
import com.wolt.wm.training.bank.account.models.AccountType
import com.wolt.wm.training.bank.account.models.Currency
import com.wolt.wm.training.bank.db.tables.records.AccountRecord
import com.wolt.wm.training.bank.db.tables.references.ACCOUNT
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
        )

    private fun Account.toRecord(): AccountRecord =
        AccountRecord().also {
            it.id = id
            it.customerId = customerId
            it.balance = balance
            it.currency = currency.name
            it.type = type.name
            it.status = status.name
            it.createdAt = createdAt
            it.updatedAt = updatedAt
        }

    fun getAccounts(
        pageSize: Int,
        page: Int,
    ): List<Account> {
        val offset = (page - 1) * pageSize
        val query = ctx.selectFrom(ACCOUNT).offset(offset).limit(pageSize)
        val records = query.fetch()

        return records.map { it.toDomain() }
    }

    fun getAccountsByCustomerId(customerId: UUID): List<Account> {
        val accountRecords = ctx.selectFrom(ACCOUNT).where(ACCOUNT.CUSTOMER_ID.eq(customerId)).fetch()

        return accountRecords.map { it.toDomain() }
    }

    fun findAccount(accountId: UUID): Account? {
        val record = ctx.selectFrom(ACCOUNT).where(ACCOUNT.ID.eq(accountId)).fetchOne() ?: return null

        return record.toDomain()
    }

    fun createAccount(account: Account): Account {
        val record = ctx.insertInto(ACCOUNT).set(account.toRecord()).returning().fetchOne()

        return record!!.toDomain()
    }

    fun updateAccount(account: Account): Account {
        val record = ctx.update(ACCOUNT).set(account.toRecord()).where(ACCOUNT.ID.eq(account.id)).returning().fetchOne()

        return record!!.toDomain()
    }

    fun deleteAccount(accountId: UUID) {
        ctx.deleteFrom(ACCOUNT).where(ACCOUNT.ID.eq(accountId)).execute()
    }
}
