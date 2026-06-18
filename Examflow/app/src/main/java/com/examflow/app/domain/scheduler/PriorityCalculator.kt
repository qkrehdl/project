package com.examflow.app.domain.scheduler

import com.examflow.app.data.local.ExamWithTopics
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

class PriorityCalculator {
    fun examScore(exam: ExamWithTopics, today: LocalDate, urgentBoost: Boolean): Double {
        val priorityWeight = when (exam.exam.priority.coerceIn(1, 5)) {
            1 -> 1.0
            2 -> 1.2
            3 -> 1.5
            4 -> 1.8
            else -> 2.0
        }
        val daysLeft = max(0, ChronoUnit.DAYS.between(today, exam.exam.examDate).toInt())
        var dateWeight = when {
            daysLeft <= 3 -> 2.0
            daysLeft <= 7 -> 1.7
            daysLeft <= 14 -> 1.5
            daysLeft <= 30 -> 1.2
            else -> 1.0
        }
        if (urgentBoost && daysLeft <= 7) {
            dateWeight *= 1.5
        }
        val pages = exam.topics.sumOf { it.pageCount }.coerceAtLeast(1)
        return priorityWeight * dateWeight * pages
    }
}
