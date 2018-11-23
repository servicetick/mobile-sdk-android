package com.servicetick.android.library.entities

import androidx.room.Ignore
import androidx.room.Relation
import com.servicetick.android.library.view.questions.QuestionView

internal class SurveyQuestion {

    var id: Long? = null

    var surveyId: Long? = null

    var originalQuestionId: Long = 0

    lateinit var question: String

    var questionTypeId: Int = 0

    @Ignore
    var questionType: QuestionType? = null
        get() = QuestionType.fromInt(questionTypeId)

    var initiallyVisible: Boolean = true

    var deleted: Boolean = false

    var questionOrder: Int = 0

    var pageId: Long? = null

    var minRequiredAnswers: Int? = null

    var maxRequiredAnswers: Int? = null

    var marginId: String? = null

    var margin: String? = null

    var horizontal: Boolean = true

    var isTableQuestion: Boolean = false

    var surveyTableQuestionDescription: String? = null

    var completed: Boolean = false

    @Relation(parentColumn = "id", entityColumn = "questionId")
    var options: List<SurveyQuestionOption>? = emptyList()

    var textBoxType: String = ""

    override fun toString(): String {
        return "SurveyQuestion(id=$id, surveyId=$surveyId, originalQuestionId=$originalQuestionId, question='$question', questionTypeId=$questionTypeId, questionType=$questionType, initiallyVisible=$initiallyVisible, deleted=$deleted, questionOrder=$questionOrder, pageId=$pageId, minRequiredAnswers=$minRequiredAnswers, maxRequiredAnswers=$maxRequiredAnswers, marginId=$marginId, margin=$margin, horizontal=$horizontal, isTableQuestion=$isTableQuestion, surveyTableQuestionDescription=$surveyTableQuestionDescription, completed=$completed, textBoxType=$textBoxType)\n   options=$options"
    }
}