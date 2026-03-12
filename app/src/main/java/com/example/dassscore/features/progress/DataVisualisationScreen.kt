package com.example.dassscore.features.progress

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.features.result.getAnxietyLevel
import com.example.dassscore.features.result.getDepressionLevel
import com.example.dassscore.features.result.getStressLevel
import com.example.dassscore.model.DassResult
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class StudentProfileData(
        val userId: String,
        val name: String,
        val className: String, // Represents Department
        val division: String, // Represents Year
        val rbt: String,
        val mobileNumber: String,
        val parentContactNumber: String
)

data class StudentDassSummary(
        val profile: StudentProfileData,
        val depressionLevel: String,
        val anxietyLevel: String,
        val stressLevel: String,
        val timestamp: Long
)

data class AggregatedDassData(
        val totalAssessments: Int = 0,
        val depressionLevelCounts: Map<String, Int> = emptyMap(),
        val anxietyLevelCounts: Map<String, Int> = emptyMap(),
        val stressLevelCounts: Map<String, Int> = emptyMap(),
        val depressedStudents: List<StudentDassSummary> = emptyList(),
        val anxiousStudents: List<StudentDassSummary> = emptyList(),
        val stressedStudents: List<StudentDassSummary> = emptyList(),
        val normalStudents: List<StudentDassSummary> = emptyList(),
        val allStudents: List<StudentDassSummary> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataVisualizationScreen(
        repository: FirebaseRepository,
        onBack: () -> Unit,
        onStudentClick: (String) -> Unit = {}
) {
    var allResults by remember { mutableStateOf<List<DassResult>?>(null) }
    var allProfiles by remember { mutableStateOf<Map<String, StudentProfileData>?>(null) }
    var aggregatedData by remember { mutableStateOf<AggregatedDassData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

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

    var selectedDepartment by remember { mutableStateOf("All") }
    var isDepartmentExpanded by remember { mutableStateOf(false) }

    var selectedYear by remember { mutableStateOf("All") }
    var isYearExpanded by remember { mutableStateOf(false) }

    var selectedDepressionLevelFilter by remember { mutableStateOf("All") }
    var selectedAnxietyLevelFilter by remember { mutableStateOf("All") }
    var selectedStressLevelFilter by remember { mutableStateOf("All") }

    val severityLevels = listOf("All", "Normal", "Extremely Severe", "Severe", "Moderate", "Mild")
    var selectedTabIndex by remember { mutableStateOf(0) } //
    val tabs = listOf("Depression", "Anxiety", "Stress", "Normal", "All Students")

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val resultsDeferred = async { repository.getAllDassResults() }
            val profilesDeferred = async { repository.getAllUserProfiles() }

            val resultsResponse = resultsDeferred.await()
            val profilesResponse = profilesDeferred.await()

            withContext(Dispatchers.Main) {
                if (resultsResponse.isSuccess && profilesResponse.isSuccess) {
                    val rawResults = resultsResponse.getOrNull() ?: emptyList()
                    val rawProfiles = profilesResponse.getOrNull() ?: emptyMap()

                    // Convert raw map to StudentProfileData
                    val parsedProfiles =
                            rawProfiles.mapValues { (userId, profileMap) ->
                                StudentProfileData(
                                        userId = userId,
                                        name = profileMap["name"] as? String ?: "Unknown",
                                        className = profileMap["className"] as? String ?: "N/A",
                                        division = profileMap["division"] as? String ?: "N/A",
                                        rbt = profileMap["rbt"] as? String ?: "N/A",
                                        mobileNumber = profileMap["mobileNumber"] as? String
                                                        ?: "N/A",
                                        parentContactNumber =
                                                profileMap["parentContactNumber"] as? String
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

    LaunchedEffect(allResults, allProfiles, selectedDepartment, selectedYear) {
        if (allResults != null && allProfiles != null) {
            aggregatedData =
                    filterAndAggregateData(
                            allResults!!,
                            allProfiles!!,
                            selectedDepartment,
                            selectedYear
                    )
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Overall Results") },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // --- Filter UI Section ---
            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Department Dropdown
                ExposedDropdownMenuBox(
                        expanded = isDepartmentExpanded,
                        onExpandedChange = { isDepartmentExpanded = it },
                        modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                            value = selectedDepartment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isDepartmentExpanded
                                )
                            },
                            modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                            expanded = isDepartmentExpanded,
                            onDismissRequest = { isDepartmentExpanded = false }
                    ) {
                        departments.forEach { department ->
                            DropdownMenuItem(
                                    text = { Text(department) },
                                    onClick = {
                                        selectedDepartment = department
                                        isDepartmentExpanded = false
                                    }
                            )
                        }
                    }
                }

                // Year Dropdown
                ExposedDropdownMenuBox(
                        expanded = isYearExpanded,
                        onExpandedChange = { isYearExpanded = it },
                        modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                            value = selectedYear,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isYearExpanded)
                            },
                            modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                            expanded = isYearExpanded,
                            onDismissRequest = { isYearExpanded = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        selectedYear = year
                                        isYearExpanded = false
                                    }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Student Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    }
            )
            ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> Text(errorMessage!!, modifier = Modifier.padding(16.dp))
                    aggregatedData != null && aggregatedData!!.totalAssessments > 0 -> {
                        val data = aggregatedData!!
                        val context = LocalContext.current
                        // Content now depends on the selected tab
                        when (selectedTabIndex) {
                            // Depression Page
                            0 -> {
                                val filteredByLevel =
                                        if (selectedDepressionLevelFilter == "All") {
                                            data.depressedStudents
                                        } else {
                                            data.depressedStudents.filter {
                                                it.depressionLevel == selectedDepressionLevelFilter
                                            }
                                        }
                                val filteredStudents =
                                        filteredByLevel.filter {
                                            it.profile.name.contains(searchQuery, ignoreCase = true)
                                        }
                                CategoryPage(
                                        totalAssessments = data.totalAssessments,
                                        pieChartData = data.depressionLevelCounts,
                                        pieChartTitle = "Depression Severity Distribution",
                                        studentListTitle =
                                                "Depressed Students (${filteredStudents.size})",
                                        students = filteredStudents,
                                        severityLevels = severityLevels,
                                        selectedLevel = selectedDepressionLevelFilter,
                                        onLevelSelected = { selectedDepressionLevelFilter = it },
                                        onDownloadClick = {
                                            createAndSaveCsv(
                                                    context,
                                                    "depressed_students.csv",
                                                    filteredStudents
                                            )
                                        },
                                        onStudentClick = onStudentClick
                                )
                            }
                            // Anxiety Page
                            1 -> {
                                val filteredByLevel =
                                        if (selectedAnxietyLevelFilter == "All") {
                                            data.anxiousStudents
                                        } else {
                                            data.anxiousStudents.filter {
                                                it.anxietyLevel == selectedAnxietyLevelFilter
                                            }
                                        }
                                val filteredStudents =
                                        filteredByLevel.filter {
                                            it.profile.name.contains(searchQuery, ignoreCase = true)
                                        }
                                CategoryPage(
                                        totalAssessments = data.totalAssessments,
                                        pieChartData = data.anxietyLevelCounts,
                                        pieChartTitle = "Anxiety Severity Distribution",
                                        studentListTitle =
                                                "Anxious Students (${filteredStudents.size})",
                                        students = filteredStudents,
                                        severityLevels = severityLevels,
                                        selectedLevel = selectedAnxietyLevelFilter,
                                        onLevelSelected = { selectedAnxietyLevelFilter = it },
                                        onDownloadClick = {
                                            createAndSaveCsv(
                                                    context,
                                                    "anxious_students.csv",
                                                    filteredStudents
                                            )
                                        },
                                        onStudentClick = onStudentClick
                                )
                            }
                            // Stress Page
                            2 -> {
                                val filteredByLevel =
                                        if (selectedStressLevelFilter == "All") {
                                            data.stressedStudents
                                        } else {
                                            data.stressedStudents.filter {
                                                it.stressLevel == selectedStressLevelFilter
                                            }
                                        }
                                val filteredStudents =
                                        filteredByLevel.filter {
                                            it.profile.name.contains(searchQuery, ignoreCase = true)
                                        }
                                CategoryPage(
                                        totalAssessments = data.totalAssessments,
                                        pieChartData = data.stressLevelCounts,
                                        pieChartTitle = "Stress Severity Distribution",
                                        studentListTitle =
                                                "Stressed Students (${filteredStudents.size})",
                                        students = filteredStudents,
                                        severityLevels = severityLevels,
                                        selectedLevel = selectedStressLevelFilter,
                                        onLevelSelected = { selectedStressLevelFilter = it },
                                        onDownloadClick = {
                                            createAndSaveCsv(
                                                    context,
                                                    "stressed_students.csv",
                                                    filteredStudents
                                            )
                                        },
                                        onStudentClick = onStudentClick
                                )
                            }
                            // Normal Students Page
                            3 -> {
                                val filteredStudents =
                                        data.normalStudents.filter {
                                            it.profile.name.contains(searchQuery, ignoreCase = true)
                                        }
                                LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        StudentListSection(
                                                title =
                                                        "Normal Students (${filteredStudents.size})",
                                                students = filteredStudents,
                                                levels = emptyList(), // No filter tabs needed
                                                selectedLevel = "",
                                                onLevelSelected = {},
                                                onDownloadClick = {
                                                    createAndSaveCsv(
                                                            context,
                                                            "normal_students.csv",
                                                            filteredStudents
                                                    )
                                                },
                                                onStudentClick = onStudentClick
                                        )
                                    }
                                }
                            }
                            4 -> {
                                val filteredStudents =
                                        data.allStudents.filter {
                                            it.profile.name.contains(searchQuery, ignoreCase = true)
                                        }
                                AllStudentsPage(
                                        students = filteredStudents,
                                        onDownloadClick = {
                                            createAndSaveCsv(
                                                    context,
                                                    "all_students.csv",
                                                    filteredStudents
                                            )
                                        },
                                        onStudentClick = onStudentClick
                                )
                            }
                        }
                    }
                    else -> Text("No assessment data available for the selected filters.")
                }
            }
        }
    }
}

