package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.getSystemService
import androidx.core.view.forEach
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyQuestionOption

internal class RadioQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    private var radioGroup: RadioGroup? = null

    init {
        postLayout(context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_radio, this, true))
    }

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    override fun postLayout(view: View?) {
        super.postLayout(view)
        view?.run {
            radioGroup = findViewById(R.id.radioGroup)
        }
    }


    override fun updateView() {
        super.updateView()

        radioGroup?.run {

            question?.options?.forEach {
                addView(radioButton(it), RadioGroup.LayoutParams(0, RadioGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
        }
    }

    private fun radioButton(questionOption: SurveyQuestionOption): AppCompatRadioButton = AppCompatRadioButton(context).apply {
        text = questionOption.option
        id = questionOption.id?.toInt() ?: -1
    }

    private fun getChecked(): Array<String> {
        val checked = arrayListOf<String>()
        radioGroup?.forEach { view ->
            if (view is RadioButton && view.isChecked) {
                checked.add(view.text.toString())
            }
        }

        return checked.toTypedArray()
    }

    override fun isValid(): Boolean {

        val valid = super.isValid() || minRequiredAnswers() == 0 || (maxRequiredAnswers() != 0 && radioGroup?.checkedRadioButtonId != -1)

        if (!valid) {
            // TODO Error handling
//            setError()
        }

        return valid
    }
}