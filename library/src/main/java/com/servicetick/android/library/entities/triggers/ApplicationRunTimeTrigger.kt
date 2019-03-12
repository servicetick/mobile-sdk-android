package com.servicetick.android.library.entities.triggers

internal class ApplicationRunTimeTrigger(tag: String, runTime: Long, presentation: TriggerPresentation = TriggerPresentation.START_ACTIVITY) : Trigger(presentation, tag, -1) {

    private var applicationRunTime: Long = 0
        get() {
            return data[DATA_KEY_RUN_TIME]?.let { it as Long } ?: let { 0L }
        }
        set(value) {
            field = value
            data[DATA_KEY_RUN_TIME] = field
        }

    internal constructor(trigger: Trigger) : this(trigger.tag, trigger.config[CONFIG_KEY_RUN_TIME] as Long, trigger.presentation) {
        clone(trigger)
    }

    init {
        config[CONFIG_KEY_RUN_TIME] = runTime
    }

    override fun updateApplicationRunTime(time: Long, checkFire: Boolean) {
        if (active && !fired) {
            applicationRunTime += time
            scheduleSave()
        }
    }

    /**
     * We override this because we want to trigger at next run
     */
    override fun updateApplicationRunCount(count: Int, checkFire: Boolean) = fireTriggerIfRequired(checkFire)

    override fun updateData(data: HashMap<String, Any>?) {
        data?.let {
            val runTime = data[DATA_KEY_RUN_TIME]
            if (runTime is Long) {
                updateApplicationRunTime(runTime, false)
            }
        }
    }

    private fun getConfigRunTime(): Long = config[CONFIG_KEY_RUN_TIME] as Long
    override fun shouldFire(): Boolean = applicationRunTime >= getConfigRunTime()

    companion object {
        private const val CONFIG_KEY_RUN_TIME = "run_time"
        private const val DATA_KEY_RUN_TIME = "run_time"
    }
}