@Composable
private fun AllStudentsPage(
        students: List<StudentDassSummary>,
        onDownloadClick: () -> Unit,
        onStudentClick: (String) -> Unit
) {
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StudentListSection(
                    title = "All Students (${students.size})",
                    students = students,
                    levels = emptyList(),
                    selectedLevel = "",
                    onLevelSelected = {},
                    onDownloadClick = onDownloadClick,
                    onStudentClick = onStudentClick
            )
        }
    }
}

@Composable
private fun CategoryPage(
        totalAssessments: Int,
        pieChartData: Map<String, Int>,
        pieChartTitle: String,
        studentListTitle: String,
        students: List<StudentDassSummary>,
        severityLevels: List<String>,
        selectedLevel: String,
        onLevelSelected: (String) -> Unit,
        onDownloadClick: () -> Unit,
        onStudentClick: (String) -> Unit
) {
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                    "Total Assessments (Filtered): $totalAssessments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )
        }
        item { PieChart(data = pieChartData, title = pieChartTitle) }
        item {
            StudentListSection(
                    title = studentListTitle,
                    students = students,
                    levels = severityLevels,
                    selectedLevel = selectedLevel,
                    onLevelSelected = onLevelSelected,
                    onDownloadClick = onDownloadClick,
                    onStudentClick = onStudentClick
            )
        }
    }
}

