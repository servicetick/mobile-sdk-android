package com.servicetick.android.library.entities

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.room.Ignore
import androidx.room.Relation
import com.servicetick.android.library.AppExecutors
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.activity.SurveyActivity
import com.servicetick.android.library.entities.db.BaseSurveyQuestion
import com.servicetick.android.library.entities.db.BaseSurveyResponse
import com.servicetick.android.library.fragment.SurveyFragment
import com.servicetick.android.library.triggers.ManualTrigger
import com.servicetick.android.library.triggers.Trigger
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

    @Ignore
    internal var triggers: MutableList<Trigger> = mutableListOf()

    @Relation(parentColumn = "id", entityColumn = "surveyId", entity = BaseSurveyResponse::class)
    @PublishedApi
    internal var response: MutableList<SurveyResponse> = mutableListOf()

    fun addTrigger(trigger: Trigger) {
    }

    private fun startTrigger(trigger: Trigger): Fragment? {

        // Until we add a trigger max_activation" count
        if (getResponse().isComplete) {
            Log.d("Skipping trigger ($id already answered)")
            return null
        }

        return when (trigger.presentation) {
            Trigger.Presentation.FRAGMENT -> SurveyFragment.create(id)
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

    @Ignore
    private var isAnswerInjectionComplete = false

    internal fun getPageCount(): Int = pageTransitions.size

    fun start(presentation: Trigger.Presentation = Trigger.Presentation.START_ACTIVITY): Fragment? = startTrigger(ManualTrigger(presentation))

    override fun toString(): String {
        return "Survey(id=$id, title=$title, type=$type, state=$state, lastUpdated=${lastUpdated?.time.toString()}, refreshInterval=$refreshInterval)\n   pageTransitions=$pageTransitions\n   questionOptionActions=$questionOptionActions\n   questions=$questions\n"
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
}