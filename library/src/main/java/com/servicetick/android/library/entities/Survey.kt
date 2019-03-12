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
    @Ignore
    internal var isRefreshDue: Boolean = false
        get() {
            lastUpdated?.let {
                val now = Calendar.getInstance()
                it.timeInMillis += refreshInterval
                return now.after(it) || state == State.ENQUEUED
            }
            return true
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
    internal var foreverObservers: MutableList<SurveyObserver> = mutableListOf()
    @Transient
    private var lifecycleObservers: HashMap<LifecycleOwner, SurveyObserver> = hashMapOf()

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

    internal fun addTrigger(trigger: Trigger) {
        if (triggers.none { it.tag == trigger.tag }) {
            trigger.surveyId = id
            triggers.add(trigger)
        }
    }

    fun getTrigger(triggerTag: String): Trigger? {
        return triggers.first { it.tag == triggerTag && it.active }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: SurveyObserver) {

        if (lifecycleOwner.lifecycle.currentState === Lifecycle.State.DESTROYED) {
            return
        }
        addObserver(observer, lifecycleOwner)
    }

    fun observeForever(observer: SurveyObserver) {
        addObserver(observer)
    }

    fun removeObservers(lifecycleOwner: LifecycleOwner) {
        lifecycleObservers.remove(lifecycleOwner)
    }

    fun removeObserver(observer: SurveyObserver) {
        foreverObservers.remove(observer)
    }

    internal fun notifyPageChangeObservers(newPage: Int, oldPage: Int) {
        Log.d("SurveyObserver: Notifying onPageChange forever:${foreverObservers.size}, lifecycle:${lifecycleObservers.size}")
        foreverObservers.forEach { observer ->
            observer.onPageChange(newPage, oldPage)
        }

        lifecycleObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                entry.value.onPageChange(newPage, oldPage)
            }
        }
    }

    internal fun notifySurveyCompleteObservers() {
        Log.d("SurveyObserver: Notifying onSurveyComplete forever:${foreverObservers.size}, lifecycle:${lifecycleObservers.size}")
        foreverObservers.forEach { observer ->
            observer.onSurveyComplete()
        }

        lifecycleObservers.forEach { entry ->
            if (entry.key.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                entry.value.onSurveyComplete()
            }
        }
    }

    private fun addObserver(observer: SurveyObserver, lifecycleOwner: LifecycleOwner? = null) {

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

    internal fun startTrigger(trigger: Trigger): Fragment? {

        // Until we add a trigger max_activation" count
        if (getResponse().isComplete) {
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

    internal fun getPageCount(): Int = renderablePages.size

    internal fun complete() {
        getResponse().complete()
        notifySurveyCompleteObservers()
    }

    fun start(presentation: TriggerPresentation = TriggerPresentation.START_ACTIVITY): Fragment? = startTrigger(ManualTrigger(presentation))

    override fun toString(): String {
        return "Survey(id=$id, title=$title, type=$type, state=$state, lastUpdated=${lastUpdated?.time.toString()}, refreshInterval=$refreshInterval)\n   pageTransitions=$pageTransitions\n   questionOptionActions=$questionOptionActions\n   questions=$questions\n   triggers=$triggers\n"
    }

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

    interface SurveyObserver {
        fun onPageChange(newPage: Int, oldPage: Int)
        fun onSurveyComplete()
    }
}