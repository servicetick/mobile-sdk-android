package com.servicetick.android.library.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.servicetick.android.library.R
import com.servicetick.android.library.activity.SurveyActivity
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.view.ExtendedViewPager
import com.servicetick.android.library.viewmodel.SurveysViewModel
import kotlinx.android.synthetic.main.fragment_survey.*
import kotlinx.android.synthetic.main.fragment_survey.view.*
import lilhermit.android.remotelogger.library.Log
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SurveyFragment : BaseFragment() {

    private val viewModel: SurveysViewModel by sharedViewModel()
    private var viewPager: ExtendedViewPager? = null
    private lateinit var survey: Survey

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_survey, container, false)

        viewPager = view.findViewById(R.id.viewPager) as ExtendedViewPager
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                updateView(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        view.buttonNext.setOnClickListener {
            movePage()
        }
        view.buttonBack.setOnClickListener {
            movePage(false)
        }
        view.buttonFinish.setOnClickListener {
            if (getCurrentFragment().canAdvance()) {

                survey.getResponse().complete()

                // Finish the fragment / activity
                // TODO potential callback on completion?!
                if (requireActivity() is SurveyActivity) {
                    requireActivity().finish()
                } else {
                    // TODO this will need to be removed and the user will remove or pop backstack
                    // depending on their implementation. We will need a callback for this
                    requireFragmentManager().popBackStack()
                }
            }
        }

        return view
    }

    private fun updateView(step: Int = 0) {
        buttonBack.visibility = if (step > 0) View.VISIBLE else View.INVISIBLE
        buttonNext.visibility = if (getCurrentFragment().isCompletePage()) View.GONE else View.VISIBLE
        buttonFinish.visibility = if (getCurrentFragment().isCompletePage()) View.VISIBLE else View.GONE
    }

    private fun movePage(forward: Boolean = true) {

        if (forward && !getCurrentFragment().canAdvance()) {

            // Scroll to invalid question if out of view
            // TODO fix swiping dismiss, we need a CoordinatorLayout
            showSnackBar(R.string.missed_required_questions)

            return
        }

        getCurrentFragment().syncPageAnswers()
        survey.getResponse().save()

        viewPager?.let { viewPager ->

            if (forward && viewPager.currentItem < getAdapter().count - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else if (!forward && viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.getLong(ARG_SURVEY_ID)?.let { id ->
            if (id > 0L) {
                viewModel.getSurvey(id).observe(this, Observer {
                    it?.run {
                        survey = this
                        survey.injectResponseAnswers()
                        viewPager?.adapter = SurveyPageAdapter(fragmentManager)
                        updateView()
                    }
                })
            } else {
                Log.e("Can't retrieve survey ID is null (SurveyFragment)")
            }
        }
    }

    private fun getAdapter(): SurveyPageAdapter = viewPager?.adapter as SurveyPageAdapter
    private fun getCurrentFragment(): SurveyPageFragment = getAdapter().getItem(viewPager?.currentItem
            ?: 0) as SurveyPageFragment

    internal inner class SurveyPageAdapter constructor(private var fm: FragmentManager?) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val fragment = fm?.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + position)
            return if (fragment !== null) {
                fragment
            } else {
                SurveyPageFragment.create(survey.renderablePages[position])
            }
        }

        override fun getCount(): Int = survey.getPageCount()
    }

    companion object {
        private const val ARG_SURVEY_ID = "com.servicetick.android.library.fragment.survey_id"
        internal const val TAG = "com.servicetick.android.library.fragment.SurveyFragment"

        fun create(surveyId: Long): SurveyFragment {
            val fragment = SurveyFragment()
            val args = Bundle()
            args.putLong(ARG_SURVEY_ID, surveyId)
            fragment.arguments = args
            return fragment
        }
    }
}