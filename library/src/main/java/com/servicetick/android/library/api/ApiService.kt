package com.servicetick.android.library.api

import com.servicetick.android.library.entities.SurveyResponse
import com.servicetick.android.library.entities.api.PostSurveyRequest
import com.servicetick.android.library.entities.api.PostSurveyResponse
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface ApiService {

    /**
     * @POST Get the survey structure
     */
    @POST("survey")
    fun getSurvey(@Body postSurveyRequest: PostSurveyRequest): Call<PostSurveyResponse>

    /**
     * @POST The Survey Response
     */
    @POST("importer")
    fun postSurveyResponse(@Body surveyResponse: SurveyResponse): Call<ResponseBody>
}