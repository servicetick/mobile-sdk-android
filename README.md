[![Release](https://jitpack.io/v/com.gitlab.servicetick/mobile-sdk-android.svg)](https://jitpack.io/#com.gitlab.servicetick/mobile-sdk-android)

# ServiceTick Mobile SDK for Android

The ServiceTick Mobile SDK allows you to add ServiceTick surveys to your Android application.

## Features

* Adding multiple surveys with multiple pages / questions to your app
* Surveys are downloaded and updated periodically
* Responses are submitting to ServiceTick Importer when a Internet connection is available or queued until such time
* Launching the surveys in a new Activity or Fragment
* Receiving callbacks from the Survey as the users completes them
  * Page changing
  * Survey completion
  * Survey already completed
* Manually launching surveys
* Adding Triggers
  * Application Run Count Trigger
  * Application Run Time Trigger
* Observing Triggers for when the fire
  * Launching the survey from a fired trigger

## Documentation
- Prerequisites
  - ServiceTick Client account ID
  - Survey ID
  - Survey Access key
  - Importer access key
- [Installation](docs/installation.md)
- Configuration [Kotlin](docs/configuration_kotlin.md) | [Java](docs/configuration_java.md)
- Basic usage [Kotlin](docs/basic_usage_kotlin.md) | [Java](docs/basic_usage_java.md)
- Triggers [Kotlin](docs/triggers_kotlin.md) | [Java](docs/triggers_java.md)
