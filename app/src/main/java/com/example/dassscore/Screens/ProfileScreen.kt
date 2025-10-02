package com.example.dassscore.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dassscore.FirebaseRepository
import com.example.dassscore.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    repository: FirebaseRepository,
    onProfileSaved: () -> Unit
) {
    var rbt by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var parentContactNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    val departments = listOf(
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

    var isDepartmentExpanded by remember { mutableStateOf(false) }
    var selectedDepartment by remember { mutableStateOf("") }

    var isYearExpanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf("") }


    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Icon",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Complete Your Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = rbt,
                onValueChange = { rbt = it },
                label = { Text("RBT") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = parentContactNumber,
                onValueChange = { parentContactNumber = it },
                label = { Text("Parents Contact Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Department Dropdown ---
            ExposedDropdownMenuBox(
                expanded = isDepartmentExpanded,
                onExpandedChange = { isDepartmentExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDepartment,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Department") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDepartmentExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = isYearExpanded,
                onExpandedChange = { isYearExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedYear,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Year") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isYearExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    val profileData = mapOf(
                        "rbt" to rbt,
                        "name" to name,
                        "mobileNumber" to mobileNumber,
                        "parentContactNumber" to parentContactNumber,
                        "className" to selectedDepartment,
                        "division" to selectedYear,
                        "email" to (user.email ?: ""),
                        "role" to "student"
                    )
                    coroutineScope.launch {
                        repository.updateUserProfile(user.uid, profileData).fold(
                            onSuccess = {
                                isLoading = false
                                onProfileSaved()
                            },
                            onFailure = { exception ->
                                isLoading = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to save profile: ${exception.message}",
                                        actionLabel = "Dismiss",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && rbt.isNotBlank() && name.isNotBlank() && mobileNumber.isNotBlank() && parentContactNumber.isNotBlank() && selectedDepartment.isNotBlank() && selectedYear.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Profile", fontSize = 16.sp)
                }
            }
        }
    }
}