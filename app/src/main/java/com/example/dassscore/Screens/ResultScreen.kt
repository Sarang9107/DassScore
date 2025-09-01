package com.example.dassscore.screens

import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.DassCategory
import com.example.dassscore.DassQuestion
import com.example.dassscore.DassResult
import com.example.dassscore.FirebaseRepository
import com.example.dassscore.ScoreInterpretation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ResultScreen(
    result: DassResult,
    onRestart: () -> Unit,
    onViewHistory: () -> Unit,
    firebaseRepository: FirebaseRepository // Added FirebaseRepository
) {
    // Save the result when the screen is first composed
    LaunchedEffect(key1 = result) {
        if (result.userId.isNotBlank()) { // Ensure userId is present
            CoroutineScope(Dispatchers.IO).launch {
                val saveResult = firebaseRepository.saveDassResult(
                    userId = result.userId,
                    responses = result.responses,
                    dassScores = result
                )
                saveResult.fold(
                    onSuccess = {
                        Log.d("ResultScreen", "DASS result saved successfully for user ${result.userId}")
                    },
                    onFailure = { exception ->
                        Log.e("ResultScreen", "Failed to save DASS result for user ${result.userId}: ${exception.message}", exception)
                    }
                )
            }
        } else {
            Log.w("ResultScreen", "Skipping save: userId is blank in DassResult.")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your DASS-42 Results",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Assessment completed successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val interpretations = getScoreInterpretations(result)

        items(interpretations) { interpretation ->
            ScoreCard(interpretation)
        }

        item {
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retake", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("History", fontSize = 14.sp)
                }
            }
        }

        item {
            // Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "This assessment is for informational purposes only and should not replace professional medical advice. Please consult a mental health professional for proper diagnosis and treatment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreCard(interpretation: ScoreInterpretation) {
    val animatedScore by animateIntAsState(
        targetValue = interpretation.score,
        animationSpec = tween(1000), label = "score"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = interpretation.color.copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            interpretation.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp),
                            tint = interpretation.color
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        interpretation.category,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    animatedScore.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = interpretation.color
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = interpretation.color.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    interpretation.level,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = interpretation.color
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                interpretation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}


fun getScoreInterpretations(result: DassResult): List<ScoreInterpretation> {
    return listOf(
        ScoreInterpretation(
            category = "Depression",
            score = result.depressionScore,
            level = getDepressionLevel(result.depressionScore),
            color = getDepressionColor(result.depressionScore),
            icon = Icons.Default.Warning,
            description = getDepressionDescription(result.depressionScore)
        ),
        ScoreInterpretation(
            category = "Anxiety",
            score = result.anxietyScore,
            level = getAnxietyLevel(result.anxietyScore),
            color = getAnxietyColor(result.anxietyScore),
            icon = Icons.Default.Warning,
            description = getAnxietyDescription(result.anxietyScore)
        ),
        ScoreInterpretation(
            category = "Stress",
            score = result.stressScore,
            level = getStressLevel(result.stressScore),
            color = getStressColor(result.stressScore),
            icon = Icons.Default.Warning,
            description = getStressDescription(result.stressScore)
        )
    )
}

// DASS-42 Scoring Thresholds (different from DASS-21)
fun getDepressionLevel(score: Int): String = when {
    score <= 9 -> "Normal"
    score <= 13 -> "Mild"
    score <= 20 -> "Moderate"
    score <= 27 -> "Severe"
    else -> "Extremely Severe"
}

fun getAnxietyLevel(score: Int): String = when {
    score <= 7 -> "Normal"
    score <= 9 -> "Mild"
    score <= 14 -> "Moderate"
    score <= 19 -> "Severe"
    else -> "Extremely Severe"
}

fun getStressLevel(score: Int): String = when {
    score <= 14 -> "Normal"
    score <= 18 -> "Mild"
    score <= 25 -> "Moderate"
    score <= 33 -> "Severe"
    else -> "Extremely Severe"
}

fun getDepressionColor(score: Int): Color = when {
    score <= 9 -> Color(0xFF10B981)
    score <= 13 -> Color(0xFFF59E0B)
    score <= 20 -> Color(0xFFEF4444)
    score <= 27 -> Color(0xFFDC2626)
    else -> Color(0xFF991B1B)
}

fun getAnxietyColor(score: Int): Color = when {
    score <= 7 -> Color(0xFF10B981)
    score <= 9 -> Color(0xFFF59E0B)
    score <= 14 -> Color(0xFFEF4444)
    score <= 19 -> Color(0xFFDC2626)
    else -> Color(0xFF991B1B)
}

fun getStressColor(score: Int): Color = when {
    score <= 14 -> Color(0xFF10B981)
    score <= 18 -> Color(0xFFF59E0B)
    score <= 25 -> Color(0xFFEF4444)
    score <= 33 -> Color(0xFFDC2626)
    else -> Color(0xFF991B1B)
}

fun getDepressionDescription(score: Int): String = when {
    score <= 9 -> "Your depression levels are within the normal range. You're experiencing minimal symptoms of depression."
    score <= 13 -> "You may be experiencing mild depression symptoms. Consider monitoring your mood and practicing self-care."
    score <= 20 -> "You're showing moderate levels of depression. It may be beneficial to speak with a mental health professional."
    score <= 27 -> "You're experiencing severe depression symptoms. Professional support is strongly recommended."
    else -> "You're showing extremely severe depression levels. Please seek immediate professional help."
}

fun getAnxietyDescription(score: Int): String = when {
    score <= 7 -> "Your anxiety levels are within the normal range. You're managing stress and worry effectively."
    score <= 9 -> "You may be experiencing mild anxiety symptoms. Consider relaxation techniques and stress management."
    score <= 14 -> "You're showing moderate anxiety levels. Professional guidance could help you develop coping strategies."
    score <= 19 -> "You're experiencing severe anxiety symptoms. Professional support is strongly recommended."
    else -> "You're showing extremely severe anxiety levels. Please seek immediate professional help."
}

fun getStressDescription(score: Int): String = when {
    score <= 7 -> "Your anxiety levels are within the normal range. You're managing stress and worry effectively."
    score <= 9 -> "You may be experiencing mild anxiety symptoms. Consider relaxation techniques and stress management."
    score <= 14 -> "You're showing moderate anxiety levels. Professional guidance could help you develop coping strategies."
    score <= 19 -> "You're experiencing severe anxiety symptoms. Professional support is strongly recommended."
    else -> "You're showing extremely severe anxiety levels. Please seek immediate professional help."
}