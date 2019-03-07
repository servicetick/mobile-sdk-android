package com.servicetick.android.library.entities.triggers

class ApplicationRunTimeTrigger(tag: String, runTime: Long, presentation: Presentation = Presentation.START_ACTIVITY) : Trigger(presentation, tag, -1) {

    private var applicationRunTime: Long = 0
        get() {
            return data[DATA_KEY_RUN_TIME]?.let { it as Long } ?: let { 0L }
        }
        set(value) {
            field += value
            data[DATA_KEY_RUN_TIME] = field
        }

    internal constructor(trigger: Trigger) : this(trigger.tag, trigger.config[CONFIG_KEY_RUN_TIME] as Long, trigger.presentation) {
        clone(trigger)
    }

    init {
        config[CONFIG_KEY_RUN_TIME] = runTime
    }

    override fun updateApplicationRunTime(time: Long) {
        applicationRunTime += time
        super.updateApplicationRunTime(time)
    }

    companion object {
        private const val CONFIG_KEY_RUN_TIME = "run_time"
        private const val DATA_KEY_RUN_TIME = "run_time"
    }
}