package com.examflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FixedScheduleEntity::class,
        ExamEntity::class,
        ExamTopicEntity::class,
        StudyPlanEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ExamFlowDatabase : RoomDatabase() {
    abstract fun dao(): ExamFlowDao

    companion object {
        @Volatile private var instance: ExamFlowDatabase? = null

        fun getInstance(context: Context): ExamFlowDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExamFlowDatabase::class.java,
                    "examflow.db"
                ).build().also { instance = it }
            }
    }
}
