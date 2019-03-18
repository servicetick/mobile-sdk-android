package com.servicetick.android.library.entities

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.room.Ignore
import androidx.room.Relation
import com.servicetick.android.library.AppExecutors
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.activity.SurveyActivity
import com.servicetick.android.library.entities.db.BaseSurveyQuestion
import com.servicetick.android.library.entities.db.BaseSurveyResponse
import com.servicetick.android.library.entities.triggers.ManualTrigger
import com.servicetick.android.library.entities.triggers.Trigger
import com.servicetick.android.library.entities.triggers.TriggerPresentation
import com.servicetick.android.library.fragment.SurveyFragment
import lilhermit.android.remotelogger.library.Log
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.util.*
import java.util.concurrent.TimeUnit


class Survey internal constructor(val id: Long) : KoinComponent {

    @PublishedApi
    internal var title: String? = null
    @PublishedApi
    internal var type: String? = null
    @PublishedApi
    internal var lastUpdated: Calendar? = null
    @PublishedApi
    internal var refreshInterval: Long = DEFAULT_REFRESH_INTERVAL
        set(value) {
            field = if (value >= MINIMUM_REFRESH_INTERVAL) value else MINIMUM_REFRESH_INTERVAL
        }

    @PublishedApi
    internal var state = Survey.State.ENQUEUED

    @Relation(parentColumn = "id", entityColumn = "surveyId")
    @PublishedApi
    internal var pageTransitions: List<SurveyPageTransition> = emptyList()

    @Relation(parentColumn = "id", entityColumn = "surveyId")
    @PublishedApi
    internal var questionOptionActions: List<SurveyQuestionOptionAction> = emptyList()

    @Relation(parentColumn = "id", entityColumn = "surveyId", entity = BaseSurveyQuestion::class)
    @PublishedApi
    internal var questions: MutableList<SurveyQuestion> = mutableListOf()

    @PublishedApi
    @Relation(parentColumn = "id", entityColumn = "surveyId", entity = Trigger::class)
    internal var triggers: MutableList<Trigger> = mutableListOf()

    @Relation(parentColumn = "id", entityColumn = "surveyId", entity = BaseSurveyResponse::class)
    @PublishedApi
    internal var response: MutableList<SurveyResponse> = mutableListOf()

    @Ignore
    private var isAnswerInjectionComplete = false

    @delegate:Ignore
    internal val renderablePages: List<SurveyPageTransition> by lazy {

        val pageIds = questions.filter {
            it.shouldRender()
        }.distinctBy {
            it.pageId
        }.map {
            it.pageId
        }
        if (pageIds.isNotEmpty()) pageTransitions.filter { pageIds.contains(it.sourcePageId) }.sortedBy { it.order } else emptyList()
    }

