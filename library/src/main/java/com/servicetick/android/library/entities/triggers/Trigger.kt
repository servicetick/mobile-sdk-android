package com.servicetick.android.library.entities.triggers

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.servicetick.android.library.workers.SaveTriggerDataWorker

@Entity(tableName = "triggers")
open class Trigger internal constructor(val presentation: Presentation = Presentation.START_ACTIVITY, @PrimaryKey val tag: String, var surveyId: Long) {

    internal constructor(trigger: Trigger) : this(trigger.presentation, trigger.tag, trigger.surveyId) {
        clone(trigger)
    }

    protected fun clone(trigger: Trigger) {
        trigger.let {
            active = it.active
            config = it.config
            data = it.data
            surveyId = it.surveyId
        }
    }

    @PublishedApi
    internal var type: String = javaClass.simpleName
    @PublishedApi
    internal var active: Boolean = true
    @PublishedApi
    internal var config: HashMap<String, Any> = hashMapOf()
    @PublishedApi
    internal var data: HashMap<String, Any> = hashMapOf()

    internal fun canStore(): Boolean = javaClass.kotlin != ManualTrigger::class

    override fun toString(): String {
        return "Trigger(presentation=$presentation, tag='$tag', surveyId=$surveyId, type='$type', active=$active, config=$config, data=$data)"
    }

    internal open fun updateApplicationRunCount(count : Int = 1) = Unit
    internal open fun updateApplicationRunTime(time: Long) = Unit
    internal open fun updateData(data : HashMap<String, Any>?) = Unit

    protected fun scheduleSave() {
        SaveTriggerDataWorker.enqueue(this)
    }

    companion object {
        internal fun convertTrigger(trigger: Trigger): Trigger {
            return when (trigger.type) {
                "ApplicationRunCountTrigger" -> ApplicationRunCountTrigger(trigger)
                "ApplicationRunTimeTrigger" -> ApplicationRunTimeTrigger(trigger)
                else -> trigger
            }
        }
    }

    enum class Presentation {

        /**
         * This mode starts an activity with the Survey in
         */
        START_ACTIVITY,

        /**
         * This mode returns you a fragment either directly or via a callbacl
         */
        FRAGMENT
    }
}