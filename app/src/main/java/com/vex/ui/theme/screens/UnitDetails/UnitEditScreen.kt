package com.vex.ui.theme.screens.UnitDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vex.model.UnitItem
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitEditScreen(
    navController: NavHostController,
    viewModel: UnitEditViewModel = viewModel(),
    unitId: String? // null for creating a new unit
) {
    val isNew = unitId == null
    val unitFlow = if (!isNew) viewModel.getUnit(unitId!!) else flowOf(null)
    val unit by unitFlow.collectAsState(initial = null)

    // Preload form fields with existing unit data if editing
    var code by remember { mutableStateOf(unit?.code ?: "") }
    var name by remember { mutableStateOf(unit?.name ?: "") }
    var lecturer by remember { mutableStateOf(unit?.lecturer ?: "") }
    var semester by remember { mutableStateOf(unit?.semester ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Add Unit" else "Edit Unit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LaunchedEffect(unit) {
                unit?.let {
                    code = it.code
                    name = it.name
                    lecturer = it.lecturer
                    semester = it.semester
                }
            }
            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") })
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = lecturer, onValueChange = { lecturer = it }, label = { Text("Lecturer") })
            OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") })

            errorMessage?.let { Text(it, color = Color.Red) }

            Button(
                onClick = {
                    isSaving = true
                    val newUnit = UnitItem(
                        id = unit?.id ?: viewModel.generateId(),
                        code = code.trim(),
                        name = name.trim(),
                        lecturer = lecturer.trim(),
                        semester = semester.trim()
                    )

                    viewModel.saveUnit(
                        unit = newUnit,
                        onSuccess = {
                            isSaving = false
                            navController.popBackStack() // return to previous screen
                        },
                        onError = {
                            isSaving = false
                            errorMessage = it
                        }
                    )
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isNew) "Create Unit" else "Update Unit")
            }
        }
    }
}



