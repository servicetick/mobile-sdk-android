package com.servicetick.android.library.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.repository.SurveyRepository
import javax.inject.Inject

internal class SurveysViewModel @Inject constructor(private val surveyRepository: SurveyRepository) : ViewModel() {

    fun getSurvey(id: Long): LiveData<Survey?> = surveyRepository.getSurvey(id)
    fun getQuestionsForPage(sourcePageId: Long): LiveData<List<SurveyQuestion>> = surveyRepository.getQuestionsForPage(sourcePageId)
}