package com.servicetick.android.library.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "survey_questions_options")
internal class SurveyQuestionOption {

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

    override fun toString(): String {
        return "SurveyQuestionOption(id=$id, questionId=$questionId, option=$option, descriptor='$descriptor', order=$order)"
    }


}