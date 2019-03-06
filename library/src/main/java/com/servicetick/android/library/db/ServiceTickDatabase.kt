package com.servicetick.android.library.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.servicetick.android.library.db.converters.Converters
import com.servicetick.android.library.entities.SurveyPageTransition
import com.servicetick.android.library.entities.SurveyQuestionOption
import com.servicetick.android.library.entities.SurveyQuestionOptionAction
import com.servicetick.android.library.entities.SurveyResponseAnswer
import com.servicetick.android.library.entities.db.BaseSurvey
import com.servicetick.android.library.entities.db.BaseSurveyQuestion
import com.servicetick.android.library.entities.db.BaseSurveyResponse
import com.servicetick.android.library.entities.db.Statistic
import com.servicetick.android.library.entities.triggers.Trigger


@Database(entities = [
    BaseSurvey::class,
    SurveyPageTransition::class,
    SurveyQuestionOptionAction::class,
    BaseSurveyQuestion::class,
    SurveyQuestionOption::class,
    BaseSurveyResponse::class,
    SurveyResponseAnswer::class,
    Statistic::class,
    Trigger::class
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
internal abstract class ServiceTickDatabase : RoomDatabase() {
    abstract fun serviceTickDao(): ServiceTickDao
}