package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.ktx.toDp

internal abstract class QuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {

    internal var question: SurveyQuestion? = null
        set(value) {
            field = value
            updateView()
        }

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    protected open fun updateView() {
        setupQuestionText()

        isGone = !(question?.initiallyVisible ?: true)
    }

    protected open fun setupQuestionText() {
        findViewById<TextView>(R.id.question)?.run {
            text = question?.question
        }
    }

    protected open fun postLayout(view: View?) {
        setPadding(16.toDp(resources), 16.toDp(resources), 16.toDp(resources), 16.toDp(resources))
        view?.run {
        }
    }

    internal open fun isValid() = !isVisible
    internal open fun syncAnswer() {
        // NOOP
    }

    protected fun isAnswerSyncable() = question?.isAnswerable() == true && question?.answer != null
    protected fun getAnswerId() = question?.answer?.answer.let { answer ->
        if (answer.isNullOrEmpty()) -1L else answer.toLong()
    }

    protected fun minRequiredAnswers() = question?.minRequiredAnswers ?: 0
    protected fun maxRequiredAnswers() = question?.maxRequiredAnswers ?: 0
}
