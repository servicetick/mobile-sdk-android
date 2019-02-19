package com.servicetick.android.library.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import com.servicetick.android.library.R
import com.servicetick.android.library.entities.SurveyPageTransition
import com.servicetick.android.library.entities.SurveyQuestion
import com.servicetick.android.library.view.questions.QuestionView
import com.servicetick.android.library.viewmodel.SurveysViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SurveyPageFragment : BaseFragment() {

    private val viewModel: SurveysViewModel by sharedViewModel()
    private val pageTransition: SurveyPageTransition? by lazy { arguments?.getParcelable<SurveyPageTransition>(ARG_PAGE_TRANSITION) }
    private var questions: List<SurveyQuestion>? = null
    private val questionContainer: LinearLayout? by lazy { view?.findViewById<LinearLayout>(R.id.questionsContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageTransition?.let { page ->

            viewModel.getQuestionsForPage(page.sourcePageId).observe(this, Observer { questionsList ->

                questions = questionsList
                questionsList.forEach { question ->
                    if (question.shouldRender()) {
                        questionContainer?.addView(question.getView(requireContext()))
                    }
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_survey_page, container, false) as ViewGroup

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

    fun syncPageAnswers() {
        questionContainer?.forEach { questionView ->
            if (questionView is QuestionView) {
                questionView.syncAnswer()
                questionView.question
            }
        }
    }

    fun isCompletePage(): Boolean = pageTransition?.isCompletionPage ?: false

    companion object {
        private const val ARG_PAGE_TRANSITION = "com.servicetick.android.library.fragment.survey_page_fragment.page_transition"

        internal fun create(pageTransition: SurveyPageTransition): SurveyPageFragment {
            val fragment = SurveyPageFragment()
            Bundle().run {
                putParcelable(ARG_PAGE_TRANSITION, pageTransition)
                fragment.arguments = this
            }
            return fragment
        }
    }
}