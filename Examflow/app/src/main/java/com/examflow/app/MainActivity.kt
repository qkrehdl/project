package com.examflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.examflow.app.ui.navigation.ExamFlowDestination
import com.examflow.app.ui.navigation.bottomDestinations
import com.examflow.app.ui.screens.CalendarScreen
import com.examflow.app.ui.screens.HomeScreen
import com.examflow.app.ui.screens.ScheduleScreen
import com.examflow.app.ui.screens.SettingsScreen
import com.examflow.app.ui.theme.ExamFlowTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ExamFlowViewModel by viewModels {
        ExamFlowViewModel.factory((application as ExamFlowApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExamFlowTheme {
                ExamFlowApp(viewModel)
            }
        }
    }
}

@Composable
private fun ExamFlowApp(viewModel: ExamFlowViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: ExamFlowDestination.Home.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(destination.mark) },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ExamFlowDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ExamFlowDestination.Calendar.route) { CalendarScreen(viewModel) }
            composable(ExamFlowDestination.Schedule.route) { ScheduleScreen(viewModel) }
            composable(ExamFlowDestination.Home.route) { HomeScreen(viewModel) }
            composable(ExamFlowDestination.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}
