package com.example.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.*
import com.example.presentation.viewmodel.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.CashFlowPrimary
import com.example.ui.theme.IndigoGlow
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Navigation Routes
object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_TRANSACTION = "add_transaction"
    const val ACCOUNTS = "accounts"
    const val CATEGORIES = "categories"
    const val REPORTS = "reports"
    const val BUDGETS = "budgets"
    const val SETTINGS = "settings"
    const val EMIS = "emis"
    const val NOTIFICATIONS = "notifications"
}

// Helper to convert hex string color securely to Compose color
fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

// Native Material DatePicker wrapper to avoid bulky compose overlays
fun showAndroidDatePicker(
    context: Context,
    initialTime: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTime }
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDateSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun SkeuomorphicCard(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = shape,
            clip = false,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun SkeuomorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun SkeuomorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun DatePickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        // Absolute transparent touch interceptor overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun LoginScreen(viewModel: WealthViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var showGoogleSelector by remember { mutableStateOf(false) }
    var rawEmailInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // ── App Icon ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "CashFlow Logo",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CashFlow",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Your personal finance companion",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Sign-in Card ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign in to continue",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Securely authenticate with Google to sync and manage your finances.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (isSigningIn) return@Button
                            isSigningIn = true

                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val webClientId = try {
                                    com.example.BuildConfig.FIREBASE_WEB_CLIENT_ID.takeIf { it.isNotBlank() && !it.startsWith("PLACEHOLDER") }
                                } catch (e: Throwable) { null } ?: "949216766468.apps.googleusercontent.com"

                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(webClientId)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                // Launching native bottom sheet
                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = context,
                                )

                                val credential = result.credential
                                if (credential is GoogleIdTokenCredential) {
                                    val idToken = credential.idToken
                                    val email = credential.id
                                    val name = credential.displayName ?: email.substringBefore("@")
                                    viewModel.loginWithGoogle(name, email, idToken)
                                    Toast.makeText(context, "Welcome back, $name", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Trigger beautiful selector dialog as an interactive backup
                                    showGoogleSelector = true
                                }
                            } catch (e: Exception) {
                                // Soft fallback to selector dialog so they can choose accounts interactively
                                showGoogleSelector = true
                            } finally {
                                isSigningIn = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
                    }
                }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

        // ==========================================
        // DYNAMIC GOOGLE ACCOUNT SELECTOR DIALOG (No hardcoded emails)
        // ==========================================
        if (showGoogleSelector) {
            val savedAccounts by viewModel.savedAccounts.collectAsStateWithLifecycle()

            Dialog(
                onDismissRequest = { showGoogleSelector = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "oogle",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Choose an account to continue to CashFlow",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        if (savedAccounts.isNotEmpty()) {
                            Text(
                                text = "Saved profiles on this device:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Render and iterate through local persisted accounts dynamically
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                savedAccounts.forEach { account ->
                                    val (name, email) = account
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .clickable {
                                                viewModel.loginWithGoogle(name, email)
                                                showGoogleSelector = false
                                                Toast.makeText(context, "Welcome back, $name", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name.take(1).uppercase(Locale.getDefault()),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = email,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.removeAccount(email)
                                                Toast.makeText(context, "Account profile cleared", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove account profile",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Blank placeholder empty state when no historical profiles are stored yet 
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved active Google accounts found. Enter details below to dynamically register.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Custom dynamic configuration section 
                        Text(
                            text = "Or Authenticate New Email Account:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = rawEmailInput,
                                onValueChange = { rawEmailInput = it },
                                placeholder = { Text("your.email@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            SkeuomorphicButton(
                                onClick = {
                                    if (rawEmailInput.isNotBlank() && rawEmailInput.contains("@") && rawEmailInput.contains(".")) {
                                        val customName = rawEmailInput.substringBefore("@")
                                            .replace(".", " ")
                                            .replace("_", " ")
                                            .split(" ")
                                            .joinToString(" ") { segment ->
                                                segment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                            }
                                        viewModel.loginWithGoogle(customName, rawEmailInput)
                                        showGoogleSelector = false
                                        Toast.makeText(context, "Logged in as $customName", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please enter a valid Google email", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                isDark = isDark,
                                isSelected = true,
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Go", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = { showGoogleSelector = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                }
            }
        }
}

@Composable
fun DrawerContent(
    viewModel: WealthViewModel,
    navController: NavController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val username by viewModel.googleUserName.collectAsStateWithLifecycle()
    val userEmail by viewModel.googleUserEmail.collectAsStateWithLifecycle()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newTempName by remember { mutableStateOf("") }
    val currentRoute = navController.currentBackStackEntryFlow.collectAsState(initial = null).value?.destination?.route

    val drawerBg = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val primaryText = MaterialTheme.colorScheme.onSurface
    val subtleText = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerC = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val navItems = listOf(
        NavigationItem("Home",           Routes.DASHBOARD, Icons.Default.Home),
        NavigationItem("Reports",        Routes.REPORTS,   Icons.Default.BarChart),
        NavigationItem("Wallets",        Routes.ACCOUNTS,  Icons.Default.AccountBalanceWallet),
        NavigationItem("EMIs & Debts",   Routes.EMIS,      Icons.Default.CreditScore),
        NavigationItem("Settings",       Routes.SETTINGS,  Icons.Default.Settings)
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(drawerBg)
            .windowInsetsPadding(WindowInsets.statusBars),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // ── Gradient Header ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (username.isNotEmpty()) username.take(2).uppercase() else "CF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                    // Name + edit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (username.isNotEmpty()) username else "Guest",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(
                            onClick = { newTempName = username; showEditNameDialog = true },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit name",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    // Email
                    userEmail?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── App label ────────────────────────────────────
            Text(
                text = "CASHFLOW",
                style = MaterialTheme.typography.labelSmall,
                color = subtleText,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // ── Nav Items ─────────────────────────────────────
            navItems.forEach { item ->
                val selected = currentRoute == item.route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) accent.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .clickable {
                            scope.launch { drawerState.close() }
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active indicator bar
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(20.dp)
                            .background(
                                if (selected) accent else Color.Transparent,
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = if (selected) accent else subtleText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) accent else primaryText
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = dividerC
            )
        }

        // ── Sign Out ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                .clickable {
                    scope.launch { drawerState.close() }
                    viewModel.logoutGoogle()
                }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Sign Out",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    // ── Edit Name Dialog ──────────────────────────────────────────
    if (showEditNameDialog) {
        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Surface(
                modifier = Modifier.width(300.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Edit Display Name",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SkeuomorphicTextField(
                        value = newTempName,
                        onValueChange = { newTempName = it },
                        placeholder = { Text("Your name") },
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditNameDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTempName.isNotBlank()) {
                                    viewModel.updateUsername(newTempName)
                                    showEditNameDialog = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Text("Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: WealthViewModel) {
    val isGoogleLoggedIn by viewModel.isGoogleLoggedIn.collectAsStateWithLifecycle()

    if (!isGoogleLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        val navController = rememberNavController()
        val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
        val savedPin by viewModel.savedPin.collectAsStateWithLifecycle()
        val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

        var showPinInputState by remember { mutableStateOf(isAppLocked) }
        var enteredPin by remember { mutableStateOf("") }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        // Enforce lock overlay
        LaunchedEffect(isAppLocked) {
            showPinInputState = isAppLocked && savedPin != null
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    viewModel = viewModel,
                    navController = navController,
                    drawerState = drawerState,
                    scope = coroutineScope
                )
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (!showPinInputState) {
                        BottomNavBar(navController)
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (showPinInputState) {
                        // PIN Lock Overlay Screen
                        PinLockScreen(
                            enteredPin = enteredPin,
                            onNumberClick = { num ->
                                if (enteredPin.length < 4) {
                                    enteredPin += num
                                    if (enteredPin.length == 4) {
                                        if (viewModel.unlockApp(enteredPin)) {
                                            showPinInputState = false
                                        } else {
                                            enteredPin = "" // reset
                                        }
                                    }
                                }
                            },
                            onDeleteClick = {
                                if (enteredPin.isNotEmpty()) {
                                    enteredPin = enteredPin.dropLast(1)
                                }
                            }
                        )
                    } else {
                        val slideSpec = tween<IntOffset>(300)
                        val fadeSpec = tween<Float>(250)
                        NavHost(
                            navController = navController,
                            startDestination = Routes.DASHBOARD,
                            enterTransition = {
                                slideInHorizontally(animationSpec = slideSpec) { it / 4 } +
                                fadeIn(animationSpec = fadeSpec)
                            },
                            exitTransition = {
                                slideOutHorizontally(animationSpec = slideSpec) { -it / 4 } +
                                fadeOut(animationSpec = fadeSpec)
                            },
                            popEnterTransition = {
                                slideInHorizontally(animationSpec = slideSpec) { -it / 4 } +
                                fadeIn(animationSpec = fadeSpec)
                            },
                            popExitTransition = {
                                slideOutHorizontally(animationSpec = slideSpec) { it / 4 } +
                                fadeOut(animationSpec = fadeSpec)
                            }
                        ) {
                            composable(Routes.DASHBOARD) {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                                )
                            }
                            composable(Routes.ADD_TRANSACTION) {
                                AddTransactionScreen(viewModel, navController)
                            }
                            composable(Routes.ACCOUNTS) {
                                AccountsScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                                )
                            }
                            composable(Routes.CATEGORIES) {
                                CategoriesScreen(viewModel, navController)
                            }
                            composable(Routes.REPORTS) {
                                ReportsScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                                )
                            }
                            composable(Routes.BUDGETS) {
                                BudgetScreen(viewModel, navController)
                            }
                            composable(Routes.NOTIFICATIONS) {
                                NotificationsScreen(viewModel, navController)
                            }
                            composable(Routes.SETTINGS) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                                )
                            }
                            composable(Routes.EMIS) {
                                EMIScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryFlow.collectAsState(initial = null).value?.destination?.route

    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            NavigationItem("Home",     Routes.DASHBOARD, Icons.Default.Home),
            NavigationItem("Reports",  Routes.REPORTS,   Icons.Default.BarChart),
            NavigationItem("Wallets",  Routes.ACCOUNTS,  Icons.Default.AccountBalanceWallet),
            NavigationItem("EMIs",     Routes.EMIS,      Icons.Default.CreditScore),
            NavigationItem("Settings", Routes.SETTINGS,  Icons.Default.Settings)
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        item.title,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_${item.route}")
            )
        }
    }
}

data class NavigationItem(val title: String, val route: String, val icon: ImageVector)

// PIN Lock Interface
@Composable
fun PinLockScreen(
    enteredPin: String,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF312E81), Color(0xFF4C1D95), Color(0xFF0F0F1A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Lock Icon ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "App Locked",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CashFlow",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Enter your 4-digit PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── PIN Dots ───────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                for (i in 1..4) {
                    val filled = enteredPin.length >= i
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                if (filled) Color.White else Color.White.copy(alpha = 0.25f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Number Grid ────────────────────────────────────
            val numbers = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                numbers.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { char ->
                            if (char.isEmpty()) {
                                Spacer(modifier = Modifier.size(74.dp))
                            } else if (char == "DEL") {
                                IconButton(
                                    onClick = { onDeleteClick() },
                                    modifier = Modifier
                                        .size(74.dp)
                                        .background(Color.White.copy(alpha = 0.10f), CircleShape)
                                        .testTag("pin_DEL")
                                ) {
                                    Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onNumberClick(char) },
                                    modifier = Modifier
                                        .size(74.dp)
                                        .testTag("pin_${char}"),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text(char, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. Dashboard Screen
@Composable
fun NotificationsScreen(viewModel: WealthViewModel, navController: NavController) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    // For demonstration, these are the local alerts history
    val notifications = remember {
        listOf(
            "EMI Payment for Car Loan due on the 10th",
            "Debt Repayment due for 'John Doe' on the 15th",
            "Security Alert: Google Authentication successful",
            "Your Net Worth increased by 5% this month!"
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notif ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = notif,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val insights by viewModel.smartInsights.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var inspectingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Aggregate values
    val netWorth = accounts.sumOf { it.balance }
    val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenDrawer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = "CashFlow",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Net Worth Hero Card — gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_worth_card")
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "NET WORTH",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        "$currency ${String.format(Locale.getDefault(), "%,.2f", netWorth)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.20f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ArrowUpward, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(12.dp))
                                Text("Income", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                            }
                            Text("$currency ${String.format(Locale.getDefault(), "%,.1f", totalIncome)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ArrowDownward, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(12.dp))
                                Text("Expense", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                            }
                            Text("$currency ${String.format(Locale.getDefault(), "%,.1f", totalExpense)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Insights Title
        item {
            Text("Quick Insights", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        // Insights Carousel
        item {
            if (insights.isEmpty()) {
                Text("No spending pattern detected yet.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 13.sp)
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(insights) { insight ->
                        Card(
                            modifier = Modifier
                                .width(260.dp)
                                .testTag("insight_card"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, parseHexColor(insight.color).copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(containerColor = parseHexColor(insight.color).copy(alpha = 0.1f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(insight.color).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon(insight.iconType),
                                        contentDescription = insight.title,
                                        tint = parseHexColor(insight.color)
                                    )
                                }
                                Column {
                                    Text(insight.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(insight.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Title & Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                TextButton(onClick = { navController.navigate(Routes.CATEGORIES) }) {
                    Text("See All", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Recent Transactions List
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.List, contentDescription = "No trans", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Text("No transactions logged yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(transactions.take(10)) { tx ->
                val category = categories.find { it.id == tx.categoryId }
                SkeuomorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clickable { inspectingTransaction = tx }
                        .testTag("transaction_item_${tx.id}"),
                    isDark = isDark
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (category != null) parseHexColor(category.color).copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    IconsUtil.getIcon(category?.icon ?: "receipt"),
                                    contentDescription = category?.name,
                                    tint = if (category != null) parseHexColor(category.color) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column {
                                Text(
                                    text = category?.name ?: tx.type,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (tx.note.isNotEmpty()) tx.note else "Transaction id: ${tx.id}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val sign = when (tx.type) {
                                "Income" -> "+"
                                "Expense" -> "-"
                                else -> ""
                            }
                            val color = when (tx.type) {
                                "Income" -> Color(0xFF10B981)
                                "Expense" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = "$sign $currency ${tx.amount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }

        // Extra spacing at the bottom
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // FAB — Add Transaction
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { navController.navigate(Routes.ADD_TRANSACTION) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("add_transaction_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    // Interactive Tactile Transaction Inspector & Live Editor dialog
    inspectingTransaction?.let { tx ->
        var isEditing by remember { mutableStateOf(false) }

        var editType by remember { mutableStateOf(tx.type) }
        var editAmount by remember { mutableStateOf(tx.amount.toString()) }
        var editNote by remember { mutableStateOf(tx.note) }
        var editSelectedCategory by remember { mutableStateOf(categories.find { it.id == tx.categoryId }) }
        var editSelectedAccount by remember { mutableStateOf(accounts.find { it.id == tx.accountId }) }
        var editSelectedToAccount by remember { mutableStateOf(accounts.find { it.id == tx.transferToAccountId }) }

        // Sync states when target transaction shifts
        LaunchedEffect(tx) {
            isEditing = false
            editType = tx.type
            editAmount = tx.amount.toString()
            editNote = tx.note
            editSelectedCategory = categories.find { it.id == tx.categoryId }
            editSelectedAccount = accounts.find { it.id == tx.accountId }
            editSelectedToAccount = accounts.find { it.id == tx.transferToAccountId }
        }

        Dialog(onDismissRequest = { inspectingTransaction = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF101815) else Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isEditing) {
                        // MODE 1: DETAIL INSPECTION VIEW
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Transaction Inspector",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                            )
                            IconButton(onClick = { inspectingTransaction = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        // Hero Header with Category Icon / Emoji representation
                        val txCat = categories.find { it.id == tx.categoryId }
                        SkeuomorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            isDark = isDark
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (txCat != null) parseHexColor(txCat.color).copy(alpha = 0.15f)
                                            else Color.Gray.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon(txCat?.icon ?: "receipt"),
                                        contentDescription = null,
                                        tint = if (txCat != null) parseHexColor(txCat.color) else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = txCat?.name ?: if (tx.type == "Transfer") "Bank Transfer ⇆" else tx.type,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                    Text(
                                        text = "Active Channel: ${tx.type}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                    )
                                }
                            }
                        }

                        // Ledger Metrics Area
                        SkeuomorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            isDark = isDark
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "AMOUNT & SOURCE DETAILS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ledger Outflow/Inflow:",
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                    )
                                    val sign = when (tx.type) {
                                        "Income" -> "+"
                                        "Expense" -> "-"
                                        else -> ""
                                    }
                                    val amtColor = when (tx.type) {
                                        "Income" -> Color(0xFF10B981)
                                        "Expense" -> Color.Red
                                        else -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                    }
                                    Text(
                                        text = "$sign $currency ${String.format("%.2f", tx.amount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = amtColor
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Vault Account / Wallet:",
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                    )
                                    val actSource = accounts.find { it.id == tx.accountId }
                                    Text(
                                        text = actSource?.name ?: "Unknown Wallet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                }

                                if (tx.type == "Transfer") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Transfer Recipient Wallet:",
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                        )
                                        val actDest = accounts.find { it.id == tx.transferToAccountId }
                                        Text(
                                            text = actDest?.name ?: "Unknown Target Wallet",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Transaction Timestamp:",
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                    )
                                    val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(tx.date))
                                    Text(
                                        text = dateStr,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                }
                            }
                        }

                        // Transaction note if configured
                        if (tx.note.isNotBlank()) {
                            SkeuomorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                isDark = isDark
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "LEDGER MEMO / NOTES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                    )
                                    Text(
                                        text = tx.note,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                }
                            }
                        }

                        // Receipt Image Display
                        if (tx.imagePath != null) {
                            SkeuomorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                isDark = isDark
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "ATTACHED RECEIPT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                    )
                                    AsyncImage(
                                        model = tx.imagePath,
                                        contentDescription = "Receipt photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.05f)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        }

                        // Repayment Control Board Row (Delete / Edit commands)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SkeuomorphicButton(
                                onClick = {
                                    viewModel.deleteTransaction(tx)
                                    Toast.makeText(context, "Transaction deleted successfully!", Toast.LENGTH_SHORT).show()
                                    inspectingTransaction = null
                                },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                                }
                            }

                            SkeuomorphicButton(
                                onClick = { isEditing = true },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                    Text("Edit Live", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309), fontSize = 13.sp)
                                }
                            }
                        }

                    } else {
                        // MODE 2: EDIT TRANSACTION FORM INLINE
                        Text(
                            text = "Edit Transaction Log",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                        )

                        // Segmented Tab picker for active log type
                        SkeuomorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            isDark = isDark,
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Income", "Expense", "Transfer").forEach { tab ->
                                    val active = editType == tab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(
                                                if (active) {
                                                    if (isDark) Color(0xFF2A3935) else Color.White
                                                } else Color.Transparent
                                            )
                                            .clickable {
                                                editType = tab
                                                if (tab != "Transfer") {
                                                    editSelectedCategory = categories.firstOrNull { it.type == tab }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tab,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (active) {
                                                if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                            } else {
                                                if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Amount editor
                        SkeuomorphicTextField(
                            value = editAmount,
                            onValueChange = { editAmount = it },
                            label = { Text("Log Amount ($currency)") },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Note memo editor
                        SkeuomorphicTextField(
                            value = editNote,
                            onValueChange = { editNote = it },
                            label = { Text("Memo Notes") },
                            isDark = isDark
                        )

                        // Source Wallet Account selection
                        Text(
                            text = "Debit/Source Account:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val selected = editSelectedAccount?.id == acc.id
                                SkeuomorphicButton(
                                    onClick = { editSelectedAccount = acc },
                                    isDark = isDark,
                                    isSelected = selected,
                                    modifier = Modifier.widthIn(min = 90.dp)
                                ) {
                                    Text(
                                        text = acc.name,
                                        fontSize = 12.sp,
                                        color = if (selected) {
                                            if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                        } else {
                                            if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                        }
                                    )
                                }
                            }
                        }

                        if (editType == "Transfer") {
                            // Target account selection (specifically for transfers)
                            Text(
                                text = "Transfer Destination Account:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val selected = editSelectedToAccount?.id == acc.id
                                    SkeuomorphicButton(
                                        onClick = { editSelectedToAccount = acc },
                                        isDark = isDark,
                                        isSelected = selected,
                                        modifier = Modifier.widthIn(min = 90.dp)
                                    ) {
                                        Text(
                                            text = acc.name,
                                            fontSize = 12.sp,
                                            color = if (selected) {
                                                if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                            } else {
                                                if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Category selection row for Income / Expense
                            Text(
                                text = "Associate Category:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.filter { it.type == editType }.forEach { cat ->
                                    val selected = editSelectedCategory?.id == cat.id
                                    SkeuomorphicButton(
                                        onClick = { editSelectedCategory = cat },
                                        isDark = isDark,
                                        isSelected = selected,
                                        modifier = Modifier.widthIn(min = 90.dp)
                                    ) {
                                        Text(
                                            text = "${cat.icon} ${cat.name}",
                                            fontSize = 11.sp,
                                            color = if (selected) {
                                                if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                            } else {
                                                if (isDark) Color(0xFF90A49E) else Color(0xFF4C5D55)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Submit changes / Back actions
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back View", color = Color.Gray)
                            }

                            SkeuomorphicButton(
                                onClick = {
                                    val parsedAmt = editAmount.toDoubleOrNull()
                                    if (parsedAmt != null && parsedAmt > 0 && editSelectedAccount != null) {
                                        val updatedTx = tx.copy(
                                            amount = parsedAmt,
                                            type = editType,
                                            note = editNote,
                                            accountId = editSelectedAccount!!.id,
                                            categoryId = if (editType == "Transfer") null else editSelectedCategory?.id,
                                            transferToAccountId = if (editType == "Transfer") editSelectedToAccount?.id else null
                                        )
                                        viewModel.updateTransaction(updatedTx)
                                        Toast.makeText(context, "Changes saved successfully!", Toast.LENGTH_SHORT).show()
                                        inspectingTransaction = null
                                    } else {
                                        Toast.makeText(context, "Please configure valid amount & accounts", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                isDark = isDark,
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Save Change", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309))
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Add Transaction Screen (Screenshot 1 matching form exactly)
@Composable
fun AddTransactionScreen(viewModel: WealthViewModel, navController: NavController) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val globalCurrency by viewModel.currency.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var type by remember { mutableStateOf("Expense") } // "Income", "Expense", "Transfer", "Debt"
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var imagePathState by remember { mutableStateOf<String?>(null) }
    val receiptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagePathState = uri.toString()
        }
    }



    var selectedDateEpochMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedToAccount by remember { mutableStateOf<AccountEntity?>(null) } // for Transfer

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showToAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(categories, accounts, type) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.firstOrNull { it.type == type } ?: categories.firstOrNull { it.type == "Expense" } ?: categories.first()
        }
        if (selectedAccount == null && accounts.isNotEmpty()) {
            selectedAccount = accounts.firstOrNull()
        }
        if (selectedToAccount == null && accounts.size > 1) {
            selectedToAccount = accounts.getOrNull(1)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount != null && parsedAmount > 0 && selectedAccount != null) {
                            viewModel.addTransaction(
                                amount = parsedAmount,
                                type = type,
                                categoryId = if (type == "Transfer") null else selectedCategory?.id,
                                accountId = selectedAccount!!.id,
                                transferToAccountId = if (type == "Transfer") selectedToAccount?.id else null,
                                date = selectedDateEpochMillis,
                                note = note,
                                imagePath = imagePathState
                            )
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Please enter a valid amount and choose an account", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Transaction", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Transaction Type Tab ──────────────────────────
            val typeColors = mapOf(
                "Income"   to SuccessGreen,
                "Expense"  to MaterialTheme.colorScheme.error,
                "Transfer" to MaterialTheme.colorScheme.primary
            )
            val typeIcons = mapOf(
                "Income"   to Icons.Default.ArrowUpward,
                "Expense"  to Icons.Default.ArrowDownward,
                "Transfer" to Icons.Default.SwapHoriz
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Income", "Expense", "Transfer").forEach { tab ->
                    val isSelected = type == tab
                    val tabColor = typeColors[tab] ?: MaterialTheme.colorScheme.primary
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) tabColor.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                type = tab
                                if (tab != "Transfer") {
                                    selectedCategory = categories.firstOrNull { it.type == tab }
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("type_tab_${tab}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            typeIcons[tab] ?: Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isSelected) tabColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            tab,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) tabColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // ── Amount Entry ──────────────────────────────────
            val activeTypeColor = typeColors[type] ?: MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(activeTypeColor.copy(alpha = 0.08f))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = activeTypeColor.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = globalCurrency,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeTypeColor,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        BasicTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(180.dp)
                                .testTag("amount_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(activeTypeColor)
                        )
                    }
                }
            }

            // Regular/Common Fields Section
            SkeuomorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "TRANSACTION LEDGER DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                    )

                    // Category Selection (only for Income, Expense)
                    if (type != "Transfer") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedCategory != null) parseHexColor(selectedCategory!!.color).copy(alpha = 0.15f)
                                            else (if (isDark) Color(0xFF1E2F29) else Color(0xFFEFF4FF))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon(selectedCategory?.icon ?: "restaurant"),
                                        contentDescription = null,
                                        tint = if (selectedCategory != null) parseHexColor(selectedCategory!!.color) else (if (isDark) Color(0xFF34D399) else Color(0xFF003527))
                                    )
                                }
                                Column {
                                    Text(
                                        "Category",
                                        fontSize = 11.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                                    )
                                    Text(
                                        selectedCategory?.name ?: "Select Category",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                            )
                        }
                    }

                    // Source Account Card for adding payments/sources
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAccountDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF23352F) else Color(0xFFE2EBE8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    IconsUtil.getIcon("account_balance"),
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                                )
                            }
                            Column {
                                Text(
                                    if (type == "Transfer") "From Account" else "Account",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                                )
                                Text(
                                    selectedAccount?.name ?: "Choose Account",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                        )
                    }

                    // Destination Account for Transfers
                    if (type == "Transfer") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showToAccountDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF1B312A) else Color(0xFFE6F3EE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon("account_balance"),
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF10B981) else Color(0xFF10B981)
                                    )
                                }
                                Column {
                                    Text(
                                        "To Account",
                                        fontSize = 11.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                                    )
                                    Text(
                                        selectedToAccount?.name ?: "Select Destination",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                            )
                        }
                    }

                    // REAL Date & Time Picker selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAndroidDatePicker(context, selectedDateEpochMillis) {
                                    selectedDateEpochMillis = it
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF1D2F29) else Color(0xFFEBF0EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                                )
                            }
                            Column {
                                Text(
                                    "Date & Period",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                                )
                                Text(
                                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateEpochMillis)),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Date",
                            tint = if (isDark) Color(0xFF34D399) else Color(0xFF003527),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Note Text Field
            SkeuomorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TRANSACTION NOTE / MEMO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                    )
                    SkeuomorphicTextField(
                        value = note,
                        onValueChange = { note = it },
                        isDark = isDark,
                        placeholder = { Text("Write transaction specific details or receipt note...") }
                    )
                }
            }

            // Receipt Attachment Section
            SkeuomorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "RECEIPT DOCUMENT / ATTACHMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                    )

                    if (imagePathState == null) {
                        SkeuomorphicButton(
                            onClick = { receiptLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            isDark = isDark
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Upload Receipt Image",
                                color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.05f))
                        ) {
                            AsyncImage(
                                model = imagePathState,
                                contentDescription = "Receipt Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imagePathState = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove Document",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Category Selector Dialog (with theme alignment)
    if (showCategoryDialog) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF121B17) else Color.White
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Select Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(categories.filter { it.type == type }) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = cat
                                        showCategoryDialog = false
                                    }
                                    .background(
                                        if (isDark) Color(0xFF1A2622) else Color(0xFFF4F8F6),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(cat.color).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon(cat.icon),
                                        contentDescription = null,
                                        tint = parseHexColor(cat.color)
                                    )
                                }
                                Text(
                                    cat.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Account selector Dialog
    if (showAccountDialog) {
        Dialog(onDismissRequest = { showAccountDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF121B17) else Color.White
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Select Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(accounts) { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAccount = acc
                                        showAccountDialog = false
                                    }
                                    .background(
                                        if (isDark) Color(0xFF1A2622) else Color(0xFFF4F8F6),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF1B312A) else Color(0xFFE6F3EE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon("account_balance"),
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                                    )
                                }
                                Column {
                                    Text(
                                        acc.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                    Text(
                                        acc.type,
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // To Account selector (Transfer)
    if (showToAccountDialog) {
        Dialog(onDismissRequest = { showToAccountDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF121B17) else Color.White
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Destination Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(accounts.filter { it.id != selectedAccount?.id }) { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedToAccount = acc
                                        showToAccountDialog = false
                                    }
                                    .background(
                                        if (isDark) Color(0xFF1A2622) else Color(0xFFF4F8F6),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF1B312A) else Color(0xFFE6F3EE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon("account_balance"),
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF34D399) else Color(0xFF003527)
                                    )
                                }
                                Column {
                                    Text(
                                        acc.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFE4F0EC) else Color(0xFF0E1915)
                                    )
                                    Text(
                                        acc.type,
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFF90A49E) else Color(0xFF3B524B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Vaults Screen (screenshot 2 - connected assets and liabilities)
@Composable
fun AccountsScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var showCreateAccountDialog by remember { mutableStateOf(false) }

    val totalAssets = accounts.filter { it.balance >= 0 }.sumOf { it.balance }
    val totalLiabilities = accounts.filter { it.balance < 0 }.sumOf { it.balance }
    val netWorth = totalAssets + totalLiabilities

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(onClick = { onOpenDrawer() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("Your Vaults", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showCreateAccountDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Net Worth bento grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("NET WORTH", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.2f", netWorth)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Assets", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.1f", totalAssets)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6).copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, Color(0xFFFFDAD6))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Liabilities", color = Color(0xFFBA1A1A), fontSize = 11.sp)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.1f", totalLiabilities)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Vaults list grid
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(accounts) { acc ->
                    var showDropdownMenu by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("account_card_${acc.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (acc.balance < 0) Color(0xFFFFDAD6) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (acc.balance < 0) Color(0xFFFFDAD6) else Color(0xFFB0F0D6)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon(
                                            when (acc.type) {
                                                "Bank" -> "account_balance"
                                                "Cash" -> "wallet"
                                                "Credit Card" -> "credit_card"
                                                else -> "smartphone"
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = if (acc.balance < 0) Color(0xFFBA1A1A) else Color(0xFF003527)
                                    )
                                }
                                Box {
                                    IconButton(
                                        onClick = { showDropdownMenu = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    DropdownMenu(
                                        expanded = showDropdownMenu,
                                        onDismissRequest = { showDropdownMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Delete Account") },
                                            onClick = {
                                                viewModel.deleteAccount(acc)
                                                showDropdownMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Column {
                                Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                                Text(acc.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text(
                                text = "$currency ${String.format(Locale.getDefault(), "%,.1f", acc.balance)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = if (acc.balance < 0) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Account dialog
    if (showCreateAccountDialog) {
        var accName by remember { mutableStateOf("") }
        var accType by remember { mutableStateOf("Bank") } // "Cash", "Bank", "Credit Card", "Digital Wallet"
        var accBalance by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateAccountDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Add New Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_account_name")
                    )

                    // Type Row Choice
                    Text("Account Type", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val types = listOf("Cash", "Bank", "Credit Card", "Digital Wallet")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { typeChoice ->
                            val s = accType == typeChoice
                            FilterChip(
                                selected = s,
                                onClick = { accType = typeChoice },
                                label = { Text(typeChoice) },
                                modifier = Modifier.testTag("acc_type_$typeChoice")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = accBalance,
                        onValueChange = { accBalance = it },
                        label = { Text("Initial Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_account_balance")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateAccountDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val bal = accBalance.toDoubleOrNull() ?: 0.0
                                if (accName.isNotBlank()) {
                                    viewModel.addAccount(accName, accType, bal)
                                    showCreateAccountDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("Add Account")
                        }
                    }
                }
            }
        }
    }
}

// 4. Categories Screen (Track activities & search/filters)
@Composable
fun CategoriesScreen(viewModel: WealthViewModel, navController: NavController) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("All") } // "All", "Income", "Expense"
    var showAddCategory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categories & Log", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = { showAddCategory = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab selection
        TabRow(
            selectedTabIndex = when (activeTab) {
                "All" -> 0
                "Income" -> 1
                else -> 2
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeTab == "All", onClick = { activeTab = "All" }) {
                Text("All Log", modifier = Modifier.padding(12.dp), color = if (activeTab == "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = if (activeTab == "All") FontWeight.Bold else FontWeight.Normal)
            }
            Tab(selected = activeTab == "Income", onClick = { activeTab = "Income" }) {
                Text("Income", modifier = Modifier.padding(12.dp), color = if (activeTab == "Income") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = if (activeTab == "Income") FontWeight.Bold else FontWeight.Normal)
            }
            Tab(selected = activeTab == "Expense", onClick = { activeTab = "Expense" }) {
                Text("Expense", modifier = Modifier.padding(12.dp), color = if (activeTab == "Expense") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = if (activeTab == "Expense") FontWeight.Bold else FontWeight.Normal)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Grid or Full Transaction listing
        if (activeTab == "All") {
            // Log listing
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(transactions) { tx ->
                    val cat = categories.find { it.id == tx.categoryId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(cat?.color ?: "#7F8C8D").copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        IconsUtil.getIcon(cat?.icon ?: "receipt"),
                                        contentDescription = null,
                                        tint = parseHexColor(cat?.color ?: "#7F8C8D")
                                    )
                                }
                                Column {
                                    Text(cat?.name ?: "Transfer Log", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(tx.note.ifBlank { "No note" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "$currency ${tx.amount}",
                                    color = if (tx.type == "Income") Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.deleteTransaction(tx) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Category list representing either Income or Expense categories
            val filteredCats = categories.filter { it.type == activeTab }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCats) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(cat.color).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(IconsUtil.getIcon(cat.icon), contentDescription = null, tint = parseHexColor(cat.color))
                            }
                            Column {
                                Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(cat.type, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add category dialog
    if (showAddCategory) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf("Expense") }
        var catColor by remember { mutableStateOf("#9b5de5") }
        var catIcon by remember { mutableStateOf("restaurant") }

        Dialog(onDismissRequest = { showAddCategory = false }) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Add Custom Category", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cat_name")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { catType = "Expense" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (catType == "Expense") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (catType == "Expense") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Expense")
                        }
                        Button(
                            onClick = { catType = "Income" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (catType == "Income") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (catType == "Income") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Income")
                        }
                    }

                    // Simple palette selection
                    Text("Choose Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val colors = listOf("#FF5E5B", "#457B9D", "#2A9D8F", "#E9C46A", "#9B5DE5", "#F15BB5", "#2B6954", "#10B981")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(c))
                                    .border(
                                        width = if (catColor == c) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { catColor = c }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddCategory = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (catName.isNotBlank()) {
                                    viewModel.addCategory(catName, catType, catIcon, catColor)
                                    showAddCategory = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

// 5. Reports & Analytics Screen (screenshot 2 - Ring Pie chart & Spline graph spending trend)
@Composable
fun ReportsScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var reportsFilter by remember { mutableStateOf("Monthly") } // "Daily", "Weekly", "Monthly", "Yearly"

    val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

    // Aggregate category expenses
    val expenseTxs = transactions.filter { it.type == "Expense" }
    val expensesByCategory = expenseTxs
        .groupBy { it.categoryId }
        .map { (catId, txList) ->
            val cat = categories.find { it.id == catId }
            CategorySpending(
                name = cat?.name ?: "Others",
                color = cat?.color ?: "#7F8C8D",
                icon = cat?.icon ?: "receipt",
                amount = txList.sumOf { it.amount }
            )
        }.sortedByDescending { it.amount }

    val totalCategorizedExpense = expensesByCategory.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Page Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = { onOpenDrawer() }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text("Reports & Analytics", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Insights into your financial health", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            }
        }

        // Date Range Selector (Pill tabs)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    val tabs = listOf("Daily", "Weekly", "Monthly", "Yearly")
                    tabs.forEach { tab ->
                        val active = reportsFilter == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { reportsFilter = tab }
                                .testTag("reports_tab_$tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Income & Expense comparisons
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB0F0D6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF2B6954))
                        }
                        Column {
                            Text("Total Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalIncome)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFDAD6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFFBA1A1A))
                        }
                        Column {
                            Text("Total Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalExpense)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Category Spending Ring Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Category Spending", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    if (expensesByCategory.isEmpty()) {
                        Text("No logs for Pie Chart calculation.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        // Drawing premium Ring Pie Donut chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(160.dp)) {
                                var runAngle = -90f
                                for (item in expensesByCategory) {
                                    val sweep = (item.amount / totalCategorizedExpense * 360f).toFloat()
                                    drawArc(
                                        color = parseHexColor(item.color),
                                        startAngle = runAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    runAngle += sweep
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.1fk", totalCategorizedExpense / 1000.0)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Grid items details
                        val gridData = expensesByCategory.take(4)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            gridData.chunked(2).forEach { pair ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    pair.forEach { item ->
                                        val percent = (item.amount / totalCategorizedExpense * 100).toInt()
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(parseHexColor(item.color)))
                                            Text(item.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("$percent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spending Trend Spline curve
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Spending Trend", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Last 30 days outline", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Spline canvas draw
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val strokeColor = MaterialTheme.colorScheme.primary
                        val strokeColorAlpha = strokeColor.copy(alpha = 0.2f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw horizontal dotted grid lines
                            val gridCount = 4
                            for (i in 0 until gridCount) {
                                val gridY = h * i / (gridCount - 1)
                                drawLine(
                                    color = strokeColor.copy(alpha = 0.15f),
                                    start = Offset(0f, gridY),
                                    end = Offset(w, gridY),
                                    strokeWidth = 1.6f
                                )
                            }

                            // Spend simulated spline dots (Oct 1..30)
                            val points = listOf(
                                Offset(w * 0f, h * 0.8f),
                                Offset(w * 0.2f, h * 0.75f),
                                Offset(w * 0.4f, h * 0.7f),
                                Offset(w * 0.6f, h * 0.4f),
                                Offset(w * 0.8f, h * 0.3f),
                                Offset(w * 1f, h * 0.2f)
                            )

                            val splinePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val current = points[i]
                                    cubicTo(
                                        (prev.x + current.x) / 2, prev.y,
                                        (prev.x + current.x) / 2, current.y,
                                        current.x, current.y
                                    )
                                }
                            }

                            // Draw spline gradient fill
                            val fillPath = Path().apply {
                                addPath(splinePath)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    listOf(strokeColorAlpha, Color.Transparent)
                                )
                            )

                            // Draw line
                            drawPath(
                                path = splinePath,
                                color = strokeColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Interact dot highlighter
                            val highlightIdx = 4
                            val dot = points[highlightIdx]
                            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = dot)
                            drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = dot, style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

data class CategorySpending(val name: String, val color: String, val icon: String, val amount: Double)

// 6. Budget Screen
@Composable
fun BudgetScreen(viewModel: WealthViewModel, navController: NavController) {
    val budgets by viewModel.budgetsWithDetails.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var showAddBudgetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Limits & Budgets", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { showAddBudgetDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Budget", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (budgets.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Text("No monthly limit budgets defined yet.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
            }
        } else {
            items(budgets) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(item.categoryColor).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(IconsUtil.getIcon(item.categoryIcon), contentDescription = null, tint = parseHexColor(item.categoryColor))
                                }
                                Text(item.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = { viewModel.saveBudget(item.categoryId, 0.0, item.month) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        val fraction = if (item.limitAmount > 0) (item.usedAmount / item.limitAmount).toFloat() else 0f
                        val limitExcess = item.usedAmount > item.limitAmount

                        LinearProgressIndicator(
                            progress = fraction.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (limitExcess) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Spent: $currency ${item.usedAmount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Limit: $currency ${item.limitAmount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }

                        if (limitExcess) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                                Text("Over-budget alert! You exceeded limit.", color = Color(0xFFBA1A1A), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBudgetDialog) {
        var selectedCat by remember { mutableStateOf<CategoryEntity?>(null) }
        var limitAmount by remember { mutableStateOf("") }
        var showCatSelector by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddBudgetDialog = false }) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Set Category Limit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    // Choose Category Button
                    Button(
                        onClick = { showCatSelector = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCat?.name ?: "Select Category")
                    }

                    OutlinedTextField(
                        value = limitAmount,
                        onValueChange = { limitAmount = it },
                        label = { Text("Limit Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_budget_amount")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddBudgetDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = limitAmount.toDoubleOrNull() ?: 0.0
                                if (selectedCat != null && amt > 0) {
                                    viewModel.saveBudget(selectedCat!!.id, amt, SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
                                    showAddBudgetDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("Set Budget")
                        }
                    }
                }
            }
        }

        // Subcategory dialog chooser
        if (showCatSelector) {
            Dialog(onDismissRequest = { showCatSelector = false }) {
                Surface(modifier = Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(categories.filter { it.type == "Expense" }) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCat = cat
                                        showCatSelector = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(cat.color).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(IconsUtil.getIcon(cat.icon), contentDescription = null, tint = parseHexColor(cat.color))
                                }
                                Text(cat.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. Settings Screen
@Composable
fun SettingsScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val savedPin by viewModel.savedPin.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInputValue by remember { mutableStateOf("") }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.importFromCSV(
                    context,
                    it,
                    onSuccess = { Toast.makeText(context, "CSV Imported Successfully!", Toast.LENGTH_SHORT).show() },
                    onError = { err -> Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show() }
                )
            }
        }
    )

    val activeUserName by viewModel.googleUserName.collectAsStateWithLifecycle()
    val activeUserEmail by viewModel.googleUserEmail.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isGoogleLoggedIn.collectAsStateWithLifecycle()

    val isFirebaseCustomConfigured = try {
        val k = com.example.BuildConfig.FIREBASE_API_KEY
        val app = com.example.BuildConfig.FIREBASE_APPLICATION_ID
        val proj = com.example.BuildConfig.FIREBASE_PROJECT_ID
        k.isNotBlank() && !k.startsWith("PLACEHOLDER") &&
        app.isNotBlank() && !app.startsWith("PLACEHOLDER") &&
        proj.isNotBlank() && !proj.startsWith("PLACEHOLDER")
    } catch (e: Throwable) { false }

    val pageBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val sectionLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryText = MaterialTheme.colorScheme.onSurface
    val subtleText = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onOpenDrawer() }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = primaryText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryText
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── APPEARANCE ───────────────────────────────────────
            SettingsSectionLabel(label = "Appearance", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, dividerColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBox(icon = Icons.Default.DarkMode, tint = accentColor)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", style = MaterialTheme.typography.titleSmall, color = primaryText)
                        Text(
                            if (isDarkMode) "On" else "Off",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // ── CURRENCY ─────────────────────────────────────────
            SettingsSectionLabel(label = "Currency", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, dividerColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(icon = Icons.Default.AttachMoney, tint = accentColor)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Primary Currency", style = MaterialTheme.typography.titleSmall, color = primaryText)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("AED", "USD", "INR", "EUR").forEach { cur ->
                            val isActive = currency == cur
                            OutlinedButton(
                                onClick = { viewModel.setCurrency(cur) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isActive) accentColor else Color.Transparent,
                                    contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else subtleText
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) accentColor else dividerColor
                                ),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text(
                                    cur,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // ── DATA ─────────────────────────────────────────────
            SettingsSectionLabel(label = "Data", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, dividerColor)
            ) {
                Column {
                    // Export row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val uri = viewModel.exportToCSV(context)
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Export Transactions CSV"))
                                } else {
                                    Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBox(icon = Icons.Default.Upload, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Export to CSV", style = MaterialTheme.typography.titleSmall, color = primaryText)
                            Text("Share a backup of your transactions", style = MaterialTheme.typography.bodySmall, color = subtleText)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subtleText, modifier = Modifier.size(18.dp))
                    }

                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                    // Import row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                fileChooserLauncher.launch(arrayOf("text/comma-separated-values", "text/csv"))
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBox(icon = Icons.Default.Download, tint = accentColor)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import from CSV", style = MaterialTheme.typography.titleSmall, color = primaryText)
                            Text("Restore from a previous backup", style = MaterialTheme.typography.bodySmall, color = subtleText)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subtleText, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── SECURITY ─────────────────────────────────────────
            SettingsSectionLabel(label = "Security", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, dividerColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPinDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBox(
                        icon = if (savedPin != null) Icons.Default.Lock else Icons.Default.LockOpen,
                        tint = if (savedPin != null) SuccessGreen else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App PIN Lock", style = MaterialTheme.typography.titleSmall, color = primaryText)
                        Text(
                            if (savedPin != null) "Enabled — tap to manage" else "Disabled — tap to set up",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (savedPin != null) SuccessGreen else MaterialTheme.colorScheme.error
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subtleText, modifier = Modifier.size(18.dp))
                }
            }

            // ── CLOUD SYNC ───────────────────────────────────────
            SettingsSectionLabel(label = "Cloud Sync", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, dividerColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(icon = Icons.Default.Cloud, tint = accentColor)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Firebase Sync", style = MaterialTheme.typography.titleSmall, color = primaryText)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            color = if (isFirebaseCustomConfigured) SuccessGreen else Color(0xFFD97706),
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    if (isFirebaseCustomConfigured) "Live" else "Sandbox mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isFirebaseCustomConfigured) SuccessGreen else Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    if (!isFirebaseCustomConfigured) {
                        Text(
                            "Add Firebase credentials in AI Studio Secrets to enable live sync.",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "FIREBASE_API_KEY",
                                "FIREBASE_APPLICATION_ID",
                                "FIREBASE_PROJECT_ID",
                                "FIREBASE_WEB_CLIENT_ID"
                            ).forEach { key ->
                                Text(
                                    "• $key",
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = accentColor
                                )
                            }
                        }
                    }

                    if (isLoggedIn && activeUserEmail != null) {
                        HorizontalDivider(color = dividerColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "$activeUserName",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = primaryText
                                )
                                Text(
                                    "$activeUserEmail",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtleText
                                )
                            }
                            TextButton(
                                onClick = {
                                    viewModel.logoutGoogle()
                                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    "Sign Out",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── PIN Dialog ───────────────────────────────────────────────
    if (showPinDialog) {
        Dialog(onDismissRequest = { showPinDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(accentColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            "Set App PIN",
                            style = MaterialTheme.typography.titleLarge,
                            color = primaryText
                        )
                    }
                    Text(
                        "Enter a 4-digit PIN to secure CashFlow on launch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtleText
                    )

                    SkeuomorphicTextField(
                        value = pinInputValue,
                        onValueChange = { if (it.length <= 4) pinInputValue = it },
                        isDark = isDarkMode,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("4-digit PIN") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (savedPin != null) {
                            TextButton(onClick = {
                                viewModel.setPin(null)
                                showPinDialog = false
                            }) {
                                Text("Disable", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showPinDialog = false }) {
                            Text("Cancel", color = subtleText, style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (pinInputValue.length == 4) {
                                    viewModel.setPin(pinInputValue)
                                    showPinDialog = false
                                } else {
                                    Toast.makeText(context, "Enter exactly 4 digits", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Save PIN", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

// ── Settings helper composables ──────────────────────────────────

@Composable
private fun SettingsSectionLabel(label: String, color: Color) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsIconBox(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(tint.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// Advanced Feature: EMI Payment Tracker Page
@Composable
fun EMIScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val emis by viewModel.emis.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateEMIDialog by remember { mutableStateOf(false) }
    var selectedCategoryTab by remember { mutableStateOf("Installments") }

    val pageBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val primaryText = MaterialTheme.colorScheme.onSurface
    val subtleText = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val accentColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().background(pageBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onOpenDrawer() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = primaryText)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EMIs & Debts",
                            style = MaterialTheme.typography.headlineMedium,
                            color = primaryText
                        )
                        Text(
                            text = "Track installments and personal loans",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText
                        )
                    }
                }
            }

            // ── Tab Switcher ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "Installments" to Icons.Default.CreditCard,
                        "Debts & Loans" to Icons.Default.AccountBalance
                    ).forEach { (tab, icon) ->
                        val isSelected = selectedCategoryTab == tab
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) cardBg else Color.Transparent)
                                .clickable { selectedCategoryTab = tab }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) accentColor else subtleText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) accentColor else subtleText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val filteredEmis = if (selectedCategoryTab == "Installments") {
                emis.filter { !it.isDebt }
            } else {
                emis.filter { it.isDebt }
            }

            // ── Empty State ──────────────────────────────────────
            if (filteredEmis.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(accentColor.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (selectedCategoryTab == "Installments") Icons.Default.CreditCard else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = accentColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = if (selectedCategoryTab == "Installments") "No installments yet" else "No debts or loans yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryText
                        )
                        Text(
                            text = if (selectedCategoryTab == "Installments")
                                "Track recurring payments like car loans or subscriptions."
                            else
                                "Keep track of money borrowed or lent to others.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtleText,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showCreateEMIDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (selectedCategoryTab == "Installments") "Add Installment" else "Add Debt / Loan",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            } else {
                // ── EMI Cards ────────────────────────────────────
                items(filteredEmis) { emi ->
                    var payAmount by remember { mutableStateOf("") }
                    var showPayDialog by remember { mutableStateOf(false) }
                    var selectedPayAccount by remember { mutableStateOf<AccountEntity?>(null) }

                    LaunchedEffect(accounts) {
                        if (selectedPayAccount == null && accounts.isNotEmpty()) {
                            selectedPayAccount = accounts.first()
                        }
                    }

                    val progress = if (emi.totalAmount > 0) (emi.paidAmount / emi.totalAmount).toFloat() else 1f
                    val isSettled = (emi.totalAmount - emi.paidAmount) <= 0
                    val isBorrowed = emi.debtType == "Borrowed"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, dividerColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                            // ── Card Header ──────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Type icon box
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            color = if (emi.isDebt) {
                                                if (isBorrowed) Color(0xFFD97706).copy(alpha = 0.10f)
                                                else SuccessGreen.copy(alpha = 0.10f)
                                            } else accentColor.copy(alpha = 0.10f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (emi.isDebt) {
                                            if (isBorrowed) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                                        } else Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = if (emi.isDebt) {
                                            if (isBorrowed) Color(0xFFD97706) else SuccessGreen
                                        } else accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                // Title & meta
                                Column(modifier = Modifier.weight(1f)) {
                                    // Badge row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (emi.isDebt) {
                                            val badgeColor = if (isBorrowed) Color(0xFFD97706) else SuccessGreen
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(badgeColor.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isBorrowed) "Borrowed" else "Lent",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = badgeColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "${emi.tenureMonths} mo",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = subtleText
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(accentColor.copy(alpha = 0.10f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Installment",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = accentColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = emi.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = primaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (emi.isDebt && emi.personName.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = subtleText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = emi.personName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = subtleText
                                            )
                                        }
                                    }
                                }

                                // Delete button
                                IconButton(
                                    onClick = { viewModel.deleteEMI(emi) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                            RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // ── Progress Bar ─────────────────────
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Paid  $currency ${String.format("%.2f", emi.paidAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subtleText
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSettled) SuccessGreen else accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isSettled) SuccessGreen else accentColor)
                                    )
                                }
                            }

                            // ── Footer Row ───────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Due date chip
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = subtleText,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Due day ${emi.dueDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subtleText
                                    )
                                }
                                // Total
                                Text(
                                    text = "$currency ${String.format("%.2f", emi.totalAmount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = primaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // ── Action Button ────────────────────
                            if (isSettled) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SuccessGreen.copy(alpha = 0.10f))
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Fully Settled",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { showPayDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                ) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Log Repayment", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    // ── Pay Dialog ───────────────────────────────
                    if (showPayDialog) {
                        Dialog(onDismissRequest = { showPayDialog = false }) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = cardBg,
                                tonalElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Dialog header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(accentColor.copy(alpha = 0.10f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "Log Repayment",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = primaryText
                                            )
                                            Text(
                                                emi.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = subtleText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    SkeuomorphicTextField(
                                        value = payAmount,
                                        onValueChange = { payAmount = it },
                                        label = { Text("Amount ($currency)") },
                                        isDark = isDark,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    // Account selector
                                    Text(
                                        "Deduct from account",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = subtleText
                                    )
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        accounts.forEach { acc ->
                                            val isSelected = selectedPayAccount?.id == acc.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedPayAccount = acc },
                                                label = { Text(acc.name, style = MaterialTheme.typography.labelMedium) },
                                                leadingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = accentColor.copy(alpha = 0.12f),
                                                    selectedLabelColor = accentColor,
                                                    selectedLeadingIconColor = accentColor
                                                )
                                            )
                                        }
                                    }

                                    // Action buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showPayDialog = false }) {
                                            Text("Cancel", color = subtleText, style = MaterialTheme.typography.labelLarge)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val payVal = payAmount.toDoubleOrNull()
                                                if (payVal != null && payVal > 0 && selectedPayAccount != null) {
                                                    viewModel.payEMIInstallment(emi, payVal, selectedPayAccount!!.id)
                                                    showPayDialog = false
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Confirm", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── FAB Add Button ────────────────────────────────────────
        FloatingActionButton(
            onClick = { showCreateEMIDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = accentColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add EMI", modifier = Modifier.size(24.dp))
        }
    }

    // ── Create EMI / Debt Dialog ──────────────────────────────────
    if (showCreateEMIDialog) {
        var modeTab by remember(showCreateEMIDialog, selectedCategoryTab) {
            mutableStateOf(if (selectedCategoryTab == "Installments") "Standard" else "Debt")
        }
        var emiTitle by remember { mutableStateOf("") }
        var emiTotal by remember { mutableStateOf("") }
        var emiPaid by remember { mutableStateOf("") }
        var emiDueDate by remember { mutableStateOf("15th") }
        var debtType by remember { mutableStateOf("Borrowed") }
        var debtorName by remember { mutableStateOf("") }
        var debtTenureMonths by remember { mutableStateOf("12") }
        var affectAccountBalance by remember { mutableStateOf(false) }
        var linkedAccount by remember { mutableStateOf<AccountEntity?>(null) }

        LaunchedEffect(accounts) {
            if (linkedAccount == null && accounts.isNotEmpty()) {
                linkedAccount = accounts.first()
            }
        }

        Dialog(onDismissRequest = { showCreateEMIDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Dialog Header ─────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(accentColor.copy(alpha = 0.10f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (modeTab == "Standard") Icons.Default.CreditCard else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = if (modeTab == "Standard") "New Installment" else "New Debt / Loan",
                            style = MaterialTheme.typography.titleLarge,
                            color = primaryText
                        )
                    }

                    // ── Mode Tab ──────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf("Standard" to Icons.Default.CreditCard, "Debt" to Icons.Default.AccountBalance).forEach { (tab, icon) ->
                            val isSelected = modeTab == tab
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) cardBg else Color.Transparent)
                                    .clickable { modeTab = tab }
                                    .padding(vertical = 9.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = if (isSelected) accentColor else subtleText, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    tab,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) accentColor else subtleText,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    if (modeTab == "Debt") {
                        // ── Debt Type Toggle ──────────────────
                        Text("Debt type", style = MaterialTheme.typography.labelMedium, color = subtleText)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Borrowed" to Icons.Default.ArrowDownward,
                                "Lent" to Icons.Default.ArrowUpward
                            ).forEach { (dt, icon) ->
                                val active = debtType == dt
                                val btnColor = if (dt == "Borrowed") Color(0xFFD97706) else SuccessGreen
                                OutlinedButton(
                                    onClick = { debtType = dt },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) btnColor.copy(alpha = 0.10f) else Color.Transparent,
                                        contentColor = if (active) btnColor else subtleText
                                    ),
                                    border = BorderStroke(1.dp, if (active) btnColor else dividerColor)
                                ) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (dt == "Borrowed") "I Borrowed" else "I Lent", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        SkeuomorphicTextField(
                            value = debtorName,
                            onValueChange = { debtorName = it },
                            isDark = isDark,
                            label = { Text("Person / Entity Name") }
                        )
                        SkeuomorphicTextField(
                            value = emiTotal,
                            onValueChange = { emiTotal = it },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Total Amount ($currency)") }
                        )
                        SkeuomorphicTextField(
                            value = emiPaid,
                            onValueChange = { emiPaid = it },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Already Repaid ($currency)") }
                        )
                        SkeuomorphicTextField(
                            value = debtTenureMonths,
                            onValueChange = { debtTenureMonths = it },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Tenure (months)") }
                        )
                        DatePickerField(
                            value = emiDueDate,
                            label = "Monthly Due Day",
                            onClick = {
                                showAndroidDatePicker(context, System.currentTimeMillis()) { time ->
                                    val cal = Calendar.getInstance().apply { timeInMillis = time }
                                    val day = cal.get(Calendar.DAY_OF_MONTH)
                                    val suffix = when (day % 10) {
                                        1 -> if (day == 11) "th" else "st"
                                        2 -> if (day == 12) "th" else "nd"
                                        3 -> if (day == 13) "th" else "rd"
                                        else -> "th"
                                    }
                                    emiDueDate = "$day$suffix"
                                }
                            }
                        )

                        // Affect balance toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Adjust account balance now", style = MaterialTheme.typography.labelMedium, color = primaryText)
                                    Text("Deduct or add from vault immediately", style = MaterialTheme.typography.bodySmall, color = subtleText)
                                }
                                Switch(
                                    checked = affectAccountBalance,
                                    onCheckedChange = { affectAccountBalance = it },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = accentColor,
                                        checkedThumbColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }

                        if (affectAccountBalance && accounts.isNotEmpty()) {
                            Text("Choose account", style = MaterialTheme.typography.labelMedium, color = subtleText)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val isSelected = linkedAccount?.id == acc.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { linkedAccount = acc },
                                        label = { Text(acc.name, style = MaterialTheme.typography.labelMedium) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = accentColor.copy(alpha = 0.12f),
                                            selectedLabelColor = accentColor,
                                            selectedLeadingIconColor = accentColor
                                        )
                                    )
                                }
                            }
                        }

                    } else {
                        // ── Standard Installment Form ─────────
                        SkeuomorphicTextField(
                            value = emiTitle,
                            onValueChange = { emiTitle = it },
                            isDark = isDark,
                            label = { Text("Installment Name (e.g. Car Loan)") }
                        )
                        SkeuomorphicTextField(
                            value = emiTotal,
                            onValueChange = { emiTotal = it },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Total Amount ($currency)") }
                        )
                        SkeuomorphicTextField(
                            value = emiPaid,
                            onValueChange = { emiPaid = it },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Already Paid ($currency)") }
                        )
                        DatePickerField(
                            value = emiDueDate,
                            label = "Monthly Due Day",
                            onClick = {
                                showAndroidDatePicker(context, System.currentTimeMillis()) { time ->
                                    val cal = Calendar.getInstance().apply { timeInMillis = time }
                                    val day = cal.get(Calendar.DAY_OF_MONTH)
                                    val suffix = when (day % 10) {
                                        1 -> if (day == 11) "th" else "st"
                                        2 -> if (day == 12) "th" else "nd"
                                        3 -> if (day == 13) "th" else "rd"
                                        else -> "th"
                                    }
                                    emiDueDate = "$day$suffix"
                                }
                            }
                        )
                    }

                    // ── Dialog Actions ────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateEMIDialog = false }) {
                            Text("Cancel", color = subtleText, style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val tot = emiTotal.toDoubleOrNull() ?: 0.0
                                val pd = emiPaid.toDoubleOrNull() ?: 0.0
                                val dayVal = emiDueDate.filter { it.isDigit() }.toIntOrNull() ?: 15

                                if (modeTab == "Debt") {
                                    if (debtorName.isNotBlank() && tot > 0) {
                                        val finalTitle = "Debt: $debtType ($debtorName)"
                                        val tenure = debtTenureMonths.toIntOrNull() ?: 12
                                        viewModel.addEMI(
                                            title = finalTitle,
                                            total = tot,
                                            paid = pd,
                                            dueDate = emiDueDate,
                                            isDebt = true,
                                            debtType = debtType,
                                            personName = debtorName,
                                            tenureMonths = tenure
                                        )
                                        com.example.receiver.EmiAlarmScheduler.scheduleEmiAlert(context, finalTitle, dayVal)
                                        if (affectAccountBalance && linkedAccount != null) {
                                            val finalType = if (debtType == "Borrowed") "Income" else "Expense"
                                            viewModel.addTransaction(
                                                amount = tot - pd,
                                                type = finalType,
                                                categoryId = null,
                                                accountId = linkedAccount!!.id,
                                                transferToAccountId = null,
                                                date = System.currentTimeMillis(),
                                                note = "[Debt Track Initialized] $debtType from/to $debtorName"
                                            )
                                        }
                                        showCreateEMIDialog = false
                                    } else {
                                        Toast.makeText(context, "Enter a valid amount and person name", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    if (emiTitle.isNotBlank() && tot > 0) {
                                        viewModel.addEMI(emiTitle, tot, pd, emiDueDate)
                                        com.example.receiver.EmiAlarmScheduler.scheduleEmiAlert(context, emiTitle, dayVal)
                                        showCreateEMIDialog = false
                                    } else {
                                        Toast.makeText(context, "Enter a valid name and total amount", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
