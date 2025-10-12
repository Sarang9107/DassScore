package com.example.dassscore.features.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dassscore.model.StudentProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProgressScreen(onBack: () -> Unit) {
    // Sample data - in a real app, this would come from a ViewModel or repository
    val studentProgressList = listOf(
        StudentProgress("Student 1", listOf(10, 20, 30)),
        StudentProgress("Student 2", listOf(40, 50, 60)),
        StudentProgress("Student 3", listOf(70, 80, 90))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(studentProgressList) { studentProgress ->
                StudentProgressCard(studentProgress = studentProgress)
            }
        }
    }
}

@Composable
fun StudentProgressCard(studentProgress: StudentProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = studentProgress.studentName,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scores: ${studentProgress.scores.joinToString()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            val average = studentProgress.scores.average()
            Text(
                text = "Average Score: %.2f".format(average),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}