package com.servicetick.android.library.entities

import androidx.room.Ignore
import androidx.room.Relation
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
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
    internal var isComplete: Boolean = false

    @PublishedApi
    internal var syncStamp: Calendar? = null

    @PublishedApi
    internal var id: Long = 0

    @PublishedApi
    @Relation(parentColumn = "id", entityColumn = "surveyResponseId")
    internal var answers: MutableList<SurveyResponseAnswer> = mutableListOf()

    @Ignore
    internal var answersMap: MutableMap<Long, SurveyResponseAnswer> = mutableMapOf()

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
}