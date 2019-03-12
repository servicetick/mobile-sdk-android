package com.servicetick.android.library.entities.triggers.builders

import com.servicetick.android.library.entities.triggers.ManualTrigger
import com.servicetick.android.library.entities.triggers.Trigger
import com.servicetick.android.library.entities.triggers.TriggerPresentation

open class TriggerBuilder {

    internal open fun build(): Trigger {
        return ManualTrigger(TriggerPresentation.START_ACTIVITY)
    }
}