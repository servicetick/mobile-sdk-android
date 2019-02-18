package com.servicetick.android.library.entities

import android.os.Parcel
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.servicetick.android.library.entities.db.BaseSurveyResponse

@Entity(tableName = "survey_response_answers",
        foreignKeys = [ForeignKey(entity = BaseSurveyResponse::class, parentColumns = ["id"], childColumns = ["surveyResponseId"], onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["surveyResponseId"]), Index(value = ["surveyQuestionId"])])
internal class SurveyResponseAnswer() : KParcelable {

    @PrimaryKey(autoGenerate = true)
    @PublishedApi
    internal var id: Long = 0

    @PublishedApi
    internal var surveyResponseId: Long = 0

    @Expose
    @SerializedName("QuestionId")
    @PublishedApi
    internal var surveyQuestionId: Long = 0

    @Expose
    @SerializedName("QuestionTypeId")
    @PublishedApi
    internal var surveyQuestionTypeId: Int = 0

    @Expose
    @SerializedName("Answer")
    @PublishedApi
    internal var answer: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        surveyResponseId = parcel.readLong()
        surveyQuestionId = parcel.readLong()
        surveyQuestionTypeId = parcel.readInt()
        answer = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(surveyResponseId)
        parcel.writeLong(surveyQuestionId)
        parcel.writeInt(surveyQuestionTypeId)
        parcel.writeString(answer)
    }

    override fun toString(): String {
        return "SurveyResponseAnswer(id=$id, surveyResponseId=$surveyResponseId, surveyQuestionId=$surveyQuestionId, surveyQuestionTypeId=$surveyQuestionTypeId, answer='$answer')"
    }

    companion object {

        @JvmField
        val CREATOR = parcelableCreator(::SurveyResponseAnswer)
    }
}