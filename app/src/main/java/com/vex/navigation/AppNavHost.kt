package com.vex.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vex.ui.theme.screens.UnitList.UnitListViewModel
import com.vex.ui.theme.screens.UnitList.UnitList_Screen
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.vex.ui.theme.screens.Auth.AuthViewModel
import com.vex.ui.theme.screens.Auth.LoginScreen
import com.vex.ui.theme.screens.Auth.RegisterScreen
import com.vex.ui.theme.screens.Home.HomeViewModel
import com.vex.ui.theme.screens.UnitDetails.UnitDetailScreen
import com.vex.ui.theme.screens.UnitDetails.UnitEditScreen
import com.vex.ui.theme.screens.UnitDetails.UnitEditViewModel
import com.vex.ui.theme.screens.Assignment.AssignmentEditScreen
import com.vex.ui.theme.screens.Assignment.AssignmentListScreen
import com.vex.ui.theme.screens.Home.DashboardScreen
import com.vex.ui.theme.screens.Profile.ProfileCheckScreen
import com.vex.ui.theme.screens.Profile.ProfileEditScreen
import com.vex.ui.theme.screens.Profile.ProfileScreen
import com.vex.ui.theme.screens.Profile.ProfileSetupScreen
import com.vex.ui.theme.screens.Topic.TopicDetailScreen
import com.vex.ui.theme.screens.Topic.TopicEditScreen
import com.vex.ui.theme.screens.Topic.TopicListScreen
import com.vex.ui.theme.screens.Topic.TopicListViewModel
import com.vex.ui.theme.screens.UnitDetails.UnitDetailViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = ROUTE_PROFILE_CHECK,
    viewModel: AuthViewModel
) {
    val navController = rememberNavController()   // create here
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(ROUTE_UNITLIST) {
            val vm: UnitListViewModel = viewModel()
            val units by vm.units.collectAsState()
            UnitList_Screen(
                navController = navController,
                units = units,
                onUnitClick = { unit -> navController.navigate("unitDetail/${unit.id}") },
                viewModel = viewModel()
            )
        }

        composable(ROUTE_REGISTER) {
            RegisterScreen(
                viewModel = viewModel,
                onRegistered = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_REGISTER) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(ROUTE_REGISTER)
                }
            )
        }
        composable(ROUTE_DASHBOARD) {
            val homeViewModel: HomeViewModel = viewModel()

            DashboardScreen(
                navController = navController,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_DASHBOARD) { inclusive = true }
                    }
                },
                viewModel = homeViewModel        // pass the same ViewModel
            )
        }

        composable(
            route = "topicDetail/{unitId}/{topicId}",
            arguments = listOf(
                navArgument("unitId") { type = NavType.StringType },
                navArgument("topicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable

            TopicDetailScreen(unitId = unitId, topicId = topicId, navController = navController)
        }



        composable(
            route = ROUTE_UNIT_DETAIL,
            arguments = listOf(navArgument("unitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable

            // Create the ViewModel using unitId
            val viewModel: UnitDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return UnitDetailViewModel(unitId) as T
                    }
                }
            )

            UnitDetailScreen(
                navController = navController,
                viewModel = viewModel
            )
        }



        composable(
            route = ROUTE_UNIT_EDIT,
            arguments = listOf(navArgument("unitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val vm: UnitEditViewModel = viewModel()

            if (unitId == "new") {
                UnitEditScreen(
                    navController = navController,
                    viewModel = vm,
                    unitId = null // create mode
                )
            } else {
                val unitNullable by vm.getUnit(unitId).collectAsState(initial = null)

                if (unitNullable == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    UnitEditScreen(
                        navController = navController,
                        viewModel = vm,
                        unitId = unitNullable?.id ?: return@composable
                    )
                }
            }
        }
        composable(
            route = ROUTE_TOPIC_LIST,
            arguments = listOf(navArgument("unitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable

            val viewModel: TopicListViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return TopicListViewModel(unitId) as T
                    }
                }
            )

            TopicListScreen(
                unitId = unitId,
                navController = navController,
                viewModel = viewModel
            )
        }


        composable(ROUTE_TOPIC_EDIT) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val topicId = backStackEntry.arguments?.getString("topicId") ?: "new"
            TopicEditScreen(unitId = unitId, topicId = topicId, navController = navController)
        }
        // Assignment List
        composable(
            route = ROUTE_ASSIGNMENT_LIST,
            arguments = listOf(
                navArgument("unitId") { type = NavType.StringType },
                navArgument("topicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable

            AssignmentListScreen(unitId = unitId, topicId = topicId, navController = navController)
        }

// Assignment Edit
        composable(
            route = ROUTE_ASSIGNMENT_EDIT,
            arguments = listOf(
                navArgument("unitId") { type = NavType.StringType },
                navArgument("topicId") { type = NavType.StringType },
                navArgument("assignmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
            val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: return@composable

            AssignmentEditScreen(
                unitId = unitId,
                topicId = topicId,
                assignmentId = assignmentId,
                navController = navController
            )
        }
        composable(ROUTE_PROFILE_CHECK) { ProfileCheckScreen(navController) }
        composable(ROUTE_PROFILE_SCREEN) { ProfileScreen(navController) }
        composable(ROUTE_PROFILE_SETUP) {
            ProfileSetupScreen(onProfileSaved = {
                navController.navigate(ROUTE_DASHBOARD) {
                    popUpTo(ROUTE_DASHBOARD) { inclusive = true }
                }
            })
        }
        composable(ROUTE_PROFILE_EDIT) {
            ProfileEditScreen(navController)
        }
    }
}
