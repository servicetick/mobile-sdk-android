package com.servicetick.android.library.fragment

import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getLong(ARG_SURVEY_ID)?.let { id ->
            if (id > 0L) {

                viewModel.getSurvey(id)?.run {
                    survey = this
                    survey.injectResponseAnswers()
                }


            } else {
                Log.e("Can't retrieve survey ID is null (SurveyFragment)")
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_survey, container, false)

        viewPager = (view.findViewById(R.id.viewPager) as ExtendedViewPager).apply {

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    updateView(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            adapter = SurveyPageAdapter(childFragmentManager)
            // Update the view after ViewPager has completed layout
            post {
                updateView()
            }
        }

        view.buttonNext.setOnClickListener {
            movePage()
        }
        view.buttonBack.setOnClickListener {
            movePage(false)
        }
        view.buttonFinish.setOnClickListener {
            if (getCurrentFragment().canAdvance()) {

                survey.complete()

                // Finish the activity
                if (requireActivity() is SurveyActivity) {
                    requireActivity().finish()
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
                survey.notifyPageChangeObservers(viewPager.currentItem + 2, viewPager.currentItem + 1)
                viewPager.currentItem = viewPager.currentItem + 1
            } else if (!forward && viewPager.currentItem > 0) {
                survey.notifyPageChangeObservers(viewPager.currentItem, viewPager.currentItem + 1)
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }

    private fun getAdapter(): SurveyPageAdapter = viewPager?.adapter as SurveyPageAdapter
    private fun getCurrentFragment(): SurveyPageFragment = getAdapter().getFragment(viewPager?.currentItem ?: 0)

    internal inner class SurveyPageAdapter constructor(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

        private val pageFragments: SparseArray<SurveyPageFragment> = SparseArray()

        override fun getItem(position: Int): Fragment {
            return SurveyPageFragment.create(survey.renderablePages[position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as SurveyPageFragment
            pageFragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, fragment: Any) {
            pageFragments.remove(position)
            super.destroyItem(container, position, fragment)
        }

        fun getFragment(position: Int): SurveyPageFragment = if (pageFragments.size() >= position) pageFragments[position] else SurveyPageFragment()

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