package com.example.dassscore.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.example.dassscore.R
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.data.repository.User
import com.example.dassscore.ui.theme.PrimaryBlue
import com.example.dassscore.ui.theme.PrimaryBlueDark
import com.example.dassscore.ui.theme.ThemeUtils
import com.example.dassscore.ui.theme.isAppDarkTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
        repository: FirebaseRepository,
        onAuthSuccess: (User) -> Unit,
        onForgotPasswordClick: () -> Unit
) {
        var isLogin by remember { mutableStateOf(true) }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var successMessage by remember { mutableStateOf("") }

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val context = LocalContext.current
        val credentialManager = remember { CredentialManager.create(context) }
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

        val isDark = isAppDarkTheme()
        val backgroundBrush = ThemeUtils.appBackgroundBrush(isDark)
        val contentColor = ThemeUtils.appContentColor(isDark)
        val subtleColor = ThemeUtils.appSubtleContentColor(isDark)
        val cardColor = ThemeUtils.appCardColor(isDark)
        val cardBorderColor = ThemeUtils.appCardBorderColor(isDark)
        val fieldBorderColor = ThemeUtils.appTextFieldBorderColor(isDark)
        val fieldLabelColor = ThemeUtils.appTextFieldLabelColor(isDark)
        val fieldTextColor = ThemeUtils.appTextFieldTextColor(isDark)
        val accentColor = if (isDark) PrimaryBlueDark else PrimaryBlue

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

                        // Modern Logo Presentation
                        Box(
                                modifier =
                                        Modifier.size(120.dp)
                                                .clip(CircleShape)
                                                .background(ThemeUtils.appCircleBackgroundColor(isDark))
                                                .border(
                                                        2.dp,
                                                        ThemeUtils.appCircleBorderColor(isDark),
                                                        CircleShape
                                                )
                                                .padding(16.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                Image(
                                        painter =
                                                painterResource(
                                                        id = R.drawable.dass
                                                ),
                                        contentDescription = "App Logo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Dynamic Title
                        AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(1000)),
                        ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                                text =
                                                        if (isLogin) "Welcome Back"
                                                        else "Create Account",
                                                style =
                                                        MaterialTheme.typography.headlineLarge.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                letterSpacing = 1.sp
                                                        ),
                                                color = contentColor
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        if (isLogin)
                                                                "Sign in to continue your wellness journey"
                                                        else "Join to track your mental wellness",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = subtleColor,
                                                textAlign = TextAlign.Center
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Glassmorphism Card for Inputs
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor = cardColor
                                        ),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                cardBorderColor
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
                                                        Text(
                                                                "Email",
                                                                color = fieldLabelColor
                                                        )
                                                },
                                                leadingIcon = {
                                                        Icon(
                                                                Icons.Default.Email,
                                                                contentDescription = null,
                                                                tint = fieldLabelColor
                                                        )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                colors =
                                                        OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = accentColor,
                                                                unfocusedBorderColor = fieldBorderColor,
                                                                focusedTextColor = fieldTextColor,
                                                                unfocusedTextColor = fieldTextColor,
                                                                cursorColor = accentColor
                                                        )
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Password Field
                                        OutlinedTextField(
                                                value = password,
                                                onValueChange = {
                                                        password = it
                                                        errorMessage = ""
                                                },
                                                label = {
                                                        Text(
                                                                "Password",
                                                                color = fieldLabelColor
                                                        )
                                                },
                                                leadingIcon = {
                                                        Icon(
                                                                Icons.Default.Lock,
                                                                contentDescription = null,
                                                                tint = fieldLabelColor
                                                        )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                visualTransformation =
                                                        PasswordVisualTransformation(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors =
                                                        OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = accentColor,
                                                                unfocusedBorderColor = fieldBorderColor,
                                                                focusedTextColor = fieldTextColor,
                                                                unfocusedTextColor = fieldTextColor,
                                                                cursorColor = accentColor
                                                        )
                                        )

                                        if (!isLogin) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                OutlinedTextField(
                                                        value = confirmPassword,
                                                        onValueChange = {
                                                                confirmPassword = it
                                                                errorMessage = ""
                                                        },
                                                        label = {
                                                                Text(
                                                                        "Confirm Password",
                                                                        color = fieldLabelColor
                                                                )
                                                        },
                                                        leadingIcon = {
                                                                Icon(
                                                                        Icons.Default.Lock,
                                                                        contentDescription = null,
                                                                        tint = fieldLabelColor
                                                                )
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true,
                                                        visualTransformation =
                                                                PasswordVisualTransformation(),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors =
                                                                OutlinedTextFieldDefaults.colors(
                                                                        focusedBorderColor = accentColor,
                                                                        unfocusedBorderColor = fieldBorderColor,
                                                                        focusedTextColor = fieldTextColor,
                                                                        unfocusedTextColor = fieldTextColor,
                                                                        cursorColor = accentColor
                                                                )
                                                )
                                        }

                                        // Forgot Password Link
                                        if (isLogin) {
                                                Box(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        contentAlignment = Alignment.CenterEnd
                                                ) {
                                                        TextButton(
                                                                onClick = onForgotPasswordClick
                                                        ) {
                                                                Text(
                                                                        "Forgot Password?",
                                                                        color = accentColor,
                                                                        fontWeight =
                                                                                FontWeight.SemiBold
                                                                )
                                                        }
                                                }
                                        } else {
                                                Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        // Messages
                                        if (errorMessage.isNotEmpty()) {
                                                Text(
                                                        text = errorMessage,
                                                        color = MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        if (successMessage.isNotEmpty()) {
                                                Text(
                                                        text = successMessage,
                                                        color = Color(0xFF4CAF50),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Main Action Button
                                        Button(
                                                onClick = {
                                                        if (email.isBlank() || password.isBlank()) {
                                                                errorMessage =
                                                                        "Please fill in all fields"
                                                                return@Button
                                                        }
                                                        if (!isLogin && password != confirmPassword
                                                        ) {
                                                                errorMessage =
                                                                        "Passwords do not match"
                                                                return@Button
                                                        }
                                                        if (password.length < 6) {
                                                                errorMessage =
                                                                        "Password must be at least 6 characters"
                                                                return@Button
                                                        }
                                                        isLoading = true
                                                        coroutineScope.launch {
                                                                val result =
                                                                        if (isLogin) {
                                                                                repository
                                                                                        .signInWithEmail(
                                                                                                email,
                                                                                                password
                                                                                        )
                                                                        } else {
                                                                                repository
                                                                                        .signUpWithEmail(
                                                                                                email,
                                                                                                password
                                                                                        )
                                                                        }
                                                                result.fold(
                                                                        onSuccess = { user ->
                                                                                isLoading = false
                                                                                onAuthSuccess(user)
                                                                        },
                                                                        onFailure = { exception ->
                                                                                isLoading = false
                                                                                errorMessage =
                                                                                        exception
                                                                                                .message
                                                                                                ?: "Authentication failed"
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
                                                        Text(
                                                                if (isLogin) "Login"
                                                                else "Register",
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Toggle Login/Signup
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text =
                                                if (isLogin) "Don't have an account?"
                                                else "Already have an account?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = subtleColor
                                )
                                TextButton(
                                        onClick = {
                                                isLogin = !isLogin
                                                errorMessage = ""
                                                successMessage = ""
                                                confirmPassword = ""
                                        }
                                ) {
                                        Text(
                                                if (isLogin) "Sign Up" else "Sign In",
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                                Box(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .height(1.dp)
                                                        .background(ThemeUtils.appDividerColor(isDark))
                                )
                                Text(
                                        " OR ",
                                        color = subtleColor,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Box(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .height(1.dp)
                                                        .background(ThemeUtils.appDividerColor(isDark))
                                )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Google Sign In Button
                        Button(
                                onClick = {
                                        isLoading = true
                                        errorMessage = ""
                                        coroutineScope.launch {
                                                try {
                                                        val webClientId =
                                                                context.getString(
                                                                        R.string
                                                                                .default_web_client_id
                                                                )

                                                        // Helper to handle the credential result
                                                        suspend fun handleCredentialResult(
                                                                result: androidx.credentials.GetCredentialResponse
                                                        ) {
                                                                val credential = result.credential
                                                                if (credential.type ==
                                                                        GoogleIdTokenCredential
                                                                                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                                                ) {
                                                                        val googleIdTokenCredential =
                                                                                GoogleIdTokenCredential
                                                                                        .createFrom(
                                                                                                credential
                                                                                                        .data
                                                                                        )
                                                                        repository
                                                                                .signInWithGoogle(
                                                                                        googleIdTokenCredential
                                                                                                .idToken
                                                                                )
                                                                                .fold(
                                                                                        onSuccess = {
                                                                                                user ->
                                                                                                isLoading =
                                                                                                        false
                                                                                                onAuthSuccess(
                                                                                                        user
                                                                                                )
                                                                                        },
                                                                                        onFailure = {
                                                                                                exception ->
                                                                                                isLoading =
                                                                                                        false
                                                                                                errorMessage =
                                                                                                        exception
                                                                                                                .message
                                                                                                                ?: "Google Sign In Failed"
                                                                                        }
                                                                                )
                                                                } else {
                                                                        isLoading = false
                                                                        errorMessage =
                                                                                "Unexpected credential type"
                                                                }
                                                        }

                                                        try {
                                                                // First try seamless sign-in
                                                                val googleIdOption =
                                                                        GetGoogleIdOption.Builder()
                                                                                .setFilterByAuthorizedAccounts(
                                                                                        false
                                                                                )
                                                                                .setServerClientId(
                                                                                        webClientId
                                                                                )
                                                                                .setAutoSelectEnabled(
                                                                                        true
                                                                                )
                                                                                .build()

                                                                val request =
                                                                        GetCredentialRequest
                                                                                .Builder()
                                                                                .addCredentialOption(
                                                                                        googleIdOption
                                                                                )
                                                                                .build()

                                                                val result =
                                                                        credentialManager
                                                                                .getCredential(
                                                                                        request =
                                                                                                request,
                                                                                        context =
                                                                                                context
                                                                                )
                                                                handleCredentialResult(result)
                                                        } catch (
                                                                e: NoCredentialException
                                                        ) {
                                                                // Fallback: show the full Google
                                                                // account picker
                                                                val signInWithGoogleOption =
                                                                        GetSignInWithGoogleOption
                                                                                .Builder(
                                                                                        webClientId
                                                                                )
                                                                                .build()

                                                                val request =
                                                                        GetCredentialRequest
                                                                                .Builder()
                                                                                .addCredentialOption(
                                                                                        signInWithGoogleOption
                                                                                )
                                                                                .build()

                                                                val result =
                                                                        credentialManager
                                                                                .getCredential(
                                                                                        request =
                                                                                                request,
                                                                                        context =
                                                                                                context
                                                                                )
                                                                handleCredentialResult(result)
                                                        }
                                                } catch (e: Exception) {
                                                        isLoading = false
                                                        errorMessage =
                                                                e.message ?: "Google Sign In Failed"
                                                }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isLoading,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = if (isDark) Color.White else Color(0xFFF1F5F9),
                                                contentColor = Color(0xFF1F1F1F)
                                        ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                                Image(
                                        painter = painterResource(id = R.drawable.ic_google),
                                        contentDescription = "Google Logo",
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                        "Continue with Google",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                }
        }
}
