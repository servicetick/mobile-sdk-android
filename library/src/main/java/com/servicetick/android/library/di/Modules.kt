package com.servicetick.android.library.di

import androidx.room.Room
import com.google.gson.GsonBuilder
import com.servicetick.android.library.AppExecutors
import com.servicetick.android.library.BuildConfig
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.api.ApiService
import com.servicetick.android.library.db.ServiceTickDatabase
import com.servicetick.android.library.repository.SurveyRepository
import com.servicetick.android.library.viewmodel.SurveysViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val libraryModule = module {

    single {
        Room.databaseBuilder(androidContext(), ServiceTickDatabase::class.java, "service_tick.db").build()
    }

    single {
        val db: ServiceTickDatabase = get()
        db.serviceTickDao()
    }

    single {
        GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }

    single {
        val httpClientBuilder = OkHttpClient.Builder()

        // Add HttpLogging for requests
        if (BuildConfig.DEBUG) {
            val httpIntercept = HttpLoggingInterceptor()
            httpIntercept.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder.addInterceptor(httpIntercept)
        }

        httpClientBuilder.build()
    }

    single<ApiService> {
        Retrofit.Builder()
                .baseUrl(ServiceTick.get().getBaseUrl())
                .client(get())
                .addConverterFactory(GsonConverterFactory.create((get() as GsonBuilder).create()))
//                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create<ApiService>(ApiService::class.java)
    }

    single {
        ServiceTick.get()
    }

    single {
        SurveyRepository(get(), get())
    }

    single {
        AppExecutors()
    }

    viewModel { SurveysViewModel(get()) }

}