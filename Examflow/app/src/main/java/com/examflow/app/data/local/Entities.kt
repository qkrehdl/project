package com.examflow.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.examflow.app.domain.model.ScheduleMode
import java.time.LocalDate

@Entity(tableName = "fixed_schedules")
data class FixedScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,
    val startMinute: Int,
    val endMinute: Int,
    val title: String,
    val isAvailable: Boolean
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val examDate: LocalDate,
    val priority: Int
)

@Entity(
    tableName = "exam_topics",
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("examId")]
)
data class ExamTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val name: String,
    val pageCount: Int
)

@Entity(
    tableName = "study_plans",
    indices = [Index("date"), Index("examId"), Index("topicId")]
)
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val startMinute: Int,
    val endMinute: Int,
    val examId: Long,
    val topicId: Long?,
    val subjectName: String,
    val topicName: String,
    val pageCount: Int,
    val isReview: Boolean
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val scheduleMode: ScheduleMode = ScheduleMode.BALANCED
)
