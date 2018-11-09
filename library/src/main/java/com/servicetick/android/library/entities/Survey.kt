package com.servicetick.android.library.entities

import androidx.room.Ignore
import androidx.room.Relation
import com.servicetick.android.library.entities.db.BaseSurveyQuestion
import com.servicetick.android.library.triggers.Trigger
import java.util.*
import java.util.concurrent.TimeUnit

class Survey internal constructor(val id: Long) {

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

    fun addTrigger(trigger: Trigger) {
    }

    override fun toString(): String {
        return "Survey(id=$id, title=$title, type=$type, state=$state, lastUpdated=${lastUpdated?.time.toString()}, refreshInterval=$refreshInterval)\n   pageTransitions=$pageTransitions\n   questionOptionActions=$questionOptionActions\n   questions=$questions\n"
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