package com.vex.ui.theme.screens.Home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vex.model.DueAssignment
import com.vex.navigation.ROUTE_PROFILE_SCREEN
import com.vex.navigation.ROUTE_UNITLIST
import androidx.compose.runtime.collectAsState
import com.vex.model.Topic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    var menuExpanded by remember { mutableStateOf(false) }
    val units by viewModel.unitsCount.collectAsState()
    val assignments by viewModel.assignmentsCount.collectAsState()
    val due by viewModel.dueAssignments.collectAsState()
    val topics by viewModel.dailyTopics.collectAsState(initial = emptyList())
    val unitNames by viewModel.unitNamesMap.collectAsState(initial = emptyMap())
    val profile by viewModel.profile.collectAsState()
    val firstName = profile.firstName.ifBlank { "Student" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(ROUTE_PROFILE_SCREEN)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                menuExpanded = false
                                onLogout()
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        HomeScreenContent(
            modifier = Modifier.padding(innerPadding),
            userFirstName = firstName,
            unitsCount = units,
            assignmentsCount = assignments,
            dueAssignments = due,
            dailyTopics = topics,
            unitNamesMap = unitNames,
            onAssignmentClick = { unitId, topicId, assignmentId ->
                navController.navigate("assignmentEdit/$unitId/$topicId/$assignmentId")
            },
            onTopicClick = { unitId, topicId ->
                navController.navigate("topicDetail/$unitId/$topicId")
            },
            onGoToUnits = { navController.navigate(ROUTE_UNITLIST) }
        )
    }
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    userFirstName: String,
    unitsCount: Int,
    assignmentsCount: Int,
    dueAssignments: List<DueAssignment>,
    dailyTopics: List<Pair<Topic, String>>,
    unitNamesMap: Map<String, String>,
    onAssignmentClick: (unitId: String, topicId: String, assignmentId: String) -> Unit,
    onTopicClick: (unitId: String, topicId: String) -> Unit,
    onGoToUnits: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GreetingCard(userFirstName)
        StatsRow(unitsCount, assignmentsCount)
        DueAssignmentsCard(dueAssignments, onAssignmentClick)
        UnitsCard(unitsCount, onGoToUnits)
        DailyRevisionCard(dailyTopics, unitNamesMap, onTopicClick)
    }
}

/* --- Individual sections, each with consistent styling --- */

@Composable
private fun GreetingCard(name: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "Hello, $name 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Here’s your study overview for today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatsRow(units: Int, assignments: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),   // optional side padding
        horizontalArrangement = Arrangement.spacedBy(16.dp) // gap between cards
    ) {
        StatBox(units, "Units", Modifier.weight(1f))
        StatBox(assignments, "Assignments", Modifier.weight(1f))
    }
}

@Composable
private fun StatBox(count: Int, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,                 // 👈 weight is passed in here
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
private fun DueAssignmentsCard(
    dueAssignments: List<DueAssignment>,
    onAssignmentClick: (unitId: String, topicId: String, assignmentId: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Assignments Due Soon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (dueAssignments.isEmpty()) {
                Text("No upcoming deadlines 🎉")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    dueAssignments.forEach { a ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAssignmentClick(a.unitId, a.topicId, a.assignmentId) },
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(a.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Lecturer: ${a.lecturer}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitsCard(units: Int, onGoToUnits: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoToUnits() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("View Units",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("You currently have $units units")
        }
    }
}

@Composable
fun DailyRevisionCard(
    dailyTopics: List<Pair<Topic, String>>,
    unitNamesMap: Map<String, String>,
    onTopicClick: (unitId: String, topicId: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Today's Revision Topics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (dailyTopics.isEmpty()) {
                Text("No topics to revise today 🎉")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dailyTopics.forEach { (topic, unitId) ->
                        val unitName = unitNamesMap[unitId] ?: "Unknown Unit"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTopicClick(unitId, topic.id) },
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(topic.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium)
                                Text("Unit: $unitName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}










