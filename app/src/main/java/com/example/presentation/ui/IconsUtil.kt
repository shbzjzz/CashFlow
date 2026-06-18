package com.example.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

object IconsUtil {
    fun getIcon(key: String): ImageVector {
        return when (key.trim().lowercase()) {
            "restaurant", "food" -> Icons.Default.List
            "directions_car", "transport" -> Icons.Default.LocationOn
            "home", "rent" -> Icons.Default.Home
            "receipt", "bills" -> Icons.Default.Menu
            "shopping_cart", "shopping" -> Icons.Default.ShoppingCart
            "fitness_center", "gym" -> Icons.Default.Star
            "payments", "salary" -> Icons.Default.PlayArrow
            "trending_up", "investments" -> Icons.Default.KeyboardArrowUp
            "card_giftcard", "gifts" -> Icons.Default.Favorite
            "warning" -> Icons.Default.Warning
            "lightbulb" -> Icons.Default.Info
            "settings" -> Icons.Default.Settings
            "account_balance", "bank" -> Icons.Default.Home
            "smartphone", "wallet" -> Icons.Default.Lock
            "credit_card" -> Icons.Default.Menu
            "person" -> Icons.Default.Person
            "search" -> Icons.Default.Search
            "add" -> Icons.Default.Add
            "close" -> Icons.Default.Close
            "check" -> Icons.Default.Check
            "delete" -> Icons.Default.Delete
            "edit" -> Icons.Default.Edit
            else -> Icons.Default.MoreVert
        }
    }
}
