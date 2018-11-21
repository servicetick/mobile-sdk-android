package com.servicetick.android.library.entities

import android.os.Parcel
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "survey_page_transitions", primaryKeys = ["surveyId", "sourcePageId"])
internal class SurveyPageTransition() : KParcelable {

    var surveyId: Long = 0

    @Expose
    @SerializedName("SourcePageId")
    var sourcePageId: Long = 0

    @Expose
    @SerializedName("IsCompletionPage")
    var isCompletionPage = false

    @Expose
    @SerializedName("TargetPageId")
    var targetPageId: Long? = null

    @Expose
    @SerializedName("Conditions")
    @Ignore
    var conditions: List<Any> = emptyList()

    constructor(parcel: Parcel) : this() {
        surveyId = parcel.readLong()
        sourcePageId = parcel.readLong()
        isCompletionPage = parcel.readBoolean()
        targetPageId = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(surveyId)
        parcel.writeLong(sourcePageId)
        parcel.writeBoolean(isCompletionPage)
        parcel.writeValue(targetPageId)
    }

    override fun toString(): String {
        return "SurveyPageTransitions(surveyId=$surveyId, sourcePageId=$sourcePageId, isCompletionPage=$isCompletionPage, targetPageId=$targetPageId, conditions=$conditions)"
    }

    companion object {

        @JvmField
        val CREATOR = parcelableCreator(::SurveyPageTransition)
    }
}