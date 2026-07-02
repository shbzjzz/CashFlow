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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.ImeAction
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
import com.example.ui.theme.*
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

data class CountryInfo(
    val name: String,
    val currency: String,
    val flag: String, // String representation or emoji for now
    val code: String
)

val Countries = listOf(
    CountryInfo("United Arab Emirates", "AED", "🇦🇪", "UAE"),
    CountryInfo("India", "INR", "🇮🇳", "IND"),
    CountryInfo("Pakistan", "PKR", "🇵🇰", "PAK"),
    CountryInfo("Bangladesh", "BDT", "🇧🇩", "BGD"),
    CountryInfo("Philippines", "PHP", "🇵🇭", "PHL"),
    CountryInfo("Egypt", "EGP", "🇪🇬", "EGY"),
    CountryInfo("Saudi Arabia", "SAR", "🇸🇦", "SAU"),
    CountryInfo("Qatar", "QAR", "🇶🇦", "QAT"),
    CountryInfo("Kuwait", "KWD", "🇰🇼", "KWT"),
    CountryInfo("Oman", "OMR", "🇴🇲", "OMN"),
    CountryInfo("Bahrain", "BHD", "🇧🇭", "BHR"),
    CountryInfo("United States", "USD", "🇺🇸", "USA"),
    CountryInfo("United Kingdom", "GBP", "🇬🇧", "GBR"),
    CountryInfo("European Union", "EUR", "🇪🇺", "EUR")
)

// Helper to convert hex string color securely to Compose color
fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Sizes {
    val iconSm = 16.dp
    val iconSmall = 16.dp
    val iconMedium = 24.dp
    val iconMd = 24.dp
    val iconLarge = 32.dp
    val iconXl = 48.dp
    val touchMinimum = 48.dp
    val dividerThickness = 1.dp
}

object Opacity {
    val subtle = 0.6f
    val hovered = 0.1f
    val pressed = 0.2f
}

