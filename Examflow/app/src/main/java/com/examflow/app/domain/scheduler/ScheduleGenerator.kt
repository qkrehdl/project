package com.examflow.app.domain.scheduler

import com.examflow.app.data.local.ExamTopicEntity
import com.examflow.app.data.local.ExamWithTopics
import com.examflow.app.data.local.FixedScheduleEntity
import com.examflow.app.data.local.StudyPlanEntity
import com.examflow.app.domain.model.ScheduleMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class ScheduleGenerator(
    private val priorityCalculator: PriorityCalculator = PriorityCalculator(),
    private val reviewAllocator: ReviewAllocator = ReviewAllocator()
) {
    fun generate(
        fixedSchedules: List<FixedScheduleEntity>,
        exams: List<ExamWithTopics>,
        mode: ScheduleMode,
        today: LocalDate = LocalDate.now()
    ): List<StudyPlanEntity> {
        val usableExams = exams
            .filter { !it.exam.examDate.isBefore(today) && it.topics.isNotEmpty() }
            .sortedBy { it.exam.examDate }
        if (usableExams.isEmpty()) return emptyList()

        val lastExamDate = usableExams.maxOf { it.exam.examDate }
        val strategy = ScheduleModeStrategy.from(mode)
        val freeBlocks = createFreeBlocks(fixedSchedules, today, lastExamDate, strategy.maxDailyBlocks)
        if (freeBlocks.isEmpty()) return emptyList()

        val totalBlocks = freeBlocks.size
        val regularTarget = reviewAllocator.regularBlocks(totalBlocks)
        val examBlocks = allocateExamBlocks(usableExams, today, regularTarget, strategy)
        val regularSessions = buildRegularSessions(usableExams, examBlocks)
        val reviewSessions = buildReviewSessions(usableExams, totalBlocks - regularSessions.size)

        val queue = if (strategy.preferSubjectStreaks) {
            regularSessions.sortedWith(compareBy<SessionRequest> { it.exam.exam.examDate }.thenBy { it.topic.name }) + reviewSessions
        } else {
            interleave(regularSessions) + reviewSessions
        }.toMutableList()

        val plans = mutableListOf<StudyPlanEntity>()
        for (block in freeBlocks) {
            val index = chooseSessionIndex(queue, block.date)
            if (index == -1) continue
            val session = queue.removeAt(index)
            plans += session.toPlan(block)
        }
        return plans.sortedWith(compareBy<StudyPlanEntity> { it.date }.thenBy { it.startMinute })
    }

    private fun createFreeBlocks(
        fixedSchedules: List<FixedScheduleEntity>,
        today: LocalDate,
        lastExamDate: LocalDate,
        maxDailyBlocks: Int
    ): List<TimeBlock> {
        val available = fixedSchedules.filter { it.isAvailable && it.endMinute - it.startMinute >= 60 }
        val blocks = mutableListOf<TimeBlock>()
        val dayCount = ChronoUnit.DAYS.between(today, lastExamDate).toInt()
        for (offset in 0..dayCount) {
            val date = today.plusDays(offset.toLong())
            val daySchedules = available.filter { it.dayOfWeek == date.dayOfWeek.value }
            var count = 0
            for (schedule in daySchedules.sortedBy { it.startMinute }) {
                var minute = schedule.startMinute
                while (minute + 60 <= schedule.endMinute && count < maxDailyBlocks) {
                    blocks += TimeBlock(date, minute, minute + 60)
                    minute += 60
                    count++
                }
                if (count >= maxDailyBlocks) break
            }
        }
        return blocks
    }

    private fun allocateExamBlocks(
        exams: List<ExamWithTopics>,
        today: LocalDate,
        regularTarget: Int,
        strategy: ScheduleModeStrategy
    ): Map<Long, Int> {
        val scores = exams.associate { it.exam.id to priorityCalculator.examScore(it, today, strategy.urgentBoost) }
        val totalScore = scores.values.sum().takeIf { it > 0.0 } ?: return emptyMap()
        val initial = exams.associate { exam ->
            exam.exam.id to max(1, (regularTarget * (scores.getValue(exam.exam.id) / totalScore)).roundToInt())
        }.toMutableMap()
        while (initial.values.sum() > regularTarget && initial.values.any { it > 1 }) {
            val key = initial.maxBy { it.value }.key
            initial[key] = initial.getValue(key) - 1
        }
        while (initial.values.sum() < regularTarget) {
            val key = scores.maxBy { it.value }.key
            initial[key] = initial.getValue(key) + 1
        }
        return initial
    }

    private fun buildRegularSessions(
        exams: List<ExamWithTopics>,
        examBlocks: Map<Long, Int>
    ): List<SessionRequest> {
        val result = mutableListOf<SessionRequest>()
        for (exam in exams) {
            val blocks = examBlocks[exam.exam.id] ?: 0
            val totalPages = exam.topics.sumOf { it.pageCount }.coerceAtLeast(1)
            val topicBlocks = exam.topics.associateWith { topic ->
                max(1, (blocks * (topic.pageCount.toDouble() / totalPages)).roundToInt())
            }.toMutableMap()
            while (topicBlocks.values.sum() > blocks && topicBlocks.values.any { it > 1 }) {
                val key = topicBlocks.maxBy { it.value }.key
                topicBlocks[key] = topicBlocks.getValue(key) - 1
            }
            for ((topic, count) in topicBlocks) {
                val pagesPerBlock = ceil(topic.pageCount.toDouble() / count.coerceAtLeast(1)).toInt()
                repeat(count) {
                    result += SessionRequest(exam, topic, pagesPerBlock, isReview = false)
                }
            }
        }
        return result
    }

    private fun buildReviewSessions(exams: List<ExamWithTopics>, count: Int): List<SessionRequest> {
        if (count <= 0) return emptyList()
        val pool = exams.flatMap { exam ->
            exam.topics.map { topic -> SessionRequest(exam, topic, pageCount = 0, isReview = true) }
        }.sortedBy { it.exam.exam.examDate }
        if (pool.isEmpty()) return emptyList()
        return List(count) { pool[it % pool.size] }
    }

    private fun interleave(sessions: List<SessionRequest>): List<SessionRequest> {
        val grouped = sessions.groupBy { it.exam.exam.id }.mapValues { it.value.toMutableList() }.toMutableMap()
        val result = mutableListOf<SessionRequest>()
        while (grouped.isNotEmpty()) {
            val keys = grouped.keys.toList()
            for (key in keys) {
                val list = grouped[key] ?: continue
                result += list.removeAt(0)
                if (list.isEmpty()) grouped.remove(key)
            }
        }
        return result
    }

    private fun chooseSessionIndex(queue: List<SessionRequest>, date: LocalDate): Int {
        if (queue.isEmpty()) return -1
        val regular = queue.indexOfFirst { !it.isReview && date.isBefore(it.exam.exam.examDate) }
        if (regular != -1) return regular
        val review = queue.indexOfFirst {
            it.isReview && !date.isAfter(it.exam.exam.examDate) &&
                ChronoUnit.DAYS.between(date, it.exam.exam.examDate) <= 3
        }
        if (review != -1) return review
        return queue.indexOfFirst { !date.isAfter(it.exam.exam.examDate) }
    }

    private data class TimeBlock(val date: LocalDate, val startMinute: Int, val endMinute: Int)

    private data class SessionRequest(
        val exam: ExamWithTopics,
        val topic: ExamTopicEntity,
        val pageCount: Int,
        val isReview: Boolean
    ) {
        fun toPlan(block: TimeBlock): StudyPlanEntity = StudyPlanEntity(
            date = block.date,
            startMinute = block.startMinute,
            endMinute = block.endMinute,
            examId = exam.exam.id,
            topicId = topic.id,
            subjectName = exam.exam.subjectName,
            topicName = topic.name,
            pageCount = pageCount,
            isReview = isReview
        )
    }
}
