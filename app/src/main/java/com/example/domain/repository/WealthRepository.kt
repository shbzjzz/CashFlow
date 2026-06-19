package com.example.domain.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

interface WealthRepository {
    // Accounts
    fun getAllAccountsFlow(isSecondCountry: Boolean): Flow<List<AccountEntity>>
    suspend fun getAllAccounts(isSecondCountry: Boolean): List<AccountEntity>
    suspend fun getAccountById(id: Int): AccountEntity?
    suspend fun insertAccount(account: AccountEntity): Long
    suspend fun updateAccount(account: AccountEntity)
    suspend fun deleteAccount(account: AccountEntity)

    // Categories
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>
    suspend fun getAllCategories(): List<CategoryEntity>
    suspend fun getCategoryById(id: Int): CategoryEntity?
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
    suspend fun initDefaultCategories()

    // Transactions
    fun getAllTransactionsFlow(isSecondCountry: Boolean): Flow<List<TransactionEntity>>
    suspend fun getAllTransactions(isSecondCountry: Boolean): List<TransactionEntity>
    suspend fun getTransactionById(id: Int): TransactionEntity?
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    fun getTransactionsInRangeFlow(start: Long, end: Long, isSecondCountry: Boolean): Flow<List<TransactionEntity>>

    // Budgets
    fun getBudgetsForMonthFlow(month: String, isSecondCountry: Boolean): Flow<List<BudgetEntity>>
    suspend fun getBudgetsForMonth(month: String, isSecondCountry: Boolean): List<BudgetEntity>
    suspend fun insertBudget(budget: BudgetEntity): Long
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(budget: BudgetEntity)

    // EMIs
    fun getAllEMIsFlow(isSecondCountry: Boolean): Flow<List<EMIEntity>>
    suspend fun insertEMI(emi: EMIEntity): Long
    suspend fun updateEMI(emi: EMIEntity)
    suspend fun deleteEMI(emi: EMIEntity)

    // Backup & Restore
    suspend fun getAllAccountsDirect(): List<AccountEntity>
    suspend fun getAllTransactionsDirect(): List<TransactionEntity>
    suspend fun getAllBudgetsDirect(): List<BudgetEntity>
    suspend fun getAllEMIsDirect(): List<EMIEntity>
    suspend fun clearAllData()
}