object Corners {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

object Shadows {
    object Light {
        val subtle = 2.dp
        val medium = 6.dp
        val soft = 8.dp
    }
}

@Composable
fun CountryBadge(flag: String, countryCode: String, size: androidx.compose.ui.unit.Dp = 24.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(flag, fontSize = (size.value * 0.5f).sp)
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
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    // Mint Finance: soft, warm cards with gentle elevation and a faint brand-tinted shadow.
    Card(
        modifier = modifier.shadow(
            elevation = 3.dp,
            shape = shape,
            clip = false,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
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
    // Mint Finance: pill-ish buttons with soft elevation and a gentle selected state.
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = if (isSelected) null
                 else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    // Mint Finance: fields feel like soft inset wells on cream surfaces.
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
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
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
    
    var name by remember { mutableStateOf("") }
    var homeCountry by remember { mutableStateOf(Countries[0]) }
    var showHomeMenu by remember { mutableStateOf(false) }
    
    var addSecondCountry by remember { mutableStateOf(false) }
    var secondCountry by remember { mutableStateOf(Countries[1]) }
    var showSecondMenu by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .shadow(10.dp, RoundedCornerShape(32.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Welcome to CashFlow",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Let's set up your profile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            SkeuomorphicTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                isDark = isDark,
                modifier = Modifier.fillMaxWidth().testTag("name_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Select your Default Country",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHomeMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(homeCountry.flag, fontSize = 28.sp)
                            Column {
                                Text(homeCountry.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Currency: ${homeCountry.currency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                
                DropdownMenu(
                    expanded = showHomeMenu,
                    onDismissRequest = { showHomeMenu = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surface)
                ) {
                    Countries.forEach { country ->
                        DropdownMenuItem(
                            text = { 
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(country.flag, fontSize = 20.sp)
                                    Text(country.name, color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                homeCountry = country
                                showHomeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = addSecondCountry,
                    onCheckedChange = { addSecondCountry = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    "Track work/home country data too?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (addSecondCountry) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSecondMenu = true },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(secondCountry.flag, fontSize = 28.sp)
                                Column {
                                    Text(secondCountry.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Currency: ${secondCountry.currency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showSecondMenu,
                        onDismissRequest = { showSecondMenu = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(MaterialTheme.colorScheme.surface)
                    ) {
                        Countries.filter { it != homeCountry }.forEach { country ->
                            DropdownMenuItem(
                                text = { 
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(country.flag, fontSize = 20.sp)
                                        Text(country.name, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                },
                                onClick = {
                                    secondCountry = country
                                    showSecondMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.registerUser(
                        name = name,
                        homeCountry = homeCountry.name,
                        homeCurrency = homeCountry.currency,
                        secCountry = if (addSecondCountry) secondCountry.name else null,
                        secCurrency = if (addSecondCountry) secondCountry.currency else null
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("register_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text("Start Tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowTopAppBar(
    title: String,
    viewModel: WealthViewModel,
    onOpenDrawer: () -> Unit,
    onNotificationClick: () -> Unit = {},
    showCountrySelector: Boolean = true
) {
    val isSecondActive by viewModel.isSecondCountryActive.collectAsStateWithLifecycle()
    val hasSecondCountry by viewModel.hasSecondCountry.collectAsStateWithLifecycle()
    val homeCountryName by viewModel.homeCountryName.collectAsStateWithLifecycle()
    val secondCountryName by viewModel.secondCountryName.collectAsStateWithLifecycle()

    val currentCountry = if (isSecondActive) secondCountryName else homeCountryName
    val currentFlag = Countries.find { it.name == currentCountry }?.flag ?: "🌍"
    
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        actions = {
            if (showCountrySelector && hasSecondCountry) {
                Box {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showMenu = true }
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.Transparent
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(currentFlag, fontSize = 16.sp)
                            Text(currentCountry.take(3).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                val homeInfo = Countries.find { it.name == homeCountryName }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(homeInfo?.flag ?: "🌍")
                                    Text(homeCountryName + (if (!isSecondActive) " (Current)" else "")) 
                                }
                            },
                            onClick = { 
                                viewModel.toggleCountry(false)
                                showMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                val secInfo = Countries.find { it.name == secondCountryName }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(secInfo?.flag ?: "🌍")
                                    Text(secondCountryName + (if (isSecondActive) " (Current)" else "")) 
                                }
                            },
                            onClick = { 
                                viewModel.toggleCountry(true)
                                showMenu = false 
                            }
                        )
                    }
                }
            }
            IconButton(onClick = onNotificationClick) {
                BadgedBox(badge = { Badge { Text("2") } }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun DrawerContent(
    viewModel: WealthViewModel,
    navController: NavController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditCountryDialog by remember { mutableStateOf(false) }
    var newTempName by remember { mutableStateOf("") }
    var showAddCategoryByDrawer by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntryFlow.collectAsState(initial = null).value?.destination?.route

    val drawerBg = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val primaryText = MaterialTheme.colorScheme.onSurface
    val subtleText = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerC = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val homeCountryName by viewModel.homeCountryName.collectAsStateWithLifecycle()
    val secondCountryName by viewModel.secondCountryName.collectAsStateWithLifecycle()
    val hasSecondCountry by viewModel.hasSecondCountry.collectAsStateWithLifecycle()
    val isSecondActive by viewModel.isSecondCountryActive.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavigationItem("Home",           Routes.DASHBOARD, Icons.Default.Home),
        NavigationItem("Analytics",      Routes.REPORTS,   Icons.Default.BarChart),
        NavigationItem("Wallets",        Routes.ACCOUNTS,  Icons.Default.AccountBalanceWallet),
        NavigationItem("Debts Only",     Routes.EMIS,      Icons.Default.CreditScore),
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
            // ── Mint Gradient Header ─────────────────────────────
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (username.isNotEmpty()) username.take(1).uppercase() else "CF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
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
                            Icon(Icons.Default.Edit, "Edit", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        }
                    }

                    // Country Info in Drawer
                    val currentFlag = Countries.find { it.name == homeCountryName }?.flag ?: "🌍"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Home: $currentFlag $homeCountryName",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            if (hasSecondCountry) {
                                val altFlag = Countries.find { it.name == secondCountryName }?.flag ?: "🌍"
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Alt: $altFlag $secondCountryName",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        IconButton(
                            onClick = { showEditCountryDialog = true },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit Country", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        }
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
                val selected = if (item.route == Routes.DASHBOARD) {
                    currentRoute == Routes.DASHBOARD ||
                    currentRoute == Routes.ADD_TRANSACTION ||
                    currentRoute == Routes.NOTIFICATIONS ||
                    currentRoute == Routes.CATEGORIES ||
                    currentRoute == Routes.BUDGETS
                } else {
                    currentRoute == item.route
                }
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
                            if (item.route == Routes.DASHBOARD) {
                                navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                            } else if (currentRoute != item.route) {
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

            // ── Add Category option from side drawer ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        scope.launch { drawerState.close() }
                        showAddCategoryByDrawer = true
                    }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = "Add Category",
                    tint = subtleText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Add Category",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = primaryText
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = dividerC
            )
        }

        // ── Logout ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                .clickable {
                    scope.launch { drawerState.close() }
                    viewModel.logout()
                }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Logout",
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

    // ── Edit Country Dialog ─────────────────────────────────────────
    if (showEditCountryDialog) {
        var tempHome by remember { mutableStateOf<CountryInfo?>(Countries.find { it.name == homeCountryName }) }
        var tempAlt by remember { mutableStateOf<CountryInfo?>(Countries.find { it.name == secondCountryName }) }

        Dialog(onDismissRequest = { showEditCountryDialog = false }) {
            Surface(
                modifier = Modifier.width(320.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Edit Countries",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text("Home Country", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    var homeExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { homeExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tempHome?.let { "${it.flag} ${it.name}" } ?: "Select Home")
                        }
                        DropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                            Countries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.flag} ${c.name}") },
                                    onClick = { tempHome = c; homeExpanded = false }
                                )
                            }
                        }
                    }

                    Text("Alternate Country (Optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    var altExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { altExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tempAlt?.let { "${it.flag} ${it.name}" } ?: "None")
                        }
                        DropdownMenu(expanded = altExpanded, onDismissRequest = { altExpanded = false }) {
                            DropdownMenuItem(text = { Text("None") }, onClick = { tempAlt = null; altExpanded = false })
                            Countries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.flag} ${c.name}") },
                                    onClick = { tempAlt = c; altExpanded = false }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditCountryDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val home = tempHome
                                val alt = tempAlt
                                if (home != null) {
                                    viewModel.updateCountries(
                                        homeCountry = home.name,
                                        homeCurrency = home.currency,
                                        secCountry = alt?.name,
                                        secCurrency = alt?.currency
                                    )
                                    showEditCountryDialog = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }

    // Add category dialog launched from Side Drawer
    if (showAddCategoryByDrawer) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf("Expense") }
        var catColor by remember { mutableStateOf("#9b5de5") }
        var catIcon by remember { mutableStateOf("restaurant") }

        Dialog(onDismissRequest = { showAddCategoryByDrawer = false }) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Add Custom Category", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cat_name_drawer")
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
                    val colorPalette = listOf("#4F9D8A", "#D4A373", "#5BB98C", "#E2A93B", "#9C8EC4", "#E8A07E", "#2F6B5C", "#6BA4C7")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorPalette.forEach { c ->
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
                        TextButton(onClick = { showAddCategoryByDrawer = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (catName.isNotBlank()) {
                                    viewModel.addCategory(catName, catType, catIcon, catColor)
                                    showAddCategoryByDrawer = false
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

@Composable
fun AppNavigation(viewModel: WealthViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
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
                        .padding(bottom = paddingValues.calculateBottomPadding())
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
            val selected = if (item.route == Routes.DASHBOARD) {
                currentRoute == Routes.DASHBOARD ||
                currentRoute == Routes.ADD_TRANSACTION ||
                currentRoute == Routes.NOTIFICATIONS ||
                currentRoute == Routes.CATEGORIES ||
                currentRoute == Routes.BUDGETS
            } else {
                currentRoute == item.route
            }
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (item.route == Routes.DASHBOARD) {
                        navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                    } else if (currentRoute != item.route) {
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
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
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
                    listOf(Color(0xFF2F6B5C), Color(0xFF4F9D8A), Color(0xFF121A17))
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: WealthViewModel, navController: NavController) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "All caught up!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Any actions or alerts will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = { viewModel.clearNotifications() }
                        ) {
                            Text("Clear All")
                        }
                    }
                }
                items(notifications) { notif ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alert",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Sizes.iconSm)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = notif.time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
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
fun DashboardScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val insights by viewModel.smartInsights.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    
    val hasSecondCountry by viewModel.hasSecondCountry.collectAsStateWithLifecycle()
    val netWorth = accounts.sumOf { it.balance }
    val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

    var inspectingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            CashFlowTopAppBar(
                title = "Wealth",
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // Net Worth Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm)
                        .shadow(Shadows.Light.medium, RoundedCornerShape(Corners.xxl)),
                    shape = RoundedCornerShape(Corners.xxl),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Net Worth",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "$currency ${String.format(Locale.getDefault(), "%,.2f", netWorth)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Income", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalIncome)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.18f)))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Expense", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalExpense)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Insights Title
            item {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
                                    .width(280.dp)
                                    .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            IconsUtil.getIcon(insight.iconType),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(insight.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Text(insight.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { navController.navigate(Routes.CATEGORIES) }) {
                        Text("See All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions logged yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transactions.take(10).forEach { tx ->
                            val category = categories.find { it.id == tx.categoryId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inspectingTransaction = tx }
                                    .shadow(8.dp, RoundedCornerShape(18.dp)),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                IconsUtil.getIcon(category?.icon ?: "receipt"),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(category?.name ?: tx.type, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            Text(tx.note.ifEmpty { "Transaction" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Text(
                                        text = "${if (tx.type == "Income") "+" else "-"} $currency ${tx.amount}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (tx.type == "Income") Color(0xFF5BB98C) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    // FAB
    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { navController.navigate(Routes.ADD_TRANSACTION) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 4.dp),
            modifier = Modifier.testTag("add_transaction_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(26.dp))
        }
    }

    // Interactive Transaction Inspector & Live Editor dialog
    inspectingTransaction?.let { tx ->
        var isEditing by remember { mutableStateOf(false) }

        var editType by remember { mutableStateOf(tx.type) }
        var editAmount by remember { mutableStateOf(tx.amount.toString()) }
        var editNote by remember { mutableStateOf(tx.note) }
        var editSelectedCategory by remember { mutableStateOf(categories.find { it.id == tx.categoryId }) }
        var editSelectedAccount by remember { mutableStateOf(accounts.find { it.id == tx.accountId }) }
        var editSelectedToAccount by remember { mutableStateOf(accounts.find { it.id == tx.transferToAccountId }) }
        var editDate by remember { mutableStateOf(tx.date) }

        // Sync states when target transaction shifts
        LaunchedEffect(tx) {
            isEditing = false
            editType = tx.type
            editAmount = tx.amount.toString()
            editNote = tx.note
            editSelectedCategory = categories.find { it.id == tx.categoryId }
            editSelectedAccount = accounts.find { it.id == tx.accountId }
            editSelectedToAccount = accounts.find { it.id == tx.transferToAccountId }
            editDate = tx.date
        }

        Dialog(onDismissRequest = { inspectingTransaction = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF121A17) else Color.White
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
                                text = "Transaction Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
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
                                        tint = if (txCat != null) parseHexColor(txCat.color) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = txCat?.name ?: if (tx.type == "Transfer") "Bank Transfer ⇆" else tx.type,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tx.type,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    text = "Details",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Amount",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val sign = when (tx.type) {
                                        "Income" -> "+"
                                        "Expense" -> "-"
                                        else -> ""
                                    }
                                    val amtColor = when (tx.type) {
                                        "Income" -> Color(0xFF5BB98C)
                                        "Expense" -> Color(0xFFE07A6E)
                                        else -> MaterialTheme.colorScheme.onSurface
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
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
                                    )
                                    val actSource = accounts.find { it.id == tx.accountId }
                                    Text(
                                        text = actSource?.name ?: "Unknown Wallet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
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
                                            color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
                                        )
                                        val actDest = accounts.find { it.id == tx.transferToAccountId }
                                        Text(
                                            text = actDest?.name ?: "Unknown Target Wallet",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
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
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
                                    )
                                    val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(tx.date))
                                    Text(
                                        text = dateStr,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
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
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
                                    )
                                    Text(
                                        text = tx.note,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
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
                                        color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
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
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D), modifier = Modifier.size(16.dp))
                                    Text("Edit Live", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D), fontSize = 13.sp)
                                }
                            }
                        }

                    } else {
                        // MODE 2: EDIT TRANSACTION FORM INLINE
                        Text(
                            text = "Edit Transaction Log",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
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
                                                    if (isDark) Color(0xFF24332C) else Color.White
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
                                                if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
                                            } else {
                                                if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
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
                            color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
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
                                            if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
                                        } else {
                                            if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
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
                                color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
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
                                                if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
                                            } else {
                                                if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
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
                                color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
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
                                                if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
                                            } else {
                                                if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Date picker in edit mode
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAndroidDatePicker(context, editDate) {
                                        editDate = it
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Transaction Date:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF6B7F75)
                            )
                            val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(editDate))
                            Text(
                                text = dateStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                                            transferToAccountId = if (editType == "Transfer") editSelectedToAccount?.id else null,
                                            date = editDate
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
                                Text("Save Change", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D))
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Add Transaction Screen (Screenshot 1 matching form exactly)
@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = {
                    Text(
                        text = "Add Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Shadows.Light.medium
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
                        .height(54.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Transaction", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
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
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Corners.lg))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Income", "Expense", "Transfer").forEach { tab ->
                    val isSelected = type == tab
                    val tabColor = typeColors[tab] ?: MaterialTheme.colorScheme.primary
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Corners.md))
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
                    .clip(RoundedCornerShape(Corners.xl))
                    .background(activeTypeColor.copy(alpha = Opacity.hovered))
                    .padding(vertical = Spacing.xxl),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(
                        text = "TRANSACTION LEDGER DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
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
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
                                    )
                                    Text(
                                        selectedCategory?.name ?: "Select Category",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
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
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
                                    color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
                                )
                                Text(
                                    selectedAccount?.name ?: "Choose Account",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
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
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
                                        tint = if (isDark) Color(0xFF5BB98C) else Color(0xFF5BB98C)
                                    )
                                }
                                Column {
                                    Text(
                                        "To Account",
                                        fontSize = 11.sp,
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
                                    )
                                    Text(
                                        selectedToAccount?.name ?: "Select Destination",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
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
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
                                    color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
                                )
                                Text(
                                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateEpochMillis)),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
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
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = "TRANSACTION NOTE / MEMO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
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
                        color = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D)
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
                                tint = if (isDark) Color(0xFFE2A93B) else Color(0xFFA87B4D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                "Upload Receipt Image",
                                color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(Corners.md))
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
                                    .padding(Spacing.sm)
                                    .background(Color.Black.copy(alpha = Opacity.subtle), CircleShape)
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
                                        RoundedCornerShape(Corners.md)
                                    )
                                    .padding(Spacing.md),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
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
                                    color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
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
                                        RoundedCornerShape(Corners.md)
                                    )
                                    .padding(Spacing.md),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
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
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
                                    )
                                    Text(
                                        acc.type,
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
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
                                        RoundedCornerShape(Corners.md)
                                    )
                                    .padding(Spacing.md),
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
                                        color = if (isDark) Color(0xFFE8F0EB) else Color(0xFF22372E)
                                    )
                                    Text(
                                        acc.type,
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFF9FB2A8) else Color(0xFF3B524B)
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

    Scaffold(
        topBar = {
            CashFlowTopAppBar(
                title = "Vaults",
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // Net Worth Bento Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm).shadow(Shadows.Light.soft, RoundedCornerShape(Corners.xl)),
                    shape = RoundedCornerShape(Corners.xl),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                        Column {
                            Text("Total Net Worth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currency ${String.format(Locale.getDefault(), "%,.2f", netWorth)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Assets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalAssets)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.07f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Liabilities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalLiabilities)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accounts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showCreateAccountDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Accounts list replacing the old small 2-column grid
            items(accounts) { acc ->
                var showMenu by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Sizes.iconXl)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = Opacity.hovered), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    IconsUtil.getIcon(when(acc.type) { "Bank" -> "account_balance" "Cash" -> "payments" else -> "credit_card" }),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Sizes.iconMd)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!viewModel.isSecondCountryActive.collectAsStateWithLifecycle().value.not()) {
                                        CountryBadge(
                                            flag = if (acc.isSecondCountry) "🌐" else "🏠",
                                            countryCode = if (acc.isSecondCountry) "SEC" else "PRI",
                                            size = 20.dp
                                        )
                                    }
                                }
                                Text(
                                    text = acc.type,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (acc.balance >= 0) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = Opacity.hovered)
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = Opacity.hovered)
                                    }
                                ),
                                shape = RoundedCornerShape(Corners.md)
                            ) {
                                Text(
                                    text = "$currency ${String.format(Locale.getDefault(), "%,.2f", acc.balance)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (acc.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                                )
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(Sizes.touchMinimum)) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(Sizes.iconSm), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Delete") }, onClick = { viewModel.deleteAccount(acc); showMenu = false })
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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

// 4. Categories Screen (renamed to All Trans screen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: WealthViewModel, navController: NavController) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("All") } // "All", "Income", "Expense"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Trans",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        ) {
            // Tab selection
            TabRow(
                selectedTabIndex = when (activeTab) {
                    "All" -> 0
                    "Income" -> 1
                    else -> 2
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (activeTab.let { _ -> true }) {
                        val idx = when (activeTab) { "All" -> 0; "Income" -> 1; else -> 2 }
                        if (idx < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[idx]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    }
                }
            ) {
                Tab(selected = activeTab == "All", onClick = { activeTab = "All" }) {
                    Text("All Log", modifier = Modifier.padding(12.dp), color = if (activeTab == "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = if (activeTab == "All") FontWeight.SemiBold else FontWeight.Normal)
                }
                Tab(selected = activeTab == "Income", onClick = { activeTab = "Income" }) {
                    Text("Income", modifier = Modifier.padding(12.dp), color = if (activeTab == "Income") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = if (activeTab == "Income") FontWeight.SemiBold else FontWeight.Normal)
                }
                Tab(selected = activeTab == "Expense", onClick = { activeTab = "Expense" }) {
                    Text("Expense", modifier = Modifier.padding(12.dp), color = if (activeTab == "Expense") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = if (activeTab == "Expense") FontWeight.SemiBold else FontWeight.Normal)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log listing
            val filteredTxs = remember(transactions, activeTab) {
                if (activeTab == "All") transactions
                else transactions.filter { it.type == activeTab }
            }

            if (filteredTxs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions logged in this category",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTxs) { tx ->
                        val cat = categories.find { it.id == tx.categoryId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(cat?.color ?: "#7F8C8D").copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            IconsUtil.getIcon(cat?.icon ?: "receipt"),
                                            contentDescription = null,
                                            tint = parseHexColor(cat?.color ?: "#7F8C8D"),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            cat?.name ?: if (tx.type == "Transfer") "Transfer Swapped" else "General log",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            tx.note.ifBlank { "No note" },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$currency ${tx.amount}",
                                        color = if (tx.type == "Income") Color(0xFF5BB98C) else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { viewModel.deleteTransaction(tx) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
fun ReportsScreen(
    viewModel: WealthViewModel,
    navController: NavController,
    onOpenDrawer: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val reportsFilter by viewModel.reportsFilter.collectAsStateWithLifecycle()

    val filteredTransactions = remember(transactions, reportsFilter) {
        val now = Calendar.getInstance()
        val startOfToday = now.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val startOfWeek = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val startOfMonth = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val startOfYear = Calendar.getInstance().apply { set(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

        transactions.filter { tx ->
            when (reportsFilter) {
                "Daily" -> tx.date >= startOfToday
                "Weekly" -> tx.date >= startOfWeek
                "Monthly" -> tx.date >= startOfMonth
                "Yearly" -> tx.date >= startOfYear
                else -> true
            }
        }
    }

    val totalIncome = filteredTransactions.filter { it.type == "Income" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "Expense" }.sumOf { it.amount }

    // Aggregate category expenses
    val expenseTxs = filteredTransactions.filter { it.type == "Expense" }
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

    Scaffold(
        topBar = {
            CashFlowTopAppBar(
                title = "Analytics",
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Income Card
                    Card(
                        modifier = Modifier.weight(1f).height(96.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalIncome)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    // Expense Card
                    Card(
                        modifier = Modifier.weight(1f).height(96.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TrendingDown, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalExpense)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Filter
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { tab ->
                            val active = reportsFilter == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { viewModel.updateReportsFilter(tab) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    tab,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Spending Breakdown
            item {
                Text("Spending Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            if (expensesByCategory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No expense logs for this period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        expensesByCategory.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(parseHexColor(item.color).copy(alpha = 0.18f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(IconsUtil.getIcon(item.icon), null, tint = parseHexColor(item.color), modifier = Modifier.size(20.dp))
                                        }
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                            val pct = (item.amount / totalExpense * 100).toInt()
                                            Text("$pct% of total spending", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        text = "$currency ${String.format(Locale.getDefault(), "%,.0f", item.amount)}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pie Chart (Donut)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Category Allocation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        
                        if (expensesByCategory.isNotEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(160.dp)) {
                                    var startAngle = -90f
                                    expensesByCategory.forEach { item ->
                                        val sweepAngle = (item.amount / totalExpense * 360f).toFloat()
                                        drawArc(
                                            color = parseHexColor(item.color),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$currency ${String.format(Locale.getDefault(), "%,.0f", totalExpense)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("Insufficient data for chart", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
    var selectedCat by remember { mutableStateOf<CategoryEntity?>(null) }
    var limitAmount by remember { mutableStateOf("") }
    var showCatSelector by remember { mutableStateOf(false) }

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
                Text("Limits & Budgets", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(item.categoryColor).copy(alpha = 0.18f)),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
    val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInputValue by remember { mutableStateOf("") }
    var showColorThemeMenu by remember { mutableStateOf(false) }

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

    val pageBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val sectionLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryText = MaterialTheme.colorScheme.onSurface
    val subtleText = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val accentColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            CashFlowTopAppBar(
                title = "Settings",
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) },
                showCountrySelector = false
            )
        },
        containerColor = pageBg
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

            // ── APPEARANCE ───────────────────────────────────────
            SettingsSectionLabel(label = "Appearance", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
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

            // Color Theme Selector
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
                        .clickable { showColorThemeMenu = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBox(icon = Icons.Default.Palette, tint = accentColor)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Color Theme", style = MaterialTheme.typography.titleSmall, color = primaryText)
                        Text(colorTheme.label, style = MaterialTheme.typography.bodySmall, color = subtleText)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subtleText, modifier = Modifier.size(18.dp))
                }
            }

            if (showColorThemeMenu) {
                Dialog(onDismissRequest = { showColorThemeMenu = false }, properties = DialogProperties(usePlatformDefaultWidth = true)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(Corners.xl)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Text("Select Color Theme", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = Spacing.lg))

                            ColorTheme.values().forEach { theme ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(Corners.lg))
                                        .clickable {
                                            viewModel.setColorTheme(theme)
                                            showColorThemeMenu = false
                                        }
                                        .padding(Spacing.md),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(theme.light, CircleShape)
                                            .border(if (colorTheme == theme) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Text(theme.label, modifier = Modifier.weight(1f))
                                    if (colorTheme == theme) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Sizes.iconSm))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── DATA ─────────────────────────────────────────────
            SettingsSectionLabel(label = "Data", color = sectionLabelColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
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
                shape = RoundedCornerShape(18.dp),
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

            Spacer(modifier = Modifier.height(24.dp))
        }
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
fun SettingsSectionLabel(label: String, color: Color) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun SettingsIconBox(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
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

    var showCreateEMIDialog by remember { mutableStateOf(false) }
    var selectedCategoryTab by remember { mutableStateOf("Installments") }

    Scaffold(
        topBar = {
            CashFlowTopAppBar(
                title = "Payment Tracker",
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateEMIDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, "Add New", modifier = Modifier.size(26.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingVals)) {
            // Tab Switcher
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    "Installments" to Icons.Default.CreditCard,
                    "Debts & Loans" to Icons.Default.AccountBalance
                )
                tabs.forEach { (tab, icon) ->
                    val isSelected = selectedCategoryTab == tab
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent).clickable { selectedCategoryTab = tab }.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            val filteredEmis = if (selectedCategoryTab == "Installments") {
                emis.filter { !it.isDebt }
            } else {
                emis.filter { it.isDebt }
            }

            if (filteredEmis.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val icon = if (selectedCategoryTab == "Installments") Icons.Default.CreditCard else Icons.Default.AccountBalance
                        val title = if (selectedCategoryTab == "Installments") "No installments yet" else "No debts yet"
                        val desc = if (selectedCategoryTab == "Installments") 
                            "Track recurring payments like car loans or subscriptions." 
                            else "Keep track of money you owe or are owed."
                        val btnLabel = if (selectedCategoryTab == "Installments") "Add Installment" else "Add Debt"

                        Box(
                            modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(
                                 imageVector = icon,
                                 contentDescription = null,
                                 modifier = Modifier.size(48.dp),
                                 tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                             )
                        }
                        
                        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = desc, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showCreateEMIDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(btnLabel)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
                    items(filteredEmis) { emi ->
                        EMICard(emi, viewModel, currency, isDark, accounts)
                    }
                }
            }
        }
    }

    if (showCreateEMIDialog) {
        CreateEMIDialog(
            viewModel = viewModel,
            selectedTab = selectedCategoryTab,
            onDismiss = { showCreateEMIDialog = false },
            currency = currency,
            isDark = isDark,
            accounts = accounts
        )
    }
}

@Composable
fun EMICard(
    emi: EMIEntity,
    viewModel: WealthViewModel,
    currency: String,
    isDark: Boolean,
    accounts: List<AccountEntity>
) {
    // ... Simplified EMI Card (re-implementing concisely to avoid errors)
    var showPayDialog by remember { mutableStateOf(false) }
    var payAmount by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<AccountEntity?>(accounts.firstOrNull()) }

    val progress = if (emi.totalAmount > 0) (emi.paidAmount / emi.totalAmount).toFloat() else 1f
    val isSettled = (emi.totalAmount - emi.paidAmount) <= 0
    val isBorrowed = emi.debtType == "Borrowed"

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(if (emi.isDebt) (if (isBorrowed) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward) else Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(emi.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        if (emi.isDebt) {
                            Text(if (isBorrowed) "From ${emi.personName}" else "To ${emi.personName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val monthlyAmt = emi.totalAmount / emi.tenureMonths
                            val formattedMonthlyAmt = String.format(Locale.getDefault(), "%,.2f", monthlyAmt)
                            Text("Monthly: $currency $formattedMonthlyAmt • Due: ${emi.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                IconButton(onClick = { viewModel.deleteEMI(emi) }) {
                    Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }

            // Progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp), strokeCap = StrokeCap.Round, color = if (isSettled) SuccessGreen else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$currency ${emi.paidAmount} / ${emi.totalAmount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (!isSettled) {
                Button(onClick = { showPayDialog = true }, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                    Text("Log Repayment", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth().background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(10.dp), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settled", color = SuccessGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showPayDialog) {
        var accountMenuExpanded by remember { mutableStateOf(false) }
        // Simple Pay Dialog
        Dialog(onDismissRequest = { showPayDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Log Repayment", style = MaterialTheme.typography.titleLarge)
                    SkeuomorphicTextField(value = payAmount, onValueChange = { payAmount = it }, isDark = isDark, label = { Text("Amount") })

                    if (accounts.isNotEmpty()) {
                        Text(
                            text = "Debit Account:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { accountMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedAccount?.name ?: "Choose Account")
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = accountMenuExpanded,
                                onDismissRequest = { accountMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            selectedAccount = acc
                                            accountMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPayDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            val v = payAmount.toDoubleOrNull()
                            if (v != null && selectedAccount != null) {
                                viewModel.payEMIInstallment(emi, v, selectedAccount!!.id)
                                showPayDialog = false
                            }
                        }) { Text("Confirm") }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEMIDialog(
    viewModel: WealthViewModel,
    selectedTab: String,
    onDismiss: () -> Unit,
    currency: String,
    isDark: Boolean,
    accounts: List<AccountEntity>
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var totalAmountInput by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf("") }
    var debtTypeInput by remember { mutableStateOf("Borrowed") }
    var tenureMonthsInput by remember { mutableStateOf("12") }

    var dueDateEpoch by remember { mutableStateOf(System.currentTimeMillis()) }
    val formatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val dueDateDisplay = formatter.format(java.util.Date(dueDateEpoch))

    var adjustBalance by remember { mutableStateOf(false) }
    var selectedAccountForAdjustment by remember { mutableStateOf(accounts.firstOrNull()) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val isDebt = selectedTab != "Installments"

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(if (isDebt) "New Debt / Loan" else "New Installment", style = MaterialTheme.typography.titleLarge)

                if (isDebt) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Borrowed", "Lent").forEach { type ->
                            FilterChip(
                                selected = debtTypeInput == type,
                                onClick = { debtTypeInput = type },
                                label = { Text(if (type == "Borrowed") "Borrowed" else "Lent") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    SkeuomorphicTextField(value = personName, onValueChange = { personName = it }, label = { Text("Person Name") }, isDark = isDark)
                }

                SkeuomorphicTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isDebt) "Reason / Note" else "Installment Name") },
                    isDark = isDark
                )

                SkeuomorphicTextField(
                    value = totalAmountInput,
                    onValueChange = { totalAmountInput = it },
                    label = { Text("Total Amount ($currency)") },
                    isDark = isDark,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )

                if (!isDebt) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DatePickerField(
                            value = dueDateDisplay,
                            label = "Due Date",
                            onClick = {
                                showAndroidDatePicker(context, dueDateEpoch) { epoch ->
                                    dueDateEpoch = epoch
                                }
                            },
                            modifier = Modifier.weight(1.5f)
                        )
                        SkeuomorphicTextField(
                            value = tenureMonthsInput,
                            onValueChange = { tenureMonthsInput = it },
                            label = { Text("Tenure (mo)") },
                            isDark = isDark,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val totalAmt = totalAmountInput.toDoubleOrNull() ?: 0.0
                    val tenure = tenureMonthsInput.toIntOrNull() ?: 12
                    if (totalAmt > 0 && tenure > 0) {
                        val splitAmt = totalAmt / tenure
                        val formattedSplit = String.format(java.util.Locale.getDefault(), "%,.2f", splitAmt)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Monthly Installment Due: $currency $formattedSplit / month",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Adjust transaction balance option
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Adjust from Account Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = adjustBalance,
                            onCheckedChange = { adjustBalance = it }
                        )
                    }

                    if (adjustBalance && accounts.isNotEmpty()) {
                        Text(
                            text = "Select Account to Adjust:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { accountMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedAccountForAdjustment?.name ?: "Choose Account")
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = accountMenuExpanded,
                                onDismissRequest = { accountMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            selectedAccountForAdjustment = acc
                                            accountMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = totalAmountInput.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && (title.isNotBlank() || (isDebt && personName.isNotBlank()))) {
                                val finalTitle = title.ifBlank { if (isDebt) "$debtTypeInput from $personName" else "Untitled" }
                                viewModel.addEMI(
                                    title = finalTitle,
                                    total = amount,
                                    paid = 0.0,
                                    dueDate = if (!isDebt) dueDateDisplay else "N/A",
                                    isDebt = isDebt,
                                    debtType = if (isDebt) debtTypeInput else "Installment",
                                    personName = personName,
                                    tenureMonths = tenureMonthsInput.toIntOrNull() ?: 12,
                                    adjustAccountBalance = adjustBalance,
                                    accountId = if (adjustBalance) selectedAccountForAdjustment?.id else null
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text("Create", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}