package dev.romio.remock.di

import android.content.Context
import dev.romio.remock.Service
import dev.romio.remock.interceptor.ReMockInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Graph {

    private lateinit var okHttpClient: OkHttpClient

    fun initialize(context: Context) {
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(ReMockInterceptor(context))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://v2.jokeapi.dev")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service by lazy {
        retrofit.create(Service::class.java)
    }
}