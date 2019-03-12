package com.servicetick.android.library.entities.triggers.builders

import com.servicetick.android.library.entities.triggers.ApplicationRunTimeTrigger
import com.servicetick.android.library.entities.triggers.TriggerPresentation

class ApplicationRunTimeTriggerBuilder {

    companion object : TriggerBuilder() {
        private var runTime: Long? = null
        private var tag: String? = null
        private var presentation: TriggerPresentation = TriggerPresentation.START_ACTIVITY

        fun setTag(tag: String): Companion {
            this.tag = tag
            return this
        }

        fun setPresentation(presentation: TriggerPresentation): Companion {
            this.presentation = presentation
            return this
        }

        fun setRunTime(runTime: Long): Companion {
            this.runTime = runTime
            return this
        }

        override fun build(): ApplicationRunTimeTrigger {

            tag?.let { triggerTag ->

                runTime?.let { triggerRunTime ->
                    return ApplicationRunTimeTrigger(triggerTag, triggerRunTime, presentation)
                } ?: run {
                    throw RuntimeException("You must call setRunTime in the TriggerBuilder")
                }

            } ?: run {
                throw RuntimeException("You must call setTag in the TriggerBuilder")
            }

        }
    }
}