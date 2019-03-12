package com.servicetick.android.library.db.converters

import android.os.Parcel
import androidx.room.TypeConverter
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.triggers.TriggerPresentation
import java.util.*


class Converters {

    companion object {

        @TypeConverter
        @JvmStatic
        fun longToDate(value: Long?): Date? {
            return value?.run { Date(this) }
        }

        @TypeConverter
        @JvmStatic
        fun dateToLong(date: Date?): Long? {
            return date?.time
        }

        @TypeConverter
        @JvmStatic
        fun longToCalendar(value: Long?): Calendar? {
            return value?.run {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = value
                calendar
            }
        }

        @TypeConverter
        @JvmStatic
        fun calendarToLong(calendar: Calendar?): Long? {
            return calendar?.timeInMillis
        }

        @TypeConverter
        @JvmStatic
        fun surveyStateToString(surveyState: Survey.State?): String {
            return surveyState?.name ?: Survey.State.ENQUEUED.name
        }

        @TypeConverter
        @JvmStatic
        fun stringToSurveyState(surveyState: String): Survey.State {
            return Survey.State.valueOf(surveyState)
        }

        @TypeConverter
        @JvmStatic
        fun presentationToString(presentation: TriggerPresentation): String = presentation.toString()

        @TypeConverter
        @JvmStatic
        fun stringToPresentation(presentationName: String): TriggerPresentation = TriggerPresentation.valueOf(presentationName)

        @TypeConverter
        @JvmStatic
        fun hashMapToByteArray(map: HashMap<String, Any>): ByteArray {
            val parcel = Parcel.obtain()
            parcel.writeMap(map)
            val output = parcel.marshall()
            parcel.recycle()
            return output
        }

        @TypeConverter
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun byteArrayToHashMap(byteArray: ByteArray?): HashMap<String, Any> {

            val parcel = Parcel.obtain()
            parcel.unmarshall(byteArray, 0, byteArray?.size ?: 0)
            parcel.setDataPosition(0)
            val bundle = parcel.readHashMap(ClassLoader.getSystemClassLoader())
            parcel.recycle()
            return bundle as HashMap<String, Any>
        }
    }
}