package com.servicetick.android.library

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
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

    @JvmField
    @JvmSynthetic
    internal var weakReference = WeakReference<Context>(context.applicationContext)
    private var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    @JvmField
    @JvmSynthetic
    internal val surveyMap: MutableMap<Long, Survey> = mutableMapOf()
    private val config: MutableMap<String, Any> = mutableMapOf(
            "base_url" to "https://api.servicetick.com/v1/",
            "force_refresh" to false,
            "debug" to false
    )
    @JvmField
    @JvmSynthetic
    internal var clientAccountId: Long? = null
    @JvmField
    @JvmSynthetic
    internal var surveyAccessKey: String? = null
    @JvmField
    @JvmSynthetic
    internal var importerAccessKey: String? = null
    private val serviceTickDao: ServiceTickDao by inject()
    private val statistics = TriggerHelper(object : TriggerHelper.TriggerHelperCallback {
        override fun onApplicationRun() {
            surveyMap.values.forEach { survey ->
                survey.triggers.forEach { trigger ->
                    trigger.updateApplicationRunCount()
                }
            }
        }

        override fun onApplicationRunTimeUpdate(time: Long) {
            surveyMap.values.forEach { survey ->
                survey.triggers.forEach { trigger ->
                    trigger.updateApplicationRunTime(time)
                }
            }
        }
    })

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

    @JvmOverloads
    fun addSurvey(surveyBuilder: SurveyBuilder, observer: Survey.StateChangeObserver? = null, lifecycleOwner: LifecycleOwner? = null) {

        val survey = surveyBuilder.build()

        if (!surveyMap.contains(survey.id)) {

            surveyMap[survey.id] = survey
            getSurveyByState(survey.id)?.addStateChangeObserver(observer, lifecycleOwner)

            SurveyInitWorker.enqueue(survey, this, object : Observer<WorkInfo> {

                private var previousState: Survey.State? = null

                override fun onChanged(workStatus: WorkInfo?) {
                    if (workStatus?.state == WorkInfo.State.SUCCEEDED) {
                        val newState = SurveyInitWorker.getOutputDataState(workStatus)
                        if (previousState != newState) {
                            surveyMap[survey.id]?.notifyStateChangeObservers()
                            previousState = newState
                        }

                        // Only enqueues one as ExistingPeriodicWorkPolicy.KEEP
                        SurveyInitWorker.enqueueRefreshAll()

                        SyncResponsesWorker.enqueue()
                    }
                }

            })
        }
    }

    fun observeSurveyStateChange(id: Long, lifecycleOwner: LifecycleOwner, observer: Survey.StateChangeObserver) {
        if (lifecycleOwner.lifecycle.currentState === Lifecycle.State.DESTROYED) {
            return
        }
        getSurveyByState(id)?.addStateChangeObserver(observer, lifecycleOwner)
    }

    fun observeSurveyStateChangeForever(id: Long, observer: Survey.StateChangeObserver) {
        getSurveyByState(id)?.addStateChangeObserver(observer)
    }

    fun removeSurveyStateChangeObservers(id: Long, lifecycleOwner: LifecycleOwner) {
        getSurveyByState(id)?.removeStateChangeObservers(lifecycleOwner)
    }

    fun removeSurveyStateChangeObserver(id: Long, observer: Survey.StateChangeObserver) {
        getSurveyByState(id)?.removeStateChangeObserver(observer)
    }

    fun getSurvey(id: Long): Survey? {
        return surveyMap.filter { it.value.state == Survey.State.INITIALISED }[id]
    }

    /**
     * Get a survey based on state (null = any state)
     */
    @JvmSynthetic
    internal fun getSurveyByState(id: Long, state: Survey.State? = null): Survey? {
        return state?.let { surveyState ->
            surveyMap.filter { it.value.state == surveyState }[id]
        } ?: let {
            surveyMap[id]
        }
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

    @JvmSynthetic
    internal fun getBaseUrl(): String {
        return config["base_url"] as String
    }

    @JvmSynthetic
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

    @JvmSynthetic
    internal fun getDebug(): Boolean = config["debug"] as Boolean

    companion object {

        @Volatile
        internal var singleton: ServiceTick? = null

        @JvmStatic
        fun setClientAccountId(clientAccountId: Long): ServiceTick.Companion {
            singleton().clientAccountId = clientAccountId
            return this
        }

        @JvmStatic
        fun setSurveyAccessKey(surveyAccessKey: String): ServiceTick.Companion {
            singleton().surveyAccessKey = surveyAccessKey
            return this
        }

        @JvmStatic
        fun setImporterAccessKey(importerAccessKey: String): ServiceTick.Companion {
            singleton().importerAccessKey = importerAccessKey
            return this
        }

        @JvmStatic
        fun setConfig(config: Pair<String, Any>): ServiceTick.Companion {
            singleton().setConfig(config)
            return this
        }

        @JvmStatic
        fun setConfig(key: String, value: Any): ServiceTick.Companion {
            setConfig(Pair(key, value))
            return this
        }

        @JvmStatic
        fun build(): ServiceTick = singleton()

        private fun singleton(): ServiceTick {
            singleton?.let {
                return it
            } ?: run {
                throw IllegalStateException("You must initialise ServiceTick Mobile SDK before using get()")
            }
        }

        @JvmStatic
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