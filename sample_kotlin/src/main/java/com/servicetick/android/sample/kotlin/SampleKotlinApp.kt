package com.servicetick.android.sample.kotlin

import android.app.Application
import android.preference.PreferenceManager
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.SurveyBuilder
import com.servicetick.android.library.entities.triggers.builders.ApplicationRunCountTriggerBuilder
import com.servicetick.android.library.entities.triggers.builders.ApplicationRunTimeTriggerBuilder

class SampleKotlinApp : Application() {

    override fun onCreate() {
        super.onCreate()

        ServiceTick
                .setClientAccountId(CLIENT_ACCOUNT_ID)
                .setSurveyAccessKey(SURVEY_ACCESS_KEY)
                .setImporterAccessKey(IMPORTER_ACCESS_KEY)

        val triggersOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_TRIGGERS_ON, false)

        val surveyBuilder = SurveyBuilder
                .create(SampleKotlinApp.SURVEY_ID)
                .addTrigger(ApplicationRunCountTriggerBuilder.setTag(TRIGGER_APP_RUN_COUNT).setRunCount(TRIGGER_APP_RUN_COUNT_VAL).setActive(triggersOn))
                .addTrigger(ApplicationRunTimeTriggerBuilder.setTag(TRIGGER_APP_RUN_TIME).setRunTime(TRIGGER_APP_RUN_TIME_VAL).setActive(triggersOn))

        ServiceTick.get().addSurvey(surveyBuilder)
    }

    companion object {
        internal const val SURVEY_ID = 3025L
        private const val CLIENT_ACCOUNT_ID = 530L
        private const val SURVEY_ACCESS_KEY = "41bd9e6d-a817-46f2-8f2c-dc85ba9a0fc2"
        private const val IMPORTER_ACCESS_KEY = "f9b3e579-9ac1-4f0f-a20e-be3aa6cbce17"

        const val PREF_TRIGGERS_ON = "triggers_on"
        const val TRIGGER_APP_RUN_COUNT = "app_run_count"
        const val TRIGGER_APP_RUN_TIME = "app_run_time"
        const val TRIGGER_APP_RUN_TIME_VAL = 120L
        const val TRIGGER_APP_RUN_COUNT_VAL = 5
    }
}