package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Nullable
import androidx.core.content.getSystemService
import com.servicetick.android.library.R

internal class InformationBoxQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        postLayout(context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_information_box, this, true))
    }

    override fun isValid(): Boolean {
        return true
    }
}