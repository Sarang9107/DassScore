package com.example.dassscore.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.ui.theme.PrimaryBlue
import com.example.dassscore.ui.theme.PrimaryBlueDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(repository: FirebaseRepository, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Premium background gradient
    val backgroundBrush =
            Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
            )

    Box(
            modifier =
                    Modifier.fillMaxSize().background(backgroundBrush).pointerInput(Unit) {
                        detectTapGestures(
                                onTap = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                        )
                    }
    ) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(horizontal = 28.dp, vertical = 24.dp)
                                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {

            // Title
            AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                            text = "Reset Password",
                            style =
                                    MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                    ),
                            color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = "Enter your email address to receive a password reset link",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphism Card for Input
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                    border =
                            androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.15f)
                            )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Email Field
                    OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = ""
                                successMessage = ""
                            },
                            label = {
                                Text("Email Address", color = Color.White.copy(alpha = 0.7f))
                            },
                            leadingIcon = {
                                Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryBlueDark,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = PrimaryBlueDark
                                    )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Messages
                    if (errorMessage.isNotEmpty()) {
                        Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (successMessage.isNotEmpty()) {
                        Text(
                                text = successMessage,
                                color = Color(0xFF4CAF50), // Green point
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Main Action Button
                    Button(
                            onClick = {
                                if (email.isBlank()) {
                                    errorMessage = "Please enter your email address"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = ""
                                successMessage = ""
                                coroutineScope.launch {
                                    repository
                                            .sendPasswordResetEmail(email)
                                            .fold(
                                                    onSuccess = {
                                                        isLoading = false
                                                        successMessage =
                                                                "Password reset email sent! Please check your inbox and spam folder."
                                                    },
                                                    onFailure = { exception ->
                                                        isLoading = false
                                                        errorMessage =
                                                                exception.message
                                                                        ?: "Failed to send reset email. Make sure the email is registered."
                                                    }
                                            )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading,
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = PrimaryBlue,
                                            contentColor = Color.White
                                    )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Reset Link", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login Link
            TextButton(onClick = onBack) {
                Text("Back to Login", fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
            }
        }
    }
}
