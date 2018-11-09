package com.servicetick.android.library.entities

import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "survey_question_option_action", primaryKeys = ["surveyId", "pageId"])
internal class SurveyQuestionOptionAction {

    var surveyId: Long = 0

    @Expose
    @SerializedName("PageId")
    var pageId: Long = 0

    @Expose
    @SerializedName("PageName")
    var pageName: String? = null

    @Expose
    @SerializedName("Order")
    var order: Long = 0

    @Expose
    @SerializedName("MaxQuestions")
    var maxQuestions: Long = 0

    @Expose
    @SerializedName("IsCompletionPage")
    var isCompletionPage: Boolean = false

    @Expose
    @SerializedName("QuestionId")
    var questionId: Long? = null

    @Expose
    @SerializedName("OptionId")
    var optionId: Long? = null

    @Expose
    @SerializedName("NextPageId")
    var nextPageId: Long? = 0

    @Expose
    @SerializedName("TargetQuestionId")
    var targetQuestionId: Long? = 0

    override fun toString(): String {
        return "SurveyQuestionOptionAction(surveyId=$surveyId, pageId=$pageId, pageName=$pageName, order=$order, maxQuestions=$maxQuestions, isCompletionPage=$isCompletionPage, questionId=$questionId, optionId=$optionId, nextPageId=$nextPageId, targetQuestionId=$targetQuestionId)"
    }


}