fun filterAndAggregateData(
        allResults: List<DassResult>,
        allProfiles: Map<String, StudentProfileData>,
        selectedDepartment: String,
        selectedYear: String
): AggregatedDassData {
    val filteredResults =
            allResults.filter { result ->
                val profile = allProfiles[result.userId]
                if (profile == null) return@filter false

                val departmentMatches =
                        selectedDepartment == "All" || profile.className == selectedDepartment
                val yearMatches = selectedYear == "All" || profile.division == selectedYear
                departmentMatches && yearMatches
            }

    if (filteredResults.isEmpty()) return AggregatedDassData()

    val allSummaries =
            filteredResults.mapNotNull { result ->
                allProfiles[result.userId]?.let { profile ->
                    StudentDassSummary(
                            profile = profile,
                            depressionLevel = getDepressionLevel(result.depressionScore),
                            anxietyLevel = getAnxietyLevel(result.anxietyScore),
                            stressLevel = getStressLevel(result.stressScore),
                            timestamp = result.timestamp
                    )
                }
            }

    val depressionLevelCounts = allSummaries.groupingBy { it.depressionLevel }.eachCount()
    val anxietyLevelCounts = allSummaries.groupingBy { it.anxietyLevel }.eachCount()
    val stressLevelCounts = allSummaries.groupingBy { it.stressLevel }.eachCount()

    val summariesByUser = allSummaries.groupBy { it.profile.userId }

    val depressedStudents = mutableListOf<StudentDassSummary>()
    val anxiousStudents = mutableListOf<StudentDassSummary>()
    val stressedStudents = mutableListOf<StudentDassSummary>()
    val normalStudents = mutableListOf<StudentDassSummary>()
    val allStudents = mutableListOf<StudentDassSummary>()

    summariesByUser.forEach { (_, userSummaries) ->
        val latestSummary = userSummaries.maxByOrNull { it.timestamp } ?: return@forEach
        allStudents.add(latestSummary)

        val hasDepression = userSummaries.any { it.depressionLevel != "Normal" }
        val hasAnxiety = userSummaries.any { it.anxietyLevel != "Normal" }
        val hasStress = userSummaries.any { it.stressLevel != "Normal" }

        if (hasDepression) depressedStudents.add(latestSummary)
        if (hasAnxiety) anxiousStudents.add(latestSummary)
        if (hasStress) stressedStudents.add(latestSummary)

        if (!hasDepression && !hasAnxiety && !hasStress) {
            normalStudents.add(latestSummary)
        }
    }

    val severityOrder =
            mapOf(
                    "Extremely Severe" to 0,
                    "Severe" to 1,
                    "Moderate" to 2,
                    "Mild" to 3,
                    "Normal" to 4
            )

    depressedStudents.sortWith(compareBy { severityOrder[it.depressionLevel] })
    anxiousStudents.sortWith(compareBy { severityOrder[it.anxietyLevel] })
    stressedStudents.sortWith(compareBy { severityOrder[it.stressLevel] })
    normalStudents.sortBy { it.profile.name }
    allStudents.sortBy { it.profile.name }

    return AggregatedDassData(
            totalAssessments = filteredResults.size,
            depressionLevelCounts = depressionLevelCounts,
            anxietyLevelCounts = anxietyLevelCounts,
            stressLevelCounts = stressLevelCounts,
            depressedStudents = depressedStudents,
            anxiousStudents = anxiousStudents,
            stressedStudents = stressedStudents,
            normalStudents = normalStudents,
            allStudents = allStudents
    )
}

