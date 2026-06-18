package com.examflow.app.data.repository

import com.examflow.app.data.local.AppSettingsEntity
import com.examflow.app.data.local.ExamEntity
import com.examflow.app.data.local.ExamFlowDao
import com.examflow.app.data.local.ExamTopicEntity
import com.examflow.app.data.local.ExamWithTopics
import com.examflow.app.data.local.FixedScheduleEntity
import com.examflow.app.data.local.StudyPlanEntity
import com.examflow.app.domain.model.ScheduleMode
import com.examflow.app.domain.scheduler.ScheduleGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class ExamFlowRepository(
    private val dao: ExamFlowDao,
    private val generator: ScheduleGenerator = ScheduleGenerator()
) {
    val fixedSchedules: Flow<List<FixedScheduleEntity>> = dao.observeFixedSchedules()
    val exams: Flow<List<ExamWithTopics>> = dao.observeExamsWithTopics()
    val studyPlans: Flow<List<StudyPlanEntity>> = dao.observeStudyPlans()
    val settings: Flow<AppSettingsEntity> = dao.observeSettings().map { it ?: AppSettingsEntity() }

    suspend fun ensureDefaultSettings() = dao.insertDefaultSettings()

    suspend fun saveFixedSchedule(entity: FixedScheduleEntity) = dao.upsertFixedSchedule(entity)
    suspend fun deleteFixedSchedule(id: Long) = dao.deleteFixedSchedule(id)
    suspend fun saveExam(entity: ExamEntity) = dao.upsertExam(entity)
    suspend fun deleteExam(id: Long) = dao.deleteExam(id)
    suspend fun saveTopic(entity: ExamTopicEntity) = dao.upsertTopic(entity)
    suspend fun deleteTopic(id: Long) = dao.deleteTopic(id)
    suspend fun updateMode(mode: ScheduleMode) = dao.updateScheduleMode(mode)

    suspend fun generatePlans(): Boolean {
        val fixed = fixedSchedules.first()
        val examList = exams.first()
        val mode = settings.first().scheduleMode
        val plans = generator.generate(fixed, examList, mode, LocalDate.now())
        dao.clearStudyPlans()
        if (plans.isNotEmpty()) dao.insertStudyPlans(plans)
        return plans.isNotEmpty()
    }

    suspend fun resetAll() {
        dao.clearStudyPlans()
        dao.clearFixedSchedules()
        dao.clearExams()
        dao.clearSettings()
        dao.insertDefaultSettings()
    }

    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("mode", settings.first().scheduleMode.name)
        root.put("fixedSchedules", JSONArray().also { array ->
            fixedSchedules.first().forEach {
                array.put(JSONObject()
                    .put("id", it.id)
                    .put("dayOfWeek", it.dayOfWeek)
                    .put("startMinute", it.startMinute)
                    .put("endMinute", it.endMinute)
                    .put("title", it.title)
                    .put("isAvailable", it.isAvailable))
            }
        })
        root.put("exams", JSONArray().also { array ->
            exams.first().forEach {
                array.put(JSONObject()
                    .put("id", it.exam.id)
                    .put("subjectName", it.exam.subjectName)
                    .put("examDate", it.exam.examDate.toString())
                    .put("priority", it.exam.priority)
                    .put("topics", JSONArray().also { topics ->
                        it.topics.forEach { topic ->
                            topics.put(JSONObject()
                                .put("id", topic.id)
                                .put("name", topic.name)
                                .put("pageCount", topic.pageCount))
                        }
                    }))
            }
        })
        return root.toString()
    }

    suspend fun importJson(text: String) {
        val root = JSONObject(text)
        resetAll()
        dao.updateScheduleMode(ScheduleMode.valueOf(root.optString("mode", ScheduleMode.BALANCED.name)))
        val fixed = root.optJSONArray("fixedSchedules") ?: JSONArray()
        for (i in 0 until fixed.length()) {
            val item = fixed.getJSONObject(i)
            dao.upsertFixedSchedule(FixedScheduleEntity(
                dayOfWeek = item.getInt("dayOfWeek"),
                startMinute = item.getInt("startMinute"),
                endMinute = item.getInt("endMinute"),
                title = item.getString("title"),
                isAvailable = item.getBoolean("isAvailable")
            ))
        }
        val examsArray = root.optJSONArray("exams") ?: JSONArray()
        for (i in 0 until examsArray.length()) {
            val item = examsArray.getJSONObject(i)
            val newExamId = dao.upsertExam(ExamEntity(
                subjectName = item.getString("subjectName"),
                examDate = LocalDate.parse(item.getString("examDate")),
                priority = item.getInt("priority")
            ))
            val topics = item.optJSONArray("topics") ?: JSONArray()
            for (j in 0 until topics.length()) {
                val topic = topics.getJSONObject(j)
                dao.upsertTopic(ExamTopicEntity(
                    examId = newExamId,
                    name = topic.getString("name"),
                    pageCount = topic.getInt("pageCount")
                ))
            }
        }
        generatePlans()
    }
}
