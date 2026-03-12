package com.example.dassscore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.dassscore.navigation.DassScoreApp
import com.example.dassscore.ui.theme.DassScoreTheme
import com.example.dassscore.ui.theme.ThemePreferenceHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeHelper = ThemePreferenceHelper(this)

        enableEdgeToEdge()
        setContent {
            val isSystemDark = isSystemInDarkTheme()
            val isDarkMode by themeHelper.themeFlow.collectAsState(initial = isSystemDark)

            DassScoreTheme(darkTheme = isDarkMode) { DassScoreApp() }
        }
    }
}
