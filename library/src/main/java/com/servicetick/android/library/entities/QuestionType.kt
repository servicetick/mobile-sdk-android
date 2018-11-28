package com.servicetick.android.library.entities

internal enum class QuestionType {

    /**
     * Represents checkbox question type
     */
    MULTIPLE_SELECT_CHECKBOX,

    /**
     * Represents texbox/textarea question type
     */
    SINGLE_TEXT_BOX,

    /**
     * Represents an information area which does not accept input
     */
    INFORMATION_BOX,

    /**
     * Represents IVR recording of the callers voice
     */
    RECORDING,

    /**
     * Represents radio buttons and single DTMF selection
     */
    SINGLE_SELECT_RADIO,

    /**
     * DEPRECATED: Represents an overall satisfaction question
     */
    SERVICE_TICK_SCORE,

    /**
     * Represents a flow control decision
     */
    JUMP_TO_PAGE,

    /**
     * Represents a question which will alert based on a condition and provide flow control
     */
    ALERT_OR_RETURN,

    /**
     * Represents a drop down box for the online channel
     */
    DROP_DOWN_BOX,

    /**
     * Represents a social media question type
     */
    SOCIAL_MEDIA,

    /**
     * Represents a Table question type
     */
    TABLE_QUESTION;


    companion object {

        fun fromInt(value: Int): QuestionType {
            return when (value) {
                1 -> MULTIPLE_SELECT_CHECKBOX
                2 -> SINGLE_TEXT_BOX
                3 -> INFORMATION_BOX
                4 -> RECORDING
                5 -> SINGLE_SELECT_RADIO
                6 -> SERVICE_TICK_SCORE
                7 -> JUMP_TO_PAGE
                8 -> ALERT_OR_RETURN
                9 -> DROP_DOWN_BOX
                10 -> SOCIAL_MEDIA
                11 -> TABLE_QUESTION
                else -> TABLE_QUESTION
            }
        }

    }
}