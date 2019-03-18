package com.servicetick.android.sample.java;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.servicetick.android.library.ServiceTick;
import com.servicetick.android.library.entities.Survey;
import com.servicetick.android.library.entities.triggers.Trigger;
import com.servicetick.android.library.entities.triggers.TriggerPresentation;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivityFragment extends Fragment {

    private Survey survey = null;
    private Boolean triggersOn = false;
    private TextView appRunCountTriggerTextView = null;
    private TextView appRunTimeTriggerTextView = null;
    private Button toggleTriggersButton = null;
    private Button manualSurveyActivityButton = null;
    private Button manualSurveyFragmentButton = null;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        triggersOn = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(SampleJavaApp.PREF_TRIGGERS_ON, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appRunCountTriggerTextView = view.findViewById(R.id.app_run_count_trigger);
        appRunTimeTriggerTextView = view.findViewById(R.id.app_run_time_trigger);

        Survey.StateChangeObserver stateChangeObserver = new Survey.StateChangeObserver() {
            @Override
            public void onSurveyStateChange(@NotNull Survey.State surveyState, @Nullable Survey survey) {

                if (surveyState == Survey.State.INITIALISED && survey != null) {

                    MainActivityFragment.this.survey = survey;

                    Toast.makeText(requireContext(), R.string.survey_initialised_successfully, Toast.LENGTH_SHORT).show();

                    Trigger trigger1 = getTriggerWrapper(SampleJavaApp.TRIGGER_APP_RUN_COUNT);
                    if (trigger1 != null) {
                        trigger1.observe(MainActivityFragment.this, new Trigger.TriggerFiredObserver() {
                            @Override
                            public void triggerFired(@NotNull Trigger trigger) {
                                if (appRunCountTriggerTextView != null) {
                                    appRunCountTriggerTextView.setText(R.string.trigger_app_run_count_triggered);
                                }

                                // You can launcher your survey from here by calling Trigger.launchSurvey() which
                                // Survey.ExecutionObserver and lifecycle owner
                            }
                        });
                    }

                    Trigger trigger2 = getTriggerWrapper(SampleJavaApp.TRIGGER_APP_RUN_TIME);
                    if (trigger2 != null) {
                        trigger2.observe(MainActivityFragment.this, new Trigger.TriggerFiredObserver() {
                            @Override
                            public void triggerFired(@NotNull Trigger trigger) {
                                appRunTimeTriggerTextView.setText(R.string.trigger_app_run_time_triggered);

                                // You can launcher your survey from here by calling Trigger.launchSurvey() which
                                // Survey.ExecutionObserver and lifecycle owner
                            }
                        });
                    }
                    updateView();
                }


            }
        };

        ServiceTick.get().observeSurveyStateChange(SampleJavaApp.SURVEY_ID, this, stateChangeObserver);

        manualSurveyFragmentButton = view.findViewById(R.id.manual_survey_fragment);
        if (manualSurveyFragmentButton != null) {
            manualSurveyFragmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (survey != null) {

                        Survey.ExecutionObserver manualSurveyFragmentObserver = new Survey.ExecutionObserver() {
                            @Override
                            public void onPageChange(int newPage, int oldPage) {
                                Toast.makeText(requireContext(), getString(R.string.on_page_change_toast, newPage, oldPage), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSurveyComplete() {
                                requireFragmentManager().popBackStack("survey", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }

                            @Override
                            public void onSurveyAlreadyComplete() {
                                Toast.makeText(requireContext(), R.string.survey_already_completed, Toast.LENGTH_SHORT).show();
                            }
                        };

                        Fragment fragment = survey.start(TriggerPresentation.FRAGMENT, manualSurveyFragmentObserver, requireActivity());
                        if (fragment != null) {
                            requireFragmentManager().beginTransaction().replace(R.id.content, fragment, "survey_fragment").addToBackStack("survey").commit();
                        }
                    }

                }
            });
        }

        manualSurveyActivityButton = view.findViewById(R.id.manual_survey_activity);
        if (manualSurveyActivityButton != null) {
            manualSurveyActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (survey != null) {

                        Survey.ExecutionObserver manualSurveyFragmentObserver = new Survey.ExecutionObserver() {
                            @Override
                            public void onPageChange(int newPage, int oldPage) {
                                Toast.makeText(requireContext(), getString(R.string.on_page_change_toast, newPage, oldPage), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSurveyComplete() {
                            }

                            @Override
                            public void onSurveyAlreadyComplete() {
                                Toast.makeText(requireContext(), R.string.survey_already_completed, Toast.LENGTH_SHORT).show();
                            }
                        };

                        survey.start(TriggerPresentation.START_ACTIVITY, manualSurveyFragmentObserver, requireActivity());
                    }
                }
            });
        }

        toggleTriggersButton = view.findViewById(R.id.toggle_triggers);
        if (toggleTriggersButton != null) {
            toggleTriggersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTriggerState();
                }
            });
        }

        updateView();
    }

    /**
     * Wrapper method which gets Trigger regardless of active state
     * <p>
     * Survey.getTrigger(String) only returns active triggers, generally SDK devs will not have to change trigger
     * states at runtime
     */
    @Nullable
    private Trigger getTriggerWrapper(String tag) {
        if (survey != null) {
            for (Trigger trigger : survey.getAllTriggers()) {
                if (trigger.getTag().equals(tag)) {
                    return trigger;
                }
            }
        }
        return null;
    }

    private void toggleTriggerState() {
        triggersOn = !triggersOn;

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
        editor.putBoolean(SampleJavaApp.PREF_TRIGGERS_ON, triggersOn);
        editor.apply();

        if (triggersOn) {
            Toast.makeText(requireContext(), R.string.triggers_will_start_next_launch, Toast.LENGTH_LONG).show();
        }

        Trigger appRunCountTrigger = getTriggerWrapper(SampleJavaApp.TRIGGER_APP_RUN_COUNT);
        if (appRunCountTrigger != null) {
            appRunCountTrigger.setActive(triggersOn);
        }

        Trigger appRunTimeTrigger = getTriggerWrapper(SampleJavaApp.TRIGGER_APP_RUN_TIME);
        if (appRunTimeTrigger != null) {
            appRunTimeTrigger.setActive(triggersOn);
        }

        updateTriggerViews();
    }

    private void updateTriggerViews() {
        if (toggleTriggersButton != null) {
            toggleTriggersButton.setEnabled(survey != null);
            toggleTriggersButton.setText(triggersOn ? R.string.triggers_on : R.string.triggers_off);
        }

        if (appRunCountTriggerTextView != null) {
            appRunCountTriggerTextView.setText(triggersOn ? R.string.trigger_app_run_count_active : R.string.trigger_app_run_count_not_active);
        }

        if (appRunTimeTriggerTextView != null) {
            appRunTimeTriggerTextView.setText(triggersOn ? R.string.trigger_app_run_time_active : R.string.trigger_app_run_time_not_active);
        }
    }

    private void updateView() {
        if (manualSurveyFragmentButton != null) {
            manualSurveyFragmentButton.setEnabled(survey != null);
            manualSurveyActivityButton.setEnabled(survey != null);
        }

        updateTriggerViews();
    }
}