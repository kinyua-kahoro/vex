package com.vex.ui.theme.screens.Topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vex.model.Topic
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicEditScreen(
    unitId: String,
    navController: NavHostController,
    topicId: String?, // "new" means create
    viewModel: TopicListViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TopicListViewModel(unitId) as T
        }
    })
) {
    val isNew = topicId == "new"
    val topicFlow = if (!isNew) viewModel.getTopic(topicId!!) else flowOf(null)
    val topic by topicFlow.collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRevised by remember { mutableStateOf(false) }
    var priority by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pre-fill form when editing
    LaunchedEffect(topic) {
        topic?.let {
            title = it.title
            description = it.description
            isRevised = it.isRevised
            priority = it.priority
        }
    }

    val snackHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Add Topic" else "Edit Topic") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isNew) "Create a new topic" else "Update topic details",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Enter topic title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Brief details about the topic") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isRevised, onCheckedChange = { isRevised = it })
                Spacer(Modifier.width(8.dp))
                Text("Mark as revised")
            }

            OutlinedTextField(
                value = priority.toString(),
                onValueChange = { input ->
                    // keep only digits, parse to Int or default to 0, then clamp to 0–10
                    val num = input.filter { it.isDigit() }.toIntOrNull()?.coerceIn(0, 10) ?: 0
                    priority = num
                },
                label = { Text("Priority") },
                placeholder = { Text("0 = lowest priority") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    isSaving = true
                    val newTopic = Topic(
                        id = topic?.id ?: viewModel.generateId(),
                        title = title.trim(),
                        description = description.trim(),
                        isRevised = isRevised,
                        priority = priority
                    )
                    viewModel.addOrUpdateTopic(
                        newTopic,
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
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isNew) "Create Topic" else "Update Topic")
            }
        }
    }
}

