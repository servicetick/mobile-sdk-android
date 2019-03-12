package com.servicetick.android.library.entities.triggers.builders

import com.servicetick.android.library.entities.triggers.ApplicationRunCountTrigger
import com.servicetick.android.library.entities.triggers.TriggerPresentation

class ApplicationRunCountTriggerBuilder {

    companion object : TriggerBuilder() {
        private var runCount: Int? = null
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

        fun setRunCount(runCount: Int): Companion {
            this.runCount = runCount
            return this
        }

        override fun build(): ApplicationRunCountTrigger {

            tag?.let { triggerTag ->

                runCount?.let { triggerRunCount ->
                    return ApplicationRunCountTrigger(triggerTag, triggerRunCount, presentation)

                } ?: run {
                    throw RuntimeException("You must call setRunCount in the TriggerBuilder")
                }
            } ?: run {
                throw RuntimeException("You must call setTag in the TriggerBuilder")
            }
        }
    }
}