    @Transient
    @JvmField
    @JvmSynthetic
    internal var foreverExecutionObservers: MutableList<ExecutionObserver> = mutableListOf()
    @Transient
    @JvmField
    @JvmSynthetic
    internal var lifecycleExecutionObservers: HashMap<LifecycleOwner, ExecutionObserver> = hashMapOf()
    @Transient
    @JvmField
    @JvmSynthetic
    internal var foreverStateChangeObservers: MutableList<Survey.StateChangeObserver> = mutableListOf()
    @Transient
    @JvmField
    @JvmSynthetic
    internal var lifecycleStateChangeObservers: HashMap<LifecycleOwner, Survey.StateChangeObserver> = hashMapOf()


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
                    removeExecutionObservers(lifecycleOwner)
                    removeStateChangeObservers(lifecycleOwner)
                }
            }
        }
    }

    @JvmSynthetic
    internal fun isRefreshDue(): Boolean {
        lastUpdated?.let {
            val now = Calendar.getInstance()
            it.timeInMillis += refreshInterval
            return now.after(it) || state == State.ENQUEUED
        }
        return true
    }

    @JvmSynthetic
    internal fun addTrigger(trigger: Trigger) {
        if (triggers.none { it.tag == trigger.tag }) {
            trigger.surveyId = id
            triggers.add(trigger)
        }
    }

    fun getAllTriggers(): List<Trigger> {
        return triggers
    }

    fun getTrigger(triggerTag: String): Trigger? {
        return triggers.firstOrNull { it.tag == triggerTag && it.active }
    }

    @JvmSynthetic
    internal fun addStateChangeObserver(stateChangeObserver: Survey.StateChangeObserver?, lifecycleOwner: LifecycleOwner? = null) {
        stateChangeObserver?.let { observer ->

            if (lifecycleOwner == null) {
                if (!foreverStateChangeObservers.contains(observer)) {
                    if (state == State.ENQUEUED) {
                        foreverStateChangeObservers.add(observer)
                        notifyStateChangeObservers()
                    } else {
                        Log.d("StateChangeObserver: Survey already initialised (Notifying onSurveyStateChange, not adding listener)")
                        notifyStateChangeObserver(observer)
                    }

                }
            } else {
                if (!lifecycleStateChangeObservers.containsKey(lifecycleOwner)) {
                    if (state == State.ENQUEUED) {
                        lifecycleStateChangeObservers[lifecycleOwner] = observer
                        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                        notifyStateChangeObservers()
                    } else {
                        Log.d("StateChangeObserver: Survey already initialised (Notifying onSurveyStateChange, not adding listener)")
                        notifyStateChangeObserver(observer)
                    }
                }
            }
        }
    }

    fun observeStateChange(lifecycleOwner: LifecycleOwner, observer: Survey.StateChangeObserver) {

        if (lifecycleOwner.lifecycle.currentState === Lifecycle.State.DESTROYED) {
            return
        }
        addStateChangeObserver(observer, lifecycleOwner)
    }

    fun observeStateChangeForever(observer: Survey.StateChangeObserver) {
        addStateChangeObserver(observer)
    }

    fun removeStateChangeObservers(lifecycleOwner: LifecycleOwner) {
        lifecycleStateChangeObservers.remove(lifecycleOwner)
    }

    fun removeStateChangeObserver(observer: Survey.StateChangeObserver) {
        foreverStateChangeObservers.remove(observer)
    }

    @JvmSynthetic
    internal fun notifyPageChangeObservers(newPage: Int, oldPage: Int) {
        Log.d("ExecutionObserver: Notifying onPageChange forever:${foreverExecutionObservers.size}, lifecycle:${lifecycleExecutionObservers.size}")
        foreverExecutionObservers.forEach { observer ->
            observer.onPageChange(newPage, oldPage)
        }

        lifecycleExecutionObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                entry.value.onPageChange(newPage, oldPage)
            }
        }
    }

    @JvmSynthetic
    internal fun observeExecution(lifecycleOwner: LifecycleOwner, observer: ExecutionObserver) {

        if (lifecycleOwner.lifecycle.currentState === Lifecycle.State.DESTROYED) {
            return
        }
        addExecutionObserver(observer, lifecycleOwner)
    }

    @JvmSynthetic
    internal fun observeExecutionForever(observer: ExecutionObserver) {
        addExecutionObserver(observer)
    }

    fun removeExecutionObservers(lifecycleOwner: LifecycleOwner) {
        lifecycleExecutionObservers.remove(lifecycleOwner)
    }

    fun removeExecutionObserver(observer: ExecutionObserver) {
        foreverExecutionObservers.remove(observer)
    }

    private fun notifyStateChangeObserver(observer: StateChangeObserver) {
        observer.onSurveyStateChange(state, if (state != State.ENQUEUED) this else null)
    }

    @JvmSynthetic
    internal fun notifyStateChangeObservers() {
        Log.d("StateChangeObserver: Notifying onSurveyStateChange forever:${foreverStateChangeObservers.size}, lifecycle:${lifecycleStateChangeObservers.size}")
        foreverStateChangeObservers.forEach { observer ->
            notifyStateChangeObserver(observer)
        }

        lifecycleStateChangeObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                notifyStateChangeObserver(entry.value)
            }
        }
    }

    private fun notifySurveyCompleteObservers() {
        Log.d("ExecutionObserver: Notifying onSurveyComplete forever:${foreverExecutionObservers.size}, lifecycle:${lifecycleExecutionObservers.size}")
        foreverExecutionObservers.forEach { executionObserver ->
            executionObserver.onSurveyComplete()
            removeExecutionObserver(executionObserver)
        }

        lifecycleExecutionObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                entry.value.onSurveyComplete()
                removeExecutionObservers(entry.key)
            }
        }
    }

    private fun notifySurveyAlreadyCompleteObservers() {
        Log.d("ExecutionObserver: Notifying onSurveyAlreadyComplete forever:${foreverExecutionObservers.size}, lifecycle:${lifecycleExecutionObservers.size}")
        foreverExecutionObservers.forEach { executionObserver ->
            executionObserver.onSurveyAlreadyComplete()
            removeExecutionObserver(executionObserver)
        }

        lifecycleExecutionObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                entry.value.onSurveyAlreadyComplete()
                removeExecutionObservers(entry.key)
            }
        }
    }

    @JvmSynthetic
    internal fun addExecutionObserver(observerExecutionObserver: ExecutionObserver?, lifecycleOwner: LifecycleOwner? = null) {

        observerExecutionObserver?.let { observer ->

            if (lifecycleOwner == null) {
                if (!foreverExecutionObservers.contains(observer)) {
                    foreverExecutionObservers.add(observer)
                }
            } else {
                if (!lifecycleExecutionObservers.containsKey(lifecycleOwner)) {
                    lifecycleExecutionObservers[lifecycleOwner] = observer
                    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
                }
            }
        }
    }

    @JvmSynthetic
    internal fun startTrigger(trigger: Trigger): Fragment? {

        // Until we add a trigger max_activation" count
        if (getResponse().isComplete) {
            notifySurveyAlreadyCompleteObservers()
            Log.d("Skipping trigger ($id already answered)")
            return null
        }

        return when (trigger.presentation) {
            TriggerPresentation.FRAGMENT -> SurveyFragment.create(id)
            else -> {
                ServiceTick.get().weakReference.get()?.let { context ->

                    Intent(context, SurveyActivity::class.java).run {
                        putExtra(SurveyActivity.EXTRA_SURVEY_ID, id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(this)
                    }
                }
                null
            }
        }
    }

    @JvmSynthetic
    internal fun getPageCount(): Int = renderablePages.size

    @JvmSynthetic
    internal fun complete() {
        getResponse().complete()
        notifySurveyCompleteObservers()
    }

    @JvmOverloads
    fun start(presentation: TriggerPresentation = TriggerPresentation.START_ACTIVITY, observer: ExecutionObserver? = null, lifecycleOwner: LifecycleOwner? = null): Fragment? {
        addExecutionObserver(observer, lifecycleOwner)
        return startTrigger(ManualTrigger(presentation))
    }

    override fun toString(): String {
        return "Survey(id=$id, title=$title, type=$type, state=$state, lastUpdated=${lastUpdated?.time.toString()}, refreshInterval=$refreshInterval)\n   pageTransitions=$pageTransitions\n   questionOptionActions=$questionOptionActions\n   questions=$questions\n   triggers=$triggers\n"
    }

    @JvmSynthetic
    internal fun injectResponseAnswers() {

        if (!isAnswerInjectionComplete) {

            val appExecutors: AppExecutors = get()
            appExecutors.generalBackground().execute {

                getResponse().answers.forEach { answer ->
                    questions.find {
                        it.id == answer.surveyQuestionId
                    }.run {
                        this?.answer = answer
                    }
                }
                isAnswerInjectionComplete = true
            }
        }
    }

    @JvmSynthetic
    internal fun getResponse(): SurveyResponse {
        return if (response.isNotEmpty()) {
            response.last()
        } else {
            buildResponse().also {
                response.add(it)
                it.save()
            }
        }
    }

    @JvmSynthetic
    internal fun convertTriggerClasses() {
        triggers.forEachIndexed { index, trigger ->
            if (trigger.javaClass.kotlin == Trigger::class) {
                triggers[index] = Trigger.convertTrigger(trigger)
            }
        }
    }

    private fun buildResponse(): SurveyResponse {
        return SurveyResponse().apply {
            surveyId = this@Survey.id
            questions.filter {
                it.isAnswerable()
            }.forEach { question ->
                addAnswer(SurveyResponseAnswer().apply {
                    surveyQuestionId = question.id ?: 0
                    surveyQuestionTypeId = question.questionTypeId
                })
            }
        }
    }

    companion object {
        internal val DEFAULT_REFRESH_INTERVAL = TimeUnit.HOURS.toMillis(24)
        internal val MINIMUM_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(60)
    }

    enum class State {

        /**
         * The Survey has been queued for initialisation
         */
        ENQUEUED,

        /**
         * The Survey has been initialised and ready to use
         */
        INITIALISED,

        /**
         * The Survey has been disabled
         */
        DISABLED
    }

    interface ExecutionObserver {
        fun onPageChange(newPage: Int, oldPage: Int)
        fun onSurveyComplete()
        fun onSurveyAlreadyComplete()
    }

    interface StateChangeObserver {
        fun onSurveyStateChange(surveyState: Survey.State, survey: Survey?)
    }
}