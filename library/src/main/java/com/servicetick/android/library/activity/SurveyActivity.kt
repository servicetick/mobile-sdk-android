package com.servicetick.android.library.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.servicetick.android.library.fragment.SurveyFragment

class SurveyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val surveyId = intent?.extras?.getLong(EXTRA_SURVEY_ID)
        if (savedInstanceState === null && surveyId != null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, SurveyFragment.create(surveyId), SurveyFragment.TAG)
                    .commit()
        }
    }

    companion object {
        internal const val EXTRA_SURVEY_ID = "com.servicetick.android.library.activity.survey_id"
    }
}