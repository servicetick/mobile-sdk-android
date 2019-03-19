# Basic usage - Java

## Adding a survey

It is best to add the survey as early into the Application lifecycle as possible, this gives the SDK a chance to download / update the Survey. It is recommended to do this in `onCreate` of `Application` and the simplest form is below:

```java
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ...

        SurveyBuilder surveyBuilder = SurveyBuilder
                .create({SURVEY_ID_HERE})

        serviceTick.addSurvey(surveyBuilder);
    }
}
```

## Getting your initialised survey via `Survey.StateChangeObserver`

When a survey is added to the SDK it is enqueued for download/update, once this is complete a survey is then initialised or disabled (if it is disabled via the ServiceTick Console)

The following registers an observer for the given `survey_id` this can be done in `Activity`/`Fragment`

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Survey.StateChangeObserver stateChangeObserver = new Survey.StateChangeObserver() {
        @Override
        public void onSurveyStateChange(@NotNull Survey.State surveyState, @Nullable Survey survey) {
            if (surveyState == Survey.State.INITIALISED && survey != null) {

                //  Here we can:
                // - Store our survey to a field/variable
                // - Manually trigger
                // - Observe triggers (detailed later)
            }
        }
    };

    ServiceTick.get().observeSurveyStateChange({SURVEY_ID_HERE}, this, stateChangeObserver);
}
```

## Getting your survey via getSurvey()

The observer method is the best way to obtain a copy of your survey however you can use the `get()` method. This method is however discouraged as your survey might not yet be initialised and will return `null`.

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Survey survey = ServiceTick.get().getSurvey({SURVEY_ID_HERE});
}
```

## Manually triggering a survey
You can manually trigger your survey from either an `Activity` or `Fragment`, the only parameter required is how you want the survey "presenting". This tells the SDK to start an new activity or return a `Fragment` for you to add to your `FragmentManager`

### Starting a new Activity

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Once you have your survey, manually launching it is simple
    if (survey != null) {
      survey.start();
    }
}
```

### Starting via a Fragment

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Once you have your survey, manually launching it is simple
    if (survey != null) {
        Fragment fragment = survey.start(TriggerPresentation.FRAGMENT);
        if (fragment != null) {
            requireFragmentManager().beginTransaction().replace(R.id.content, fragment, "survey_fragment").addToBackStack("survey").commit();
        }
    }
}
```

## Observing a Survey whilst it's being completed with `Survey.ExecutionObserver`

You can register an observer with the `start` method (as above) or the `launchSurvey` method of trigger (covered later). The observer has 3 methods, `onPageChange`, `onSurveyComplete` and `onSurveyAlreadyComplete`

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (survey != null) {
        // Setup the observer
        Survey.ExecutionObserver manualSurveyFragmentObserver = new Survey.ExecutionObserver() {
            @Override
            public void onSurveyAlreadyComplete() {
                // This is called if you try to launch a survey which has already been completed
            }

            @Override
            public void onSurveyComplete() {
                // Pop the fragment from the back stack
                requireFragmentManager().popBackStack("survey", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

            @Override
            public void onPageChange(int newPage, int oldPage) {
                // This is called as the user progresses through the survey pages
            }
        };

        Fragment fragment = survey.start(TriggerPresentation.FRAGMENT, manualSurveyFragmentObserver, this);
        if (fragment != null) {
            requireFragmentManager().beginTransaction().replace(R.id.content, fragment,  "survey_fragment").addToBackStack("survey").commit();
        }
    }
}
```

_Note: The 3rd parameter of `start` is a LifecycleOwner (`Activity`/`Fragment`) which if null will observe the Survey forever. See here for more information on [Android Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle)_
