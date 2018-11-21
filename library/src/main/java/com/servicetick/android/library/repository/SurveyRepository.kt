package com.servicetick.android.library.repository

import androidx.lifecycle.LiveData
import com.servicetick.android.library.AppExecutors
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyQuestion
import javax.inject.Inject


internal class SurveyRepository @Inject constructor(private val serviceTickDao: ServiceTickDao, private val appExecutors: AppExecutors) {

    fun getSurvey(id: Long): LiveData<Survey?> = serviceTickDao.getSurveyAsLiveData(id)
    fun getQuestionsForPage(pageId: Long): LiveData<List<SurveyQuestion>> = serviceTickDao.getQuestionsForPageAsLiveData(pageId)
}