package com.example.dassscore.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.ui.theme.ThemePreferenceHelper
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val themeHelper = ThemePreferenceHelper(context)
    val isDarkMode by themeHelper.themeFlow.collectAsState(initial = themeHelper.isDarkMode())
    val coroutineScope = rememberCoroutineScope()

    val backgroundBrush =
            if (isDarkMode) {
                Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            } else {
                Brush.verticalGradient(colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0)))
            }

    val textColor = if (isDarkMode) Color.White else Color.Black
    val cardColor = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White
    val iconColor = if (isDarkMode) Color.White else Color.Black

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text("Settings", fontWeight = FontWeight.Bold, color = textColor)
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = iconColor
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                )
                )
            },
            containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation =
                                if (isDarkMode) CardDefaults.cardElevation(0.dp)
                                else CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector =
                                            if (isDarkMode) Icons.Default.DarkMode
                                            else Icons.Default.LightMode,
                                    contentDescription = "Theme Icon",
                                    tint = iconColor
                            )

                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                    "Dark Mode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                            )
                        }
                        Switch(
                                checked = isDarkMode,
                                onCheckedChange = { isChecked ->
                                    coroutineScope.launch { themeHelper.setDarkMode(isChecked) }
                                },
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor =
                                                        MaterialTheme.colorScheme.primary,
                                                uncheckedThumbColor = Color.Gray,
                                                uncheckedTrackColor = Color.LightGray
                                        )
                        )
                    }
                }
            }
        }
    }
}
