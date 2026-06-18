package com.examflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.examflow.app.ExamFlowViewModel
import com.examflow.app.R
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(viewModel: ExamFlowViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()
    val lastExamDate = state.exams.maxOfOrNull { it.exam.examDate } ?: today.plusMonths(1)
    val calendarState = rememberCalendarState(
        startMonth = YearMonth.from(today.minusMonths(1)),
        endMonth = YearMonth.from(lastExamDate.plusMonths(1)),
        firstVisibleMonth = YearMonth.from(today),
        firstDayOfWeek = daysOfWeek().first()
    )
    val studyDates = state.studyPlans.map { it.date }.toSet()
    val examDates = state.exams.map { it.exam.examDate }.toSet()
    val selectedPlans = state.studyPlans.filter { it.date == selectedDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.calendar_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            QuietCard {
                HorizontalCalendar(
                    state = calendarState,
                    monthHeader = { month ->
                        Text(
                            text = stringResource(R.string.month_year_format, month.yearMonth.year, month.yearMonth.monthValue),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    },
                    dayContent = { day ->
                        CalendarDayCell(
                            day = day,
                            selected = day.date == selectedDate,
                            hasPlan = day.date in studyDates,
                            hasExam = day.date in examDates,
                            onClick = { selectedDate = day.date }
                        )
                    }
                )
            }
            SectionTitle(stringResource(R.string.month_day_format, selectedDate.monthValue, selectedDate.dayOfMonth))
        }
        if (selectedPlans.isEmpty()) {
            item {
                QuietCard {
                    Text(stringResource(R.string.empty_calendar), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(selectedPlans) { plan ->
                QuietCard {
                    Text(
                        stringResource(R.string.time_range_format, formatMinute(plan.startMinute), formatMinute(plan.endMinute)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(plan.subjectName, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (plan.isReview) "${plan.topicName} ${stringResource(R.string.review)}" else plan.topicName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!plan.isReview) Text(stringResource(R.string.pages_format, plan.pageCount), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    selected: Boolean,
    hasPlan: Boolean,
    hasExam: Boolean,
    onClick: () -> Unit
) {
    val inMonth = day.position == DayPosition.MonthDate
    val background = when {
        selected || hasExam -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val textColor = when {
        selected || hasExam -> Color.White
        inMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }
    Column(
        modifier = Modifier.height(42.dp).clickable(enabled = inMonth, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(background),
            contentAlignment = Alignment.Center
        ) {
            Text(day.date.dayOfMonth.toString(), color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(2.dp))
        Box(Modifier.size(4.dp).clip(CircleShape).background(if (hasPlan && inMonth) MaterialTheme.colorScheme.primary else Color.Transparent))
    }
}
