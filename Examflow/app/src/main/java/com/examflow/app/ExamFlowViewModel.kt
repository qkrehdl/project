package com.examflow.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.examflow.app.data.local.ExamEntity
import com.examflow.app.data.local.ExamTopicEntity
import com.examflow.app.data.local.ExamWithTopics
import com.examflow.app.data.local.FixedScheduleEntity
import com.examflow.app.data.local.StudyPlanEntity
import com.examflow.app.data.repository.ExamFlowRepository
import com.examflow.app.domain.model.ScheduleMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExamFlowUiState(
    val fixedSchedules: List<FixedScheduleEntity> = emptyList(),
    val exams: List<ExamWithTopics> = emptyList(),
    val studyPlans: List<StudyPlanEntity> = emptyList(),
    val scheduleMode: ScheduleMode = ScheduleMode.BALANCED,
    val message: String? = null
)

class ExamFlowViewModel(private val repository: ExamFlowRepository) : ViewModel() {
    val uiState: StateFlow<ExamFlowUiState> = combine(
        repository.fixedSchedules,
        repository.exams,
        repository.studyPlans,
        repository.settings
    ) { fixed, exams, plans, settings ->
        ExamFlowUiState(fixed, exams, plans, settings.scheduleMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamFlowUiState())

    init {
        viewModelScope.launch { repository.ensureDefaultSettings() }
    }

    fun saveFixedSchedule(entity: FixedScheduleEntity) = viewModelScope.launch {
        repository.saveFixedSchedule(entity)
    }

    fun deleteFixedSchedule(id: Long) = viewModelScope.launch { repository.deleteFixedSchedule(id) }

    fun saveExam(entity: ExamEntity, onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        onSaved(repository.saveExam(entity))
    }

    fun deleteExam(id: Long) = viewModelScope.launch { repository.deleteExam(id) }

    fun saveTopic(entity: ExamTopicEntity) = viewModelScope.launch { repository.saveTopic(entity) }

    fun deleteTopic(id: Long) = viewModelScope.launch { repository.deleteTopic(id) }

    fun updateMode(mode: ScheduleMode) = viewModelScope.launch { repository.updateMode(mode) }

    fun generatePlans(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        onResult(repository.generatePlans())
    }

    fun resetAll(onDone: () -> Unit) = viewModelScope.launch {
        repository.resetAll()
        onDone()
    }

    fun exportJson(onDone: (String) -> Unit) = viewModelScope.launch {
        onDone(repository.exportJson())
    }

    fun importJson(text: String, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        runCatching { repository.importJson(text) }.onSuccess { onDone(true) }.onFailure { onDone(false) }
    }

    fun plansFor(date: LocalDate): List<StudyPlanEntity> = uiState.value.studyPlans.filter { it.date == date }

    companion object {
        fun factory(repository: ExamFlowRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExamFlowViewModel(repository) as T
                }
            }
    }
}
