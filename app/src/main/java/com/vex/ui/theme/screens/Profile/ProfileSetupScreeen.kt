package com.vex.ui.theme.screens.Profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vex.model.UserProfile

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileViewModel = viewModel(),
    onProfileSaved: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var levelOfStudy by remember { mutableStateOf("") }
    var courseOfStudy by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Complete Your Profile", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") })
        OutlinedTextField(value = secondName, onValueChange = { secondName = it }, label = { Text("Second Name") })
        OutlinedTextField(value = university, onValueChange = { university = it }, label = { Text("University") })
        OutlinedTextField(value = regNumber, onValueChange = { regNumber = it }, label = { Text("Registration Number") })
        OutlinedTextField(value = levelOfStudy, onValueChange = { levelOfStudy = it }, label = { Text("Level of Study") })
        OutlinedTextField(value = courseOfStudy, onValueChange = { courseOfStudy = it }, label = { Text("Course of Study") })

        Button(
            onClick = {
                val profile = UserProfile(
                    firstName, secondName, university,
                    regNumber, levelOfStudy, courseOfStudy
                )
                viewModel.saveProfile(profile) { success ->
                    if (success) onProfileSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }
    }
}
