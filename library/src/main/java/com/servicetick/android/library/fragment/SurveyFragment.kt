package com.servicetick.android.library.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.servicetick.android.library.R
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.viewmodel.SurveysViewModel
import lilhermit.android.remotelogger.library.Log

class SurveyFragment : BaseFragment() {

    private var viewModel: SurveysViewModel? = null
    private var surveyId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceTick = ServiceTick.get()
        serviceTick.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_survey, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = getViewModel(SurveysViewModel::class.java)

        arguments?.getLong(ARG_SURVEY_ID)?.let { id ->
            if (id > 0L) {
                viewModel?.getSurvey(id)?.observe(this, Observer {
                })
            } else {
                Log.e("Can't retrieve survey ID is null (SurveyFragment)")
            }
        }

    }

    companion object {
        private const val ARG_SURVEY_ID = "com.servicetick.android.library.fragment.Fsurvey_id"
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