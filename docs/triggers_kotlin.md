# Triggers - Kotlin

## Creating Triggers

Triggers are created using one of two Builders `ApplicationRunCountTriggerBuilder` / `ApplicationRunCountTriggerBuilder`

```Kotlin
class MyApp : Application() {

  override fun onCreate() {
      super.onCreate()

      ...

      val trigger1 = ApplicationRunCountTriggerBuilder
              .setTag("my_run_count_trigger")
              .setRunCount(10)

      val trigger2 = ApplicationRunTimeTriggerBuilder
              .setTag("my_run_time_trigger")
              .setRunTime(600)
  }
}
```

The above creates two triggers, one which fires once the application is run 10 times and the second which fires once the app has been running 10minutes. Each trigger must be given a unique tag which is used to get the trigger at a later date. Once the trigger conditions are met the trigger observers will be called. From the observer you can then launch the survey however if you don't launch the survey observers will be called until you do. This allows you add extra logic before launching surveys. Once the survey is launched from the trigger it is marked as as "fired" and will no longer call the observers.

_Note: The run time Trigger will fire next time the app is run after the number of seconds has been surpassed._

## Adding the Triggers

Triggers are then added to the `SurveyBuilder` when the Survey is added

```Kotlin
class MyApp : Application() {

  override fun onCreate() {
      super.onCreate()

      ...

      val surveyBuilder = SurveyBuilder
          .create({SURVEY_ID_HERE})
          .addTrigger(trigger1)
          .addTrigger(trigger2)

      serviceTick.addSurvey(surveyBuilder)
  }
}
```

## Observing Triggers

Once you have your initialised survey in `Activity` or `Fragment` you can observe for Trigger fires

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    survey?.getTrigger("my_run_count_trigger")?.observe(this) {

      // This is called when the trigger is fired and "it" will contain the
      // Trigger. You add extra logic here or add a dialog asking for user
      // feedback for example

      // To launch the survey you call, this will also mark the trigger as fired
      it.launchSurvey()
    }
}
```

Just like `Survey.start` you can pass a Survey.ExecutionObserver and Android LifecycleOwner to `Trigger.launchSurvey`, [please see here](basic_usage_kotlin.md#observing-a-survey-whilst-its-being-performed-with-surveyexecutionobserver) for more information.
