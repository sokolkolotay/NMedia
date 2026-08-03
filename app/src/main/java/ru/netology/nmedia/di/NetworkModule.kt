package ru.netology.nmedia.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = context
                    .getSharedPreferences("auth", Context.MODE_PRIVATE)
                    .getString("token", null)

                val request = chain.request()
                    .newBuilder()
                    .apply {
                        token?.let { header("Authorization", it) }
                    }
                    .build()

                chain.proceed(request)
            }

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }

        return builder.build()
    }


    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("${BuildConfig.BASE_URL}api/slow/")
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()


    @Provides
    @Singleton
    fun providePostsApi(
        retrofit: Retrofit
    ): PostApiService =
        retrofit.create(PostApiService::class.java)
}
