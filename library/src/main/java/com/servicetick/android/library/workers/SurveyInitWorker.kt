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
import com.servicetick.android.library.entities.triggers.Trigger
import io.multifunctions.letCheckNull
import lilhermit.android.remotelogger.library.Log
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.TimeUnit

internal class SurveyInitWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    private val apiService: ApiService by inject()
    private val serviceTickDao: ServiceTickDao by inject()
    private val serviceTick: ServiceTick by inject()

    override fun doWork(): Result {

        val id = inputData.getLong(KEY_ID, 0L)
        if (id != 0L) {

            val databaseSurvey = serviceTickDao.getSurvey(id)
            if (databaseSurvey == null || databaseSurvey.isRefreshDue || serviceTick.getForceRefresh()) {

                when {
                    databaseSurvey == null -> Log.d("Refreshing survey: $id Reason: not-in-db")
                    serviceTick.getForceRefresh() -> Log.d("Refreshing survey: $id Reason: force")
                    else -> Log.d("Refreshing survey: $id Reason: due")
                }

                makeApiCall(id)?.let { apiSurvey ->
                    updateSurvey(apiSurvey, databaseSurvey)
                    updateSurveyTriggersOnly(apiSurvey.id, databaseSurvey)

                    // Because we can't use the apiSurvey type of BaseSurvey, it's a little messy but we are in a thread
                    // and it's cleaner than creating a clone constructor and converting (We would have to convert the
                    // List<BaseSurveyQuestion> to List<SurveyQuestion> too!
                    serviceTickDao.getSurvey(apiSurvey.id)?.let { survey ->
                        serviceTick.surveyMap[survey.id] = survey
                    }

                    return ListenableWorker.Result.success(putOutputDataState(apiSurvey.state))

                } ?: run {
                    return ListenableWorker.Result.retry()
                }

            } else {
                updateSurveyTriggersOnly(databaseSurvey.id, databaseSurvey)
                serviceTick.surveyMap[databaseSurvey.id] = databaseSurvey

                return ListenableWorker.Result.success(putOutputDataState(databaseSurvey.state))
            }

        } else {

            // Refresh all surveys
            serviceTickDao.getSurveys().forEach { databaseSurvey ->
                if (serviceTick.getForceRefresh() || databaseSurvey.isRefreshDue) {

                    when {
                        serviceTick.getForceRefresh() -> Log.d("Refreshing survey: ${databaseSurvey.id} Reason: force")
                        else -> Log.d("Refreshing survey: ${databaseSurvey.id} Reason: due")
                    }

                    makeApiCall(databaseSurvey.id)?.let { apiSurvey ->
                        updateSurvey(apiSurvey, databaseSurvey)
                    }
                } else {
                    Log.d("No refresh required for survey: ${databaseSurvey.id}")
                }
            }

            return ListenableWorker.Result.success()
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

    private fun updateSurveyTriggersOnly(id: Long, databaseSurvey: Survey?) {

        val newSurvey = serviceTick.getSurveyByState(id)

        newSurvey?.triggers?.forEachIndexed { index, trigger ->
            newSurvey.triggers[index] = Trigger.convertTrigger(trigger)
        }

        if (databaseSurvey != null) {

            if (newSurvey?.triggers?.isEmpty() == true) {
                databaseSurvey.triggers.forEach { trigger ->
                    trigger.active = false

                    if (trigger.canStore()) {
                        serviceTickDao.insert(trigger)
                    }
                }
            } else {
                newSurvey?.triggers?.forEach { trigger ->

                    if (trigger.canStore()) {
                        trigger.updateData(databaseSurvey.triggers.firstOrNull {
                            it.tag == trigger.tag
                        }?.data)

                        serviceTickDao.insert(trigger)
                    }
                }

                // Disable the triggers not currently added
                serviceTickDao.disableTriggers(id, newSurvey?.triggers?.map { it.tag } ?: emptyList())
            }

        } else {
            newSurvey?.let { survey ->
                serviceTickDao.insert(survey.triggers)
            }
        }
    }

    private fun updateSurvey(apiSurvey: BaseSurvey, databaseSurvey: Survey?) {

        val newSurvey = serviceTick.getSurveyByState(apiSurvey.id)

        // Save some values
        apiSurvey.refreshInterval = newSurvey?.refreshInterval ?: Survey.DEFAULT_REFRESH_INTERVAL
        apiSurvey.state = if (!apiSurvey.enabled) Survey.State.DISABLED else Survey.State.INITIALISED

        serviceTickDao.insert(apiSurvey)

        //Purge changed or removed questions
        serviceTickDao.purgeQuestions(apiSurvey.id, apiSurvey.questions.mapNotNull {
            it.id
        }.toTypedArray())
    }

    companion object {
        private const val KEY_ID = "id"
        private const val DATA_STATE = "state"

        internal fun getOutputDataState(workInfo: WorkInfo) = Survey.State.valueOf(workInfo.outputData.getString(DATA_STATE)
                ?: Survey.State.ENQUEUED.name)

        internal fun putOutputDataState(state: Survey.State) = Data.Builder().putString(DATA_STATE, state.name).build()

        internal fun enqueue(survey: Survey, lifecycleOwner: LifecycleOwner? = null, observer: Observer<WorkInfo>? = null) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val surveyInitWorker = OneTimeWorkRequestBuilder<SurveyInitWorker>()
                    .setConstraints(constraints)
                    .setInputData(Data.Builder()
                            .putLong(KEY_ID, survey.id)
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