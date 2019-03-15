package com.servicetick.android.sample.kotlin


import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.triggers.Trigger
import com.servicetick.android.library.entities.triggers.TriggerPresentation
import kotlinx.android.synthetic.main.fragment_main.*


class MainActivityFragment : Fragment() {

    private var survey: Survey? = null
    private var triggersOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        triggersOn = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(SampleKotlinApp.PREF_TRIGGERS_ON, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stateChangeObserver = object : Survey.StateChangeObserver {
            override fun onSurveyStateChange(surveyState: Survey.State, survey: Survey?) {

                lilhermit.android.remotelogger.library.Log.v("survey state: $surveyState")
                if (surveyState == Survey.State.INITIALISED) {


                    survey?.run {
                        this@MainActivityFragment.survey = this

                        Toast.makeText(context, R.string.survey_initialised_successfully, Toast.LENGTH_SHORT).show()
                        getTriggerWrapper(SampleKotlinApp.TRIGGER_APP_RUN_COUNT)?.observe(this@MainActivityFragment) {
                            app_run_count_trigger.setText(R.string.trigger_app_run_count_triggered)

                            // You can launcher your survey from here by calling Trigger.launchSurvey() which
                            // Survey.ExecutionObserver and lifecycle owner
                        }

                        getTriggerWrapper(SampleKotlinApp.TRIGGER_APP_RUN_TIME)?.observe(this@MainActivityFragment) {
                            app_run_time_trigger.setText(R.string.trigger_app_run_time_triggered)

                            // You can launcher your survey from here by calling Trigger.launchSurvey() which
                            // Survey.ExecutionObserver and lifecycle owner
                        }
                    }
                    updateView()
                }
            }
        }

        ServiceTick.get().observeSurveyStateChange(SampleKotlinApp.SURVEY_ID, this, stateChangeObserver)

        manual_survey_fragment.setOnClickListener {

            val manualSurveyFragmentObserver = object : Survey.ExecutionObserver {
                override fun onSurveyAlreadyComplete() =
                        Toast.makeText(context, R.string.survey_already_completed, Toast.LENGTH_SHORT).show()

                override fun onSurveyComplete() =
                        requireFragmentManager().popBackStack("survey", FragmentManager.POP_BACK_STACK_INCLUSIVE)

                override fun onPageChange(newPage: Int, oldPage: Int) =
                        Toast.makeText(context, getString(R.string.on_page_change_toast, newPage, oldPage), Toast.LENGTH_SHORT).show()
            }

            survey?.start(TriggerPresentation.FRAGMENT, manualSurveyFragmentObserver, requireActivity())?.let { fragment ->
                requireFragmentManager().beginTransaction().replace(R.id.content, fragment, "survey_fragment").addToBackStack("survey").commit()
            }
        }
        manual_survey_activity.setOnClickListener {
            survey?.start(TriggerPresentation.START_ACTIVITY, object : Survey.ExecutionObserver {
                override fun onSurveyAlreadyComplete() =
                        Toast.makeText(context, R.string.survey_already_completed, Toast.LENGTH_SHORT).show()

                override fun onPageChange(newPage: Int, oldPage: Int) =
                        Toast.makeText(context, getString(R.string.on_page_change_toast, newPage, oldPage), Toast.LENGTH_SHORT).show()

                override fun onSurveyComplete() {}
            })
        }
        toggle_triggers.setOnClickListener {
            toggleTriggerState()
        }

        updateView()
    }

    /**
     * Wrapper method which gets Trigger regardless of active state
     *
     * Survey.getTrigger(String) only returns active triggers, generally SDK devs will not have to change trigger
     * states at runtime
     */
    private fun getTriggerWrapper(tag: String): Trigger? {
        return survey?.getAllTriggers()?.firstOrNull { tag == it.tag }
    }

    private fun toggleTriggerState() {
        triggersOn = !triggersOn
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(SampleKotlinApp.PREF_TRIGGERS_ON, triggersOn)
        }
        if (triggersOn) {
            Toast.makeText(requireContext(), R.string.triggers_will_start_next_launch, Toast.LENGTH_LONG).show()
        }

        getTriggerWrapper(SampleKotlinApp.TRIGGER_APP_RUN_COUNT)?.active = triggersOn
        getTriggerWrapper(SampleKotlinApp.TRIGGER_APP_RUN_TIME)?.active = triggersOn
        updateTriggerViews()
    }

    private fun updateTriggerViews() {
        toggle_triggers?.run {
            isEnabled = survey != null
            setText(if (triggersOn) R.string.triggers_on else R.string.triggers_off)
        }

        app_run_count_trigger.setText(if (triggersOn) R.string.trigger_app_run_count_active else R.string.trigger_app_run_count_not_active)
        app_run_time_trigger.setText(if (triggersOn) R.string.trigger_app_run_time_active else R.string.trigger_app_run_time_not_active)
    }

    private fun updateView() {
        manual_survey_fragment.isEnabled = survey != null
        manual_survey_activity.isEnabled = survey != null
        updateTriggerViews()
    }
}
