package com.servicetick.android.library.entities.triggers

class ApplicationRunCountTrigger(tag : String, runCount : Int, presentation : Presentation = Presentation.START_ACTIVITY) : Trigger(presentation, tag, -1) {

    init {
        config[CONFIG_KEY_RUN_COUNT] = runCount
    }

    companion object {
        private const val CONFIG_KEY_RUN_COUNT = "run_count"
    }
}