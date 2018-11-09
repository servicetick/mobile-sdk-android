package com.servicetick.android.library.entities.api

import com.google.gson.annotations.Expose
import com.servicetick.android.library.entities.db.BaseSurvey

internal class PostSurveyResponse(@Expose val surveyDownload: BaseSurvey)