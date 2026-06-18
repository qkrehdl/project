package com.examflow.app.ui.navigation

import androidx.annotation.StringRes
import com.examflow.app.R

sealed class ExamFlowDestination(val route: String, @StringRes val labelRes: Int, val mark: String) {
    data object Calendar : ExamFlowDestination("calendar", R.string.tab_calendar, "●")
    data object Schedule : ExamFlowDestination("schedule", R.string.tab_schedule, "■")
    data object Home : ExamFlowDestination("home", R.string.tab_home, "◆")
    data object Settings : ExamFlowDestination("settings", R.string.tab_settings, "○")
}

val bottomDestinations = listOf(
    ExamFlowDestination.Calendar,
    ExamFlowDestination.Schedule,
    ExamFlowDestination.Home,
    ExamFlowDestination.Settings
)
