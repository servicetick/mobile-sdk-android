package com.servicetick.android.library.entities.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyPageTransition
import com.servicetick.android.library.entities.SurveyQuestionOptionAction
import java.util.*

@Entity(tableName = "surveys")
internal class BaseSurvey internal constructor(@Expose @PrimaryKey @SerializedName("SurveyId") var id: Long) {

    @Expose
    @SerializedName("SurveyTitle")
    var title: String? = null
    @Expose
    @SerializedName("SurveyType")
    var type: String? = null
    @Expose
    @SerializedName("Enabled")
    @Ignore
    var enabled: Boolean = true

    @Expose
    @SerializedName("SurveyPageTransitions")
    @Ignore
    var pageTransitions: List<SurveyPageTransition> = emptyList()

    @Expose
    @SerializedName("SurveyQuestionOptionAction")
    @Ignore
    var questionOptionActions: List<SurveyQuestionOptionAction> = emptyList()

    @Expose
    @SerializedName("SurveyQuestions")
    @Ignore
    var questions: List<BaseSurveyQuestion> = emptyList()

    var lastUpdated: Calendar? = null
    var refreshInterval: Long = Survey.DEFAULT_REFRESH_INTERVAL
    var state = Survey.State.ENQUEUED

    override fun toString(): String {
        return "BaseSurvey(id=$id, title=$title, type=$type, enabled=$enabled, lastUpdated=${lastUpdated?.time.toString()}, refreshInterval=$refreshInterval)\n   pageTransitions=$pageTransitions\n   questionOptionActions=$questionOptionActions\n   questions=$questions\n"
    }
}