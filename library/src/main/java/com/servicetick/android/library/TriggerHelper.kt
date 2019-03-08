package com.servicetick.android.library

import android.app.Activity
import android.content.Intent
import android.os.Handler
import org.koin.standalone.KoinComponent
import kotlin.collections.set

internal class TriggerHelper(private val listener: TriggerHelperCallback) : KoinComponent {

    private var activityCount: Int = 0
    private var activityStartTimes = HashMap<String, Long>()
    private val applicationRunRunnable = Runnable { listener.onApplicationRun() }
    private val handler = Handler()

    internal fun addActivity(activity: Activity?) {

        activity?.run {

            if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                incrementApplicationRunCount()
            }

            activityCount++
            activityStartTimes[javaClass.name] = getCurrentTime()
        }
    }

    internal fun removeActivity(activity: Activity?) {
        activity?.run {
            if (activityCount > 0) activityCount-- else activityCount = 0

            val key = this.javaClass.name

            activityStartTimes[key]?.let { startTime ->

                incrementApplicationTime(getCurrentTime().minus(startTime))

                activityStartTimes.remove(key)
            }
        }
    }

    private fun incrementApplicationTime(value: Long) {

        listener.onApplicationRunTimeUpdate(value)
    }

    private fun incrementApplicationRunCount() {
        handler.postDelayed(applicationRunRunnable, 2000)
    }

    private fun getCurrentTime() = System.currentTimeMillis() / 1000

    internal interface TriggerHelperCallback {

        fun onApplicationRun()
        fun onApplicationRunTimeUpdate(time: Long)
    }
}