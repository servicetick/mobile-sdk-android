package com.servicetick.android.library

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.*
import androidx.work.WorkInfo
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.workers.SurveyInitWorker
import com.servicetick.android.library.workers.SyncResponsesWorker
import lilhermit.android.remotelogger.library.Log
import org.koin.android.logger.AndroidLogger
import org.koin.core.Koin
import org.koin.log.EmptyLogger
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.lang.ref.WeakReference

class ServiceTick(context: Context) : LifecycleOwner, KoinComponent {

    internal var weakReference = WeakReference<Context>(context.applicationContext)
    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val surveyMap: MutableMap<Long, Survey> = mutableMapOf()
    private val config: MutableMap<String, Any> = mutableMapOf(
            "base_url" to "https://api.servicetick.com/v1/",
            "force_refresh" to false,
            "debug" to false
    )
    internal var clientAccountId: Long? = null
    internal var surveyAccessKey: String? = null
    internal var importerAccessKey: String? = null
    private val serviceTickDao: ServiceTickDao by inject()
    private val statistics = StatisticsHelper()

    init {
        lifecycleRegistry.markState(Lifecycle.State.STARTED)
        singleton = this
        setDebug(config["debug"] as Boolean)

        (context.applicationContext as Application).registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                statistics.removeActivity(activity)
            }

            override fun onActivityResumed(activity: Activity?) {
                statistics.addActivity(activity)
            }

            override fun onActivityStarted(activity: Activity?) {}
            override fun onActivityDestroyed(activity: Activity?) {}
            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
            override fun onActivityStopped(activity: Activity?) {}
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
        })
    }

    fun addSurvey(surveyBuilder: SurveyBuilder): LiveData<Survey.State> {

        val liveData = MutableLiveData<Survey.State>()

        val survey = surveyBuilder.build()
        liveData.postValue(survey.state)

        if (!surveyMap.contains(survey.id)) {

            SurveyInitWorker.enqueue(survey, this, object : Observer<WorkInfo> {

                private var previousState: Survey.State? = null

                override fun onChanged(workStatus: WorkInfo?) {
                    if (workStatus?.state == WorkInfo.State.SUCCEEDED) {
                        if (survey.id != 0L) {
                            serviceTickDao.getSurveyAsLiveData(survey.id).observe(this@ServiceTick, Observer {

                                it?.let { fullSurvey ->
                                    surveyMap[fullSurvey.id] = fullSurvey

                                    if (previousState != fullSurvey.state) {
                                        liveData.postValue(fullSurvey.state)
                                        previousState = fullSurvey.state
                                    }

                                    // Only enqueues one as ExistingPeriodicWorkPolicy.KEEP
                                    SurveyInitWorker.enqueueRefreshAll()

                                    SyncResponsesWorker.enqueue()
                                }
                            })
                        }
                    }
                }

            })
        }
        return liveData
    }

    fun getSurvey(id: Long): Survey? {
        return surveyMap[id]
    }

    private fun actionConfig(configPair: Pair<String, Any>) {
        when (configPair.first) {
            "debug" -> setDebug(configPair.second as Boolean)
        }
    }

    private fun setConfig(configPair: Pair<String, Any>) {
        config[configPair.first] = configPair.second
        actionConfig(configPair)
    }

    internal fun getBaseUrl(): String {
        return config["base_url"] as String
    }

    internal fun getForceRefresh(): Boolean {
        return config["force_refresh"] as Boolean
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    private fun setDebug(debug: Boolean) {
        if (debug) {
            Koin.logger = AndroidLogger()
            Log.setLevelLogging(Log.DEBUG to Log.LOG_LOCAL_ONLY)
        } else {
            Koin.logger = EmptyLogger()
            Log.setLevelLogging(Log.DEBUG to Log.LOG_NONE)
        }
    }

    companion object {

        @Volatile
        internal var singleton: ServiceTick? = null

        fun setClientAccountId(clientAccountId: Long): ServiceTick.Companion {
            singleton().clientAccountId = clientAccountId
            return this
        }

        fun setSurveyAccessKey(surveyAccessKey: String): ServiceTick.Companion {
            singleton().surveyAccessKey = surveyAccessKey
            return this
        }

        fun setImporterAccessKey(importerAccessKey: String): ServiceTick.Companion {
            singleton().importerAccessKey = importerAccessKey
            return this
        }

        fun setConfig(config: Pair<String, Any>): ServiceTick.Companion {
            singleton().setConfig(config)
            return this
        }

        fun setConfig(key: String, value: Any): ServiceTick.Companion {
            setConfig(Pair(key, value))
            return this
        }

        fun build(): ServiceTick = singleton()

        private fun singleton(): ServiceTick {
            singleton?.let {
                return it
            } ?: run {
                throw IllegalStateException("You must initialise ServiceTick Mobile SDK before using get()")
            }
        }

        fun get(): ServiceTick {
            return singleton()
        }

        internal fun internalInit(context: Context) {

            singleton?.let {
                throw RuntimeException("ServiceTick Mobile SDK already initialised")
            } ?: run {
                singleton = ServiceTick(context)
            }
        }

    }
}