package com.servicetick.android.library.entities.api

import com.google.gson.annotations.Expose

internal class PostSurveyRequest(@Expose val SurveyId: Long, @Expose val ClientAccountId: Long, @Expose val AccessKey: String)