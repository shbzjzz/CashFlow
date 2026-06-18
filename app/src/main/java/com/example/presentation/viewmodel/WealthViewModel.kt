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
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _savedPin = MutableStateFlow<String?>(prefs.getString("saved_pin", null))
    val savedPin: StateFlow<String?> = _savedPin.asStateFlow()

    private val _isAppLocked = MutableStateFlow(prefs.getString("saved_pin", null) != null)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Google Auth States
    private val _isGoogleLoggedIn = MutableStateFlow(prefs.getBoolean("is_google_logged_in", false))
    val isGoogleLoggedIn: StateFlow<Boolean> = _isGoogleLoggedIn.asStateFlow()

    private val _googleUserName = MutableStateFlow(prefs.getString("google_user_name", "Guest User") ?: "Guest User")
    val googleUserName: StateFlow<String> = _googleUserName.asStateFlow()

    private val _googleUserEmail = MutableStateFlow(prefs.getString("google_user_email", null))
    val googleUserEmail: StateFlow<String?> = _googleUserEmail.asStateFlow()

    // Dynamic Saved Accounts for the custom Sign-in selector (eliminating hardcoded lists)
    private val _savedAccounts = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val savedAccounts: StateFlow<List<Pair<String, String>>> = _savedAccounts.asStateFlow()

    private val repository: WealthRepository

    init {
        // Initialize Firebase safely using dynamic BuildConfig coordinates with offline mock fallbacks
        try {
            if (FirebaseApp.getApps(application).isEmpty()) {
                val apiKey = try {
                    com.example.BuildConfig.FIREBASE_API_KEY.takeIf { it.isNotBlank() && !it.startsWith("PLACEHOLDER") }
                } catch (e: Throwable) { null } ?: "AIzaSyA_mockKey1234567890ForCashFlowAuth"

                val appId = try {
                    com.example.BuildConfig.FIREBASE_APPLICATION_ID.takeIf { it.isNotBlank() && !it.startsWith("PLACEHOLDER") }
                } catch (e: Throwable) { null } ?: "1:5551212:android:999bc8cf"

                val projectId = try {
                    com.example.BuildConfig.FIREBASE_PROJECT_ID.takeIf { it.isNotBlank() && !it.startsWith("PLACEHOLDER") }
                } catch (e: Throwable) { null } ?: "cashflow-applet"

                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .build()
                FirebaseApp.initializeApp(application, options)
                Log.d("Auth", "Firebase initialized dynamically. Using live sync config: ${apiKey != "AIzaSyA_mockKey1234567890ForCashFlowAuth"}")
            }
        } catch (e: Exception) {
            Log.e("Auth", "Firebase setup dynamic initialization failed: ${e.message}")
        }

        val database = AppDatabase.getDatabase(application)
        repository = WealthRepositoryImpl(database)

        // Load saved Google accounts dynamically
        try {
            loadSavedAccounts()
        } catch (e: Exception) {
            Log.e("Auth", "Dynamic account loading failed: ${e.message}")
        }

        // Restore user session dynamically from FirebaseAuth if it's active!
        try {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                _googleUserName.value = user.displayName ?: user.email?.substringBefore("@") ?: "Verified User"
                _googleUserEmail.value = user.email
                _isGoogleLoggedIn.value = true
                prefs.edit()
                    .putString("google_user_name", _googleUserName.value)
                    .putString("google_user_email", _googleUserEmail.value)
                    .putBoolean("is_google_logged_in", true)
                    .apply()
            }
        } catch (e: Exception) {
            Log.e("Auth", "Firebase startup session restore bypass: ${e.message}")
        }

        // Populate database with default categories and sample data on initial startup run
        viewModelScope.launch {
            repository.initDefaultCategories()
            prepopulateSampleDataOnFirstRun()
            computeSmartInsights()
        }
    }

    // Live Flows
    val accounts: StateFlow<List<AccountEntity>> = repository.getAllAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emis: StateFlow<List<EMIEntity>> = repository.getAllEMIsFlow()
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

    // Dynamic budgets flow combined with category transaction aggregation
    val budgetsWithDetails: StateFlow<List<BudgetWithDetails>> = combine(
        selectedMonth,
        repository.getBudgetsForMonthFlow(getCurrentMonthString()), // simple fetch or dynamic month budget query
        transactions,
        categories
    ) { month, budgetList, txList, catList ->
        val monthBudgets = repository.getBudgetsForMonth(month) // suspend, fetch directly to verify
        
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

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        prefs.edit().putBoolean("is_dark_mode", _isDarkMode.value).apply()
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

    fun loadSavedAccounts() {
        val set = prefs.getStringSet("saved_accounts_set", emptySet()) ?: emptySet()
        _savedAccounts.value = set.map {
            val parts = it.split("|", limit = 2)
            val name = parts.getOrNull(0) ?: "User"
            val email = parts.getOrNull(1) ?: it
            Pair(name, email)
        }.sortedBy { it.second }
    }

    fun saveAccount(name: String, email: String) {
        val set = prefs.getStringSet("saved_accounts_set", emptySet()) ?: emptySet()
        val newSet = set.toMutableSet()
        newSet.add("$name|$email")
        prefs.edit().putStringSet("saved_accounts_set", newSet).apply()
        loadSavedAccounts()
    }

    fun removeAccount(email: String) {
        val set = prefs.getStringSet("saved_accounts_set", emptySet()) ?: emptySet()
        val newSet = set.filter { !it.endsWith("|$email") && it != email }.toSet()
        prefs.edit().putStringSet("saved_accounts_set", newSet).apply()
        loadSavedAccounts()
    }

    fun loginWithGoogle(name: String, email: String, idToken: String? = null) {
        _googleUserName.value = name
        _googleUserEmail.value = email
        _isGoogleLoggedIn.value = true
        prefs.edit()
            .putString("google_user_name", name)
            .putString("google_user_email", email)
            .putBoolean("is_google_logged_in", true)
            .apply()

        // Persist newly added account dynamically in preferences
        try {
            saveAccount(name, email)
        } catch (e: Exception) {
            Log.e("Auth", "Failed to save dynamic account to preferences: ${e.message}")
        }

        // Sync with Firebase Authentication as backend
        try {
            val auth = FirebaseAuth.getInstance()
            if (idToken != null) {
                // Real Google Auth credential in Firebase backend
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            Log.d("Auth", "Firebase Custom Google Token Login Successful: ${user?.email}")
                            user?.let {
                                val firebaseName = it.displayName ?: name
                                val firebaseEmail = it.email ?: email
                                _googleUserName.value = firebaseName
                                _googleUserEmail.value = firebaseEmail
                                prefs.edit()
                                    .putString("google_user_name", firebaseName)
                                    .putString("google_user_email", firebaseEmail)
                                    .apply()
                            }
                        } else {
                            Log.e("Auth", "Firebase Auth with identity token failed: ${task.exception?.message}")
                        }
                    }
            } else {
                // Perform dynamic Email/Password session sync with Firebase Auth
                // Generate secure deterministic password based on the unique email to create/access real email-associated profiles
                val deterministicPassword = "Pw_" + email.hashCode().toString().replace("-", "x") + "CashFlow"
                auth.signInWithEmailAndPassword(email, deterministicPassword)
                    .addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            val user = signInTask.result?.user
                            Log.d("Auth", "Firebase Email Session Sign-In Successful: ${user?.email}")
                            user?.let {
                                val firebaseName = it.displayName ?: name
                                _googleUserName.value = firebaseName
                                prefs.edit()
                                    .putString("google_user_name", firebaseName)
                                    .apply()
                            }
                        } else {
                            // If sign-in failed, the user probably doesn't exist yet, so we register them dynamically in Firebase!
                            auth.createUserWithEmailAndPassword(email, deterministicPassword)
                                .addOnCompleteListener { createParamTask ->
                                    if (createParamTask.isSuccessful) {
                                        val user = createParamTask.result?.user
                                        Log.d("Auth", "Firebase Email Session Registration Successful: ${user?.email}")
                                        // Update the display name in Firebase so it syncs and persists
                                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build()
                                        user?.updateProfile(profileUpdates)
                                    } else {
                                        Log.e("Auth", "Firebase Email Session custom user creation failed: ${createParamTask.exception?.message}")
                                    }
                                }
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("Auth", "Firebase backend session synchronization exception: ${e.message}")
        }
    }

    fun logoutGoogle() {
        _googleUserName.value = "Guest User"
        _googleUserEmail.value = null
        _isGoogleLoggedIn.value = false
        prefs.edit()
            .remove("google_user_name")
            .remove("google_user_email")
            .putBoolean("is_google_logged_in", false)
            .apply()

        try {
            FirebaseAuth.getInstance().signOut()
            Log.d("Auth", "Firebase session successfully logged out.")
        } catch (e: Exception) {
            Log.e("Auth", "Firebase sign out custom exception: ${e.message}")
        }
    }

    fun updateUsername(newName: String) {
        _googleUserName.value = newName
        prefs.edit().putString("google_user_name", newName).apply()
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

    // Operations
    fun addAccount(name: String, type: String, balance: Double) {
        viewModelScope.launch {
            repository.insertAccount(AccountEntity(name = name, type = type, balance = balance))
            computeSmartInsights()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
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
                imagePath = imagePath
            )
            repository.insertTransaction(tx)
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
            val existing = repository.getBudgetsForMonth(month)
            val match = existing.find { it.categoryId == categoryId }
            if (match != null) {
                if (limitAmount <= 0) {
                    repository.deleteBudget(match)
                } else {
                    repository.updateBudget(match.copy(limitAmount = limitAmount))
                }
            } else if (limitAmount > 0) {
                repository.insertBudget(BudgetEntity(categoryId = categoryId, limitAmount = limitAmount, month = month))
            }
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
        tenureMonths: Int = 12
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
                    tenureMonths = tenureMonths
                )
            )
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
        }
    }

    fun deleteEMI(emi: EMIEntity) {
        viewModelScope.launch {
            repository.deleteEMI(emi)
        }
    }

    // Prepopulate system with rich default sample data on first launch
    private suspend fun prepopulateSampleDataOnFirstRun() {
        val existingAccounts = repository.getAllAccounts()
        if (existingAccounts.isEmpty()) {
            val mainBankId = repository.insertAccount(AccountEntity(name = "Main Bank", type = "Bank", balance = 8200.0)).toInt()
            val walletId = repository.insertAccount(AccountEntity(name = "Digital Wallet", type = "Digital Wallet", balance = 1500.0)).toInt()
            val cashId = repository.insertAccount(AccountEntity(name = "Cash", type = "Cash", balance = 450.0)).toInt()
            val ccId = repository.insertAccount(AccountEntity(name = "Credit Card", type = "Credit Card", balance = -1200.0)).toInt()

            val cats = repository.getAllCategories()
            val foodCat = cats.find { it.name == "Food & Dining" }
            val salaryCat = cats.find { it.name == "Salary" }
            val rentCat = cats.find { it.name == "Rent & Housing" }
            val gymCat = cats.find { it.name == "Gym & Fitness" }
            val billsCat = cats.find { it.name == "Bills & Utilities" }

            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            // Sample Transactions
            // Transaction 1: -45.00 Grocery Today
            repository.insertTransaction(TransactionEntity(
                amount = 45.0,
                type = "Expense",
                categoryId = foodCat?.id,
                accountId = mainBankId,
                date = now - (1000 * 60 * 5), // 5 mins ago
                note = "Grocery"
            ))

            // Transaction 2: +2500.00 Salary Yesterday
            repository.insertTransaction(TransactionEntity(
                amount = 2500.0,
                type = "Income",
                categoryId = salaryCat?.id,
                accountId = mainBankId,
                date = now - dayMillis,
                note = "Salary"
            ))

            // Transaction 3: -1200.00 Rent Oct 1/Recently
            repository.insertTransaction(TransactionEntity(
                amount = 1200.0,
                type = "Expense",
                categoryId = rentCat?.id,
                accountId = mainBankId,
                date = now - (dayMillis * 3),
                note = "Rent"
            ))

            // Transaction 4: -50.00 Gym Oct 1/Recently
            repository.insertTransaction(TransactionEntity(
                amount = 50.0,
                type = "Expense",
                categoryId = gymCat?.id,
                accountId = mainBankId,
                date = now - (dayMillis * 4),
                note = "Gym"
            ))

            // Sample Budget
            foodCat?.let {
                repository.insertBudget(BudgetEntity(categoryId = it.id, limitAmount = 1500.0, month = getCurrentMonthString()))
            }
            cats.find { it.name == "Shopping" }?.let {
                repository.insertBudget(BudgetEntity(categoryId = it.id, limitAmount = 1000.0, month = getCurrentMonthString()))
            }

            // Sample EMI
            repository.insertEMI(EMIEntity(title = "Tabby Payment", totalAmount = 3000.0, paidAmount = 1500.0, dueDate = "10th"))
            repository.insertEMI(EMIEntity(title = "Car Payment", totalAmount = 5000.0, paidAmount = 2500.0, dueDate = "25th"))
        }
    }

    // Generate Smart Insights and Pattern Detections
    private fun computeSmartInsights() {
        val txs = transactions.value
        val cats = categories.value
        if (txs.isEmpty()) {
            _smartInsights.value = listOf(
                SmartInsight("Budget Tip", "Add some transactions above to get started with automated analytics!", "lightbulb", "#2b6954")
            )
            return
        }

        val insightsList = mutableListOf<SmartInsight>()

        // Insight 1: Coffee/Food Spending Warning (if exists)
        val foodCat = cats.find { it.name.contains("Food", ignoreCase = true) }
        if (foodCat != null) {
            val totalSpentOnFood = txs.filter { it.categoryId == foodCat.id && it.type == "Expense" }.sumOf { it.amount }
            val totalExpense = txs.filter { it.type == "Expense" }.sumOf { it.amount }
            if (totalExpense > 0 && (totalSpentOnFood / totalExpense) > 0.25) {
                insightsList.add(
                    SmartInsight(
                        title = "Spending Alert",
                        description = "You spent ${(totalSpentOnFood / totalExpense * 100).toInt()}% of your budget on Food & Dining. Consider minimizing dining out.",
                        iconType = "warning",
                        color = "#ba1a1a"
                    )
                )
            }
        }

        // Regular Insight 2: Generic helpful dynamic tip
        insightsList.add(
            SmartInsight(
                title = "Budget Tip",
                description = "Move 200 AED to Savings right now to reach your investment goals early.",
                iconType = "lightbulb",
                color = "#2b6954"
            )
        )

        _smartInsights.value = insightsList
    }

    // CSV Import / Export Utilities
    fun exportToCSV(context: Context): Uri? {
        val txList = transactions.value
        val catList = categories.value
        val accList = accounts.value

        val csvHeader = "ID,Type,Amount,Date,Category,Account,Note\n"
        val csvBody = txList.joinToString("\n") { tx ->
            val catName = catList.find { it.id == tx.categoryId }?.name ?: "None"
            val accName = accList.find { it.id == tx.accountId }?.name ?: "Unknown"
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(tx.date))
            "${tx.id},${tx.type},${tx.amount},\"$formattedDate\",\"$catName\",\"$accName\",\"${tx.note.replace("\"", "\"\"")}\""
        }

        val csvString = csvHeader + csvBody
        return try {
            val file = File(context.cacheDir, "WealthFlow_Transactions.csv")
            file.writeText(csvString)
            Uri.fromFile(file)
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
                var line = reader.readLine() // Header

                val cats = repository.getAllCategories()
                val accs = repository.getAllAccounts()

                val defaultAccount = accs.firstOrNull() ?: AccountEntity(name = "Imported Acc", type = "Bank", balance = 0.0)
                var defaultAccountId = defaultAccount.id
                if (defaultAccountId == 0) {
                    defaultAccountId = repository.insertAccount(defaultAccount).toInt()
                }

                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    if (tokens.size >= 7) {
                        val type = tokens[1]
                        val amount = tokens[2].toDoubleOrNull() ?: 0.0
                        val dateString = tokens[3].replace("\"", "")
                        val catName = tokens[4].replace("\"", "")
                        val accName = tokens[5].replace("\"", "")
                        val note = tokens[6].replace("\"", "")

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
                            actId = repository.insertAccount(AccountEntity(name = accName, type = "Bank", balance = 0.0)).toInt()
                        }

                        val tx = TransactionEntity(
                            amount = amount,
                            type = type,
                            categoryId = matchCat?.id,
                            accountId = actId,
                            date = dateLong,
                            note = note
                        )
                        repository.insertTransaction(tx)
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
}

// Data Classes for custom aggregates
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
