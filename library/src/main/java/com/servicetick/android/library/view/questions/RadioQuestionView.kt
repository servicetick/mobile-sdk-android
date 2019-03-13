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
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams


internal class RadioQuestionView @TargetApi(Build.VERSION_CODES.LOLLIPOP)
constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : QuestionView(context, attrs, defStyleAttr) {

    private val radioGroup: RadioGroup by lazy {
        findViewById<RadioGroup>(R.id.radioGroup)
    }
    private val indicatorSeekBar: IndicatorSeekBar? by lazy {
        findViewById<IndicatorSeekBar>(R.id.indicatorSeekBar)
    }
    private var renderSlider: Boolean = false
    private var sliderValue: Int? = null

    init {
        postLayout(context.getSystemService<LayoutInflater>()?.inflate(R.layout.view_question_radio, this, true))
    }

    @JvmOverloads
    internal constructor(context: Context, @Nullable attrs: AttributeSet? = null) : this(context, attrs, 0)

    override fun updateView() {
        super.updateView()

        question?.let { surveyQuestion ->

            renderSlider = surveyQuestion.options?.none {
                !it.descriptor.isNullOrBlank()
            } == false

            if (renderSlider) {

                indicatorSeekBar?.run {
                    visibility = View.VISIBLE

                    max = surveyQuestion.options?.size?.minus(1)?.toFloat() ?: 0f
                    min = 0f

                    customTickTexts(arrayOf(
                            surveyQuestion.options?.first()?.descriptor,
                            surveyQuestion.options?.last()?.descriptor))

                    val answerId = getAnswerId()
                    if (answerId != -1L) {
                        val progress = surveyQuestion.options?.indexOfFirst {
                            it.id == answerId
                        }?.toFloat() ?: 0f

                        setProgress(progress)
                        sliderValue = progress.toInt()
                    }

                    onSeekChangeListener = object : OnSeekChangeListener {
                        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {}

                        override fun onSeeking(seekParams: SeekParams?) {
                            sliderValue = seekParams?.progress
                            clearError()
                        }
                    }
                }
            } else {
                radioGroup.run {
                    visibility = View.VISIBLE

                    surveyQuestion.options?.forEach {
                        addView(radioButton(it), RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT))
                    }

                    setOnCheckedChangeListener { _, _ -> clearError() }
                }
            }
        }
    }

    private fun radioButton(questionOption: SurveyQuestionOption): AppCompatRadioButton = AppCompatRadioButton(context).apply {
        text = questionOption.option
        id = questionOption.id?.toInt() ?: -1
        isChecked = getAnswerId() == questionOption.id
    }

    private fun getCheckedIds() = arrayListOf<Int>().apply {
        radioGroup.forEach { view ->
            if (view is RadioButton && view.isChecked) {
                add(view.id)
            }
        }
    }.toTypedArray()

    override fun isValid(): Boolean {

        val valid = super.isValid() || minRequiredAnswers() == 0 || if (renderSlider) {
            (maxRequiredAnswers() != 0 && sliderValue != null)
        } else {
            (maxRequiredAnswers() != 0 && radioGroup.checkedRadioButtonId != -1)
        }

        if (valid) clearError() else setError(R.string.must_complete_question)

        return valid
    }

    override fun syncAnswer() {
        if (isAnswerSyncable()) {
            if (renderSlider) {
                sliderValue?.let { value ->
                    question?.answer?.answer = question?.options?.get(value)?.id.toString()
                }
            } else {
                question?.answer?.answer = getCheckedIds().joinToString()
            }
        }
    }
}