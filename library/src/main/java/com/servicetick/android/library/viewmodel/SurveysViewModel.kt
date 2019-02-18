package com.servicetick.android.library.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.repository.SurveyRepository

internal class SurveysViewModel constructor(private val surveyRepository: SurveyRepository) : ViewModel() {

    private lateinit var survey: Survey

    fun getSurvey(id: Long): LiveData<Survey?> {
        val liveData = MutableLiveData<Survey?>()
        if (!::survey.isInitialized || survey.id != id) {
            val tempLiveData = surveyRepository.getSurvey(id)
            tempLiveData.observeForever(object : Observer<Survey?> {
                override fun onChanged(t: Survey?) {
                    t?.let {
                        survey = it
                    }
                    liveData.postValue(t)
                    tempLiveData.removeObserver(this)
                }
            })
        } else {
            liveData.postValue(survey)
        }
        return liveData
    }

    fun getQuestionsForPage(sourcePageId: Long): LiveData<List<SurveyQuestion>> {

        val liveData = MutableLiveData<List<SurveyQuestion>>()
        if (::survey.isInitialized) {
            val questions = survey.questions.filter {
                it.pageId == sourcePageId
            }.sortedBy {
                it.questionOrder
            }

            liveData.postValue(questions)
        }
        return liveData

    }
}