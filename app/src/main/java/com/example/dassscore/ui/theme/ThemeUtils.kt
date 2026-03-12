package com.example.dassscore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Centralized theme-aware color helpers so every screen can adapt to dark/light mode.
 */
object ThemeUtils {

    // Dark gradient colors
    private val DarkGradientColors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    // Light gradient colors
    private val LightGradientColors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))

    fun appBackgroundBrush(isDark: Boolean): Brush =
        Brush.verticalGradient(
            colors = if (isDark) DarkGradientColors else LightGradientColors
        )

    fun appContentColor(isDark: Boolean): Color =
        if (isDark) Color.White else Color(0xFF1E293B)

    fun appSubtleContentColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)

    fun appCardColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.05f) else Color.White

    fun appCardBorderColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0)

    fun appIconColor(isDark: Boolean): Color =
        if (isDark) Color.White else Color(0xFF334155)

    fun appDialogContainerColor(isDark: Boolean): Color =
        if (isDark) Color(0xFF1E293B) else Color.White

    fun appDialogContentColor(isDark: Boolean): Color =
        if (isDark) Color.White else Color(0xFF1E293B)

    fun appCircleBackgroundColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)

    fun appCircleBorderColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1)

    fun appTextFieldBorderColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFCBD5E1)

    fun appTextFieldLabelColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)

    fun appTextFieldTextColor(isDark: Boolean): Color =
        if (isDark) Color.White else Color(0xFF1E293B)

    fun appDividerColor(isDark: Boolean): Color =
        if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0)
}

/**
 * Composable helper to get whether the app is in dark mode from preferences.
 */
@Composable
fun isAppDarkTheme(): Boolean {
    val context = LocalContext.current
    val themeHelper = ThemePreferenceHelper(context)
    val isDarkMode by themeHelper.themeFlow.collectAsState(initial = themeHelper.isDarkMode())
    return isDarkMode
}
