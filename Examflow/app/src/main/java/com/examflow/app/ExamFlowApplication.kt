package com.examflow.app

import android.app.Application
import com.examflow.app.data.local.ExamFlowDatabase
import com.examflow.app.data.repository.ExamFlowRepository

class ExamFlowApplication : Application() {
    val repository: ExamFlowRepository by lazy {
        ExamFlowRepository(ExamFlowDatabase.getInstance(this).dao())
    }
}
