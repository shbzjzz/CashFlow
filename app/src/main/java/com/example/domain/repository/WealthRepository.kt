package com.example.domain.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

interface WealthRepository {
    // Accounts
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>
    suspend fun getAllAccounts(): List<AccountEntity>
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
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>
    suspend fun getAllTransactions(): List<TransactionEntity>
    suspend fun getTransactionById(id: Int): TransactionEntity?
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    fun getTransactionsInRangeFlow(start: Long, end: Long): Flow<List<TransactionEntity>>

    // Budgets
    fun getBudgetsForMonthFlow(month: String): Flow<List<BudgetEntity>>
    suspend fun getBudgetsForMonth(month: String): List<BudgetEntity>
    suspend fun insertBudget(budget: BudgetEntity): Long
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(budget: BudgetEntity)

    // EMIs
    fun getAllEMIsFlow(): Flow<List<EMIEntity>>
    suspend fun insertEMI(emi: EMIEntity): Long
    suspend fun updateEMI(emi: EMIEntity)
    suspend fun deleteEMI(emi: EMIEntity)
}
