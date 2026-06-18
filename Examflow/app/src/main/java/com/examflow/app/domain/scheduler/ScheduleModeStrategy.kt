package com.examflow.app.domain.scheduler

import com.examflow.app.domain.model.ScheduleMode

data class ScheduleModeStrategy(
    val mode: ScheduleMode,
    val maxDailyBlocks: Int,
    val preferSubjectStreaks: Boolean,
    val urgentBoost: Boolean
) {
    companion object {
        fun from(mode: ScheduleMode): ScheduleModeStrategy = when (mode) {
            ScheduleMode.BALANCED -> ScheduleModeStrategy(mode, maxDailyBlocks = 6, preferSubjectStreaks = false, urgentBoost = false)
            ScheduleMode.FOCUS -> ScheduleModeStrategy(mode, maxDailyBlocks = 6, preferSubjectStreaks = true, urgentBoost = false)
            ScheduleMode.URGENT -> ScheduleModeStrategy(mode, maxDailyBlocks = 6, preferSubjectStreaks = true, urgentBoost = true)
            ScheduleMode.RELAXED -> ScheduleModeStrategy(mode, maxDailyBlocks = 3, preferSubjectStreaks = false, urgentBoost = false)
        }
    }
}
