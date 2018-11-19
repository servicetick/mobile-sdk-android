package com.servicetick.android.library.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.servicetick.android.library.viewmodel.SurveysViewModel
import com.servicetick.android.library.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton


@Module
internal abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SurveysViewModel::class)
    @Singleton
    internal abstract fun bindSurveysViewModel(surveysViewModel: SurveysViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}