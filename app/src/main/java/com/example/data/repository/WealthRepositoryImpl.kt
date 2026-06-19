package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.domain.repository.WealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WealthRepositoryImpl(private val db: AppDatabase) : WealthRepository {
    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()
    private val budgetDao = db.budgetDao()
    private val emiDao = db.emiDao()

    // Accounts
    override fun getAllAccountsFlow(isSecondCountry: Boolean): Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow(isSecondCountry)
    override suspend fun getAllAccounts(isSecondCountry: Boolean): List<AccountEntity> = accountDao.getAllAccounts(isSecondCountry)
    override suspend fun getAccountById(id: Int): AccountEntity? = accountDao.getAccountById(id)
    override suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)
    override suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    override suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    // Categories
    override fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    override suspend fun getAllCategories(): List<CategoryEntity> = categoryDao.getAllCategories()
    override suspend fun getCategoryById(id: Int): CategoryEntity? = categoryDao.getCategoryById(id)
    override suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    override suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    override suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    override suspend fun initDefaultCategories() {
        val existing = categoryDao.getAllCategories()
        if (existing.isEmpty()) {
            val defaults = listOf(
                CategoryEntity(name = "Food & Dining", type = "Expense", icon = "restaurant", color = "#ff5e5b"),
                CategoryEntity(name = "Transport", type = "Expense", icon = "directions_car", color = "#457b9d"),
                CategoryEntity(name = "Rent & Housing", type = "Expense", icon = "home", color = "#2a9d8f"),
                CategoryEntity(name = "Bills & Utilities", type = "Expense", icon = "receipt", color = "#e9c46a"),
                CategoryEntity(name = "Shopping", type = "Expense", icon = "shopping_cart", color = "#9b5de5"),
                CategoryEntity(name = "Gym & Fitness", type = "Expense", icon = "fitness_center", color = "#f15bb5"),
                CategoryEntity(name = "Salary", type = "Income", icon = "payments", color = "#2b6954"),
                CategoryEntity(name = "Investments", type = "Income", icon = "trending_up", color = "#10b981"),
                CategoryEntity(name = "Allowance & Gifts", type = "Income", icon = "card_giftcard", color = "#f39c12"),
                CategoryEntity(name = "Others", type = "Expense", icon = "more_horiz", color = "#7f8c8d")
            )
            for (category in defaults) {
                categoryDao.insertCategory(category)
            }
        }
    }

    // Transactions
    override fun getAllTransactionsFlow(isSecondCountry: Boolean): Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow(isSecondCountry)
    override suspend fun getAllTransactions(isSecondCountry: Boolean): List<TransactionEntity> = transactionDao.getAllTransactions(isSecondCountry)
    override suspend fun getTransactionById(id: Int): TransactionEntity? = transactionDao.getTransactionById(id)

    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        // Apply balance changes
        updateAccountBalancesForNewTransaction(transaction)
        return transactionDao.insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        // Rollback balance changes
        rollbackAccountBalancesForOldTransaction(transaction)
        transactionDao.deleteTransaction(transaction)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        val oldTx = transactionDao.getTransactionById(transaction.id)
        if (oldTx != null) {
            // First rollback old impact
            rollbackAccountBalancesForOldTransaction(oldTx)
        }
        // Then apply new impact
        updateAccountBalancesForNewTransaction(transaction)
        transactionDao.updateTransaction(transaction)
    }

    override fun getTransactionsInRangeFlow(start: Long, end: Long, isSecondCountry: Boolean): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsInRangeFlow(start, end, isSecondCountry)
    }

    private suspend fun updateAccountBalancesForNewTransaction(tx: TransactionEntity) {
        val sourceAcc = accountDao.getAccountById(tx.accountId)
        if (sourceAcc != null) {
            val updatedSource = when (tx.type) {
                "Income" -> sourceAcc.copy(balance = sourceAcc.balance + tx.amount)
                "Expense" -> sourceAcc.copy(balance = sourceAcc.balance - tx.amount)
                "Transfer" -> sourceAcc.copy(balance = sourceAcc.balance - tx.amount)
                else -> sourceAcc
            }
            accountDao.updateAccount(updatedSource)
        }

        if (tx.type == "Transfer" && tx.transferToAccountId != null) {
            val destAcc = accountDao.getAccountById(tx.transferToAccountId)
            if (destAcc != null) {
                accountDao.updateAccount(destAcc.copy(balance = destAcc.balance + tx.amount))
            }
        }
    }

    private suspend fun rollbackAccountBalancesForOldTransaction(tx: TransactionEntity) {
        val sourceAcc = accountDao.getAccountById(tx.accountId)
        if (sourceAcc != null) {
            val rolledBackSource = when (tx.type) {
                "Income" -> sourceAcc.copy(balance = sourceAcc.balance - tx.amount)
                "Expense" -> sourceAcc.copy(balance = sourceAcc.balance + tx.amount)
                "Transfer" -> sourceAcc.copy(balance = sourceAcc.balance + tx.amount)
                else -> sourceAcc
            }
            accountDao.updateAccount(rolledBackSource)
        }

        if (tx.type == "Transfer" && tx.transferToAccountId != null) {
            val destAcc = accountDao.getAccountById(tx.transferToAccountId)
            if (destAcc != null) {
                accountDao.updateAccount(destAcc.copy(balance = destAcc.balance - tx.amount))
            }
        }
    }

    // Budgets
    override fun getBudgetsForMonthFlow(month: String, isSecondCountry: Boolean): Flow<List<BudgetEntity>> = budgetDao.getBudgetsForMonthFlow(month, isSecondCountry)
    override suspend fun getBudgetsForMonth(month: String, isSecondCountry: Boolean): List<BudgetEntity> = budgetDao.getBudgetsForMonth(month, isSecondCountry)
    override suspend fun insertBudget(budget: BudgetEntity): Long = budgetDao.insertBudget(budget)
    override suspend fun updateBudget(budget: BudgetEntity) = budgetDao.updateBudget(budget)
    override suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)

    // EMIs
    override fun getAllEMIsFlow(isSecondCountry: Boolean): Flow<List<EMIEntity>> = emiDao.getAllEMIsFlow(isSecondCountry)
    override suspend fun insertEMI(emi: EMIEntity): Long = emiDao.insertEMI(emi)
    override suspend fun updateEMI(emi: EMIEntity) = emiDao.updateEMI(emi)
    override suspend fun deleteEMI(emi: EMIEntity) = emiDao.deleteEMI(emi)

    // Backup & Restore Implementation
    override suspend fun getAllAccountsDirect(): List<AccountEntity> = accountDao.getAllAccountsDirect()
    override suspend fun getAllTransactionsDirect(): List<TransactionEntity> = transactionDao.getAllTransactionsDirect()
    override suspend fun getAllBudgetsDirect(): List<BudgetEntity> = budgetDao.getAllBudgetsDirect()
    override suspend fun getAllEMIsDirect(): List<EMIEntity> = emiDao.getAllEMIsDirect()

    override suspend fun clearAllData() {
        transactionDao.clearTransactions()
        budgetDao.clearBudgets()
        emiDao.clearEMIs()
        accountDao.clearAccounts()
        categoryDao.clearCategories()
    }
}
