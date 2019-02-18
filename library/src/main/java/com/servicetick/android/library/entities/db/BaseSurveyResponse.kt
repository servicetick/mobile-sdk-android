package com.servicetick.android.library.entities.db

import androidx.room.*
import com.servicetick.android.library.entities.SurveyResponse
import com.servicetick.android.library.entities.SurveyResponseAnswer
import java.util.*

@Entity(tableName = "survey_responses",
        foreignKeys = [ForeignKey(entity = BaseSurvey::class, parentColumns = ["id"], childColumns = ["surveyId"], onDelete = ForeignKey.NO_ACTION)],
        indices = [Index(value = ["surveyId"])])
internal class BaseSurveyResponse() {

    constructor(surveyResponse: SurveyResponse) : this() {

        id = surveyResponse.id
        surveyId = surveyResponse.surveyId
        isComplete = surveyResponse.isComplete
        syncStamp = surveyResponse.syncStamp
        answers = surveyResponse.answers
    }

    @PublishedApi
    @PrimaryKey(autoGenerate = true)
    internal var id: Long = 0

    @PublishedApi
    internal var surveyId: Long = 0

    @PublishedApi
    internal var isComplete: Boolean = false

    @PublishedApi
    internal var syncStamp: Calendar? = null

    @Ignore
    internal var answers: List<SurveyResponseAnswer> = emptyList()

    override fun toString(): String {
        return "BaseSurveyResponse(id=$id, surveyId=$surveyId, isComplete=$isComplete, syncStamp=$syncStamp, answers=$answers)"
    }
}