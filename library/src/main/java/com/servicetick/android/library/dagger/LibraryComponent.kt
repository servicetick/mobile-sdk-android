package com.servicetick.android.library.dagger

import android.content.Context
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.workers.SurveyInitWorker
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [LibraryModule::class])

internal interface LibraryComponent : AndroidInjector<Context> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<Context>()

    fun inject(serviceTick: ServiceTick)
    fun inject(surveyInitWorker: SurveyInitWorker)
}