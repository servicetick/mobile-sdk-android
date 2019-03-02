package com.servicetick.android.library.workers

import android.content.Context
import androidx.work.*
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.db.Statistic
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

internal class SaveStatisticsWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val serviceTickDao: ServiceTickDao by inject()

    override fun doWork(): Result {
        inputData.keyValueMap.forEach { entry ->
            serviceTickDao.setStatistic(Statistic(entry.key, entry.value.toString()))
        }
        return Result.success()
    }

    companion object {
        internal fun enqueue(data: Map<String, Any>) {

            val saveStatisticsWorker = OneTimeWorkRequestBuilder<SaveStatisticsWorker>()
                    .setInputData(Data.Builder().putAll(data).build())
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .build()

            WorkManager.getInstance().run {
                enqueueUniqueWork("save_statistics", ExistingWorkPolicy.REPLACE, saveStatisticsWorker)
            }

        }
    }
}