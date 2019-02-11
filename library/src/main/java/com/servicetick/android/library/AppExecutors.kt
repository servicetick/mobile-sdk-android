package com.servicetick.android.library

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * Global executor pools for the whole application.
 *
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
internal class AppExecutors(private val generalBackground: ScheduledExecutorService, private val diskIO: ScheduledExecutorService, private val networkIO: ScheduledExecutorService, private val mainThread: Executor) {

    constructor() : this(
            Executors.newScheduledThreadPool(3),
            Executors.newScheduledThreadPool(3),
            Executors.newScheduledThreadPool(3),
            MainThreadExecutor())

    fun diskIO(): ScheduledExecutorService {
        return diskIO
    }

    fun generalBackground(): ScheduledExecutorService {
        return generalBackground
    }

    fun networkIO(): ScheduledExecutorService {
        return networkIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
