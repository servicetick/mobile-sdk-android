# Configuration - Kotlin

First we need to initialise the ServiceTick SDK with our client account id, survey access key and importer access key, this is best done in the Application object. If you have not already done so setup an Application object within your project.

1) Create an Application class


```kotlin
package com.example.myapp

import android.app.Application

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

    }
}
```

2) Add the newly created Application to the `AndroidManifest.xml` using the `android:name` attribute

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest package="com.example.myapp"
    xmlns:android="http://schemas.android.com/apk/res/android">
  <application
      android:icon="@mipmap/ic_launcher"
      android:label="@string/app_name"
      android:name=".MyApp">
    ...
  </application>
</manifest>
```

3) Initialise the ServiceTick Mobile SDK

```java
@Override
public void onCreate() {
    super.onCreate();

    val serviceTick = ServiceTick
            .setImporterAccessKey({IMPORTER_ACCESS_KEY_HERE})
            .setSurveyAccessKey({SURVEY_ACCESS_KEY_HERE})
            .setClientAccountId({CLIENT_ACCOUNT_ID_HERE})
            .build()
}
```
