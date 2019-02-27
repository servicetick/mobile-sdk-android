package com.servicetick.android.library.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.servicetick.android.library.entities.*
import com.servicetick.android.library.entities.db.BaseSurvey
import com.servicetick.android.library.entities.db.BaseSurveyQuestion
import com.servicetick.android.library.entities.db.BaseSurveyResponse

@Dao
internal interface ServiceTickDao {

    @Transaction
    fun insert(survey: BaseSurvey) {

        survey.pageTransitions.forEachIndexed { index, surveyPageTransition ->
            surveyPageTransition.surveyId = survey.id
            surveyPageTransition.order = index
        }
        survey.questionOptionActions.forEach { questionOptionAction ->
            questionOptionAction.surveyId = survey.id
        }

        insertSurvey(survey)
        insertSurveyPageTransitions(survey.pageTransitions)

        survey.questions.forEach { question ->

            question.surveyId = survey.id
            question.options.forEach { option ->
                option.questionId = question.id

            }
            insertSurveyQuestion(question)
            insertSurveyQuestionOptions(question.options)

        }
        insertSurveyQuestionOptionActions(survey.questionOptionActions)
    }

    @Transaction
    fun insert(surveyResponse: SurveyResponse) {
        val baseSurveyResponse = BaseSurveyResponse(surveyResponse)
        baseSurveyResponse.id = insertSurveyResponse(baseSurveyResponse)

        surveyResponse.answers.forEach {
            it.surveyResponseId = baseSurveyResponse.id
        }
        insertSurveyResponseAnswers(surveyResponse.answers)
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
    fun insertSurveyQuestion(question: BaseSurveyQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyQuestionOptions(entities: List<SurveyQuestionOption>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyResponse(surveyResponse: BaseSurveyResponse): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyResponseAnswers(surveyResponseAnswers: List<SurveyResponseAnswer>)

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
    @Query("SELECT * FROM survey_responses WHERE survey_responses.surveyId=:surveyId")
    fun getSurveyResponseAsLiveData(surveyId: Long): LiveData<SurveyResponse?>

    @Transaction
    @Query("SELECT * FROM survey_responses WHERE survey_responses.surveyId=:surveyId")
    fun getSurveyResponse(surveyId: Long): SurveyResponse?

    @Transaction
    @Query("SELECT * FROM survey_questions WHERE survey_questions.pageId=:pageId")
    fun getQuestionsForPageAsLiveData(pageId: Long): LiveData<List<SurveyQuestion>>

    @Transaction
    @Query("SELECT * FROM surveys")
    fun getSurveys(): List<Survey>

    @Query("DELETE from survey_questions where surveyId=:surveyId AND id NOT IN(:notInQuestionIds)")
    fun purgeQuestions(surveyId: Long, notInQuestionIds: Array<Long>)
}