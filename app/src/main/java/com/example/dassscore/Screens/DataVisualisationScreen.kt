package com.example.dassscore.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dassscore.DassResult
import com.example.dassscore.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

data class StudentProfileData(
    val userId: String,
    val name: String,
    val className: String, // Represents Department
    val division: String,  // Represents Year
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
    val stressedStudents: List<StudentDassSummary> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataVisualizationScreen(
    repository: FirebaseRepository,
    onBack: () -> Unit
) {
    var allResults by remember { mutableStateOf<List<DassResult>?>(null) }
    var allProfiles by remember { mutableStateOf<Map<String, StudentProfileData>?>(null) }
    var aggregatedData by remember { mutableStateOf<AggregatedDassData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf(
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
    val severityLevels = listOf("All", "Extremely Severe", "Severe", "Moderate", "Mild")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Depression", "Anxiety", "Stress")

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            repository.getAllDassResults().fold(
                onSuccess = { results ->
                    val userIds = results.map { it.userId }.distinct()
                    val profileJobs = userIds.map { userId ->
                        async {
                            repository.getUserProfile(userId).fold(
                                onSuccess = { profileMap ->
                                    if (profileMap != null) {
                                        userId to StudentProfileData(
                                            userId = userId,
                                            name = profileMap["name"] as? String ?: "Unknown",
                                            className = profileMap["className"] as? String ?: "N/A",
                                            division = profileMap["division"] as? String ?: "N/A",
                                            rbt = profileMap["rbt"] as? String ?: "N/A",
                                            mobileNumber = profileMap["mobileNumber"] as? String ?: "N/A",
                                            parentContactNumber = profileMap["parentContactNumber"] as? String ?: "N/A"
                                        )
                                    } else {
                                        null
                                    }
                                },
                                onFailure = {
                                    Log.e("DataViz", "Failed to fetch profile for $userId: ${it.message}")
                                    null
                                }
                            )
                        }
                    }
                    val profiles = profileJobs.awaitAll().filterNotNull().toMap()
                    withContext(Dispatchers.Main) {
                        allResults = results
                        allProfiles = profiles
                        isLoading = false
                    }
                },
                onFailure = { exception ->
                    withContext(Dispatchers.Main) {
                        errorMessage = "Failed to load data: ${exception.message}"
                        isLoading = false
                    }
                }
            )
        }
    }

    LaunchedEffect(allResults, allProfiles, selectedDepartment, selectedYear) {
        if (allResults != null && allProfiles != null) {
            aggregatedData = filterAndAggregateData(
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Filter UI Section ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDepartmentExpanded) },
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isYearExpanded) },
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
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> Text(errorMessage!!, modifier = Modifier.padding(16.dp))
                    aggregatedData != null && aggregatedData!!.totalAssessments > 0 -> {
                        val data = aggregatedData!!
                        // Content now depends on the selected tab
                        when (selectedTabIndex) {
                            // Depression Page
                            0 -> {
                                val filteredStudents = if (selectedDepressionLevelFilter == "All") {
                                    data.depressedStudents
                                } else {
                                    data.depressedStudents.filter { it.depressionLevel == selectedDepressionLevelFilter }
                                }
                                CategoryPage(
                                    totalAssessments = data.totalAssessments,
                                    pieChartData = data.depressionLevelCounts,
                                    pieChartTitle = "Depression Severity Distribution",
                                    studentListTitle = "Depressed Students",
                                    students = filteredStudents,
                                    severityLevels = severityLevels,
                                    selectedLevel = selectedDepressionLevelFilter,
                                    onLevelSelected = { selectedDepressionLevelFilter = it }
                                )
                            }
                            1 -> {
                                val filteredStudents = if (selectedAnxietyLevelFilter == "All") {
                                    data.anxiousStudents
                                } else {
                                    data.anxiousStudents.filter { it.anxietyLevel == selectedAnxietyLevelFilter }
                                }
                                CategoryPage(
                                    totalAssessments = data.totalAssessments,
                                    pieChartData = data.anxietyLevelCounts,
                                    pieChartTitle = "Anxiety Severity Distribution",
                                    studentListTitle = "Anxious Students",
                                    students = filteredStudents,
                                    severityLevels = severityLevels,
                                    selectedLevel = selectedAnxietyLevelFilter,
                                    onLevelSelected = { selectedAnxietyLevelFilter = it }
                                )
                            }
                            2 -> {
                                val filteredStudents = if (selectedStressLevelFilter == "All") {
                                    data.stressedStudents
                                } else {
                                    data.stressedStudents.filter { it.stressLevel == selectedStressLevelFilter }
                                }
                                CategoryPage(
                                    totalAssessments = data.totalAssessments,
                                    pieChartData = data.stressLevelCounts,
                                    pieChartTitle = "Stress Severity Distribution",
                                    studentListTitle = "Stressed Students",
                                    students = filteredStudents,
                                    severityLevels = severityLevels,
                                    selectedLevel = selectedStressLevelFilter,
                                    onLevelSelected = { selectedStressLevelFilter = it }
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
private fun CategoryPage(
    totalAssessments: Int,
    pieChartData: Map<String, Int>,
    pieChartTitle: String,
    studentListTitle: String,
    students: List<StudentDassSummary>,
    severityLevels: List<String>,
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
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
        item {
            PieChart(
                data = pieChartData,
                title = pieChartTitle
            )
        }
        item {
            StudentListSection(
                title = studentListTitle,
                students = students,
                levels = severityLevels,
                selectedLevel = selectedLevel,
                onLevelSelected = onLevelSelected
            )
        }
    }
}


/**
 * This function takes the pre-fetched data and applies local filters before aggregating.
 * It does NOT perform any network operations.
 */
fun filterAndAggregateData(
    allResults: List<DassResult>,
    allProfiles: Map<String, StudentProfileData>,
    selectedDepartment: String,
    selectedYear: String
): AggregatedDassData {
    val filteredResults = allResults.filter { result ->
        val profile = allProfiles[result.userId]
        if (profile == null) return@filter false

        val departmentMatches = selectedDepartment == "All" || profile.className == selectedDepartment
        val yearMatches = selectedYear == "All" || profile.division == selectedYear
        departmentMatches && yearMatches
    }

    if (filteredResults.isEmpty()) return AggregatedDassData()

    val depressionLevelCounts = mutableMapOf<String, Int>()
    val anxietyLevelCounts = mutableMapOf<String, Int>()
    val stressLevelCounts = mutableMapOf<String, Int>()

    val depressedStudents = mutableListOf<StudentDassSummary>()
    val anxiousStudents = mutableListOf<StudentDassSummary>()
    val stressedStudents = mutableListOf<StudentDassSummary>()

    filteredResults.forEach { result ->
        val depressionLevel = getDepressionLevel(result.depressionScore)
        val anxietyLevel = getAnxietyLevel(result.anxietyScore)
        val stressLevel = getStressLevel(result.stressScore)

        depressionLevelCounts[depressionLevel] = (depressionLevelCounts[depressionLevel] ?: 0) + 1
        anxietyLevelCounts[anxietyLevel] = (anxietyLevelCounts[anxietyLevel] ?: 0) + 1
        stressLevelCounts[stressLevel] = (stressLevelCounts[stressLevel] ?: 0) + 1

        allProfiles[result.userId]?.let { profile ->
            val studentSummary = StudentDassSummary(
                profile = profile,
                depressionLevel = depressionLevel,
                anxietyLevel = anxietyLevel,
                stressLevel = stressLevel,
                timestamp = result.timestamp
            )
            if (depressionLevel != "Normal") depressedStudents.add(studentSummary)
            if (anxietyLevel != "Normal") anxiousStudents.add(studentSummary)
            if (stressLevel != "Normal") stressedStudents.add(studentSummary)
        }
    }

    depressedStudents.sortBy { it.profile.name }
    anxiousStudents.sortBy { it.profile.name }
    stressedStudents.sortBy { it.profile.name }

    return AggregatedDassData(
        totalAssessments = filteredResults.size,
        depressionLevelCounts = depressionLevelCounts,
        anxietyLevelCounts = anxietyLevelCounts,
        stressLevelCounts = stressLevelCounts,
        depressedStudents = depressedStudents.distinctBy { it.profile.userId },
        anxiousStudents = anxiousStudents.distinctBy { it.profile.userId },
        stressedStudents = stressedStudents.distinctBy { it.profile.userId }
    )
}

@Composable
fun PieChart(data: Map<String, Int>, title: String) {
    if (data.isEmpty() || data.values.sum() == 0) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No data available for this chart.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
        return
    }

    val total = data.values.sum().toFloat()
    var startAngle = 0f

    val colors = listOf(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    data.entries.sortedBy { sortedKeys.indexOf(it.key) }.forEach { (level, count) ->
                        val percentage = (count / total) * 100
                        val sliceColor = colorMap[level] ?: Color.Gray
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(sliceColor, RoundedCornerShape(4.dp))
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
    onLevelSelected: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
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

            // The actual table
            StudentListTable(students = students)
        }
    }
}

@Composable
private fun VerticalDivider() {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

@Composable
fun StudentListTable(students: List<StudentDassSummary>) {
    if (students.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
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

    Box(modifier = Modifier
        .padding(top = 8.dp)
        .horizontalScroll(rememberScrollState())) {
        // This inner Column defines the actual table content that will scroll
        Column(
            Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sr. No.", Modifier.width(50.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                VerticalDivider()
                Text("Name", Modifier.width(120.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                VerticalDivider()
                Text("Department", Modifier.width(200.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                VerticalDivider()
                Text("Depression", Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                VerticalDivider()
                Text("Anxiety", Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                VerticalDivider()
                Text("Stress", Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                VerticalDivider()
                Text("Mobile", Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                VerticalDivider()
                Text("Parent's", Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            students.forEachIndexed { index, student ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text((index + 1).toString(), Modifier.width(50.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall)
                    VerticalDivider()
                    Text(student.profile.name, Modifier.width(120.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall)
                    VerticalDivider()
                    Text(student.profile.className, Modifier.width(200.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall)
                    VerticalDivider()
                    Text(student.depressionLevel, Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    VerticalDivider()
                    Text(student.anxietyLevel, Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    VerticalDivider()
                    Text(student.stressLevel, Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    VerticalDivider()
                    Text(student.profile.mobileNumber, Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall)
                    VerticalDivider()
                    Text(student.profile.parentContactNumber, Modifier.width(100.dp).padding(horizontal = 8.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall)
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}