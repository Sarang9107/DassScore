package com.example.dassscore.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.features.admin.AdminDashboardScreen
import com.example.dassscore.features.auth.AuthScreen
import com.example.dassscore.features.auth.ForgotPasswordScreen
import com.example.dassscore.features.history.HistoryScreen
import com.example.dassscore.features.home.HomeScreen
import com.example.dassscore.features.profile.ProfileScreen
import com.example.dassscore.features.profile.StudentProfileScreen
import com.example.dassscore.features.progress.DataVisualizationScreen
import com.example.dassscore.features.progress.StudentProgressScreen
import com.example.dassscore.features.result.ResultScreen
import com.example.dassscore.features.settings.SettingsScreen
import com.example.dassscore.features.test.TestScreen
import com.example.dassscore.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DassScoreApp() {
        val repository = remember { FirebaseRepository() }
        var currentUser by remember { mutableStateOf(repository.getCurrentUser()) }
        // Start with a loading screen to prevent UI flicker while checking the user's role
        var currentScreen by remember {
                mutableStateOf(if (currentUser != null) "loading" else "auth")
        }
        var responses by remember { mutableStateOf(List(42) { -1 }) }
        var currentQuestion by remember { mutableIntStateOf(0) }
        var result by remember { mutableStateOf<DassResult?>(null) }
        var savedResults by remember { mutableStateOf(listOf<DassResult>()) }
        var isLoading by remember { mutableStateOf(false) }

        var profileUpdateTrigger by remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()

        val onSignOut: () -> Unit = {
                repository.signOut()
                responses = List(42) { -1 }
                currentQuestion = 0
                result = null
                savedResults = emptyList()
                currentUser = null
        }

        LaunchedEffect(currentUser, profileUpdateTrigger) {
                if (currentUser != null) {
                        isLoading = true
                        try {
                                val userDocRef =
                                        repository
                                                .db
                                                .collection("users")
                                                .document(currentUser!!.uid)
                                val userDoc = userDocRef.get().await()
                                val userRole = userDoc.getString("role") ?: "student"

                                if (userRole == "admin") {
                                        currentScreen = "admin_dashboard"
                                } else {
                                        if (userDoc.exists() &&
                                                        !userDoc.getString("name").isNullOrBlank()
                                        ) {
                                                currentScreen = "home"
                                        } else {
                                                currentScreen = "profile"
                                        }
                                        repository
                                                .getUserDassResults(currentUser!!.uid)
                                                .fold(
                                                        onSuccess = { results ->
                                                                savedResults = results
                                                        },
                                                        onFailure = { exception ->
                                                                Log.e(
                                                                        "MainActivity",
                                                                        "Failed to get user DASS results",
                                                                        exception
                                                                )
                                                        }
                                                )
                                }
                        } catch (e: Exception) {
                                onSignOut()
                        } finally {
                                isLoading = false
                        }
                } else {
                        currentScreen = "auth"
                }
        }

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
        ) {
                when (currentScreen) {
                        "loading" -> {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                        }
                        "auth" ->
                                AuthScreen(
                                        repository = repository,
                                        onAuthSuccess = { user -> currentUser = user },
                                        onForgotPasswordClick = {
                                                currentScreen = "forgot_password"
                                        }
                                )
                        "forgot_password" ->
                                ForgotPasswordScreen(
                                        repository = repository,
                                        onBack = { currentScreen = "auth" }
                                )
                        "profile" -> {
                                currentUser?.let { user ->
                                        ProfileScreen(
                                                user = user,
                                                repository = repository,
                                                onProfileSaved = { profileUpdateTrigger++ }
                                        )
                                }
                        }
                        "home" -> {
                                currentUser?.let { user ->
                                        HomeScreen(
                                                user = user,
                                                onStartTest = {
                                                        currentScreen = "test"
                                                        responses = List(42) { -1 }
                                                        currentQuestion = 0
                                                },
                                                onViewHistory = { currentScreen = "history" },
                                                onProfileClick = {
                                                        currentScreen = "student_profile"
                                                },
                                                onSettingsClick = { currentScreen = "settings" },
                                                onSignOut = onSignOut,
                                                hasHistory = savedResults.isNotEmpty()
                                        )
                                }
                        }
                        "test" ->
                                TestScreen(
                                        questions = questions,
                                        currentQuestion = currentQuestion,
                                        responses = responses,
                                        onAnswerSelected = { answer ->
                                                responses =
                                                        responses.toMutableList().apply {
                                                                this[currentQuestion] = answer
                                                        }
                                        },
                                        onNext = {
                                                if (currentQuestion < questions.size - 1) {
                                                        currentQuestion++
                                                } else {
                                                        val (depression, anxiety, stress) =
                                                                calculateDassScores(
                                                                        responses,
                                                                        questions
                                                                )

                                                        val resultToSave =
                                                                DassResult(
                                                                        depressionScore =
                                                                                depression,
                                                                        anxietyScore = anxiety,
                                                                        stressScore = stress,
                                                                        userId = currentUser?.uid
                                                                                        ?: "",
                                                                        responses = responses
                                                                )

                                                        result = resultToSave

                                                        coroutineScope.launch {
                                                                repository
                                                                        .saveDassResult(
                                                                                resultToSave
                                                                        )
                                                                        .fold(
                                                                                onSuccess = {
                                                                                        Log.d(
                                                                                                "MainActivity",
                                                                                                "Successfully saved DASS result to Firebase."
                                                                                        )
                                                                                        savedResults =
                                                                                                savedResults +
                                                                                                        resultToSave
                                                                                },
                                                                                // FIX 3: Add proper
                                                                                // error handling to
                                                                                // see
                                                                                // why saving might
                                                                                // fail
                                                                                onFailure = {
                                                                                        exception ->
                                                                                        Log.e(
                                                                                                "MainActivity",
                                                                                                "Failed to save DASS result",
                                                                                                exception
                                                                                        )
                                                                                }
                                                                        )
                                                        }
                                                        currentScreen = "result"
                                                }
                                        },
                                        onPrevious = { if (currentQuestion > 0) currentQuestion-- },
                                        onExit = { currentScreen = "home" }
                                )
                        "result" ->
                                ResultScreen(
                                        result = result!!,
                                        firebaseRepository = repository,
                                        onRestart = {
                                                currentScreen = "home"
                                                result = null
                                        },
                                        onViewHistory = { currentScreen = "history" }
                                )
                        "history" ->
                                HistoryScreen(
                                        results = savedResults,
                                        onBack = { currentScreen = "home" },
                                        onClearHistory = { savedResults = emptyList() },
                                        repository = repository,
                                        onResultsUpdated = { updatedResults ->
                                                savedResults = updatedResults
                                        }
                                )
                        "admin_dashboard" ->
                                AdminDashboardScreen(
                                        onSignOut = onSignOut,
                                        onNavigateToVisualization = {
                                                currentScreen = "data_visualization"
                                        },
                                        // Temporarily keep this if they want a general view, but
                                        // ideally it should pass ID
                                        onNavigateToStudentProgress = {
                                                currentScreen = "student_progress"
                                        },
                                        onStudentClick = { userId ->
                                                currentScreen = "student_progress/$userId"
                                        }
                                )
                        // Handle parameterized student progress navigation
                        is String -> {
                                when {
                                        currentScreen.startsWith("student_progress/") -> {
                                                val userId =
                                                        currentScreen.substringAfter(
                                                                "student_progress/"
                                                        )
                                                StudentProgressScreen(
                                                        userId = userId,
                                                        onBack = {
                                                                currentScreen = "admin_dashboard"
                                                        }
                                                )
                                        }
                                        currentScreen == "data_visualization" -> {
                                                DataVisualizationScreen(
                                                        repository = repository,
                                                        onBack = {
                                                                currentScreen = "admin_dashboard"
                                                        },
                                                        onStudentClick = { userId ->
                                                                currentScreen =
                                                                        "student_progress/$userId"
                                                        }
                                                )
                                        }
                                        currentScreen == "student_progress" -> {
                                                // Fallback if no specific ID is provided
                                                StudentProgressScreen(
                                                        userId = currentUser?.uid ?: "",
                                                        onBack = {
                                                                currentScreen = "admin_dashboard"
                                                        }
                                                )
                                        }
                                        currentScreen == "settings" -> {
                                                SettingsScreen(onBack = { currentScreen = "home" })
                                        }
                                        currentScreen == "student_profile" -> {
                                                currentUser?.let { user ->
                                                        StudentProfileScreen(
                                                                user = user,
                                                                repository = repository,
                                                                onBack = { currentScreen = "home" }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}
