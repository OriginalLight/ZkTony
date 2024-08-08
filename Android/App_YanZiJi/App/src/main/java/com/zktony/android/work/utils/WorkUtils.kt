package com.zktony.android.work.utils

import android.app.Application
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zktony.android.work.ErrorLogCleanWork
import com.zktony.android.work.LogCleanWork
import java.util.concurrent.TimeUnit

object WorkUtils {

    fun with(app: Application) {
        setupLogCleanWork(app)
        setupErrorLogCleanWork(app)
    }

    private fun setupLogCleanWork(context: Context) {

        WorkManager.getInstance(context).enqueueUniqueWork(
            "logCleanWork",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<LogCleanWork>()
                .setInitialDelay(2, TimeUnit.MINUTES)
                .build()
        )
    }

    private fun setupErrorLogCleanWork(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "errorLogCleanWork",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ErrorLogCleanWork>()
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()
        )
    }
}