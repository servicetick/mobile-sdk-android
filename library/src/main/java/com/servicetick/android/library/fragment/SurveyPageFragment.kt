package com.servicetick.android.library.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.lifecycle.Observer
import com.servicetick.android.library.R
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.entities.SurveyPageTransition
import com.servicetick.android.library.view.questions.QuestionView
import com.servicetick.android.library.viewmodel.SurveysViewModel

class SurveyPageFragment : BaseFragment() {

    private var viewModel: SurveysViewModel? = null
    private var pageTransition: SurveyPageTransition? = null
    private var container: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageTransition = arguments?.getParcelable(ARG_PAGE_TRANSITION)

        val serviceTick = ServiceTick.get()
        serviceTick.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_survey_page, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = getViewModel(SurveysViewModel::class.java)

        pageTransition?.let { page ->
            viewModel?.getQuestionsForPage(page.sourcePageId)?.observe(this, Observer { questionList ->

                container = ((view as ScrollView)[0] as LinearLayout)

                container?.let { questionContainer ->
                    questionList.forEachIndexed { index, question ->
                        if (question.shouldRender()) {
                            questionContainer.addView(question.getView(requireContext()))
                        }
                    }

                }
            })
        }
    }

    fun canAdvance(): Boolean {

        container?.forEach { view ->
            if (view is QuestionView) {
                if (!view.isValid()) {
                    return false

                }
            }
        }
        return true
    }

    fun isCompletePage(): Boolean = pageTransition?.isCompletionPage ?: false

    companion object {
        private const val ARG_PAGE_TRANSITION = "com.servicetick.android.library.fragment.page_transition"

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