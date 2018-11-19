package com.servicetick.android.library.triggers

open class Trigger internal constructor(val presentation :Presentation = Presentation.START_ACTIVITY) {

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