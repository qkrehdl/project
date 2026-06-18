package com.examflow.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ExamWithTopics(
    @Embedded val exam: ExamEntity,
    @Relation(parentColumn = "id", entityColumn = "examId")
    val topics: List<ExamTopicEntity>
)
