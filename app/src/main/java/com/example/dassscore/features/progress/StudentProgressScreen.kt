package com.example.dassscore.features.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.features.result.getAnxietyLevel
import com.example.dassscore.features.result.getDepressionLevel
import com.example.dassscore.features.result.getStressLevel
import com.example.dassscore.model.DassResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProgressScreen(
        userId: String,
        onBack: () -> Unit,
        repository: FirebaseRepository = com.example.dassscore.data.repository.FirebaseRepository()
) {
    val viewModel: StudentProgressViewModel =
            viewModel(
                    factory =
                            object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return StudentProgressViewModel(repository) as T
                                }
                            }
            )

    val studentResults by viewModel.studentResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(userId) { viewModel.fetchResults(userId) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Progress History") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
        } else if (error != null) {
            Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) { Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error) }
        } else if (studentResults.isEmpty()) {
            Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                Text(
                        text = "No test results found for this student.",
                        style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) { items(studentResults) { result -> StudentDassResultCard(result = result) } }
        }
    }
}

@Composable
fun StudentDassResultCard(result: DassResult) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dateStr =
                    SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                            .format(Date(result.timestamp))
            Text(
                    text = "Assessment on $dateStr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val depLvl = getDepressionLevel(result.depressionScore)
            val anxLvl = getAnxietyLevel(result.anxietyScore)
            val strLvl = getStressLevel(result.stressScore)

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Depression:", fontWeight = FontWeight.SemiBold)
                Text(
                        "$depLvl (${result.depressionScore})",
                        color =
                                if (depLvl in listOf("Severe", "Extremely Severe"))
                                        MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Anxiety:", fontWeight = FontWeight.SemiBold)
                Text(
                        "$anxLvl (${result.anxietyScore})",
                        color =
                                if (anxLvl in listOf("Severe", "Extremely Severe"))
                                        MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Stress:", fontWeight = FontWeight.SemiBold)
                Text(
                        "$strLvl (${result.stressScore})",
                        color =
                                if (strLvl in listOf("Severe", "Extremely Severe"))
                                        MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
