package com.servicetick.android.library.repository

import androidx.lifecycle.LiveData
import com.servicetick.android.library.AppExecutors
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.entities.Survey
import javax.inject.Inject


internal class SurveyRepository @Inject constructor(private val serviceTickDao: ServiceTickDao, private val appExecutors: AppExecutors) {

    fun getSurvey(id: Long): LiveData<Survey?> {
        return serviceTickDao.getSurveyAsLiveData(id)
    }
}