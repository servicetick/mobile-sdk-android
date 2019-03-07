package com.servicetick.android.library.workers

import android.content.Context
import androidx.work.*
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.triggers.Trigger
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

internal class SaveTriggerDataWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val serviceTickDao: ServiceTickDao by inject()

    override fun doWork(): Result {
        inputData.getString(KEY_TAG)?.let { tag ->
            val data: HashMap<String, Any> = hashMapOf()
            inputData.keyValueMap.forEach {
                if (it.key != KEY_TAG) {
                    data[it.key] = it.value
                }
            }
            serviceTickDao.updateTriggerData(data, tag)
        }
        return Result.success()
    }

    companion object {
        private const val KEY_TAG = "tag"

        internal fun enqueue(trigger: Trigger) {

            val saveTriggerDataWorker = OneTimeWorkRequestBuilder<SaveTriggerDataWorker>()
                    .setInputData(Data.Builder().putAll(trigger.data).putString(KEY_TAG, trigger.tag).build())
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .build()

            WorkManager.getInstance().run {
                enqueueUniqueWork("save_trigger_data_${trigger.tag}", ExistingWorkPolicy.REPLACE, saveTriggerDataWorker)
            }
        }
    }
}