package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.getSystemService
import androidx.core.view.forEach
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyQuestionOption


internal class CheckboxQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    private var checkboxContainer: FlexboxLayout? = null

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        postLayout(context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_checkbox, this, true))
    }

    override fun postLayout(view: View?) {
        super.postLayout(view)
        view?.run {
            checkboxContainer = findViewById(R.id.checkboxContainer)
        }
    }

    override fun updateView() {
        super.updateView()

        checkboxContainer?.run {

            flexDirection = if (question?.horizontal == true) FlexDirection.ROW else FlexDirection.COLUMN

            question?.options?.forEach {
                addView(createCheckbox(it))
            }
        }
    }

    private fun createCheckbox(questionOption: SurveyQuestionOption): AppCompatCheckBox {

        val checkbox = AppCompatCheckBox(context)
        checkbox.text = questionOption.option
        checkbox.id = questionOption.id?.toInt() ?: -1
        return checkbox
    }

    private fun getChecked(): Array<String> {
        val checked = arrayListOf<String>()
        checkboxContainer?.forEach { view ->
            if (view.isSelected && view is AppCompatCheckBox) {
                checked.add(view.text.toString())
            }
        }

        return checked.toTypedArray()
    }

    override fun isValid(): Boolean {

        val min = question?.minRequiredAnswers ?: 0
        val max = question?.maxRequiredAnswers ?: 0

        val valid = super.isValid() || question?.minRequiredAnswers == 0 || getChecked().size in min..max


        if (!valid) {
            // TODO Error handling
//            setError()
        }

        return valid
    }
}