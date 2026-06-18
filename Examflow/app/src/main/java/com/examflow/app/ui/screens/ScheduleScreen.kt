package com.examflow.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.examflow.app.ExamFlowViewModel
import com.examflow.app.R
import com.examflow.app.data.local.ExamEntity
import com.examflow.app.data.local.ExamTopicEntity
import com.examflow.app.data.local.ExamWithTopics
import com.examflow.app.data.local.FixedScheduleEntity
import com.examflow.app.domain.model.ScheduleMode
import java.time.LocalDate

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ScheduleScreen(viewModel: ExamFlowViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var editingFixed by remember { mutableStateOf<FixedScheduleEntity?>(null) }
    var showFixedDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<ExamEntity?>(null) }
    var showExamDialog by remember { mutableStateOf(false) }
    var editingTopic by remember { mutableStateOf<Pair<Long, ExamTopicEntity?>?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.schedule_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            SectionTitle(stringResource(R.string.fixed_schedule)) {
                TextButton(onClick = {
                    editingFixed = null
                    showFixedDialog = true
                }) { Text(stringResource(R.string.add)) }
            }
        }
        if (state.fixedSchedules.isEmpty()) {
            item { QuietCard { Text(stringResource(R.string.empty_fixed), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            items(state.fixedSchedules) { schedule ->
                QuietCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(dayLabel(schedule.dayOfWeek), fontWeight = FontWeight.SemiBold)
                            Text("${stringResource(R.string.time_range_format, formatMinute(schedule.startMinute), formatMinute(schedule.endMinute))}  ${schedule.title}")
                            Text(
                                if (schedule.isAvailable) stringResource(R.string.available_time) else stringResource(R.string.busy_time),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            TextButton(onClick = {
                                editingFixed = schedule
                                showFixedDialog = true
                            }) { Text(stringResource(R.string.edit)) }
                            TextButton(onClick = { viewModel.deleteFixedSchedule(schedule.id) }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
        item {
            SectionTitle(stringResource(R.string.exam_input)) {
                TextButton(onClick = {
                    editingExam = null
                    showExamDialog = true
                }) { Text(stringResource(R.string.add_exam)) }
            }
        }
        if (state.exams.isEmpty()) {
            item { QuietCard { Text(stringResource(R.string.empty_exam), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            items(state.exams) { exam ->
                ExamCard(
                    exam = exam,
                    onEditExam = {
                        editingExam = exam.exam
                        showExamDialog = true
                    },
                    onDeleteExam = { viewModel.deleteExam(exam.exam.id) },
                    onAddTopic = { editingTopic = exam.exam.id to null },
                    onEditTopic = { topic -> editingTopic = exam.exam.id to topic },
                    onDeleteTopic = { viewModel.deleteTopic(it.id) }
                )
            }
        }
        item {
            SectionTitle(stringResource(R.string.schedule_mode))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.scheduleMode == mode,
                        onClick = { viewModel.updateMode(mode) },
                        label = { Text(modeLabel(mode)) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.generatePlans { success ->
                        Toast.makeText(
                            context,
                            context.getString(if (success) R.string.generated else R.string.need_data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) { Text(stringResource(R.string.generate_schedule)) }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showFixedDialog) {
        FixedScheduleDialog(
            initial = editingFixed,
            onDismiss = { showFixedDialog = false },
            onSave = {
                viewModel.saveFixedSchedule(it)
                showFixedDialog = false
            }
        )
    }
    if (showExamDialog) {
        ExamDialog(
            initial = editingExam,
            onDismiss = { showExamDialog = false },
            onSave = {
                viewModel.saveExam(it)
                showExamDialog = false
            }
        )
    }
    editingTopic?.let { (examId, topic) ->
        TopicDialog(
            examId = examId,
            initial = topic,
            onDismiss = { editingTopic = null },
            onSave = {
                viewModel.saveTopic(it)
                editingTopic = null
            }
        )
    }
}

@Composable
private fun ExamCard(
    exam: ExamWithTopics,
    onEditExam: () -> Unit,
    onDeleteExam: () -> Unit,
    onAddTopic: () -> Unit,
    onEditTopic: (ExamTopicEntity) -> Unit,
    onDeleteTopic: (ExamTopicEntity) -> Unit
) {
    QuietCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(exam.exam.subjectName, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.exam_date_format, exam.exam.examDate.toString()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.priority_format, exam.exam.priority), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEditExam) { Text(stringResource(R.string.edit)) }
                TextButton(onClick = onDeleteExam) { Text(stringResource(R.string.delete)) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.topic), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onAddTopic) { Text(stringResource(R.string.add_topic)) }
        }
        exam.topics.forEach { topic ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.topic_pages_format, topic.name, topic.pageCount))
                Row {
                    TextButton(onClick = { onEditTopic(topic) }) { Text(stringResource(R.string.edit)) }
                    TextButton(onClick = { onDeleteTopic(topic) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FixedScheduleDialog(
    initial: FixedScheduleEntity?,
    onDismiss: () -> Unit,
    onSave: (FixedScheduleEntity) -> Unit
) {
    var day by remember { mutableStateOf(initial?.dayOfWeek ?: 1) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var start by remember { mutableStateOf(initial?.startMinute?.let(::formatMinute) ?: "09:00") }
    var end by remember { mutableStateOf(initial?.endMinute?.let(::formatMinute) ?: "18:00") }
    var available by remember { mutableStateOf(initial?.isAvailable ?: false) }
    val titleText = stringResource(if (initial == null) R.string.add_fixed_schedule else R.string.edit_fixed_schedule)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { value ->
                        AssistChip(onClick = { day = value }, label = { Text(dayLabel(value)) }, border = null)
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) })
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text(stringResource(R.string.start_time)) })
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text(stringResource(R.string.end_time)) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.available_time), modifier = Modifier.weight(1f))
                    Switch(checked = available, onCheckedChange = { available = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val startMinute = parseMinute(start)
                val endMinute = parseMinute(end)
                if (title.isNotBlank() && startMinute != null && endMinute != null && endMinute > startMinute) {
                    onSave(FixedScheduleEntity(initial?.id ?: 0, day, startMinute, endMinute, title.trim(), available))
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun ExamDialog(initial: ExamEntity?, onDismiss: () -> Unit, onSave: (ExamEntity) -> Unit) {
    var subject by remember { mutableStateOf(initial?.subjectName ?: "") }
    var date by remember { mutableStateOf(initial?.examDate?.toString() ?: LocalDate.now().plusDays(7).toString()) }
    var priority by remember { mutableStateOf((initial?.priority ?: 3).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_exam else R.string.edit_exam)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.subject_name)) })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(R.string.exam_date)) })
                OutlinedTextField(value = priority, onValueChange = { priority = it }, label = { Text(stringResource(R.string.priority)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
                val parsedPriority = priority.toIntOrNull()?.coerceIn(1, 5)
                if (subject.isNotBlank() && parsedDate != null && parsedPriority != null) {
                    onSave(ExamEntity(initial?.id ?: 0, subject.trim(), parsedDate, parsedPriority))
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun TopicDialog(
    examId: Long,
    initial: ExamTopicEntity?,
    onDismiss: () -> Unit,
    onSave: (ExamTopicEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var pages by remember { mutableStateOf((initial?.pageCount ?: 100).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_topic else R.string.edit_topic)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.topic_name)) })
                OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text(stringResource(R.string.page_count)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val pageCount = pages.toIntOrNull()
                if (name.isNotBlank() && pageCount != null && pageCount > 0) {
                    onSave(ExamTopicEntity(initial?.id ?: 0, examId, name.trim(), pageCount))
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun parseMinute(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}
