package com.servicetick.android.library.entities

import android.content.Context
import android.os.Parcel
import androidx.room.Ignore
import androidx.room.Relation
import com.servicetick.android.library.view.questions.CheckboxQuestionView
import com.servicetick.android.library.view.questions.DropdownQuestionView
import com.servicetick.android.library.view.questions.QuestionView
import com.servicetick.android.library.view.questions.TextBoxQuestionView

internal class SurveyQuestion() : KParcelable {

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

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        surveyId = parcel.readLong()
        originalQuestionId = parcel.readLong()
        question = parcel.readString() ?: ""
        questionTypeId = parcel.readInt()
        initiallyVisible = parcel.readBoolean()
        deleted = parcel.readBoolean()
        questionOrder = parcel.readInt()
        pageId = parcel.readLong()
        minRequiredAnswers = parcel.readInt()
        maxRequiredAnswers = parcel.readInt()
        marginId = parcel.readString()
        margin = parcel.readString()
        horizontal = parcel.readBoolean()
        isTableQuestion = parcel.readBoolean()
        surveyTableQuestionDescription = parcel.readString()
        completed = parcel.readBoolean()
        options = parcel.createTypedArrayList(SurveyQuestionOption.CREATOR)
        textBoxType = parcel.readString() ?: "SingleLine"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeNullable(id) { writeLong(it) }
        writeNullable(surveyId) { writeLong(it) }
        writeLong(originalQuestionId)
        writeString(question)
        writeInt(questionTypeId)
        writeBoolean(initiallyVisible)
        writeBoolean(deleted)
        writeInt(questionOrder)
        writeNullable(pageId) { writeLong(it) }
        writeNullable(minRequiredAnswers) { writeInt(it) }
        writeNullable(maxRequiredAnswers) { writeInt(it) }
        writeString(marginId)
        writeString(margin)
        writeBoolean(horizontal)
        writeBoolean(isTableQuestion)
        writeString(surveyTableQuestionDescription)
        writeBoolean(completed)
        writeTypedArray(options?.toTypedArray(), flags)
        writeString(textBoxType)
    }

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::SurveyQuestion)
    }

    fun shouldRender(): Boolean = !deleted && shouldRenderType()

    private fun shouldRenderType(): Boolean = questionType in arrayOf(
            QuestionType.SINGLE_TEXT_BOX,
            QuestionType.MULTIPLE_SELECT_CHECKBOX,
            QuestionType.DROP_DOWN_BOX
    )

    override fun toString(): String {
        return "SurveyQuestion(id=$id, surveyId=$surveyId, originalQuestionId=$originalQuestionId, question='$question', questionTypeId=$questionTypeId, questionType=$questionType, initiallyVisible=$initiallyVisible, deleted=$deleted, questionOrder=$questionOrder, pageId=$pageId, minRequiredAnswers=$minRequiredAnswers, maxRequiredAnswers=$maxRequiredAnswers, marginId=$marginId, margin=$margin, horizontal=$horizontal, isTableQuestion=$isTableQuestion, surveyTableQuestionDescription=$surveyTableQuestionDescription, completed=$completed, textBoxType=$textBoxType)\n   options=$options"
    }

    fun getView(context: Context): QuestionView {
        return when (questionType) {
            QuestionType.MULTIPLE_SELECT_CHECKBOX -> CheckboxQuestionView(context)
            QuestionType.SINGLE_TEXT_BOX -> TextBoxQuestionView(context)
            QuestionType.DROP_DOWN_BOX -> DropdownQuestionView(context)
            else -> QuestionView(context)
        }.apply {
            this.question = this@SurveyQuestion
        }
    }
}