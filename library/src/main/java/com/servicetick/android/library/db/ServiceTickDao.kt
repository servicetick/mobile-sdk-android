package com.servicetick.android.library.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.servicetick.android.library.entities.Survey
import com.servicetick.android.library.entities.SurveyPageTransition
import com.servicetick.android.library.entities.SurveyQuestionOption
import com.servicetick.android.library.entities.SurveyQuestionOptionAction
import com.servicetick.android.library.entities.db.BaseSurvey
import com.servicetick.android.library.entities.db.BaseSurveyQuestion

@Dao
internal interface ServiceTickDao {

    @Transaction
    fun insert(survey: BaseSurvey) {

        survey.pageTransitions.forEach { surveyPageTransition ->
            surveyPageTransition.surveyId = survey.id
        }
        survey.questionOptionActions.forEach { questionOptionAction ->
            questionOptionAction.surveyId = survey.id
        }

        survey.questions.forEach { question ->

            question.surveyId = survey.id
            question.options.forEach { option ->
                option.questionId = question.id

            }
            insertSurveyQuestionOptions(question.options)
        }
        insertSurvey(survey)

        insertSurveyPageTransitions(survey.pageTransitions)
        insertSurveyQuestionOptionActions(survey.questionOptionActions)
        insertSurveyQuestions(survey.questions)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurvey(survey: BaseSurvey)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyPageTransitions(entities: List<SurveyPageTransition>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyQuestionOptionActions(entities: List<SurveyQuestionOptionAction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyQuestions(entities: List<BaseSurveyQuestion>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyQuestionOptions(entities: List<SurveyQuestionOption>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: BaseSurvey?)

    @Delete()
    fun delete(entity: BaseSurvey?)

    @Transaction
    @Query("SELECT * FROM surveys WHERE surveys.id=:id")
    fun getSurvey(id: Long): Survey?

    @Transaction
    @Query("SELECT * FROM surveys WHERE surveys.id=:id")
    fun getSurveyAsLiveData(id: Long): LiveData<Survey?>

    @Transaction
    @Query("SELECT * FROM surveys")
    fun getSurveys(): List<Survey>
}