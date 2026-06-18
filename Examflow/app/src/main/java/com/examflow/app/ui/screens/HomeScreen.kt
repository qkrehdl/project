package com.examflow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.examflow.app.ExamFlowViewModel
import com.examflow.app.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(viewModel: ExamFlowViewModel) {
    val state by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val todayPlans = state.studyPlans.filter { it.date == today }
    val upcomingExams = state.exams
        .filter { !it.exam.examDate.isBefore(today) }
        .sortedBy { it.exam.examDate }
        .take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            SectionTitle(stringResource(R.string.today_study))
        }
        if (todayPlans.isEmpty()) {
            item { QuietCard { Text(stringResource(R.string.empty_today), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            items(todayPlans) { plan ->
                QuietCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                stringResource(
                                    R.string.checked_topic_format,
                                    if (plan.isReview) "${plan.topicName} ${stringResource(R.string.review)}" else plan.topicName
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(plan.subjectName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(if (plan.isReview) stringResource(R.string.review) else stringResource(R.string.pages_format, plan.pageCount))
                    }
                }
            }
        }
        item {
            SectionTitle(stringResource(R.string.upcoming_exams))
            if (upcomingExams.isEmpty()) {
                QuietCard { Text(stringResource(R.string.empty_exam), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        items(upcomingExams) { exam ->
            val dDay = ChronoUnit.DAYS.between(today, exam.exam.examDate)
            QuietCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(exam.exam.subjectName, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(exam.exam.examDate.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(stringResource(R.string.dday_format, dDay), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
