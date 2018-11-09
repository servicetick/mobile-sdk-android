package com.servicetick.android.library.dagger

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.servicetick.android.library.BuildConfig
import com.servicetick.android.library.ServiceTick
import com.servicetick.android.library.api.ApiService
import com.servicetick.android.library.db.ServiceTickDao
import com.servicetick.android.library.db.ServiceTickDatabase
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class LibraryModule {

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()

        // Add HttpLogging for requests
        if (BuildConfig.DEBUG) {
            val httpIntercept = HttpLoggingInterceptor()
            httpIntercept.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder.addInterceptor(httpIntercept)
        }

        return httpClientBuilder.build()
    }

    @Provides
    @Singleton
    internal fun provideGsonBuilder(): GsonBuilder {
        return GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }

    @Provides
    @Singleton
    internal fun provideApiService(okHttpClient: OkHttpClient, gsonBuilder: GsonBuilder): ApiService {
        return Retrofit.Builder()
                .baseUrl(ServiceTick.get().getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
//                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create<ApiService>(ApiService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideServiceTickDatabase(context: Context): ServiceTickDatabase {
        return Room.databaseBuilder(context, ServiceTickDatabase::class.java, "service_tick.db").build()
    }

    @Singleton
    @Provides
    internal fun provideServiceTickDao(db: ServiceTickDatabase): ServiceTickDao {
        return db.serviceTickDao()
    }

    @Singleton
    @Provides
    internal fun provideServiceTick(): ServiceTick {
        return ServiceTick.get()
    }
}

