package com.servicetick.android.library.db.converters

import androidx.room.TypeConverter
import com.servicetick.android.library.entities.Survey
import java.util.*


class Converters {
    @TypeConverter
    fun longToDate(value: Long?): Date? {
        return value?.run { Date(this) }
    }

    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun longToCalendar(value: Long?): Calendar? {
        return value?.run {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = value
            calendar
        }
    }

    @TypeConverter
    fun calendarToLong(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun surveyStateToString(surveyState: Survey.State?): String {
        return surveyState?.name ?: Survey.State.ENQUEUED.name
    }

    @TypeConverter
    fun stringToSurveyState(surveyState: String): Survey.State {
        return Survey.State.valueOf(surveyState)
    }
}