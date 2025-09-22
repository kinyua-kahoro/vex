package com.vex.ui.theme.screens.Profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.navigation.ROUTE_DASHBOARD
import com.vex.navigation.ROUTE_LOGIN
import com.vex.navigation.ROUTE_PROFILE_SETUP

@Composable
fun ProfileCheckScreen(navController: NavHostController, viewModel: ProfileViewModel = viewModel()) {
    var isLoading by remember { mutableStateOf(true) }
    var hasProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }

        FirebaseDatabase.getInstance().getReference("users")
            .child(uid)
            .child("profile")                      // 👈 go into profile node
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // check if the profile node exists and has a firstName
                    hasProfile = snapshot.exists() && snapshot.hasChild("firstName")
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LaunchedEffect(hasProfile) {
            val route = if (hasProfile) ROUTE_DASHBOARD else ROUTE_PROFILE_SETUP
            navController.navigate(route) {
                popUpTo("profileCheck") { inclusive = true }
            }
        }
    }
}

