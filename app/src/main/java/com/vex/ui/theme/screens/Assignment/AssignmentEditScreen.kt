package com.vex.ui.theme.screens.Assignment

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.flowOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vex.model.Assignment
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableLongStateOf
import java.util.Calendar
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext


@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEditScreen(
    unitId: String,
    topicId: String,
    assignmentId: String?,   // "new" = create, else edit
    navController: NavHostController,
    viewModel: AssignmentListViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AssignmentListViewModel(unitId, topicId) as T
        }
    })
) {
    val isNew = assignmentId == "new"

    val assignmentFlow =
        if (!isNew && assignmentId != null) viewModel.getAssignment(assignmentId) else flowOf(null)
    val assignment by assignmentFlow.collectAsState(initial = null)

    // --------- Form state ----------
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var status by remember { mutableStateOf("pending") } // pending | done
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Preload data if editing
    if (!isNew && assignment == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    } else {
        assignment?.let { existing ->
            LaunchedEffect(existing.id) {
                title = existing.title
                notes = existing.notes
                dueDate = existing.dueDate
                status = existing.status
            }
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = dueDate }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                Calendar.getInstance().apply {
                    set(y, m, d)
                    dueDate = timeInMillis
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isNew) "New Assignment" else "Edit Assignment")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- Title ----------
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Notes ----------
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            )

            // ---------- Status ----------
            Text("Status", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("pending", "done").forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { status = option }
                            .padding(end = 4.dp)
                    ) {
                        RadioButton(
                            selected = status == option,
                            onClick = { status = option }
                        )
                        Text(option.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // ---------- Due Date ----------
            Column {
                Text("Due Date", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { datePickerDialog.show() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        java.text.SimpleDateFormat("dd MMM yyyy")
                            .format(java.util.Date(dueDate))
                    )
                }
            }

            // ---------- Save Button ----------
            Button(
                onClick = {
                    isSaving = true
                    val newAssignment = Assignment(
                        id = assignment?.id ?: viewModel.generateId(),
                        title = title.trim(),
                        notes = notes.trim(),
                        dueDate = dueDate,
                        status = status
                    )
                    viewModel.addOrUpdateAssignment(
                        newAssignment,
                        onDone = {
                            isSaving = false
                            navController.popBackStack()
                        },
                        onError = {
                            isSaving = false
                            errorMessage = it
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isNew) "Create Assignment" else "Update Assignment")
                }
            }

            // ---------- Error Message ----------
            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


