package com.example.dassscore.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
        darkColorScheme(
                primary = PrimaryBlueDark,
                onPrimary = OnPrimaryBlueDark,
                primaryContainer = PrimaryContainerBlueDark,
                onPrimaryContainer = OnPrimaryContainerBlueDark,
                secondary = SecondaryTealDark,
                onSecondary = OnSecondaryTealDark,
                secondaryContainer = SecondaryContainerTealDark,
                onSecondaryContainer = OnSecondaryContainerTealDark,
                tertiary = TertiaryRedDark,
                onTertiary = OnTertiaryRedDark,
                tertiaryContainer = TertiaryContainerRedDark,
                onTertiaryContainer = OnTertiaryContainerRedDark,
                error = ErrorRedDark,
                errorContainer = ErrorContainerRedDark,
                onError = OnErrorRedDark,
                onErrorContainer = OnErrorContainerRedDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
        )

private val LightColorScheme =
        lightColorScheme(
                primary = PrimaryBlue,
                onPrimary = OnPrimaryBlue,
                primaryContainer = PrimaryContainerBlue,
                onPrimaryContainer = OnPrimaryContainerBlue,
                secondary = SecondaryTeal,
                onSecondary = OnSecondaryTeal,
                secondaryContainer = SecondaryContainerTeal,
                onSecondaryContainer = OnSecondaryContainerTeal,
                tertiary = TertiaryRed,
                onTertiary = OnTertiaryRed,
                tertiaryContainer = TertiaryContainerRed,
                onTertiaryContainer = OnTertiaryContainerRed,
                error = ErrorRed,
                errorContainer = ErrorContainerRed,
                onError = OnErrorRed,
                onErrorContainer = OnErrorContainerRed,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
        )

@Composable
fun DassScoreTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
