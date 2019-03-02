package com.servicetick.android.library

import android.app.Activity
import android.content.Intent
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.workers.SaveStatisticsWorker
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import kotlin.collections.set

internal class StatisticsHelper : KoinComponent {

    internal var activityCount: Int = 0
    private var applicationRunCount: Int = 0
    private var applicationTime: Long = 0
    private var activityStartTimes = HashMap<String, Long>()
    private val serviceTickDao: ServiceTickDao by inject()
    private val appExecutors: AppExecutors by inject()

    init {
        appExecutors.generalBackground().execute {
            serviceTickDao.getStatistics().forEach {
                when (it.key) {
                    "applicationRunTime" -> applicationTime += it.value.toLong()
                    "applicationRunCount" -> applicationRunCount += it.value.toInt()
                }
            }
        }
    }

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

        applicationTime += value
        scheduleSave()
    }

    private fun incrementApplicationRunCount() {
        applicationRunCount++
        scheduleSave()
    }

    private fun scheduleSave() {
        SaveStatisticsWorker.enqueue(hashMapOf(
                "applicationRunCount" to applicationRunCount,
                "applicationRunTime" to applicationTime
        ))
    }

    private fun getCurrentTime() = System.currentTimeMillis() / 1000
}