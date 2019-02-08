package com.servicetick.android.library.entities

import android.os.Parcel
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.servicetick.android.library.entities.db.BaseSurveyQuestion

@Entity(tableName = "survey_questions_options",
        foreignKeys = [ForeignKey(entity = BaseSurveyQuestion::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = CASCADE)],
        indices = [Index(value = ["questionId"])])
internal class SurveyQuestionOption() : KParcelable {

    @Expose
    @PrimaryKey
    @SerializedName("OptionId")
    var id: Long? = null

    var questionId: Long? = null

    @Expose
    @SerializedName("Option")
    var option: String? = null

    @Expose
    @SerializedName("Descriptor")
    var descriptor: String? = null

    @Expose
    @SerializedName("Order")
    var order: Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Long::class.java.classLoader) as? Long
        questionId = parcel.readValue(Long::class.java.classLoader) as? Long
        option = parcel.readString()
        descriptor = parcel.readString()
        order = parcel.readInt()
    }

    override fun toString(): String {
        return "SurveyQuestionOption(id=$id, questionId=$questionId, option=$option, descriptor='$descriptor', order=$order)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeValue(id)
        writeValue(questionId)
        writeString(option)
        writeString(descriptor)
        writeInt(order)
    }

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::SurveyQuestionOption)
    }
}