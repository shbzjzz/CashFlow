package com.example.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.WealthRepositoryImpl
import com.example.domain.repository.WealthRepository
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.defaultColorTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class WealthViewModel(application: Application) : AndroidViewModel(application) {
    // Settings & State flows with local SharedPreferences persistence
    private val prefs = application.getSharedPreferences("wealthflow_prefs", Context.MODE_PRIVATE)

    private val _currency = MutableStateFlow(prefs.getString("currency", "AED") ?: "AED")
    private val _secondCountryCurrency = MutableStateFlow(prefs.getString("second_country_currency", "USD") ?: "USD")
    val secondCountryCurrency: StateFlow<String> = _secondCountryCurrency.asStateFlow()

    private val _isSecondCountryActive = MutableStateFlow(prefs.getBoolean("is_second_country_active", false))
    val isSecondCountryActive: StateFlow<Boolean> = _isSecondCountryActive.asStateFlow()

    val currency: StateFlow<String> = combine(
        _currency,
        _isSecondCountryActive,
        _secondCountryCurrency
    ) { cur, isSec, secCur ->
        if (isSec) secCur else cur
    }.stateIn(viewModelScope, SharingStarted.Eagerly, prefs.getString("currency", "AED") ?: "AED")

    private val _homeCountryName = MutableStateFlow(prefs.getString("home_country_name", "United Arab Emirates") ?: "United Arab Emirates")
    val homeCountryName: StateFlow<String> = _homeCountryName.asStateFlow()

    private val _hasSecondCountry = MutableStateFlow(prefs.getBoolean("has_second_country", false))
    val hasSecondCountry: StateFlow<Boolean> = _hasSecondCountry.asStateFlow()

    private val _secondCountryName = MutableStateFlow(prefs.getString("second_country_name", "") ?: "")
    val secondCountryName: StateFlow<String> = _secondCountryName.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _colorTheme = MutableStateFlow(
        try {
            ColorTheme.valueOf(prefs.getString("color_theme", defaultColorTheme.name) ?: defaultColorTheme.name)
        } catch (e: Exception) {
            defaultColorTheme
        }
    )
    val colorTheme: StateFlow<ColorTheme> = _colorTheme.asStateFlow()

    private val _savedPin = MutableStateFlow<String?>(prefs.getString("saved_pin", null))
    val savedPin: StateFlow<String?> = _savedPin.asStateFlow()

    private val _isAppLocked = MutableStateFlow(prefs.getString("saved_pin", null) != null)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Local Registration States
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow(prefs.getString("user_name", "Guest") ?: "Guest")
    val username: StateFlow<String> = _username.asStateFlow()

    // Dynamic Saved Accounts for the custom Sign-in selector (eliminating hardcoded lists)
    private val _savedAccounts = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val savedAccounts: StateFlow<List<Pair<String, String>>> = _savedAccounts.asStateFlow()

    private val repository: WealthRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WealthRepositoryImpl(database)

        // Populate database with default categories and sample data on initial startup run
        viewModelScope.launch {
            repository.initDefaultCategories()
            prepopulateSampleDataOnFirstRun()
            computeSmartInsights()
        }
    }

    // Live Flows
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<AccountEntity>> = _isSecondCountryActive
        .flatMapLatest { repository.getAllAccountsFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionEntity>> = _isSecondCountryActive
        .flatMapLatest { repository.getAllTransactionsFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val emis: StateFlow<List<EMIEntity>> = _isSecondCountryActive
        .flatMapLatest { repository.getAllEMIsFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Interactive Filters
    private val _selectedMonth = MutableStateFlow(getCurrentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _filterType = MutableStateFlow("All") // "All", "Income", "Expense", "Transfer"
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAccountIdFilter = MutableStateFlow<Int?>(null)
    val selectedAccountIdFilter: StateFlow<Int?> = _selectedAccountIdFilter.asStateFlow()

    private val _reportsFilter = MutableStateFlow("Monthly")
    val reportsFilter: StateFlow<String> = _reportsFilter.asStateFlow()

    // Dynamic budgets flow combined with category transaction aggregation
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val budgetsWithDetails: StateFlow<List<BudgetWithDetails>> = combine(
        selectedMonth,
        _isSecondCountryActive.flatMapLatest { repository.getBudgetsForMonthFlow(getCurrentMonthString(), it) },
        transactions,
        categories
    ) { month, budgetList, txList, catList ->
        val monthBudgets = repository.getBudgetsForMonth(month, _isSecondCountryActive.value)
        
        // Sum expenses by category for selected month
        val startOfM = getStartOfMonthTimestamp(month)
        val endOfM = getEndOfMonthTimestamp(month)

        catList.map { category ->
            val budget = monthBudgets.find { it.categoryId == category.id }
            val limit = budget?.limitAmount ?: 0.0

            val used = txList.filter {
                it.categoryId == category.id &&
                it.type == "Expense" &&
                it.date in startOfM..endOfM
            }.sumOf { it.amount }

            BudgetWithDetails(
                categoryId = category.id,
                categoryName = category.name,
                categoryColor = category.color,
                categoryIcon = category.icon,
                limitAmount = limit,
                usedAmount = used,
                month = month,
                budgetId = budget?.id
            )
        }.filter { it.limitAmount > 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart Insights State
    private val _smartInsights = MutableStateFlow<List<SmartInsight>>(emptyList())
    val smartInsights: StateFlow<List<SmartInsight>> = _smartInsights.asStateFlow()

    fun setCurrency(symbol: String) {
        _currency.value = symbol
        prefs.edit().putString("currency", symbol).apply()
    }

    fun setupSecondCountry(name: String, currency: String) {
        _hasSecondCountry.value = true
        _secondCountryName.value = name
        _secondCountryCurrency.value = currency
        prefs.edit()
            .putBoolean("has_second_country", true)
            .putString("second_country_name", name)
            .putString("second_country_currency", currency)
            .apply()
    }

    fun toggleCountry(isSecond: Boolean) {
        _isSecondCountryActive.value = isSecond
        prefs.edit().putBoolean("is_second_country_active", isSecond).apply()
    }

    fun getCurrentCurrency(): String {
        return if (_isSecondCountryActive.value) _secondCountryCurrency.value else _currency.value
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        prefs.edit().putBoolean("is_dark_mode", _isDarkMode.value).apply()
    }

    fun setColorTheme(theme: ColorTheme) {
        _colorTheme.value = theme
        prefs.edit().putString("color_theme", theme.name).apply()
    }

    fun setPin(pin: String?) {
        _savedPin.value = pin
        _isAppLocked.value = pin != null
        prefs.edit().putString("saved_pin", pin).apply()
    }

    fun unlockApp(pin: String): Boolean {
        if (_savedPin.value == pin) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (_savedPin.value != null) {
            _isAppLocked.value = true
        }
    }

    fun registerUser(name: String, homeCountry: String, homeCurrency: String, secCountry: String?, secCurrency: String?) {
        _username.value = name
        _homeCountryName.value = homeCountry
        _currency.value = homeCurrency
        
        val editor = prefs.edit()
            .putString("user_name", name)
            .putString("home_country_name", homeCountry)
            .putString("currency", homeCurrency)
            .putBoolean("is_logged_in", true)

        if (secCountry != null && secCurrency != null) {
            _hasSecondCountry.value = true
            _secondCountryName.value = secCountry
            _secondCountryCurrency.value = secCurrency
            editor.putBoolean("has_second_country", true)
                .putString("second_country_name", secCountry)
                .putString("second_country_currency", secCurrency)
        } else {
            _hasSecondCountry.value = false
            editor.putBoolean("has_second_country", false)
        }
        
        editor.apply()
        _isLoggedIn.value = true
        viewModelScope.launch {
            computeSmartInsights()
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun updateCountries(homeCountry: String, homeCurrency: String, secCountry: String?, secCurrency: String?) {
        _homeCountryName.value = homeCountry
        _currency.value = homeCurrency
        
        val editor = prefs.edit()
            .putString("home_country_name", homeCountry)
            .putString("currency", homeCurrency)

        if (secCountry != null && secCurrency != null) {
            _hasSecondCountry.value = true
            _secondCountryName.value = secCountry
            _secondCountryCurrency.value = secCurrency
            editor.putBoolean("has_second_country", true)
                .putString("second_country_name", secCountry)
                .putString("second_country_currency", secCurrency)
        } else {
            _hasSecondCountry.value = false
            editor.putBoolean("has_second_country", false)
        }
        editor.apply()
        viewModelScope.launch {
            computeSmartInsights()
        }
    }

    fun updateUsername(newName: String) {
        _username.value = newName
        prefs.edit().putString("user_name", newName).apply()
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
        viewModelScope.launch {
            computeSmartInsights()
        }
    }

    fun setFilterType(type: String) {
        _filterType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAccountIdFilter(accountId: Int?) {
        _selectedAccountIdFilter.value = accountId
    }

    fun updateReportsFilter(filter: String) {
        _reportsFilter.value = filter
    }

    // Operations
    fun addAccount(name: String, type: String, balance: Double) {
        viewModelScope.launch {
            repository.insertAccount(AccountEntity(
                name = name, 
                type = type, 
                balance = balance,
                isSecondCountry = _isSecondCountryActive.value
            ))
            addNotification(
                title = "Vault Created 🏦",
                description = "New $type vault '$name' set up with standard balance: ${getCurrentCurrency()} ${String.format(Locale.getDefault(), "%,.2f", balance)}"
            )
            computeSmartInsights()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            addNotification(
                title = "Vault Removed 🗑️",
                description = "Vault '${account.name}' has been successfully deleted."
            )
            computeSmartInsights()
        }
    }

    fun addTransaction(
        amount: Double,
        type: String,
        categoryId: Int?,
        accountId: Int,
        transferToAccountId: Int? = null,
        date: Long,
        note: String,
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                transferToAccountId = transferToAccountId,
                date = date,
                note = note,
                imagePath = imagePath,
                isSecondCountry = _isSecondCountryActive.value
            )
            repository.insertTransaction(tx)
            val descSuffix = if (note.isNotBlank()) " ($note)" else ""
            addNotification(
                title = if (type == "Transfer") "Transfer Swapped 🔄" else if (type == "Income") "Income Credit 📈" else "Debit Recorded 📉",
                description = "Recorded $type: ${getCurrentCurrency()} ${String.format(Locale.getDefault(), "%,.2f", amount)}$descSuffix."
            )
            computeSmartInsights()
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
            computeSmartInsights()
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            computeSmartInsights()
        }
    }

    fun addCategory(name: String, type: String, icon: String, color: String) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name, type = type, icon = icon, color = color))
        }
    }

    fun saveBudget(categoryId: Int, limitAmount: Double, month: String) {
        viewModelScope.launch {
            val isSecond = _isSecondCountryActive.value
            val existing = repository.getBudgetsForMonth(month, isSecond)
            val match = existing.find { it.categoryId == categoryId }
            if (match != null) {
                if (limitAmount <= 0) {
                    repository.deleteBudget(match)
                } else {
                    repository.updateBudget(match.copy(limitAmount = limitAmount))
                }
            } else if (limitAmount > 0) {
                repository.insertBudget(BudgetEntity(
                    categoryId = categoryId, 
                    limitAmount = limitAmount, 
                    month = month,
                    isSecondCountry = isSecond
                ))
            }
            addNotification(
                title = "Budget Target Saved 🎯",
                description = "Configured budget target: ${getCurrentCurrency()} ${String.format(Locale.getDefault(), "%,.2f", limitAmount)} for category matching current month."
            )
            computeSmartInsights()
        }
    }

    fun addEMI(
        title: String,
        total: Double,
        paid: Double,
        dueDate: String,
        isDebt: Boolean = false,
        debtType: String = "Borrowed",
        personName: String = "",
        tenureMonths: Int = 12,
        adjustAccountBalance: Boolean = false,
        accountId: Int? = null
    ) {
        viewModelScope.launch {
            repository.insertEMI(
                EMIEntity(
                    title = title,
                    totalAmount = total,
                    paidAmount = paid,
                    dueDate = dueDate,
                    isDebt = isDebt,
                    debtType = debtType,
                    personName = personName,
                    tenureMonths = tenureMonths,
                    isSecondCountry = _isSecondCountryActive.value
                )
            )

            if (adjustAccountBalance && accountId != null) {
                // If isDebt and Borrowed -> we get cash (Income)
                // If isDebt and Lent -> we give cash (Expense)
                // Else (EMI/Installment) -> we represent it as cash going out (Expense)
                val type = if (isDebt) {
                    if (debtType == "Borrowed") "Income" else "Expense"
                } else {
                    "Expense"
                }

                val targetCat = repository.getAllCategories().find {
                    if (type == "Income") it.name.contains("Salary", ignoreCase = true) || it.name.contains("Refund", ignoreCase = true)
                    else it.name.contains("Bills", ignoreCase = true) || it.name.contains("Debt", ignoreCase = true)
                }

                addTransaction(
                    amount = total,
                    type = type,
                    categoryId = targetCat?.id,
                    accountId = accountId,
                    date = System.currentTimeMillis(),
                    note = "Loan/EMI Setup: $title"
                )
            }

            addNotification(
                title = if (isDebt) "New Debt Added 📝" else "New Installment Tracked 🏷️",
                description = "Started tracking '$title' for a total of ${getCurrentCurrency()} ${String.format(Locale.getDefault(), "%,.2f", total)}."
            )

            // Register system alarm/notification on selected due date if it is an installment
            try {
                if (!isDebt && dueDate != "N/A") {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val parsedDate = sdf.parse(dueDate)
                    if (parsedDate != null) {
                        val monthlyAmt = total / tenureMonths
                        val cur = getCurrentCurrency()
                        com.example.receiver.EmiAlarmScheduler.scheduleEmiAlertAtDate(
                            context = getApplication<android.app.Application>().applicationContext,
                            emiTitle = title,
                            dueDateMillis = parsedDate.time,
                            amount = monthlyAmt,
                            currency = cur
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateEMI(emi: EMIEntity) {
        viewModelScope.launch {
            repository.updateEMI(emi)
        }
    }

    fun payEMIInstallment(emi: EMIEntity, amount: Double, accountId: Int) {
        viewModelScope.launch {
            val targetPaid = (emi.paidAmount + amount).coerceAtMost(emi.totalAmount)
            repository.updateEMI(emi.copy(paidAmount = targetPaid))
            
            // Log it as an expense transaction too! Excellent touch for accounting!
            val expenseCat = repository.getAllCategories().find { it.name.contains("Bills", ignoreCase = true) }
            addTransaction(
                amount = amount,
                type = "Expense",
                categoryId = expenseCat?.id,
                accountId = accountId,
                date = System.currentTimeMillis(),
                note = "EMI Payment: ${emi.title}"
            )
            addNotification(
                title = "Repayment Tracked 💵",
                description = "Paid installment of ${getCurrentCurrency()} ${String.format(Locale.getDefault(), "%,.2f", amount)} towards '${emi.title}'."
            )
        }
    }

    fun deleteEMI(emi: EMIEntity) {
        viewModelScope.launch {
            repository.deleteEMI(emi)
            addNotification(
                title = if (emi.isDebt) "Debt Cleared 📝" else "Installment Omitted 🏷️",
                description = "'${emi.title}' removed from tracker."
            )
        }
    }

    // Prepopulate system with rich default sample data on first launch
    private suspend fun prepopulateSampleDataOnFirstRun() {
        val existingAccounts = repository.getAllAccounts(false)
        if (existingAccounts.isEmpty()) {
            // Emptied as requested
        }
    }

    // Improved AI Insights logic
    private fun computeSmartInsights() {
        viewModelScope.launch {
            val txs = transactions.value
            val isSec = _isSecondCountryActive.value
            val curr = if (isSec) _secondCountryCurrency.value else _currency.value
            
            val totalInc = txs.filter { it.type == "Income" }.sumOf { it.amount }
            val totalExp = txs.filter { it.type == "Expense" }.sumOf { it.amount }
            val netFlow = totalInc - totalExp

            val topCategory = txs.filter { it.type == "Expense" }
                .groupBy { it.categoryId }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .maxByOrNull { it.value }

            val cats = categories.value
            val catName = cats.find { it.id == topCategory?.key }?.name ?: "General"

            val insightsList = mutableListOf<SmartInsight>()
            
            if (netFlow < 0) {
                insightsList.add(SmartInsight(
                    "Budget Deficit",
                    "You've spent ${curr} ${String.format("%.2f", -netFlow)} more than earned. Review top categories to cut back.",
                    "warning",
                    "#BA1A1A"
                ))
            } else if (totalInc > 0 && totalExp > (totalInc * 0.7)) {
                insightsList.add(SmartInsight(
                    "High Burn Rate",
                    "Expenses are over 70% of income. Strategy: Focus on building a 3-month emergency fund.",
                    "trending_up",
                    "#E66B12"
                ))
            } else if (netFlow > 0) {
                insightsList.add(SmartInsight(
                    "Wealth Building",
                    "Surplus of ${curr} ${String.format("%.2f", netFlow)}! Consider moving a portion into long-term growth assets.",
                    "savings",
                    "#2B6954"
                ))
            }

            if (topCategory != null) {
                insightsList.add(SmartInsight(
                    "Category Focus",
                    "Heaviest spending in **$catName** (${curr} ${String.format("%.2f", topCategory.value)}). Look for optimization opportunities.",
                    "shopping_bag",
                    "#6750A4"
                ))
            }
            
            if (insightsList.isEmpty()) {
                insightsList.add(SmartInsight(
                    "Welcome",
                    "Start tracking your daily transactions to unlock personalized financial health deep-dives.",
                    "lightbulb",
                    "#2B6954"
                ))
            }

            _smartInsights.value = insightsList
        }
    }

    // CSV Import / Export Utilities
    fun exportToCSV(context: Context): Uri? {
        return try {
            val sb = java.lang.StringBuilder()

            // 1. ACCOUNTS
            sb.append("# SECTION: ACCOUNTS\n")
            sb.append("ID,Name,Type,Balance,IsSecondCountry\n")
            val allAccounts = kotlinx.coroutines.runBlocking { repository.getAllAccountsDirect() }
            allAccounts.forEach { acc ->
                val nameEsc = acc.name.replace("\"", "\"\"")
                val typeEsc = acc.type.replace("\"", "\"\"")
                sb.append("${acc.id},\"$nameEsc\",\"$typeEsc\",${acc.balance},${acc.isSecondCountry}\n")
            }
            sb.append("\n")

            // 2. CATEGORIES
            sb.append("# SECTION: CATEGORIES\n")
            sb.append("ID,Name,Type,Icon,Color\n")
            val allCategories = kotlinx.coroutines.runBlocking { repository.getAllCategories() }
            allCategories.forEach { cat ->
                val nameEsc = cat.name.replace("\"", "\"\"")
                val typeEsc = cat.type.replace("\"", "\"\"")
                sb.append("${cat.id},\"$nameEsc\",\"$typeEsc\",\"${cat.icon}\",\"${cat.color}\"\n")
            }
            sb.append("\n")

            // 3. TRANSACTIONS
            sb.append("# SECTION: TRANSACTIONS\n")
            sb.append("ID,Amount,Type,CategoryId,AccountId,TransferToAccountId,Date,Note,ImagePath,IsSecondCountry\n")
            val allTransactions = kotlinx.coroutines.runBlocking { repository.getAllTransactionsDirect() }
            allTransactions.forEach { tx ->
                val noteEsc = tx.note.replace("\"", "\"\"")
                val imgEsc = (tx.imagePath ?: "").replace("\"", "\"\"")
                sb.append("${tx.id},${tx.amount},\"${tx.type}\",${tx.categoryId ?: ""},${tx.accountId},${tx.transferToAccountId ?: ""},${tx.date},\"$noteEsc\",\"$imgEsc\",${tx.isSecondCountry}\n")
            }
            sb.append("\n")

            // 4. BUDGETS
            sb.append("# SECTION: BUDGETS\n")
            sb.append("ID,CategoryId,LimitAmount,Month,IsSecondCountry\n")
            val allBudgets = kotlinx.coroutines.runBlocking { repository.getAllBudgetsDirect() }
            allBudgets.forEach { b ->
                sb.append("${b.id},${b.categoryId},${b.limitAmount},\"${b.month}\",${b.isSecondCountry}\n")
            }
            sb.append("\n")

            // 5. EMIS
            sb.append("# SECTION: EMIS\n")
            sb.append("ID,Title,TotalAmount,PaidAmount,DueDate,CategoryId,IsDebt,DebtType,PersonName,TenureMonths,IsSecondCountry\n")
            val allEMIs = kotlinx.coroutines.runBlocking { repository.getAllEMIsDirect() }
            allEMIs.forEach { emi ->
                val titleEsc = emi.title.replace("\"", "\"\"")
                val debtTypeEsc = emi.debtType.replace("\"", "\"\"")
                val pNameEsc = emi.personName.replace("\"", "\"\"")
                sb.append("${emi.id},\"$titleEsc\",${emi.totalAmount},${emi.paidAmount},\"${emi.dueDate}\",${emi.categoryId ?: ""},${emi.isDebt},\"$debtTypeEsc\",\"$pNameEsc\",${emi.tenureMonths},${emi.isSecondCountry}\n")
            }

            val file = File(context.cacheDir, "WealthFlow_FullBackup.csv")
            file.writeText(sb.toString())
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.example.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("WealthViewModel", "CSV Export error: ", e)
            null
        }
    }

    fun importFromCSV(context: Context, uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open file input stream")
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                val lines = mutableListOf<String>()
                var currentLine: String?
                while (reader.readLine().also { currentLine = it } != null) {
                    lines.add(currentLine!!)
                }
                reader.close()

                val isNewStyle = lines.any { it.startsWith("# SECTION:") }

                if (isNewStyle) {
                    repository.clearAllData()

                    var activeSection = ""
                    for (rawLine in lines) {
                        val line = rawLine.trim()
                        if (line.isEmpty()) continue

                        if (line.startsWith("# SECTION: ACCOUNTS")) {
                            activeSection = "ACCOUNTS"
                            continue
                        } else if (line.startsWith("# SECTION: CATEGORIES")) {
                            activeSection = "CATEGORIES"
                            continue
                        } else if (line.startsWith("# SECTION: TRANSACTIONS")) {
                            activeSection = "TRANSACTIONS"
                            continue
                        } else if (line.startsWith("# SECTION: BUDGETS")) {
                            activeSection = "BUDGETS"
                            continue
                        } else if (line.startsWith("# SECTION: EMIS")) {
                            activeSection = "EMIS"
                            continue
                        } else if (line.startsWith("#")) {
                            continue
                        }

                        if (line.startsWith("ID,Name,Type") || 
                            line.startsWith("ID,Amount,Type") || 
                            line.startsWith("ID,CategoryId,LimitAmount") ||
                            line.startsWith("ID,Title,TotalAmount")) {
                            continue
                        }

                        val tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map {
                            it.trim().removeSurrounding("\"").replace("\"\"", "\"")
                        }

                        try {
                            when (activeSection) {
                                "ACCOUNTS" -> {
                                    if (tokens.size >= 5) {
                                        val id = tokens[0].toIntOrNull() ?: 0
                                        val nameVal = tokens[1]
                                        val typeVal = tokens[2]
                                        val balanceVal = tokens[3].toDoubleOrNull() ?: 0.0
                                        val isSecondVal = tokens[4].toBoolean()
                                        repository.insertAccount(AccountEntity(
                                            id = id,
                                            name = nameVal,
                                            type = typeVal,
                                            balance = balanceVal,
                                            isSecondCountry = isSecondVal
                                        ))
                                    }
                                }
                                "CATEGORIES" -> {
                                    if (tokens.size >= 5) {
                                        val id = tokens[0].toIntOrNull() ?: 0
                                        val nameVal = tokens[1]
                                        val typeVal = tokens[2]
                                        val iconVal = tokens[3]
                                        val colorVal = tokens[4]
                                        repository.insertCategory(CategoryEntity(
                                            id = id,
                                            name = nameVal,
                                            type = typeVal,
                                            icon = iconVal,
                                            color = colorVal
                                        ))
                                    }
                                }
                                "TRANSACTIONS" -> {
                                    if (tokens.size >= 10) {
                                        val id = tokens[0].toIntOrNull() ?: 0
                                        val amountVal = tokens[1].toDoubleOrNull() ?: 0.0
                                        val typeVal = tokens[2]
                                        val catIdVal = tokens[3].toIntOrNull()
                                        val accIdVal = tokens[4].toIntOrNull() ?: 1
                                        val transToIdVal = tokens[5].toIntOrNull()
                                        val dateVal = tokens[6].toLongOrNull() ?: System.currentTimeMillis()
                                        val noteVal = tokens[7]
                                        val imgVal = tokens[8].ifBlank { null }
                                        val isSecondVal = tokens[9].toBoolean()
                                        repository.insertTransaction(TransactionEntity(
                                            id = id,
                                            amount = amountVal,
                                            type = typeVal,
                                            categoryId = catIdVal,
                                            accountId = accIdVal,
                                            transferToAccountId = transToIdVal,
                                            date = dateVal,
                                            note = noteVal,
                                            imagePath = imgVal,
                                            isSecondCountry = isSecondVal
                                        ))
                                    }
                                }
                                "BUDGETS" -> {
                                    if (tokens.size >= 5) {
                                        val id = tokens[0].toIntOrNull() ?: 0
                                        val catIdVal = tokens[1].toIntOrNull() ?: -1
                                        val limitVal = tokens[2].toDoubleOrNull() ?: 0.0
                                        val monthVal = tokens[3]
                                        val isSecondVal = tokens[4].toBoolean()
                                        repository.insertBudget(BudgetEntity(
                                            id = id,
                                            categoryId = catIdVal,
                                            limitAmount = limitVal,
                                            month = monthVal,
                                            isSecondCountry = isSecondVal
                                        ))
                                    }
                                }
                                "EMIS" -> {
                                    if (tokens.size >= 11) {
                                        val id = tokens[0].toIntOrNull() ?: 0
                                        val titleVal = tokens[1]
                                        val totalVal = tokens[2].toDoubleOrNull() ?: 0.0
                                        val paidVal = tokens[3].toDoubleOrNull() ?: 0.0
                                        val dueVal = tokens[4]
                                        val catIdVal = tokens[5].toIntOrNull()
                                        val isDebtVal = tokens[6].toBoolean()
                                        val debtTypeVal = tokens[7]
                                        val personNameVal = tokens[8]
                                        val tenureVal = tokens[9].toIntOrNull() ?: 12
                                        val isSecondVal = tokens[10].toBoolean()
                                        repository.insertEMI(EMIEntity(
                                            id = id,
                                            title = titleVal,
                                            totalAmount = totalVal,
                                            paidAmount = paidVal,
                                            dueDate = dueVal,
                                            categoryId = catIdVal,
                                            isDebt = isDebtVal,
                                            debtType = debtTypeVal,
                                            personName = personNameVal,
                                            tenureMonths = tenureVal,
                                            isSecondCountry = isSecondVal
                                        ))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WealthViewModel", "Err parsing row: ", e)
                        }
                    }
                } else {
                    val cats = repository.getAllCategories()
                    val accs = repository.getAllAccounts(_isSecondCountryActive.value)
                    val defaultAccount = accs.firstOrNull() ?: AccountEntity(name = "Imported Acc", type = "Bank", balance = 0.0, isSecondCountry = _isSecondCountryActive.value)
                    var defaultAccountId = defaultAccount.id
                    if (defaultAccountId == 0) {
                        defaultAccountId = repository.insertAccount(defaultAccount).toInt()
                    }

                    val firstLine = lines.firstOrNull() ?: ""
                    val dataLines = if (firstLine.contains("Type") || firstLine.contains("ID")) lines.drop(1) else lines

                    for (rawLine in dataLines) {
                        val line = rawLine.trim()
                        if (line.isEmpty()) continue
                        val tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map {
                            it.trim().removeSurrounding("\"").replace("\"\"", "\"")
                        }
                        if (tokens.size >= 7) {
                            val type = tokens[1]
                            val amount = tokens[2].toDoubleOrNull() ?: 0.0
                            val dateString = tokens[3]
                            val catName = tokens[4]
                            val accName = tokens[5]
                            val note = tokens[6]

                            val dateLong = try {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }

                            var matchCat = cats.find { it.name.trim().lowercase() == catName.trim().lowercase() }
                            if (matchCat == null && catName.isNotEmpty() && catName != "None") {
                                val newCatId = repository.insertCategory(CategoryEntity(name = catName, type = type, icon = "restaurant", color = "#7f8c8d")).toInt()
                                matchCat = CategoryEntity(id = newCatId, name = catName, type = type, icon = "restaurant", color = "#7f8c8d")
                            }

                            var matchAcc = accs.find { it.name.trim().lowercase() == accName.trim().lowercase() }
                            var actId = defaultAccountId
                            if (matchAcc != null) {
                                actId = matchAcc.id
                            } else if (accName.isNotEmpty() && accName != "Unknown") {
                                actId = repository.insertAccount(AccountEntity(name = accName, type = "Bank", balance = 0.0, isSecondCountry = _isSecondCountryActive.value)).toInt()
                            }

                            val tx = TransactionEntity(
                                amount = amount,
                                type = type,
                                categoryId = matchCat?.id,
                                accountId = actId,
                                date = dateLong,
                                note = note,
                                isSecondCountry = _isSecondCountryActive.value
                            )
                            repository.insertTransaction(tx)
                        }
                    }
                }

                computeSmartInsights()
                onSuccess()
            } catch (e: Exception) {
                Log.e("WealthViewModel", "CSV Import error: ", e)
                onError(e.localizedMessage ?: "Unknown parse failure")
            }
        }
    }

    // Helper functions
    private fun getCurrentMonthString(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    private fun getStartOfMonthTimestamp(monthStr: String): Long {
        return try {
            val cal = Calendar.getInstance()
            val parsed = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthStr)
            if (parsed != null) {
                cal.time = parsed
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                return cal.timeInMillis
            }
            0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getEndOfMonthTimestamp(monthStr: String): Long {
        return try {
            val cal = Calendar.getInstance()
            val parsed = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthStr)
            if (parsed != null) {
                cal.time = parsed
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                return cal.timeInMillis
            }
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private val _notifications = MutableStateFlow<List<NotificationInfo>>(
        listOf(
            NotificationInfo(
                title = "Welcome to CashFlow! 🎉",
                description = "Your personal wealth manager has been successfully set up. Let's start tracking your visual bento of finance!",
                time = "just now"
            )
        )
    )
    val notifications: StateFlow<List<NotificationInfo>> = _notifications.asStateFlow()

    fun addNotification(title: String, description: String) {
        val newList = _notifications.value.toMutableList()
        newList.add(0, NotificationInfo(title = title, description = description, time = "just now"))
        _notifications.value = newList

        // Send a real-time Android system notification
        try {
            val context = getApplication<Application>().applicationContext
            val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val builder = androidx.core.app.NotificationCompat.Builder(context, "cashflow_reminders")
                .setSmallIcon(com.example.R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    "cashflow_reminders",
                    "CashFlow Reminders",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    this.description = "Daily bookkeeping and EMI payment reminders"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}

// Data Classes for custom aggregates
data class NotificationInfo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val time: String = "just now"
)
data class BudgetWithDetails(
    val categoryId: Int,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val limitAmount: Double,
    val usedAmount: Double,
    val month: String,
    val budgetId: Int?
)

data class SmartInsight(
    val title: String,
    val description: String,
    val iconType: String,
    val color: String
)
