package com.servicetick.android.library.workers

import android.content.Context
import androidx.work.*
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.triggers.Trigger
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

internal class SaveTriggerWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val serviceTickDao: ServiceTickDao by inject()

    override fun doWork(): Result {

        val data: HashMap<String, Any> = hashMapOf()
        var tag: String? = null
        var fired = false

        inputData.keyValueMap.forEach {
            when (it.key) {
                KEY_TAG -> tag = it.value as String
                KEY_FIRED -> fired = it.value as Boolean
                else -> data[it.key] = it.value
            }
        }

        tag?.let {
            serviceTickDao.updateTrigger(it, data, fired)
        }
        return Result.success()
    }

    companion object {
        private const val KEY_TAG = "tag"
        private const val KEY_FIRED = "fired"

        internal fun enqueue(trigger: Trigger) {

            val saveTriggerDataWorker = OneTimeWorkRequestBuilder<SaveTriggerWorker>()
                    .setInputData(Data.Builder().putAll(trigger.data).putString(KEY_TAG, trigger.tag).putBoolean(KEY_FIRED, trigger.fired).build())
                    .setInitialDelay(10, TimeUnit.SECONDS)
                    .build()

            WorkManager.getInstance().run {
                enqueueUniqueWork("save_trigger_${trigger.tag}", ExistingWorkPolicy.REPLACE, saveTriggerDataWorker)
            }
        }
    }
}