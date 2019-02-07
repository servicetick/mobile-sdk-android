package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.getSystemService
import com.servicetick.android.library.R

internal class DropdownQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    private var spinner: AppCompatSpinner? = null

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        postLayout(context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_dropdown, this, true))
    }

    override fun postLayout(view: View?) {
        super.postLayout(view)
        view?.run {
            spinner = findViewById(R.id.dropdown)
        }
    }

    override fun updateView() {
        super.updateView()

        spinner?.run {


            val options = question?.options?.map {
                it.option
            }?.toMutableList() ?: mutableListOf()

            options.add(0, context.getString(R.string.please_select_one))

            ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, options).run {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adapter = this
            }
        }
    }

    override fun isValid(): Boolean {
        val valid = super.isValid() || minRequiredAnswers() == 0 || (minRequiredAnswers() != 0 && spinner?.selectedItemId ?: 1 > 0)

        if (!valid) {
            // TODO Error handling
//            setError()
        }

        return valid
    }
}