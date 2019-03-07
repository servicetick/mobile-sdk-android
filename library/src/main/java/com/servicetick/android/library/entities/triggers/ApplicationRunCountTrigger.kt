package com.servicetick.android.library.entities.triggers

import lilhermit.android.remotelogger.library.Log

class ApplicationRunCountTrigger(tag: String, runCount: Int, presentation: Presentation = Presentation.START_ACTIVITY) : Trigger(presentation, tag, -1) {

    internal constructor(trigger: Trigger) : this(trigger.tag, trigger.config[CONFIG_KEY_RUN_COUNT] as Int, trigger.presentation) {
        clone(trigger)
    }

    init {
        config[CONFIG_KEY_RUN_COUNT] = runCount
    }

    companion object {
        private const val CONFIG_KEY_RUN_COUNT = "run_count"
    }
}