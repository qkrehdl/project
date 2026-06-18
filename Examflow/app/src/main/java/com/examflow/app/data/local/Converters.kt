package com.examflow.app.data.local

import androidx.room.TypeConverter
import com.examflow.app.domain.model.ScheduleMode
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    @TypeConverter
    fun fromScheduleMode(mode: ScheduleMode): String = mode.name

    @TypeConverter
    fun toScheduleMode(value: String): ScheduleMode = ScheduleMode.valueOf(value)
}