@Composable
fun PieChart(data: Map<String, Int>, title: String) {
    if (data.isEmpty() || data.values.sum() == 0) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        "No data available for this chart.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val total = data.values.sum().toFloat()
    var startAngle = 0f

    val colors =
            listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3),
                    Color(0xFFFFC107),
                    Color(0xFFFF5722),
                    Color(0xFFE53935)
            )
    val sortedKeys = listOf("Normal", "Mild", "Moderate", "Severe", "Extremely Severe")
    val colorMap = sortedKeys.zip(colors).toMap()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val radius = minOf(canvasWidth, canvasHeight) / 2
                    val center = Offset(canvasWidth / 2, canvasHeight / 2)

                    data.entries.sortedBy { sortedKeys.indexOf(it.key) }.forEach { (level, count) ->
                        val sweepAngle = (count / total) * 360f
                        val sliceColor = colorMap[level] ?: Color.Gray

                        drawArc(
                                color = sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2)
                        )
                        startAngle += sweepAngle
                    }
                }

                Column(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 16.dp),
                        verticalArrangement = Arrangement.Center
                ) {
                    data.entries.sortedBy { sortedKeys.indexOf(it.key) }.forEach { (level, count) ->
                        val percentage = (count / total) * 100
                        val sliceColor = colorMap[level] ?: Color.Gray
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                    modifier =
                                            Modifier.size(16.dp)
                                                    .background(
                                                            sliceColor,
                                                            RoundedCornerShape(4.dp)
                                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "$level (${count}, ${"%.1f".format(percentage)}%)",
                                    style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StudentListSection(
        title: String,
        students: List<StudentDassSummary>,
        levels: List<String>,
        selectedLevel: String,
        onLevelSelected: (String) -> Unit,
        onDownloadClick: () -> Unit,
        onStudentClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Title
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDownloadClick) {
                    Icon(Icons.Default.Download, contentDescription = "Download Student List")
                }
            }

            // Filter Tabs
            if (levels.size > 1) {
                val selectedIndex = levels.indexOf(selectedLevel).coerceAtLeast(0)
                ScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        edgePadding = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    levels.forEachIndexed { index, level ->
                        Tab(
                                selected = selectedIndex == index,
                                onClick = { onLevelSelected(level) },
                                text = { Text(level, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            StudentListTable(students = students, onStudentClick = onStudentClick)
        }
    }
}

@Composable
private fun VerticalDivider() {
    Divider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

@Composable
fun StudentListTable(students: List<StudentDassSummary>, onStudentClick: (String) -> Unit) {
    if (students.isEmpty()) {
        Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    "No students match the selected filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Box(modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState())) {
        // This inner Column defines the actual table content that will scroll
        Column(Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
            Row(
                    modifier =
                            Modifier.background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.5f
                                            )
                                    )
                                    .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        "Sr. No.",
                        Modifier.width(50.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                )
                VerticalDivider()
                Text(
                        "RBT",
                        Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                )
                VerticalDivider()
                Text(
                        "Name",
                        Modifier.width(120.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                )
                VerticalDivider()
                Text(
                        "Department",
                        Modifier.width(200.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                )
                VerticalDivider()
                Text(
                        "Depression",
                        Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                )
                VerticalDivider()
                Text(
                        "Anxiety",
                        Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                )
                VerticalDivider()
                Text(
                        "Stress",
                        Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                )
                VerticalDivider()
                Text(
                        "Mobile",
                        Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                )
                VerticalDivider()
                Text(
                        "Parent's",
                        Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                )
            }

            androidx.compose.material3.Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            students.forEachIndexed { index, student ->
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                        .clickable { onStudentClick(student.profile.userId) }
                                        .padding(
                                                vertical = 4.dp
                                        ), // add a little padding to make hit area larger if needed
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            (index + 1).toString(),
                            Modifier.width(50.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                    )
                    VerticalDivider()
                    Text(
                            student.profile.rbt,
                            Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                    )
                    VerticalDivider()
                    Text(
                            student.profile.name,
                            Modifier.width(120.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                    )
                    VerticalDivider()
                    Text(
                            student.profile.className,
                            Modifier.width(200.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                    )
                    VerticalDivider()
                    Text(
                            student.depressionLevel,
                            Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                    )
                    VerticalDivider()
                    Text(
                            student.anxietyLevel,
                            Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                    )
                    VerticalDivider()
                    Text(
                            student.stressLevel,
                            Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                    )
                    VerticalDivider()
                    Text(
                            student.profile.mobileNumber,
                            Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                    )
                    VerticalDivider()
                    Text(
                            student.profile.parentContactNumber,
                            Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                    )
                }
                androidx.compose.material3.Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

private fun createAndSaveCsv(
        context: Context,
        fileName: String,
        students: List<StudentDassSummary>
) {
    val csvHeader =
            "Sr. No.,RBT,Name,Department,Year,Depression,Anxiety,Stress,Mobile,Parent's Mobile"
    val csvContent = StringBuilder()
    csvContent.append(csvHeader).append("\n")

    students.forEachIndexed { index, student ->
        val row =
                listOf(
                                (index + 1).toString(),
                                student.profile.rbt,
                                student.profile.name,
                                student.profile.className,
                                student.profile.division,
                                student.depressionLevel,
                                student.anxietyLevel,
                                student.stressLevel,
                                student.profile.mobileNumber,
                                student.profile.parentContactNumber
                        )
                        .joinToString(",")
        csvContent.append(row).append("\n")
    }

    try {
        val contentValues =
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(csvContent.toString().toByteArray())
                Toast.makeText(context, "File saved to Downloads", Toast.LENGTH_SHORT).show()
            }
        }
                ?: throw IOException("Failed to create new MediaStore record.")
    } catch (e: IOException) {
        Log.e("DataVisualizationScreen", "Error saving CSV file", e)
        Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
    }
}
