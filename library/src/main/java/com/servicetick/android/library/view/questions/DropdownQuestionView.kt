package com.servicetick.android.library.view.questions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.getSystemService
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyQuestionOption

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

            // Setup the options including a dummy "Please select" item
            val options = question?.options?.toMutableList() ?: mutableListOf()
            options.add(0, SurveyQuestionOption().apply {
                id = -1
                option = context.getString(R.string.please_select_one)
            })

            // Setup an adapter which handles the ID too
            adapter = object : ArrayAdapter<SurveyQuestionOption>(context, android.R.layout.simple_spinner_item, options) {

                private fun getText(position: Int): String = super.getItem(position)?.option ?: ""

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                        super.getView(position, convertView, parent).apply {
                            findViewById<TextView>(android.R.id.text1)?.text = getText(position)
                        }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                        super.getDropDownView(position, convertView, parent).apply {
                            findViewById<TextView>(android.R.id.text1)?.text = getText(position)
                        }

                override fun getItemId(position: Int): Long {
                    return super.getItem(position)?.id ?: -1
                }

            }.apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            options.forEachIndexed { index, surveyQuestionOption ->
                if (surveyQuestionOption.id == getAnswerId()) {
                    setSelection(index)
                }
            }
        }
    }

    override fun isValid(): Boolean {
        val valid = super.isValid() || minRequiredAnswers() == 0 || (minRequiredAnswers() != 0 && spinner?.selectedItemId ?: -1 > 0)

        if (!valid) {
            // TODO Error handling
//            setError()
        }

        return valid
    }

    override fun syncAnswer() {
        if (isAnswerSyncable()) {
            question?.answer?.answer = spinner?.selectedItemId.toString()
        }
    }
}