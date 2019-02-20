package com.servicetick.android.library.repository

import androidx.lifecycle.LiveData
import com.servicetick.android.library.AppExecutors
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.entities.SurveyResponse


internal class SurveyRepository constructor(private val serviceTickDao: ServiceTickDao, private val appExecutors: AppExecutors) {

    fun getSurvey(id: Long): LiveData<Survey?> = serviceTickDao.getSurveyAsLiveData(id)
    fun getQuestionsForPage(pageId: Long): LiveData<List<SurveyQuestion>> = serviceTickDao.getQuestionsForPageAsLiveData(pageId)

    fun saveResponse(surveyResponse: SurveyResponse) {
        appExecutors.diskIO().execute {
            serviceTickDao.insert(surveyResponse)
        }
    }
}