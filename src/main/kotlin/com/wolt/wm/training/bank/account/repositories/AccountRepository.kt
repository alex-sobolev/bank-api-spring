package com.wolt.wm.training.bank.account.repositories

import com.wolt.wm.training.bank.account.models.Account
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AccountRepository {
    private val accounts: MutableList<Account> = mutableListOf()

    fun getAccounts(query: String?, pageSize: Int, page: Int): List<Account> {
        val offset = (page - 1) * pageSize

        val filteredAccounts = when {
            query.isNullOrBlank() -> accounts
            else -> accounts.filter {
                it.id.toString().contains(query, ignoreCase = true) ||
                it.customerId.toString().contains(query, ignoreCase = true) ||
                it.balance.toString().contains(query, ignoreCase = true) ||
                it.currency.toString().contains(query, ignoreCase = true)
            }
        }

        return when {
            filteredAccounts.size >= offset + pageSize -> filteredAccounts.subList(offset, offset + pageSize)
            filteredAccounts.size - offset > 0 -> filteredAccounts.subList(offset, filteredAccounts.size)
            else -> emptyList()
        }
    }

    fun getAccountsByCustomerId(customerId: UUID): List<Account> {
        return accounts.filter { it.customerId == customerId }
    }

    fun getAccount(accountId: UUID): Account? {
        return accounts.find { it.id == accountId }
    }

    fun createAccount(account: Account) {
        accounts.add(account)
    }

    fun updateAccount(account: Account) {
        val prev = accounts.find { it.id == account.id }
        accounts.remove(prev)
        accounts.add(account)
    }

    fun deleteAccount(accountId: UUID) {
        val account = accounts.find { it.id == accountId }
        accounts.remove(account)
    }
}
