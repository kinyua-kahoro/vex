package com.vex.ui.theme.screens.Profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    var firstName by remember { mutableStateOf(profile.firstName) }
    var secondName by remember { mutableStateOf(profile.secondName) }
    var university by remember { mutableStateOf(profile.university) }
    var regNumber by remember { mutableStateOf(profile.regNumber) }
    var levelOfStudy by remember { mutableStateOf(profile.levelOfStudy) }
    var courseOfStudy by remember { mutableStateOf(profile.courseOfStudy) }

    // Load data once when screen starts
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    // When profile updates (after load), refresh the text fields
    LaunchedEffect(profile) {
        firstName = profile.firstName
        secondName = profile.secondName
        university = profile.university
        regNumber = profile.regNumber
        levelOfStudy = profile.levelOfStudy
        courseOfStudy = profile.courseOfStudy
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = secondName,
                onValueChange = { secondName = it },
                label = { Text("Second Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = university,
                onValueChange = { university = it },
                label = { Text("University") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = regNumber,
                onValueChange = { regNumber = it },
                label = { Text("Registration Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = levelOfStudy,
                onValueChange = { levelOfStudy = it },
                label = { Text("Level of Study") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = courseOfStudy,
                onValueChange = { courseOfStudy = it },
                label = { Text("Course of Study") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.editProfile(
                        firstName = firstName,
                        secondName = secondName,
                        university = university,
                        regNumber = regNumber,
                        levelOfStudy = levelOfStudy,
                        courseOfStudy = courseOfStudy
                    ) { success ->
                        if (success) {
                            Toast.makeText(
                                navController.context,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack() // navigate back
                        } else {
                            Toast.makeText(
                                navController.context,
                                "Failed to update profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
