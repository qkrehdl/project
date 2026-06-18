package com.examflow.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.examflow.app.domain.model.ScheduleMode
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExamFlowDao {
    @Query("SELECT * FROM fixed_schedules ORDER BY dayOfWeek, startMinute")
    fun observeFixedSchedules(): Flow<List<FixedScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFixedSchedule(entity: FixedScheduleEntity): Long

    @Query("DELETE FROM fixed_schedules WHERE id = :id")
    suspend fun deleteFixedSchedule(id: Long)

    @Transaction
    @Query("SELECT * FROM exams ORDER BY examDate, priority DESC")
    fun observeExamsWithTopics(): Flow<List<ExamWithTopics>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExam(entity: ExamEntity): Long

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExam(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTopic(entity: ExamTopicEntity): Long

    @Query("DELETE FROM exam_topics WHERE id = :id")
    suspend fun deleteTopic(id: Long)

    @Query("SELECT * FROM study_plans ORDER BY date, startMinute")
    fun observeStudyPlans(): Flow<List<StudyPlanEntity>>

    @Query("SELECT * FROM study_plans WHERE date = :date ORDER BY startMinute")
    fun observeStudyPlansForDate(date: LocalDate): Flow<List<StudyPlanEntity>>

    @Query("DELETE FROM study_plans")
    suspend fun clearStudyPlans()

    @Insert
    suspend fun insertStudyPlans(plans: List<StudyPlanEntity>)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("UPDATE app_settings SET scheduleMode = :mode WHERE id = 1")
    suspend fun updateScheduleMode(mode: ScheduleMode)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultSettings(settings: AppSettingsEntity = AppSettingsEntity())

    @Query("DELETE FROM fixed_schedules")
    suspend fun clearFixedSchedules()

    @Query("DELETE FROM exams")
    suspend fun clearExams()

    @Query("DELETE FROM app_settings")
    suspend fun clearSettings()
}
