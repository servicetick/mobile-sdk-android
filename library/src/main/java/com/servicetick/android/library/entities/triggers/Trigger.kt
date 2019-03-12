package com.servicetick.android.library.entities.triggers

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.workers.SaveTriggerWorker
import lilhermit.android.remotelogger.library.Log

@Entity(tableName = "triggers")
open class Trigger internal constructor(@PublishedApi internal val presentation: TriggerPresentation = TriggerPresentation.START_ACTIVITY, @PrimaryKey val tag: String, @PublishedApi internal var surveyId: Long) {

    internal constructor(trigger: Trigger) : this(trigger.presentation, trigger.tag, trigger.surveyId) {
        clone(trigger)
    }

    protected fun clone(trigger: Trigger) {
        trigger.let {
            active = it.active
            fired = it.fired
            config = it.config
            data = it.data
            surveyId = it.surveyId
            foreverObservers = it.foreverObservers
            lifecycleObservers = it.lifecycleObservers
            survey = it.survey
        }
    }

    @PublishedApi
    internal var type: String = javaClass.simpleName
    @PublishedApi
    internal var active: Boolean = true
    @PublishedApi
    internal var fired: Boolean = false
    @PublishedApi
    internal var config: HashMap<String, Any> = hashMapOf()
    @PublishedApi
    internal var data: HashMap<String, Any> = hashMapOf()

    @Transient
    private var foreverObservers: MutableList<TriggerFiredObserver> = mutableListOf()
    @Transient
    private var lifecycleObservers: HashMap<LifecycleOwner, TriggerFiredObserver> = hashMapOf()
    @Transient
    private var survey: Survey? = null
        get() {
            if (field == null) {
                field = ServiceTick.get().getSurvey(surveyId)
            }
            return field
        }

    /**
     * This allows us remove any DESTROYED lifecycle owners, keeps the
     * observer list as clean as possible
     */
    @Transient
    private val lifecycleObserver = object : GenericLifecycleObserver {
        override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event?) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                source?.let { lifecycleOwner ->
                    lifecycleOwner.lifecycle.removeObserver(this)
                    removeObservers(lifecycleOwner)
                }
            }
        }
    }

    internal fun canStore(): Boolean = javaClass.kotlin != ManualTrigger::class

    override fun toString(): String {
        return "Trigger(presentation=$presentation, tag='$tag', surveyId=$surveyId, type='$type', active=$active, config=$config, data=$data)"
    }

    internal open fun updateApplicationRunCount(count: Int = 1, checkFire: Boolean = true) = Unit
    internal open fun updateApplicationRunTime(time: Long, checkFire: Boolean = true) = Unit
    internal open fun updateData(data: HashMap<String, Any>?) = Unit
    internal open fun shouldFire() = false

    protected fun fireTriggerIfRequired(checkFire: Boolean) {

        if (checkFire) {
            val shouldFire = shouldFire()
            if (active && !fired && shouldFire) {
                notifyObservers()
            } else {
                if (fired) {
                    Log.d("Trigger (tag:$tag) already fired")
                } else if (!active) {
                    Log.d("Trigger (tag:$tag) won't fire not active")
                } else if (!shouldFire) {
                    Log.d("Trigger (tag:$tag) won't fire conditions not met")
                }
            }
        }
    }

    fun launchSurvey() {
        survey?.run {
            startTrigger(this@Trigger)
            fired = true
            scheduleSave()
        }
    }

    inline fun observe(lifecycleOwner: LifecycleOwner, crossinline action: (trigger: Trigger) -> Unit): Trigger.TriggerFiredObserver {

        val observer = object : Trigger.TriggerFiredObserver {
            override fun triggerFired(trigger: Trigger) {
                action(trigger)
            }
        }
        observe(lifecycleOwner, observer)
        return observer
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: TriggerFiredObserver) {

        if (lifecycleOwner.lifecycle.currentState === Lifecycle.State.DESTROYED) {
            return
        }
        addObserver(observer, lifecycleOwner)
    }

    fun observeForever(observer: TriggerFiredObserver) {
        addObserver(observer)
    }

    fun removeObservers(lifecycleOwner: LifecycleOwner) {
        lifecycleObservers.remove(lifecycleOwner)
    }

    fun removeObserver(observer: TriggerFiredObserver) {
        foreverObservers.remove(observer)
    }

    private fun notifyObservers() {
        Log.d("Trigger (tag:$tag) Notifying observers forever:${foreverObservers.size}, lifecycle:${lifecycleObservers.size}")
        foreverObservers.forEach { observer ->
            observer.triggerFired(this)
        }

        lifecycleObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                entry.value.triggerFired(this)
            }
        }
    }

    private fun addObserver(observer: TriggerFiredObserver, lifecycleOwner: LifecycleOwner? = null) {

        if (lifecycleOwner == null) {
            if (!foreverObservers.contains(observer)) {
                foreverObservers.add(observer)
            }
        } else {
            if (!lifecycleObservers.containsKey(lifecycleOwner)) {
                lifecycleObservers[lifecycleOwner] = observer
                lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            }
        }
    }

    protected fun scheduleSave() {
        SaveTriggerWorker.enqueue(this)
    }

    companion object {
        internal fun convertTrigger(trigger: Trigger): Trigger {
            return when (trigger.type) {
                "ApplicationRunCountTrigger" -> ApplicationRunCountTrigger(trigger)
                "ApplicationRunTimeTrigger" -> ApplicationRunTimeTrigger(trigger)
                else -> trigger
            }
        }
    }

    interface TriggerFiredObserver : LifecycleObserver {
        fun triggerFired(trigger: Trigger)
    }
}