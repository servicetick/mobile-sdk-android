package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.core.content.getSystemService
import com.google.android.material.textfield.TextInputLayout
import com.servicetick.android.library.R
import com.servicetick.android.library.ktx.toDp

internal class TextBoxQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    private var textInputLayout: TextInputLayout? = null

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        val view = context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_text_box, this, true)
        postLayout(view)
    }

    override fun updateView() {
        super.updateView()
        getEditText()?.run {
            id = question?.id?.toInt() ?: -1
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    textInputLayout?.error = ""
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
            when (question?.textBoxType) {
                "SingleLine" -> {
                    setSingleLine()
                    setLines(1)
                    maxLines = 1
                }
                "MultiLine" -> {
                    setSingleLine(false)
                    maxLines = 255
                    minHeight = 128.toDp(resources)
                }
            }

            question?.answer?.let { answer ->
                setText(answer.answer)
            }
        }
    }

    private fun getEditText(): EditText? = textInputLayout?.editText

    override fun setupQuestionText() {
        textInputLayout?.hint = question?.question
    }

    override fun postLayout(view: View?) {
        super.postLayout(view)
        view?.run {
            textInputLayout = findViewById(R.id.textBox)
        }
    }

    override fun isValid(): Boolean {

        val valid = super.isValid() || minRequiredAnswers() == 0 || (minRequiredAnswers() != 0 && getEditText()?.text?.isEmpty() == false)

        textInputLayout?.error = if (!valid) context.getString(R.string.must_complete_question) else ""

        return valid
    }

    override fun syncAnswer() {
        if (isAnswerSyncable()) {
            question?.answer?.answer = getEditText()?.text.toString()
        }
    }
}