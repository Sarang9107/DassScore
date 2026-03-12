package com.example.dassscore.features.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.ui.theme.ThemePreferenceHelper
import com.example.dassscore.ui.theme.ThemeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val themeHelper = ThemePreferenceHelper(context)
    val isDarkMode by themeHelper.themeFlow.collectAsState(initial = themeHelper.isDarkMode())
    val coroutineScope = rememberCoroutineScope()

    // Notification state (local only for now)
    var notificationsEnabled by remember { mutableStateOf(true) }

    // App version
    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    val backgroundBrush = ThemeUtils.appBackgroundBrush(isDarkMode)
    val contentColor = ThemeUtils.appContentColor(isDarkMode)
    val subtleColor = ThemeUtils.appSubtleContentColor(isDarkMode)
    val cardColor = ThemeUtils.appCardColor(isDarkMode)
    val iconColor = ThemeUtils.appIconColor(isDarkMode)
    val dividerColor = ThemeUtils.appDividerColor(isDarkMode)

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text("Settings", fontWeight = FontWeight.Bold, color = contentColor)
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
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Appearance Section ---
                Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        color = subtleColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                )

                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation =
                                if (isDarkMode) CardDefaults.cardElevation(0.dp)
                                else CardDefaults.cardElevation(4.dp)
                ) {
                    SettingsToggleRow(
                            icon =
                                    if (isDarkMode) Icons.Default.DarkMode
                                    else Icons.Default.LightMode,
                            label = "Dark Mode",
                            checked = isDarkMode,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch { themeHelper.setDarkMode(isChecked) }
                            },
                            iconColor = iconColor,
                            textColor = contentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Preferences Section ---
                Text(
                        "Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        color = subtleColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                )

                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation =
                                if (isDarkMode) CardDefaults.cardElevation(0.dp)
                                else CardDefaults.cardElevation(4.dp)
                ) {
                    SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            label = "Notifications",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            iconColor = iconColor,
                            textColor = contentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- About Section ---
                Text(
                        "About",
                        style = MaterialTheme.typography.titleSmall,
                        color = subtleColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                )

                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation =
                                if (isDarkMode) CardDefaults.cardElevation(0.dp)
                                else CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        SettingsInfoRow(
                                icon = Icons.Default.Info,
                                label = "App Version",
                                value = appVersion,
                                iconColor = iconColor,
                                textColor = contentColor,
                                subtleColor = subtleColor
                        )
                        HorizontalDivider(
                                color = dividerColor,
                                modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SettingsInfoRow(
                                icon = Icons.Default.Info,
                                label = "Emotion",
                                value = "DASS-42 Assessment",
                                iconColor = iconColor,
                                textColor = contentColor,
                                subtleColor = subtleColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
        icon: ImageVector,
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        iconColor: Color,
        textColor: Color
) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                    label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
            )
        }
        Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors =
                        SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                        )
        )
    }
}

@Composable
private fun SettingsInfoRow(
        icon: ImageVector,
        label: String,
        value: String,
        iconColor: Color,
        textColor: Color,
        subtleColor: Color
) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                    label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
            )
        }
        Text(
                value,
                fontSize = 14.sp,
                color = subtleColor
        )
    }
}
