package com.servicetick.android.library.workers

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.api.ApiService
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.api.PostSurveyRequest
import com.servicetick.android.library.entities.db.BaseSurvey
import io.multifunctions.letCheckNull
import lilhermit.android.remotelogger.library.Log
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class SurveyInitWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var serviceTickDao: ServiceTickDao
    private lateinit var serviceTick: ServiceTick

    override fun doWork(): Result {

        serviceTick = ServiceTick.get()
        serviceTick.appComponent.inject(this)

        val id = inputData.getLong(KEY_ID, 0L)
        if (id != 0L) {

            val currentSurvey = serviceTickDao.getSurvey(id)
            if (currentSurvey == null || currentSurvey.isRefreshDue || serviceTick.getForceRefresh()) {

                when {
                    currentSurvey == null -> Log.d("Refreshing survey: $id Reason: not-in-db")
                    serviceTick.getForceRefresh() -> Log.d("Refreshing survey: $id Reason: force")
                    else -> Log.d("Refreshing survey: $id Reason: due")
                }

                makeApiCall(id)?.let { newSurvey ->
                    updateSurvey(newSurvey, currentSurvey)
                    return Result.SUCCESS

                } ?: run {
                    return Result.RETRY
                }

            } else {
                return Result.SUCCESS
            }

        } else {

            // Refresh all surveys
            serviceTickDao.getSurveys().forEach { survey ->
                if (serviceTick.getForceRefresh() || survey.isRefreshDue) {

                    when {
                        serviceTick.getForceRefresh() -> Log.d("Refreshing survey: ${survey.id} Reason: force")
                        else -> Log.d("Refreshing survey: ${survey.id} Reason: due")
                    }

                    makeApiCall(survey.id)?.let { newSurvey ->
                        updateSurvey(newSurvey, survey)
                    }
                } else {
                    Log.d("No refresh required for survey: ${survey.id}")
                }
            }

            return Result.SUCCESS
        }
    }

    private fun makeApiCall(surveyId: Long): BaseSurvey? {

        Pair(serviceTick.surveyAccessKey, serviceTick.clientAccountId).letCheckNull { accessKey, clientAccountId ->

            try {
                val response = apiService.getSurvey(PostSurveyRequest(surveyId, clientAccountId, accessKey)).execute()
                if (response.isSuccessful) {
                    response.body()?.surveyDownload?.run {
                        lastUpdated = Calendar.getInstance()
                        return this
                    }
                }
            } catch (exception: Exception) {
                Log.e(exception.toString())
            }
        }

        return null
    }

    private fun updateSurvey(newSurvey: BaseSurvey, oldSurvey: Survey?) {

        // Save any values from the oldSurvey
        oldSurvey?.run {
            newSurvey.refreshInterval = refreshInterval
        } ?: run {
            newSurvey.refreshInterval = inputData.getLong(KEY_REFRESH_INTERVAL, Survey.DEFAULT_REFRESH_INTERVAL)
        }
        newSurvey.state = when {
            !newSurvey.enabled -> {
                Survey.State.DISABLED
            }
            oldSurvey == null || newSurvey.enabled -> {
                Survey.State.INITIALISED
            }
            else -> {
                oldSurvey.state
            }
        }

        serviceTickDao.insert(newSurvey)

        //Purge changed or removed questions
        serviceTickDao.purgeQuestions(newSurvey.id, newSurvey.questions.mapNotNull {
            it.id
        }.toTypedArray())
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_REFRESH_INTERVAL = "refresh_interval"

        internal fun enqueue(survey: Survey, lifecycleOwner: LifecycleOwner? = null, observer: Observer<WorkInfo>? = null) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val surveyInitWorker = OneTimeWorkRequestBuilder<SurveyInitWorker>()
                    .setConstraints(constraints)
                    .setInputData(Data.Builder()
                            .putLong(KEY_ID, survey.id)
                            .putLong(KEY_REFRESH_INTERVAL, survey.refreshInterval)
                            .build())
                    .build()

            WorkManager.getInstance().run {
                beginUniqueWork("survey_init_${survey.id}", ExistingWorkPolicy.KEEP, surveyInitWorker)
                        .enqueue()

                lifecycleOwner?.let { owner ->
                    observer?.let { obsv ->
                        getWorkInfoByIdLiveData(surveyInitWorker.id).observe(owner, obsv)
                    }

                }

            }
        }

        internal fun enqueueRefreshAll(observer: Observer<List<WorkInfo>>? = null, lifecycleOwner: LifecycleOwner? = null) {

            val surveyInitWorker = PeriodicWorkRequestBuilder<SurveyInitWorker>(24, TimeUnit.HOURS)
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()

            WorkManager.getInstance().run {

                enqueueUniquePeriodicWork("survey_init_all", ExistingPeriodicWorkPolicy.KEEP, surveyInitWorker)

                lifecycleOwner?.let { owner ->
                    observer?.let { obsv ->
                        getWorkInfosForUniqueWorkLiveData("survey_init_all").observe(owner, obsv)
                    }
                }
            }
        }
    }
}