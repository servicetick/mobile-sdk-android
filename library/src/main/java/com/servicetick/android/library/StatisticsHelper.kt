package com.servicetick.android.library

import android.app.Activity
import android.content.Intent
import org.koin.standalone.KoinComponent
import kotlin.collections.set

internal class StatisticsHelper(private val listener: StatisticsCallback) : KoinComponent {

    internal var activityCount: Int = 0
    private var activityStartTimes = HashMap<String, Long>()

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
        listener.onApplicationRun()
    }

    private fun getCurrentTime() = System.currentTimeMillis() / 1000

    internal interface StatisticsCallback {

        fun onApplicationRun()
        fun onApplicationRunTimeUpdate(time: Long)
    }
}