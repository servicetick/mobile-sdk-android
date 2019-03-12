package com.servicetick.android.library.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.repository.SurveyRepository

internal class SurveysViewModel constructor(private val surveyRepository: SurveyRepository) : ViewModel() {

    private var survey: Survey? = null

    fun getSurvey(id: Long): Survey? {
        return survey?.let {
            it
        } ?: run {
            survey = ServiceTick.get().getSurvey(id)
            survey
        }
    }

    fun getQuestionsForPage(sourcePageId: Long): LiveData<List<SurveyQuestion>> {

        val liveData = MutableLiveData<List<SurveyQuestion>>()
        survey?.run {
            val questions = questions.filter {
                it.pageId == sourcePageId
            }.sortedBy {
                it.questionOrder
            }

            liveData.postValue(questions)
        }
        return liveData

    }
}