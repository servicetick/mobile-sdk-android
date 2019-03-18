# Basic usage - Kotlin

## Adding a survey

It is best to add the survey as early into the Application lifecycle as possible, this gives the SDK a chance to download / update the Survey. It is recommended to do this in `onCreate` of `Application` and the simplest form is below:

```Kotlin
class MyApp : Application() {

  override fun onCreate() {
      super.onCreate()

      ...

      val surveyBuilder = SurveyBuilder.create({SURVEY_ID_HERE})
      serviceTick.addSurvey(surveyBuilder)
  }
}
```

## Getting your initialised survey via `Survey.StateChangeObserver`

When a survey is added to the SDK it is enqueued for download/update, once this is complete a survey is then initialised or disabled (if it is disabled via the ServiceTick Console)

The following registers an observer for the given `survey_id` this can be done in `Activity`/`Fragment`

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val stateChangeObserver = object : Survey.StateChangeObserver {
        override fun onSurveyStateChange(surveyState: Survey.State, survey: Survey?) {

            if (surveyState == Survey.State.INITIALISED) {

                survey?.run {

                  //  Here we can:
                  // - Store our survey to a field/variable
                  // - Manually trigger
                  // - Observe triggers (detailed later)
                }
            }
        }
    }

    ServiceTick.get().observeSurveyStateChange({SURVEY_ID_HERE}, this, stateChangeObserver)
}
```

## Getting your survey via get()

The observer method is the best way to obtain a copy of your survey however you can use the `get()` method. This method is however discouraged as your survey might not yet be initialised and will return `null`.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val survey = ServiceTick.get().getSurvey({SURVEY_ID_HERE})
}
```

## Manually triggering a survey
You can manually trigger your survey from either an `Activity` or `Fragment`, the only parameter required is how you want the survey "presenting". This tells the SDK to start an new activity or return a `Fragment` for you to add to your `FragmentManager`

### Starting a new Activity

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Once you have your survey, manually launching it is simple
    survey?.start()
}
```

### Starting via a Fragment

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Once you have your survey, manually launching it is simple
    survey?.start(TriggerPresentation.FRAGMENT)?.let { fragment ->
           requireFragmentManager().beginTransaction().replace(R.id.content, fragment, "survey_fragment").addToBackStack("survey").commit()
     }
}
```

## Observing a Survey whilst it's being completed with `Survey.ExecutionObserver`

You can register an observer with the `start` method (as above) or the `launchSurvey` method of trigger (covered later). The observer has 3 methods, `onPageChange`, `onSurveyComplete` and `onSurveyAlreadyComplete`

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Setup the observer
    val manualSurveyFragmentObserver = object : Survey.ExecutionObserver {
         override fun onSurveyAlreadyComplete() {
           // This is called if you try to launch a survey which has already been completed
         }

         override fun onSurveyComplete() {
           // Pop the fragment from the back stack
           requireFragmentManager().popBackStack("survey", FragmentManager.POP_BACK_STACK_INCLUSIVE)
         }

         override fun onPageChange(newPage: Int, oldPage: Int) {
           // This is called as the user progresses through the survey pages
         }
     }

     // Start the survey
     survey?.start(TriggerPresentation.FRAGMENT, manualSurveyFragmentObserver, requireActivity())?.let { fragment ->
         requireFragmentManager().beginTransaction().replace(R.id.content, fragment, "survey_fragment").addToBackStack("survey").commit()
     }
}
```

_Note: The 3rd parameter of `start` is a LifecycleOwner (`Activity`/`Fragment`) which if null will observe the Survey forever. See here for more information on [Android Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle)_
