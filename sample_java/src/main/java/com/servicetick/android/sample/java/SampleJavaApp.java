package com.servicetick.android.sample.java;

import android.app.Application;
import android.preference.PreferenceManager;

import com.servicetick.android.library.ServiceTick;
import com.servicetick.android.library.SurveyBuilder;
import com.servicetick.android.library.entities.triggers.builders.ApplicationRunCountTriggerBuilder;
import com.servicetick.android.library.entities.triggers.builders.ApplicationRunTimeTriggerBuilder;

public class SampleJavaApp extends Application {

    public static Long SURVEY_ID = 3025L;
    private static Long CLIENT_ACCOUNT_ID = 530L;
    private static String SURVEY_ACCESS_KEY = "41bd9e6d-a817-46f2-8f2c-dc85ba9a0fc2";
    private static String IMPORTER_ACCESS_KEY = "f9b3e579-9ac1-4f0f-a20e-be3aa6cbce17";

    public static String PREF_TRIGGERS_ON = "triggers_on";
    public static String TRIGGER_APP_RUN_COUNT = "app_run_count";
    public static String TRIGGER_APP_RUN_TIME = "app_run_time";
    public static Long TRIGGER_APP_RUN_TIME_VAL = 120L;
    public static int TRIGGER_APP_RUN_COUNT_VAL = 5;

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceTick
                .setImporterAccessKey(IMPORTER_ACCESS_KEY)
                .setSurveyAccessKey(SURVEY_ACCESS_KEY)
                .setClientAccountId(CLIENT_ACCOUNT_ID);

        Boolean triggersOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_TRIGGERS_ON, false);

        SurveyBuilder surveyBuilder = SurveyBuilder
                .create(SampleJavaApp.SURVEY_ID)
                .addTrigger(ApplicationRunCountTriggerBuilder.setTag(TRIGGER_APP_RUN_COUNT).setRunCount(TRIGGER_APP_RUN_COUNT_VAL).setActive(triggersOn))
                .addTrigger(ApplicationRunTimeTriggerBuilder.setTag(TRIGGER_APP_RUN_TIME).setRunTime(TRIGGER_APP_RUN_TIME_VAL).setActive(triggersOn));

        ServiceTick.get().addSurvey(surveyBuilder);
    }
}