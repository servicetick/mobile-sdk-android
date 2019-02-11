package com.servicetick.android.library.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.forEach
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyPageTransition
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.view.questions.QuestionView
import com.servicetick.android.library.viewmodel.SurveysViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SurveyPageFragment : BaseFragment() {

    private val viewModel: SurveysViewModel by viewModel()
    private var pageTransition: SurveyPageTransition? = null
    private var questions: Array<SurveyQuestion>? = null
    private var questionContainer: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.run {
            pageTransition = getParcelable(ARG_PAGE_TRANSITION)
            @Suppress("UNCHECKED_CAST")
            questions = getParcelableArray(ARG_QUESTIONS) as Array<SurveyQuestion>?
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewGroup = inflater.inflate(R.layout.fragment_survey_page, container, false) as ViewGroup
        questionContainer = viewGroup.findViewById<LinearLayout>(R.id.questionsContainer)
        questions?.forEach { question ->
            if (question.shouldRender()) {
                questionContainer?.addView(question.getView(requireContext()))
            }
        }
        return viewGroup
    }

    fun canAdvance(): Boolean {
        questionContainer?.forEach { view ->
            if (view is QuestionView) {
                val valid = view.isValid()
                if (!valid) {
                    return false
                }
            }
        }
        return true
    }

    fun isCompletePage(): Boolean = pageTransition?.isCompletionPage ?: false

    companion object {
        private const val ARG_PAGE_TRANSITION = "com.servicetick.android.library.fragment.survey_page_fragment.page_transition"
        private const val ARG_QUESTIONS = "com.servicetick.android.library.fragment.survey_page_fragment.questions"

        internal fun create(pageTransition: SurveyPageTransition, questions: List<SurveyQuestion>): SurveyPageFragment {
            val fragment = SurveyPageFragment()
            Bundle().run {
                putParcelable(ARG_PAGE_TRANSITION, pageTransition)
                putParcelableArray(ARG_QUESTIONS, questions.toTypedArray())
                fragment.arguments = this
            }
            return fragment
        }
    }
}