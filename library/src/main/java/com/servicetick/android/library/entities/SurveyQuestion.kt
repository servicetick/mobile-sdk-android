package com.servicetick.android.library.entities

import android.content.Context
import androidx.room.Ignore
import androidx.room.Relation
import com.servicetick.android.library.view.questions.DropdownQuestionView
import com.servicetick.android.library.view.questions.QuestionView
import com.servicetick.android.library.view.questions.TextBoxQuestionView

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

    fun shouldRender(): Boolean = !deleted && shouldRenderType()

    private fun shouldRenderType(): Boolean = questionType in arrayOf(
            QuestionType.SINGLE_TEXT_BOX,
            QuestionType.DROP_DOWN_BOX
    )

    override fun toString(): String {
        return "SurveyQuestion(id=$id, surveyId=$surveyId, originalQuestionId=$originalQuestionId, question='$question', questionTypeId=$questionTypeId, questionType=$questionType, initiallyVisible=$initiallyVisible, deleted=$deleted, questionOrder=$questionOrder, pageId=$pageId, minRequiredAnswers=$minRequiredAnswers, maxRequiredAnswers=$maxRequiredAnswers, marginId=$marginId, margin=$margin, horizontal=$horizontal, isTableQuestion=$isTableQuestion, surveyTableQuestionDescription=$surveyTableQuestionDescription, completed=$completed, textBoxType=$textBoxType)\n   options=$options"
    }

    fun getView(context: Context): QuestionView {
        return when (questionType) {
            QuestionType.SINGLE_TEXT_BOX -> TextBoxQuestionView(context)
            QuestionType.DROP_DOWN_BOX -> DropdownQuestionView(context)
            else -> QuestionView(context)
        }.apply {
            this.question = this@SurveyQuestion
        }
    }
}