package com.example.dassscore.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.R
import com.example.dassscore.data.repository.User
import com.example.dassscore.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        user: User,
        onStartTest: () -> Unit,
        onViewHistory: () -> Unit,
        onProfileClick: () -> Unit,
        onSettingsClick: () -> Unit,
        hasHistory: Boolean,
        onSignOut: () -> Unit
) {
        var showLogoutDialog by remember { mutableStateOf(false) }

        // Premium background gradient
        val backgroundBrush =
                Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )

        if (showLogoutDialog) {
                AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Confirm Logout") },
                        text = { Text("Are you sure you want to sign out?") },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                showLogoutDialog = false
                                                onSignOut()
                                        }
                                ) { Text("Confirm", color = PrimaryBlue) }
                        },
                        dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) {
                                        Text("Cancel", color = Color.Gray)
                                }
                        },
                        containerColor = Color(0xFF1E293B),
                        titleContentColor = Color.White,
                        textContentColor = Color.White.copy(alpha = 0.8f)
                )
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(
                                                text = "Dashboard",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                letterSpacing = 1.sp
                                        )
                                },
                                actions = {
                                        IconButton(onClick = onProfileClick) {
                                                Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Profile",
                                                        tint = Color.White
                                                )
                                        }
                                        IconButton(onClick = onSettingsClick) {
                                                Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "Settings",
                                                        tint = Color.White
                                                )
                                        }
                                        IconButton(onClick = { showLogoutDialog = true }) {
                                                Icon(
                                                        imageVector =
                                                                Icons.AutoMirrored.Filled.Logout,
                                                        contentDescription = "Sign Out",
                                                        tint = Color.White
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
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(backgroundBrush)
                                        .padding(innerPadding)
                ) {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(horizontal = 24.dp)
                                                .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                Spacer(modifier = Modifier.height(24.dp))

                                // User Greeting Card (Glassmorphism)
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                Color.White.copy(alpha = 0.05f)
                                                ),
                                        border =
                                                androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        Color.White.copy(alpha = 0.15f)
                                                )
                                ) {
                                        Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                                // Main visual element inside the card
                                                Box(
                                                        modifier =
                                                                Modifier.size(100.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                                Color.White.copy(
                                                                                        alpha = 0.1f
                                                                                )
                                                                        )
                                                                        .border(
                                                                                2.dp,
                                                                                Color.White.copy(
                                                                                        alpha = 0.2f
                                                                                ),
                                                                                CircleShape
                                                                        )
                                                                        .padding(12.dp),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Image(
                                                                painter =
                                                                        painterResource(
                                                                                id = R.drawable.dass
                                                                        ),
                                                                contentDescription =
                                                                        "DASS Assessment Logo",
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .clip(CircleShape),
                                                                contentScale = ContentScale.Crop
                                                        )
                                                }

                                                Spacer(modifier = Modifier.height(20.dp))

                                                Text(
                                                        text = "Emotional Assessment",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .headlineMedium.copy(
                                                                        fontWeight = FontWeight.Bold
                                                                ),
                                                        color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text =
                                                                "A quick and easy way to measure your emotional state. Ready to begin?",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        textAlign = TextAlign.Center
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Primary Call-to-Action Button
                                Button(
                                        onClick = onStartTest,
                                        modifier = Modifier.fillMaxWidth().height(60.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor = PrimaryBlue,
                                                        contentColor = Color.White
                                                ),
                                        elevation =
                                                ButtonDefaults.buttonElevation(
                                                        defaultElevation = 8.dp
                                                )
                                ) {
                                        Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                                "Start Assessment",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (hasHistory) {
                                        OutlinedButton(
                                                onClick = onViewHistory,
                                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors =
                                                        ButtonDefaults.outlinedButtonColors(
                                                                contentColor = Color.White
                                                        ),
                                                border =
                                                        androidx.compose.foundation.BorderStroke(
                                                                1.dp,
                                                                Color.White.copy(alpha = 0.5f)
                                                        )
                                        ) {
                                                Icon(
                                                        Icons.Default.History,
                                                        contentDescription = "View History"
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                        "View Assessment History",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Features Section
                                Text(
                                        text = "What to expect",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        FeatureRow(
                                                icon = Icons.Default.CheckCircle,
                                                title = "42 Quick Questions",
                                                subtitle = "Simple self-assessment format."
                                        )
                                        FeatureRow(
                                                icon = Icons.Default.Timer,
                                                title = "10-15 Minutes",
                                                subtitle = "A short time for valuable insight."
                                        )
                                        FeatureRow(
                                                icon = Icons.Outlined.Analytics,
                                                title = "Private & Secure",
                                                subtitle = "Provides instant, private results."
                                        )
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                        }
                }
        }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border =
                        androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.1f)
                        )
        ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                                Text(
                                        title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        subtitle,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                )
                        }
                }
        }
}
