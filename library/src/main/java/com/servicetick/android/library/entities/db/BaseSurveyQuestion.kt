package com.servicetick.android.library.entities.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.servicetick.android.library.entities.SurveyQuestionOption

@Entity(tableName = "survey_questions", foreignKeys = [ForeignKey(entity = BaseSurvey::class, parentColumns = ["id"], childColumns = ["surveyId"], onDelete = ForeignKey.CASCADE)])
internal class BaseSurveyQuestion {

    @Expose
    @SerializedName("QuestionId")
    @PrimaryKey
    var id: Long? = null

    var surveyId: Long? = null

    @Expose
    @SerializedName("OriginalQuestionId")
    var originalQuestionId: Long = 0

    @Expose
    @SerializedName("Question")
    lateinit var question: String

    @Expose
    @SerializedName("QuestionTypeId")
    var questionTypeId: Int = 0

    @Expose
    @SerializedName("InitiallyVisible")
    var initiallyVisible: Boolean = true

    @Expose
    @SerializedName("Deleted")
    var deleted: Boolean = false

    @Expose
    @SerializedName("QuestionOrder")
    var questionOrder: Int = 0

    @Expose
    @SerializedName("PageId")
    var pageId: Long? = null

    @Expose
    @SerializedName("MinRequiredAnswers")
    var minRequiredAnswers: Int? = null

    @Expose
    @SerializedName("MaxRequiredAnswers")
    var maxRequiredAnswers: Int? = null

    @Expose
    @SerializedName("MarginId")
    var marginId: String? = null

    @Expose
    @SerializedName("Margin")
    var margin: String? = null

    @Expose
    @SerializedName("Horizontal")
    var horizontal: Boolean = true

    @Expose
    @SerializedName("IsTableQuestion")
    var isTableQuestion: Boolean = false

    @Expose
    @SerializedName("SurveyTableQuestionDescription")
    var surveyTableQuestionDescription: String? = null

    @Expose
    @SerializedName("Completed")
    var completed: Boolean = false

    @Expose
    @SerializedName("Options")
    @Ignore
    var options: List<SurveyQuestionOption> = emptyList()

    @Expose
    @SerializedName("TextBoxType")
    var textBoxType: String = ""

    override fun toString(): String {
        return "SurveyQuestion(id=$id, surveyId=$surveyId, originalQuestionId=$originalQuestionId, question='$question', questionTypeId=$questionTypeId, initiallyVisible=$initiallyVisible, deleted=$deleted, questionOrder=$questionOrder, pageId=$pageId, minRequiredAnswers=$minRequiredAnswers, maxRequiredAnswers=$maxRequiredAnswers, marginId=$marginId, margin=$margin, horizontal=$horizontal, isTableQuestion=$isTableQuestion, surveyTableQuestionDescription=$surveyTableQuestionDescription, completed=$completed, options=$options, textBoxType='$textBoxType')"
    }


}