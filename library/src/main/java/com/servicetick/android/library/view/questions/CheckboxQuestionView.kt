package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.Nullable
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.getSystemService
import androidx.core.view.forEach
import androidx.core.view.updatePadding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyQuestionOption
import com.servicetick.android.library.ktx.toDp

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CheckboxQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    private val checkboxContainer: FlexboxLayout? by lazy {
        findViewById<FlexboxLayout>(R.id.checkboxContainer)
    }

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        postLayout(context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_checkbox, this, true))
    }

    override fun updateView() {
        super.updateView()

        checkboxContainer?.run {

            flexDirection = if (question?.horizontal == true) FlexDirection.ROW else FlexDirection.COLUMN

            question?.options?.forEach { option ->
                val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = 28.toDp(resources)
                }
                addView(createCheckbox(option), layoutParams)
            }
        }
    }

    private fun createCheckbox(questionOption: SurveyQuestionOption) = AppCompatCheckBox(context).apply {
        text = questionOption.option
        id = questionOption.id?.toInt() ?: -1
        updatePadding(32.toDp(resources))
        isChecked = questionOption.id == getAnswerId()
        setOnCheckedChangeListener { _, _ -> clearError() }
    }

    private fun getCheckedIds() = arrayListOf<Int>().apply {
        checkboxContainer?.forEach { view ->
            if (view is AppCompatCheckBox && view.isChecked) {
                add(view.id)
            }
        }
    }

    override fun isValid(): Boolean {
        val valid = super.isValid() || getCheckedIds().size in minRequiredAnswers()..maxRequiredAnswers()

        if (valid) clearError() else setError(R.string.must_complete_question)

        return valid
    }

    override fun syncAnswer() {
        if (isAnswerSyncable()) {
            question?.answer?.answer = getCheckedIds().joinToString("/")
        }
    }
}