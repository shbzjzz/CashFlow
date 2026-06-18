package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Cash", "Bank", "Credit Card", "Digital Wallet"
    val balance: Double
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Income", "Expense"
    val icon: String, // e.g. "restaurant", "shopping_cart", "home", "payments", "fitness_center", etc.
    val color: String // hex: e.g. "#FF4B4B"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "Income", "Expense", "Transfer"
    val categoryId: Int?, // Nullable for general notes/transfers
    val accountId: Int, // Source account
    val transferToAccountId: Int? = null, // Destination account if Transfer
    val date: Long, // Timestamp (epoch millis)
    val note: String,
    val imagePath: String? = null
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int, // category associated, or -1 for overall limit
    val limitAmount: Double,
    val month: String // YYYY-MM formatted
)

@Entity(tableName = "emis")
data class EMIEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueDate: String, // day of the month (e.g. "15th") or date
    val categoryId: Int? = null,
    val isDebt: Boolean = false,
    val debtType: String = "Borrowed", // "Borrowed", "Lent"
    val personName: String = "",
    val tenureMonths: Int = 12
)
