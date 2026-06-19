package com.example.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ─────────────────────────────────────────────────────────────
// ENHANCED TEXT INPUT WITH VALIDATION
// ─────────────────────────────────────────────────────────────
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = isError,
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = if (isError && value.isNotEmpty()) {
                { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(Corners.lg),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(if (isError) Shadows.Light.soft else 0.dp, RoundedCornerShape(Corners.lg)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            singleLine = true
        )
        
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = Spacing.sm, start = Spacing.lg),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COUNTRY BADGE WITH FLAG
// ─────────────────────────────────────────────────────────────
@Composable
fun CountryBadge(
    flag: String,
    countryCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(Corners.sm))
            .background(CountryColors.getColorForCountry(countryCode).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            flag,
            fontSize = (size.value / 1.5).sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// ENHANCED CARD WITH HOVER EFFECT
// ─────────────────────────────────────────────────────────────
@Composable
fun HoverCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(Corners.lg))
            .shadow(
                elevation = if (isPressed) Shadows.Light.medium else Shadows.Light.soft,
                shape = RoundedCornerShape(Corners.lg)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick?.invoke() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(Corners.lg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(Corners.lg)
                        )
                    } else {
                        Modifier
                    }
                ),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────
// STATE BADGE (Success, Error, Warning)
// ─────────────────────────────────────────────────────────────
@Composable
fun StateBadge(
    state: BadgeState,
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = when (state) {
        BadgeState.Success -> StateColors.Light.successLight to SuccessGreen
        BadgeState.Error -> StateColors.Light.errorLight to ErrorRed
        BadgeState.Warning -> StateColors.Light.warningLight to WarningOrange
    }
    val bgColor = if (MaterialTheme.colorScheme.background == BackgroundDark) {
        when (state) {
            BadgeState.Success -> StateColors.Dark.successLight
            BadgeState.Error -> StateColors.Dark.errorLight
            BadgeState.Warning -> StateColors.Dark.warningLight
        }
    } else {
        colors.first
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(Corners.sm))
            .background(bgColor),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.second)
            )
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.second
            )
        }
    }
}

enum class BadgeState {
    Success, Error, Warning
}

// ─────────────────────────────────────────────────────────────
// PROGRESS INDICATOR WITH LABEL
// ─────────────────────────────────────────────────────────────
@Composable
fun LabeledProgressIndicator(
    progress: Float,
    label: String,
    percentage: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                percentage,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(Corners.sm)),
            color = color,
            trackColor = trackColor
        )
    }
}

// ─────────────────────────────────────────────────────────────
// SKELETON LOADER (Placeholder)
// ─────────────────────────────────────────────────────────────
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(Corners.sm))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
    )
}

// ─────────────────────────────────────────────────────────────
// EMPTY STATE COMPONENT
// ─────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(Corners.xl)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                modifier = Modifier
                    .padding(top = Spacing.lg)
                    .height(Sizes.buttonHeight)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// IMPORTS
// ─────────────────────────────────────────────────────────────
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
