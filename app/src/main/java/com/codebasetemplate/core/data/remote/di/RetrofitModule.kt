package com.codebasetemplate.core.data.remote.di

import com.codebasetemplate.BuildConfig
import com.codebasetemplate.core.data.remote.api.ApiConstants
import com.codebasetemplate.core.data.remote.api.ApiService
import com.codebasetemplate.core.data.remote.api.AuthenticationInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Provides
    @Singleton
    fun providerApiService(): ApiService {
        val clientBuilder = OkHttpClient.Builder()
        if(clientBuilder.interceptors().find { it is AuthenticationInterceptor } == null) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)
            }
            clientBuilder.addInterceptor(httpLoggingInterceptor)
                .addInterceptor(AuthenticationInterceptor(ApiConstants.TOKEN))
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
        }


        val retrofit = Retrofit.Builder().baseUrl(ApiConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}