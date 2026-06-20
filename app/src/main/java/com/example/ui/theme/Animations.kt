package com.example.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────
// EASING CURVES
// ─────────────────────────────────────────────────────────────
object EasingCurves {
    val fastOutSlowIn = FastOutSlowInEasing
    val standard = FastOutSlowInEasing
}

// ─────────────────────────────────────────────────────────────
// SCREEN TRANSITION ANIMATIONS
// ─────────────────────────────────────────────────────────────
@Composable
fun slideInFromRightTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { 1000 },
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMillis = 300))
}

@Composable
fun slideOutToLeftTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -1000 },
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(durationMillis = 300))
}

@Composable
fun slideInFromLeftTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -1000 },
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMillis = 300))
}

@Composable
fun slideOutToRightTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { 1000 },
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(durationMillis = 300))
}

@Composable
fun fadeInTransition(): EnterTransition {
    return fadeIn(animationSpec = tween(durationMillis = 300))
}

@Composable
fun fadeOutTransition(): ExitTransition {
    return fadeOut(animationSpec = tween(durationMillis = 300))
}

@Composable
fun scaleInTransition(): EnterTransition {
    return scaleIn(
        initialScale = 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
    ) + fadeIn(animationSpec = tween(durationMillis = 200))
}

// ─────────────────────────────────────────────────────────────
// CARD INTERACTION ANIMATIONS
// ─────────────────────────────────────────────────────────────
@Composable
fun cardScaleModifier(isPressed: Boolean): Modifier {
    val scale = if (isPressed) 0.98f else 1.0f
    return Modifier.animateContentSize(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
}

// ─────────────────────────────────────────────────────────────
// CONTENT VISIBILITY ANIMATIONS
// ─────────────────────────────────────────────────────────────
@Composable
fun AnimatedVisibilityCompact(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────
// SHIMMER LOADING ANIMATION
// ─────────────────────────────────────────────────────────────
@Composable
fun shimmerLoadingAnimation(
    targetValue: Float = 1000f,
    durationMillis: Int = 1500
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    ).value
}

// ─────────────────────────────────────────────────────────────
// NUMBER COUNTER ANIMATION (for balance displays)
// ─────────────────────────────────────────────────────────────
@Composable
fun animateNumberAsString(
    value: Double,
    durationMillis: Int = 600,
    formatValue: (Double) -> String = { String.format("%.2f", it) }
): String {
    val animatedValue = animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "number_animation"
    ).value
    return formatValue(animatedValue.toDouble())
}

// ─────────────────────────────────────────────────────────────
// IMPORTS NEEDED
// ─────────────────────────────────────────────────────────────
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.rememberInfiniteTransition
