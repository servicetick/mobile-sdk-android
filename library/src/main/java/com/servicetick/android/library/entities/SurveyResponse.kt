package com.servicetick.android.library.entities

import android.os.Build
import androidx.room.Ignore
import androidx.room.Relation
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.servicetick.android.library.BuildConfig
import com.servicetick.android.library.repository.SurveyRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.util.*

internal class SurveyResponse : KoinComponent {

    @Expose
    @SerializedName("ClientAccountId")
    @Ignore
    internal var clientAccountId: Long? = null

    @Expose
    @SerializedName("AccessKey")
    @Ignore
    internal var accessKey: String? = null

    @Expose
    @PublishedApi
    @SerializedName("SurveyId")
    internal var surveyId: Long = 0

    @PublishedApi
    @Expose
    @SerializedName("IVRTRansactionComplete")
    internal var isComplete: Boolean = false

    @PublishedApi
    internal var syncStamp: Calendar? = null

    @PublishedApi
    internal var id: Long = 0

    @Expose
    @SerializedName("Answers")
    @PublishedApi
    @Relation(parentColumn = "id", entityColumn = "surveyResponseId")
    internal var answers: MutableList<SurveyResponseAnswer> = mutableListOf()

    @Ignore
    @Expose
    @SerializedName("Custom")
    private var customValues: Array<Custom> = arrayOf()

    private class Custom(@Expose val Key: String, @Expose val Value: String)

    @Ignore
    internal var answersMap: MutableMap<Long, SurveyResponseAnswer> = mutableMapOf()

    internal fun buildCustomValues() {
        customValues = arrayOf(
                Custom("brand", Build.BRAND),
                Custom("model", Build.MODEL),
                Custom("manufacturer", Build.MANUFACTURER),
                Custom("version_release", Build.VERSION.RELEASE),
                Custom("library_version", BuildConfig.VERSION_NAME),
                Custom("platform", "Android"),
                Custom("version_sdk_int", Build.VERSION.SDK_INT.toString()))
    }

    internal fun addAnswer(surveyResponseAnswer: SurveyResponseAnswer) {
        answers.add(surveyResponseAnswer)
        answersMap[surveyResponseAnswer.surveyQuestionId] = surveyResponseAnswer
    }

    private fun synchroniseAnswerMapIfRequired() {
        if (answers.size != answersMap.size) {
            answersMap.clear()
            answers.forEach { surveyResponseAnswer ->
                answersMap[surveyResponseAnswer.surveyQuestionId] = surveyResponseAnswer
            }
        }
    }

    internal fun getAnswerForQuestion(questionId: Long): SurveyResponseAnswer? {
        synchroniseAnswerMapIfRequired()
        return answersMap[questionId]
    }

    override fun toString(): String {
        return "SurveyResponse(surveyId=$surveyId, isComplete=$isComplete, syncStamp=$syncStamp, id=$id, answers=$answers)"
    }

    internal fun save() {
        val surveyRepository: SurveyRepository = get()
        surveyRepository.saveResponse(this)
    }

    internal fun complete() {
        isComplete = true
        save()

        // TODO schedule the sync
    }
}