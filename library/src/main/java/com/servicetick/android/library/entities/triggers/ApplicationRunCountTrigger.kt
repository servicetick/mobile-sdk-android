package com.servicetick.android.library.entities.triggers

class ApplicationRunCountTrigger(tag: String, runCount: Int, presentation: Presentation = Presentation.START_ACTIVITY) : Trigger(presentation, tag, -1) {

    private var applicationRunCount: Int = 0
        get() {
            return data[DATA_KEY_RUN_COUNT]?.let { it as Int } ?: let { 0 }
        }
        set(value) {
            field = value
            data[DATA_KEY_RUN_COUNT] = field
        }

    internal constructor(trigger: Trigger) : this(trigger.tag, trigger.config[CONFIG_KEY_RUN_COUNT] as Int, trigger.presentation) {
        clone(trigger)
    }

    init {
        config[CONFIG_KEY_RUN_COUNT] = runCount
    }

    override fun updateApplicationRunCount(count: Int, checkFire: Boolean) {
        applicationRunCount += count
        scheduleSave()
        fireTriggerIfRequired(checkFire)
    }

    private fun getConfigRunCount(): Int = config[CONFIG_KEY_RUN_COUNT] as Int
    override fun shouldFire(): Boolean = applicationRunCount >= getConfigRunCount()

    override fun updateData(data: HashMap<String, Any>?) {
        data?.let {
            updateApplicationRunCount(data[DATA_KEY_RUN_COUNT] as Int, false)
        }
    }

    companion object {
        private const val CONFIG_KEY_RUN_COUNT = "run_count"
        private const val DATA_KEY_RUN_COUNT = "run_count"
    }
}