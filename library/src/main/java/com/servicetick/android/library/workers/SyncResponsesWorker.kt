package com.servicetick.android.library.workers

import android.content.Context
import androidx.work.*
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.api.ApiService
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.SurveyResponse
import io.multifunctions.letCheckNull
import lilhermit.android.remotelogger.library.Log
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.TimeUnit

internal class SyncResponsesWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val apiService: ApiService by inject()
    private val serviceTickDao: ServiceTickDao by inject()
    private val serviceTick: ServiceTick by inject()

    override fun doWork(): Result {

        val surveyResponses = serviceTickDao.getSyncableSurveyResponse()

        Log.d("Survey response(s) to Importing into ServiceTick API = ${surveyResponses.size}")
        serviceTickDao.getSyncableSurveyResponse().forEach { surveyResponse ->
            surveyResponse?.run {
                makeApiCall(this)
            }
        }

        return ListenableWorker.Result.success()
    }

    private fun makeApiCall(surveyResponse: SurveyResponse) {

        Pair(serviceTick.importerAccessKey, serviceTick.clientAccountId).letCheckNull { accessKey, clientAccountId ->

            try {

                surveyResponse.also {
                    it.buildCustomValues()
                    it.clientAccountId = clientAccountId
                    it.accessKey = accessKey
                }

                val response = apiService.postSurveyResponse(surveyResponse).execute()
                if (response.isSuccessful) {

                    surveyResponse.syncStamp = Calendar.getInstance()
                    serviceTickDao.markResponseAsSynced(surveyResponse.id)
                    Log.d("  Survey response (id:${surveyResponse.id} successfully sent to ServiceTick API")
                }
            } catch (exception: Exception) {
                Log.e(exception.toString())
            }
        }
    }

    companion object {

        internal fun enqueue(reschedule: Boolean = false) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val surveyInitWorker = PeriodicWorkRequestBuilder<SyncResponsesWorker>(1, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance().run {
                enqueueUniquePeriodicWork("sync_responses", if (reschedule) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP, surveyInitWorker)
            }

        }
    }
}