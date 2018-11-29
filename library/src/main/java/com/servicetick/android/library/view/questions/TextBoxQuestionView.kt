package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
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

        val valid = super.isValid() || question?.minRequiredAnswers == 0 || (question?.minRequiredAnswers != 0 && getEditText()?.text?.isEmpty() == false)

        if (!valid) {
            // TODO Error handling
//            setError()
        }

        return valid
    }
}