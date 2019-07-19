package kr.co.yogiyo.rookiephotoapp.data.source.remote

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

import kr.co.yogiyo.rookiephotoapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {

    private const val BASE_URL = "http://45.32.46.136:8001"

    private var retrofit: Retrofit

    init {
        val httpClientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            httpClientBuilder.addNetworkInterceptor(httpLoggingInterceptor)
        }

        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }
}