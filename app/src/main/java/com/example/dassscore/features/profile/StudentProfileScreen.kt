package com.example.dassscore.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.data.repository.User
import com.example.dassscore.ui.theme.PrimaryBlue
import com.example.dassscore.ui.theme.PrimaryBlueDark
import com.example.dassscore.ui.theme.ThemeUtils
import com.example.dassscore.ui.theme.isAppDarkTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(user: User, repository: FirebaseRepository, onBack: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Profile fields
    var name by remember { mutableStateOf("") }
    var rbt by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var mobileNumber by remember { mutableStateOf("") }
    var parentContactNumber by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }

    var isDepartmentExpanded by remember { mutableStateOf(false) }
    var isYearExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val departments =
            listOf(
                    "Computer Engineering",
                    "Information Technology",
                    "Computer Science and Bussiness System",
                    "Automation and Robotics",
                    "Mechanical",
                    "Civil",
                    "Electronics & Telecommunication",
                    "Electrical"
            )
    val years = listOf("First Year", "Second Year", "Third Year", "Fourth Year")

    val isDark = isAppDarkTheme()
    val backgroundBrush = ThemeUtils.appBackgroundBrush(isDark)
    val contentColor = ThemeUtils.appContentColor(isDark)
    val subtleColor = ThemeUtils.appSubtleContentColor(isDark)
    val accentColor = if (isDark) PrimaryBlueDark else PrimaryBlue

    // Load profile on first composition
    LaunchedEffect(user.uid) {
        isLoading = true
        repository
                .getUserProfile(user.uid)
                .fold(
                        onSuccess = { data ->
                            if (data != null) {
                                name = (data["name"] as? String) ?: ""
                                rbt = (data["rbt"] as? String) ?: ""
                                mobileNumber = (data["mobileNumber"] as? String) ?: ""
                                parentContactNumber = (data["parentContactNumber"] as? String) ?: ""
                                selectedDepartment = (data["className"] as? String) ?: ""
                                selectedYear = (data["division"] as? String) ?: ""
                                email = (data["email"] as? String) ?: (user.email ?: "")
                            }
                            isLoading = false
                        },
                        onFailure = {
                            isLoading = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to load profile")
                            }
                        }
                )
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    text = "My Profile",
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    letterSpacing = 1.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = contentColor
                                )
                            }
                        },
                        actions = {
                            if (!isLoading) {
                                IconButton(
                                        onClick = {
                                            if (isEditing) {
                                                // Save
                                                if (name.isBlank() ||
                                                                rbt.isBlank() ||
                                                                mobileNumber.isBlank() ||
                                                                parentContactNumber.isBlank() ||
                                                                selectedDepartment.isBlank() ||
                                                                selectedYear.isBlank()
                                                ) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                                "Please fill in all fields"
                                                        )
                                                    }
                                                    return@IconButton
                                                }
                                                isSaving = true
                                                val profileData =
                                                        mapOf(
                                                                "rbt" to rbt,
                                                                "name" to name,
                                                                "mobileNumber" to mobileNumber,
                                                                "parentContactNumber" to
                                                                        parentContactNumber,
                                                                "className" to selectedDepartment,
                                                                "division" to selectedYear,
                                                                "email" to email,
                                                                "role" to "student"
                                                        )
                                                coroutineScope.launch {
                                                    repository
                                                            .updateUserProfile(
                                                                    user.uid,
                                                                    profileData
                                                            )
                                                            .fold(
                                                                    onSuccess = {
                                                                        isSaving = false
                                                                        isEditing = false
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "Profile updated successfully"
                                                                                )
                                                                    },
                                                                    onFailure = { e ->
                                                                        isSaving = false
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "Failed to save: ${e.message}"
                                                                                )
                                                                    }
                                                            )
                                                }
                                            } else {
                                                isEditing = true
                                            }
                                        }
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = contentColor,
                                                strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                                imageVector =
                                                        if (isEditing) Icons.Default.Save
                                                        else Icons.Default.Edit,
                                                contentDescription =
                                                        if (isEditing) "Save" else "Edit",
                                                tint = contentColor
                                        )
                                    }
                                }
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                )
                )
            },
            containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(innerPadding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(horizontal = 24.dp)
                                        .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar
                    Box(
                            modifier =
                                    Modifier.size(100.dp)
                                            .clip(CircleShape)
                                            .background(ThemeUtils.appCircleBackgroundColor(isDark))
                                            .border(
                                                    2.dp,
                                                    accentColor.copy(alpha = 0.6f),
                                                    CircleShape
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier.size(56.dp),
                                tint = contentColor.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                            text = name.ifBlank { "Student" },
                            style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color = contentColor
                    )
                    Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtleColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Details Card
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor = ThemeUtils.appCardColor(isDark)
                                    ),
                            border =
                                    androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            ThemeUtils.appCardBorderColor(isDark)
                                    )
                    ) {
                        Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                    text = if (isEditing) "Edit Profile" else "Profile Details",
                                    style =
                                            MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                            ),
                                    color = contentColor
                            )

                            if (isEditing) {
                                // Editable fields
                                ProfileTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = "Full Name",
                                        isDark = isDark
                                )
                                ProfileTextField(
                                        value = rbt,
                                        onValueChange = { rbt = it },
                                        label = "RBT",
                                        isDark = isDark
                                )
                                ProfileTextField(
                                        value = mobileNumber,
                                        onValueChange = { mobileNumber = it },
                                        label = "Mobile Number",
                                        keyboardType = KeyboardType.Phone,
                                        isDark = isDark
                                )
                                ProfileTextField(
                                        value = parentContactNumber,
                                        onValueChange = { parentContactNumber = it },
                                        label = "Parent's Contact Number",
                                        keyboardType = KeyboardType.Phone,
                                        isDark = isDark
                                )

                                // Department Dropdown
                                ExposedDropdownMenuBox(
                                        expanded = isDepartmentExpanded,
                                        onExpandedChange = { isDepartmentExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                            value = selectedDepartment,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = {
                                                Text(
                                                        "Department",
                                                        color = ThemeUtils.appTextFieldLabelColor(isDark)
                                                )
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = isDepartmentExpanded
                                                )
                                            },
                                            colors = profileTextFieldColors(isDark),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                            value = selectedYear,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = {
                                                Text("Year", color = ThemeUtils.appTextFieldLabelColor(isDark))
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = isYearExpanded
                                                )
                                            },
                                            colors = profileTextFieldColors(isDark),
                                            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                            } else {
                                // Read-only view
                                ProfileDetailRow(label = "Full Name", value = name, isDark = isDark)
                                ProfileDetailRow(label = "RBT", value = rbt, isDark = isDark)
                                ProfileDetailRow(label = "Email", value = email, isDark = isDark)
                                ProfileDetailRow(label = "Mobile Number", value = mobileNumber, isDark = isDark)
                                ProfileDetailRow(
                                        label = "Parent's Contact",
                                        value = parentContactNumber,
                                        isDark = isDark
                                )
                                ProfileDetailRow(label = "Department", value = selectedDepartment, isDark = isDark)
                                ProfileDetailRow(label = "Year", value = selectedYear, isDark = isDark)
                            }
                        }
                    }

                    if (isEditing) {
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    // Reload original data
                                    coroutineScope.launch {
                                        repository
                                                .getUserProfile(user.uid)
                                                .fold(
                                                        onSuccess = { data ->
                                                            if (data != null) {
                                                                name =
                                                                        (data["name"] as? String)
                                                                                ?: ""
                                                                rbt = (data["rbt"] as? String) ?: ""
                                                                mobileNumber =
                                                                        (data["mobileNumber"] as?
                                                                                String)
                                                                                ?: ""
                                                                parentContactNumber =
                                                                        (data[
                                                                                "parentContactNumber"] as?
                                                                                String)
                                                                                ?: ""
                                                                selectedDepartment =
                                                                        (data["className"] as?
                                                                                String)
                                                                                ?: ""
                                                                selectedYear =
                                                                        (data["division"] as?
                                                                                String)
                                                                                ?: ""
                                                            }
                                                        },
                                                        onFailure = {}
                                                )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                contentColor.copy(alpha = 0.3f)
                                        ),
                                colors =
                                        ButtonDefaults.outlinedButtonColors(
                                                contentColor = contentColor
                                        )
                        ) { Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// --- Reusable composables ---

@Composable
private fun ProfileDetailRow(label: String, value: String, isDark: Boolean) {
    val contentColor = ThemeUtils.appContentColor(isDark)
    val subtleColor = ThemeUtils.appSubtleContentColor(isDark)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = subtleColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = ThemeUtils.appDividerColor(isDark))
    }
}

@Composable
private fun ProfileTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        keyboardType: KeyboardType = KeyboardType.Text,
        isDark: Boolean
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = ThemeUtils.appTextFieldLabelColor(isDark)) },
            modifier = Modifier.fillMaxWidth(),
            colors = profileTextFieldColors(isDark),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
    )
}

@Composable
private fun profileTextFieldColors(isDark: Boolean) =
        OutlinedTextFieldDefaults.colors(
                focusedTextColor = ThemeUtils.appTextFieldTextColor(isDark),
                unfocusedTextColor = ThemeUtils.appTextFieldTextColor(isDark).copy(alpha = 0.85f),
                cursorColor = if (isDark) PrimaryBlueDark else PrimaryBlue,
                focusedBorderColor = if (isDark) PrimaryBlueDark else PrimaryBlue,
                unfocusedBorderColor = ThemeUtils.appTextFieldBorderColor(isDark),
                focusedLabelColor = if (isDark) PrimaryBlueDark else PrimaryBlue,
                unfocusedLabelColor = ThemeUtils.appTextFieldLabelColor(isDark)
        )
