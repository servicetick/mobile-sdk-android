package com.servicetick.android.library.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "survey_page_transitions", primaryKeys = ["surveyId", "sourcePageId"])
internal class SurveyPageTransition {

    var surveyId: Long = 0

    @Expose
    @SerializedName("SourcePageId")
    var sourcePageId: Long = 0

    @Expose
    @SerializedName("IsCompletionPage")
    var isCompletionPage = false

    @Expose
    @SerializedName("TargetPageId")
    var targetPageId: Long? = null

    @Expose
    @SerializedName("Conditions")
    @Ignore
    var conditions: List<Any> = emptyList()

    override fun toString(): String {
        return "SurveyPageTransitions(surveyId=$surveyId, sourcePageId=$sourcePageId, isCompletionPage=$isCompletionPage, targetPageId=$targetPageId, conditions=$conditions)"
    }
}