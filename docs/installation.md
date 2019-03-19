# Installation

Add the following to your build.gradle in the project root
```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```

Add the dependency to your module build.gradle.
```groovy
dependencies {
  implementation 'com.gitlab.servicetick.mobilesdk-android:module_name_here:${latest.version}'
}
```

${latest.version} = [![Release](https://jitpack.io/v/com.gitlab.servicetick/mobile-sdk-android.svg?label=Latest%20version)](https://jitpack.io/#com.gitlab.servicetick/mobile-sdk-android)

Now resynchronise your project.
