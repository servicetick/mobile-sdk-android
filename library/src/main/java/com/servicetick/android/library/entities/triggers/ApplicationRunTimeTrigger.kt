package com.servicetick.android.library.entities.triggers

class ApplicationRunTimeTrigger(tag: String, runTime: Long, presentation: Presentation = Presentation.START_ACTIVITY) : Trigger(presentation, tag, -1) {

    init {
        config[CONFIG_KEY_RUN_TIME] = runTime
    }

    companion object {
        private const val CONFIG_KEY_RUN_TIME = "run_time"
    }
}