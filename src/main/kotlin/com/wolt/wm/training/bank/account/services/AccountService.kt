package com.wolt.wm.training.bank.account.services

import com.wolt.wm.training.bank.account.models.Account
import com.wolt.wm.training.bank.account.repositories.AccountRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountService(private val accountRepository: AccountRepository) {
    fun getAccounts(
        query: String?,
        pageSize: Int,
        page: Int,
    ) = accountRepository.getAccounts(query = query, pageSize = pageSize, page = page)

    fun getAccountsByCustomerId(customerId: UUID): List<Account> = accountRepository.getAccountsByCustomerId(customerId)

    fun getAccount(accountId: UUID): Account? = accountRepository.getAccount(accountId)

    fun createAccount(account: Account) = accountRepository.createAccount(account)

    fun updateAccount(account: Account) = accountRepository.updateAccount(account)

    fun deleteAccount(accountId: UUID) = accountRepository.deleteAccount(accountId)
}
