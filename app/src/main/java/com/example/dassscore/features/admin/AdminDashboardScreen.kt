package com.example.dassscore.features.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.dassscore.ui.theme.ThemePreferenceHelper
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
        onSignOut: () -> Unit,
        onNavigateToVisualization: () -> Unit,
        onNavigateToStudentProgress: () -> Unit,
        onStudentClick: (String) -> Unit,
        repository: com.example.dassscore.data.repository.FirebaseRepository =
                com.example.dassscore.data.repository.FirebaseRepository()
) {
        var allResults by
                androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf<
                                List<com.example.dassscore.model.DassResult>?>(null)
                }
        var allProfiles by
                androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf<
                                Map<
                                        String,
                                        com.example.dassscore.features.progress.StudentProfileData>?>(
                                null
                        )
                }
        var aggregatedData by
                androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf<
                                com.example.dassscore.features.progress.AggregatedDassData?>(null)
                }
        var isLoading by
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
        var errorMessage by
                androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf<String?>(null)
                }

        val departments =
                listOf(
                        "All",
                        "Computer Engineering",
                        "Information Technology",
                        "Computer Science and Bussiness System",
                        "Automation and Robotics",
                        "Mechanical",
                        "Civil",
                        "Electronics & Telecommunication",
                        "Electrical"
                )
        val years = listOf("All", "First Year", "Second Year", "Third Year", "Fourth Year")

        var selectedDepartment by
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("All") }
        var isDepartmentExpanded by
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

        var selectedYear by
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("All") }
        var isYearExpanded by
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

        androidx.compose.runtime.LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                        val resultsDeferred = async { repository.getAllDassResults() }
                        val profilesDeferred = async { repository.getAllUserProfiles() }

                        val resultsResponse = resultsDeferred.await()
                        val profilesResponse = profilesDeferred.await()

                        withContext(Dispatchers.Main) {
                                if (resultsResponse.isSuccess && profilesResponse.isSuccess) {
                                        val rawResults = resultsResponse.getOrNull() ?: emptyList()
                                        val rawProfiles = profilesResponse.getOrNull() ?: emptyMap()

                                        val parsedProfiles =
                                                rawProfiles.mapValues { (userId, profileMap) ->
                                                        com.example.dassscore.features.progress
                                                                .StudentProfileData(
                                                                        userId = userId,
                                                                        name =
                                                                                profileMap[
                                                                                        "name"] as?
                                                                                        String
                                                                                        ?: "Unknown",
                                                                        className =
                                                                                profileMap[
                                                                                        "className"] as?
                                                                                        String
                                                                                        ?: "N/A",
                                                                        division =
                                                                                profileMap[
                                                                                        "division"] as?
                                                                                        String
                                                                                        ?: "N/A",
                                                                        rbt =
                                                                                profileMap[
                                                                                        "rbt"] as?
                                                                                        String
                                                                                        ?: "N/A",
                                                                        mobileNumber =
                                                                                profileMap[
                                                                                        "mobileNumber"] as?
                                                                                        String
                                                                                        ?: "N/A",
                                                                        parentContactNumber =
                                                                                profileMap[
                                                                                        "parentContactNumber"] as?
                                                                                        String
                                                                                        ?: "N/A"
                                                                )
                                                }

                                        allResults = rawResults
                                        allProfiles = parsedProfiles
                                        isLoading = false
                                } else {
                                        errorMessage = "Failed to load data."
                                        isLoading = false
                                }
                        }
                }
        }

        androidx.compose.runtime.LaunchedEffect(
                allResults,
                allProfiles,
                selectedDepartment,
                selectedYear
        ) {
                val currentResults = allResults
                val currentProfiles = allProfiles
                if (currentResults != null && currentProfiles != null) {
                        aggregatedData =
                                com.example.dassscore.features.progress.filterAndAggregateData(
                                        currentResults,
                                        currentProfiles,
                                        selectedDepartment,
                                        selectedYear
                                )
                }
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                                actions = {
                                        val context = LocalContext.current
                                        val themeHelper = ThemePreferenceHelper(context)
                                        val isDarkMode by themeHelper.themeFlow.collectAsState(initial = themeHelper.isDarkMode())
                                        val scope = rememberCoroutineScope()
                                        IconButton(onClick = {
                                                scope.launch { themeHelper.setDarkMode(!isDarkMode) }
                                        }) {
                                                Icon(
                                                        if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                        contentDescription = "Toggle Theme"
                                                )
                                        }
                                        IconButton(onClick = onSignOut) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.Logout,
                                                        contentDescription = "Sign Out"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant
                                        )
                        )
                }
        ) { paddingValues ->
                androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        item {
                                Text(
                                        "Welcome, Admin!",
                                        style = MaterialTheme.typography.headlineMedium
                                )
                        }

                        item {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        // Department Dropdown
                                        androidx.compose.material3.ExposedDropdownMenuBox(
                                                expanded = isDepartmentExpanded,
                                                onExpandedChange = { isDepartmentExpanded = it },
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                androidx.compose.material3.OutlinedTextField(
                                                        value = selectedDepartment,
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = { Text("Department") },
                                                        trailingIcon = {
                                                                androidx.compose.material3
                                                                        .ExposedDropdownMenuDefaults
                                                                        .TrailingIcon(
                                                                                expanded =
                                                                                        isDepartmentExpanded
                                                                        )
                                                        },
                                                        modifier = Modifier.menuAnchor()
                                                )
                                                ExposedDropdownMenu(
                                                        expanded = isDepartmentExpanded,
                                                        onDismissRequest = {
                                                                isDepartmentExpanded = false
                                                        }
                                                ) {
                                                        departments.forEach { department ->
                                                                DropdownMenuItem(
                                                                        text = { Text(department) },
                                                                        onClick = {
                                                                                selectedDepartment =
                                                                                        department
                                                                                isDepartmentExpanded =
                                                                                        false
                                                                        }
                                                                )
                                                        }
                                                }
                                        }

                                        // Year Dropdown
                                        androidx.compose.material3.ExposedDropdownMenuBox(
                                                expanded = isYearExpanded,
                                                onExpandedChange = { isYearExpanded = it },
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                androidx.compose.material3.OutlinedTextField(
                                                        value = selectedYear,
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = { Text("Year") },
                                                        trailingIcon = {
                                                                androidx.compose.material3
                                                                        .ExposedDropdownMenuDefaults
                                                                        .TrailingIcon(
                                                                                expanded =
                                                                                        isYearExpanded
                                                                        )
                                                        },
                                                        modifier = Modifier.menuAnchor()
                                                )
                                                ExposedDropdownMenu(
                                                        expanded = isYearExpanded,
                                                        onDismissRequest = {
                                                                isYearExpanded = false
                                                        }
                                                ) {
                                                        years.forEach { year ->
                                                                DropdownMenuItem(
                                                                        text = { Text(year) },
                                                                        onClick = {
                                                                                selectedYear = year
                                                                                isYearExpanded =
                                                                                        false
                                                                        }
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }

                        item {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        Button(
                                                onClick = onNavigateToVisualization,
                                                modifier = Modifier.weight(1f).height(56.dp)
                                        ) {
                                                Icon(
                                                        Icons.Default.Analytics,
                                                        contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("View Visualizations")
                                        }

                                        Button(
                                                onClick = onNavigateToStudentProgress,
                                                modifier = Modifier.weight(1f).height(56.dp)
                                        ) {
                                                Icon(
                                                        Icons.Default.Grading,
                                                        contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("View Student Progress")
                                        }
                                }
                        }

                        item {
                                Text(
                                        text = "Severe Students (Requires Attention)",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                        }

                        if (isLoading) {
                                item {
                                        androidx.compose.foundation.layout.Box(
                                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                                contentAlignment = Alignment.Center
                                        ) { androidx.compose.material3.CircularProgressIndicator() }
                                }
                        } else if (errorMessage != null) {
                                item {
                                        Text(
                                                errorMessage ?: "Error",
                                                color = MaterialTheme.colorScheme.error
                                        )
                                }
                        } else if (aggregatedData != null) {
                                val data = aggregatedData!!
                                val severeDepression =
                                        data.depressedStudents.filter {
                                                it.depressionLevel in
                                                        listOf("Severe", "Extremely Severe")
                                        }
                                val severeAnxiety =
                                        data.anxiousStudents.filter {
                                                it.anxietyLevel in
                                                        listOf("Severe", "Extremely Severe")
                                        }
                                val severeStress =
                                        data.stressedStudents.filter {
                                                it.stressLevel in
                                                        listOf("Severe", "Extremely Severe")
                                        }

                                val allSevere =
                                        (severeDepression + severeAnxiety + severeStress)
                                                .distinctBy { it.profile.userId }

                                if (allSevere.isEmpty()) {
                                        item {
                                                Text(
                                                        "No severe cases found in this department/year.",
                                                        style = MaterialTheme.typography.bodyLarge
                                                )
                                        }
                                } else {
                                        items(allSevere.size) { index ->
                                                val student = allSevere[index]
                                                androidx.compose.material3.Card(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(vertical = 4.dp),
                                                        onClick = {
                                                                onStudentClick(
                                                                        student.profile.userId
                                                                )
                                                        }
                                                ) {
                                                        Column(modifier = Modifier.padding(16.dp)) {
                                                                Text(
                                                                        student.profile.name,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .titleMedium
                                                                )
                                                                Text(
                                                                        "${student.profile.className} - ${student.profile.division}"
                                                                )
                                                                Text(
                                                                        "Depression: ${student.depressionLevel}",
                                                                        color =
                                                                                if (student.depressionLevel in
                                                                                                listOf(
                                                                                                        "Severe",
                                                                                                        "Extremely Severe"
                                                                                                )
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurface
                                                                )
                                                                Text(
                                                                        "Anxiety: ${student.anxietyLevel}",
                                                                        color =
                                                                                if (student.anxietyLevel in
                                                                                                listOf(
                                                                                                        "Severe",
                                                                                                        "Extremely Severe"
                                                                                                )
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurface
                                                                )
                                                                Text(
                                                                        "Stress: ${student.stressLevel}",
                                                                        color =
                                                                                if (student.stressLevel in
                                                                                                listOf(
                                                                                                        "Severe",
                                                                                                        "Extremely Severe"
                                                                                                )
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurface
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
