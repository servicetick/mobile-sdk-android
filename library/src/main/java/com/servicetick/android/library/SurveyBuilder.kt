package com.servicetick.android.library

import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.triggers.builders.TriggerBuilder
import java.util.concurrent.TimeUnit

class SurveyBuilder(surveyId: Long) {

    private var survey: Survey = Survey(surveyId)

    fun setRefreshInterval(interval: Long, intervalTimeUnit: TimeUnit): SurveyBuilder {
        survey.refreshInterval = intervalTimeUnit.toMillis(interval)
        return this
    }

    fun setRefreshInterval(millis: Long): SurveyBuilder {
        survey.refreshInterval = millis
        return this
    }

    fun addTrigger(triggerBuilder: TriggerBuilder): SurveyBuilder {
        survey.addTrigger(triggerBuilder.build())
        return this
    }

    internal fun build(): Survey {
        return survey
    }

    companion object {

        fun create(surveyId: Long): SurveyBuilder {
            return SurveyBuilder(surveyId)
        }
    }
}