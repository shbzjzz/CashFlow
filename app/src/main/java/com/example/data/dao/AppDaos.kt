package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isSecondCountry = :isSecondCountry ORDER BY name ASC")
    fun getAllAccountsFlow(isSecondCountry: Boolean): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isSecondCountry = :isSecondCountry ORDER BY name ASC")
    suspend fun getAllAccounts(isSecondCountry: Boolean): List<AccountEntity>

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    suspend fun getAllAccountsDirect(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearCategories()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isSecondCountry = :isSecondCountry ORDER BY date DESC")
    fun getAllTransactionsFlow(isSecondCountry: Boolean): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSecondCountry = :isSecondCountry ORDER BY date DESC")
    suspend fun getAllTransactions(isSecondCountry: Boolean): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsDirect(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE date >= :start AND date <= :end AND isSecondCountry = :isSecondCountry ORDER BY date DESC")
    fun getTransactionsInRangeFlow(start: Long, end: Long, isSecondCountry: Boolean): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND isSecondCountry = :isSecondCountry")
    fun getBudgetsForMonthFlow(month: String, isSecondCountry: Boolean): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE month = :month AND isSecondCountry = :isSecondCountry")
    suspend fun getBudgetsForMonth(month: String, isSecondCountry: Boolean): List<BudgetEntity>

    @Query("SELECT * FROM budgets ORDER BY month DESC")
    suspend fun getAllBudgetsDirect(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()
}

@Dao
interface EMIDao {
    @Query("SELECT * FROM emis WHERE isSecondCountry = :isSecondCountry ORDER BY id DESC")
    fun getAllEMIsFlow(isSecondCountry: Boolean): Flow<List<EMIEntity>>

    @Query("SELECT * FROM emis WHERE isSecondCountry = :isSecondCountry ORDER BY id DESC")
    suspend fun getAllEMIs(isSecondCountry: Boolean): List<EMIEntity>

    @Query("SELECT * FROM emis ORDER BY id DESC")
    suspend fun getAllEMIsDirect(): List<EMIEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEMI(emi: EMIEntity): Long

    @Update
    suspend fun updateEMI(emi: EMIEntity)

    @Delete
    suspend fun deleteEMI(emi: EMIEntity)

    @Query("DELETE FROM emis")
    suspend fun clearEMIs()
}
