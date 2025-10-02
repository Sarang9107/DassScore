package com.example.dassscore

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.dassscore.screens.AdminDashboardScreen
import com.example.dassscore.screens.DataVisualizationScreen
import com.example.dassscore.screens.*
import com.example.dassscore.ui.theme.DassScoreTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DassScoreTheme {
                DassScoreApp()
            }
        }
    }
}

data class DassResult(
    val depressionScore: Int,
    val anxietyScore: Int,
    val stressScore: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val responses: List<Int>
) {
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}

enum class DassCategory {
    DEPRESSION,
    ANXIETY,
    STRESS
}

data class DassQuestion(
    val id: Int,
    val text: String,
    val category: DassCategory
)

data class ScoreInterpretation(
    val category: String,
    val score: Int,
    val level: String,
    val color: Color,
    val icon: ImageVector,
    val description: String
)

val questions = listOf(
    DassQuestion(0,"I couldn't seem to experience any positive feeling at all", DassCategory.DEPRESSION),
    DassQuestion(1, "I found it difficult to work up the initiative to do things", DassCategory.DEPRESSION),
    DassQuestion(2, "I felt that I had nothing to look forward to", DassCategory.DEPRESSION),
    DassQuestion(3, "I felt down-hearted and blue", DassCategory.DEPRESSION),
    DassQuestion(4, "I was unable to become enthusiastic about anything", DassCategory.DEPRESSION),
    DassQuestion(5, "I felt I wasn't worth much as a person", DassCategory.DEPRESSION),
    DassQuestion(6, "I felt that life was meaningless", DassCategory.DEPRESSION),
    DassQuestion(7, "I found it hard to have energy for things", DassCategory.DEPRESSION),
    DassQuestion(8, "I felt sad and depressed", DassCategory.DEPRESSION),
    DassQuestion(9, "I found myself getting upset by quite trivial things", DassCategory.DEPRESSION),
    DassQuestion(10, "I felt that I had lost interest in just about everything", DassCategory.DEPRESSION),
    DassQuestion(11, "I felt I was pretty worthless", DassCategory.DEPRESSION),
    DassQuestion(12, "I could see nothing in the future to be hopeful about", DassCategory.DEPRESSION),
    DassQuestion(13, "I felt that life wasn't worthwhile", DassCategory.DEPRESSION),
    DassQuestion(14, "I was aware of dryness of my mouth", DassCategory.ANXIETY),
    DassQuestion(15, "I experienced breathing difficulty", DassCategory.ANXIETY),
    DassQuestion(16, "I experienced trembling (eg, in the hands)", DassCategory.ANXIETY),
    DassQuestion(17, "I was worried about situations in which I might panic and make a fool of myself", DassCategory.ANXIETY),
    DassQuestion(18, "I felt I was close to panic", DassCategory.ANXIETY),
    DassQuestion(19, "I was aware of the action of my heart in the absence of physical exertion", DassCategory.ANXIETY),
    DassQuestion(20, "I felt scared without any good reason", DassCategory.ANXIETY),
    DassQuestion(21, "I had a feeling of shakiness (eg, legs going to give way)", DassCategory.ANXIETY),
    DassQuestion(22, "I found myself in situations that made me so anxious I was most relieved when they ended", DassCategory.ANXIETY),
    DassQuestion(23, "I had a feeling of faintness", DassCategory.ANXIETY),
    DassQuestion(24, "I perspired noticeably (eg, hands sweaty) in the absence of high temperatures or physical exertion", DassCategory.ANXIETY),
    DassQuestion(25, "I felt terrified", DassCategory.ANXIETY),
    DassQuestion(26, "I was worried about situations in which I might panic", DassCategory.ANXIETY),
    DassQuestion(27, "I experienced sudden feelings of panic", DassCategory.ANXIETY),
    DassQuestion(28, "I found it hard to wind down", DassCategory.STRESS),
    DassQuestion(29, "I tended to over-react to situations", DassCategory.STRESS),
    DassQuestion(30, "I felt that I was using a lot of nervous energy", DassCategory.STRESS),
    DassQuestion(31, "I found myself getting agitated", DassCategory.STRESS),
    DassQuestion(32, "I found it difficult to relax", DassCategory.STRESS),
    DassQuestion(33, "I was intolerant of anything that kept me from getting on with what I was doing", DassCategory.STRESS),
    DassQuestion(34, "I felt that I was rather touchy", DassCategory.STRESS),
    DassQuestion(35, "I found myself getting upset rather easily", DassCategory.STRESS),
    DassQuestion(36, "I felt that I was getting worked up", DassCategory.STRESS),
    DassQuestion(37, "I found it hard to calm down after something upset me", DassCategory.STRESS),
    DassQuestion(38, "I found it difficult to tolerate interruptions to what I was doing", DassCategory.STRESS),
    DassQuestion(39, "I was in a state of nervous tension", DassCategory.STRESS),
    DassQuestion(40, "I found myself getting impatient when I was delayed in any way", DassCategory.STRESS),
    DassQuestion(41, "I felt that I was rather sensitive", DassCategory.STRESS)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DassScoreApp() {
    val repository = remember { FirebaseRepository() }
    var currentUser by remember { mutableStateOf(repository.getCurrentUser()) }
    // Start with a loading screen to prevent UI flicker while checking the user's role
    var currentScreen by remember { mutableStateOf(if (currentUser != null) "loading" else "auth") }
    var responses by remember { mutableStateOf(List(42) { -1 }) }
    var currentQuestion by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<DassResult?>(null) }
    var savedResults by remember { mutableStateOf(listOf<DassResult>()) }
    var isLoading by remember { mutableStateOf(false) }

    var profileUpdateTrigger by remember { mutableIntStateOf(0) }

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
                val userDocRef = repository.db.collection("users").document(currentUser!!.uid)
                val userDoc = userDocRef.get().await()
                val userRole = userDoc.getString("role") ?: "student"

                if (userRole == "admin") {
                    currentScreen = "admin_dashboard"
                } else {
                    if (userDoc.exists() && !userDoc.getString("name").isNullOrBlank()) {
                        currentScreen = "home"
                    } else {
                        currentScreen = "profile"
                    }
                    repository.getUserDassResults(currentUser!!.uid).fold(
                        onSuccess = { results -> savedResults = results },
                        onFailure = { exception ->
                            Log.e("MainActivity", "Failed to get user DASS results", exception)
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        when (currentScreen) {
            "loading" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            "auth" -> AuthScreen(
                repository = repository,
                onAuthSuccess = { user ->
                    currentUser = user
                }
            )
            "profile" -> {
                currentUser?.let { user ->
                    ProfileScreen(
                        user = user,
                        repository = repository,
                        onProfileSaved = {
                            profileUpdateTrigger++
                        }
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
                        onSignOut = onSignOut,
                        hasHistory = savedResults.isNotEmpty()
                    )
                }
            }
            "test" -> TestScreen(
                questions = questions,
                currentQuestion = currentQuestion,
                responses = responses,
                onAnswerSelected = { answer ->
                    responses = responses.toMutableList().apply { this[currentQuestion] = answer }
                },
                onNext = {
                    if (currentQuestion < questions.size - 1) {
                        currentQuestion++
                    } else {
                        val (depression, anxiety, stress) = calculateDassScores(responses, questions)

                        val resultToSave = DassResult(
                            depressionScore = depression,
                            anxietyScore = anxiety,
                            stressScore = stress,
                            userId = currentUser?.uid ?: "",
                            responses = responses
                        )

                        result = resultToSave

                        CoroutineScope(Dispatchers.Main).launch {
                            repository.saveDassResult(resultToSave).fold(
                                onSuccess = {
                                    Log.d("MainActivity", "Successfully saved DASS result to Firebase.")
                                    savedResults = savedResults + resultToSave
                                },
                                // FIX 3: Add proper error handling to see why saving might fail
                                onFailure = { exception ->
                                    Log.e("MainActivity", "Failed to save DASS result", exception)
                                }
                            )
                        }
                        currentScreen = "result"
                    }
                },
                onPrevious = { if (currentQuestion > 0) currentQuestion-- },
                onExit = { currentScreen = "home" }
            )
            "result" -> ResultScreen(
                result = result!!,
                firebaseRepository = repository,
                onRestart = {
                    currentScreen = "home"
                    result = null
                },
                onViewHistory = { currentScreen = "history" }
            )
            "history" -> HistoryScreen(
                results = savedResults,
                onBack = { currentScreen = "home" },
                onClearHistory = { savedResults = emptyList() },
                repository = repository,
                onResultsUpdated = { updatedResults -> savedResults = updatedResults }
            )
            "admin_dashboard" -> AdminDashboardScreen(
                onSignOut = onSignOut,
                onNavigateToVisualization = {
                    currentScreen = "data_visualization"
                }
            )
            "data_visualization" -> DataVisualizationScreen(
                repository = repository,
                onBack = { currentScreen = "admin_dashboard" }
            )
        }
    }
}

fun calculateDassScores(responses: List<Int>, questions: List<DassQuestion>): Triple<Int, Int, Int> {
    var depressionScore = 0
    var anxietyScore = 0
    var stressScore = 0

    responses.forEachIndexed { index, response ->
        if (response != -1) {
            val question = questions[index]
            when (question.category) {
                DassCategory.DEPRESSION -> depressionScore += response
                DassCategory.ANXIETY -> anxietyScore += response
                DassCategory.STRESS -> stressScore += response
            }
        }
    }
    return Triple(depressionScore, anxietyScore, stressScore)
}