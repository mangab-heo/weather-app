package com.example.weatherapp.data.source.remote

import com.example.weatherapp.data.source.remote.alpltn.ArpltnService
import com.example.weatherapp.data.source.remote.fcst.FcstService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

internal object RetrofitClient {
    private const val FCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"
    private const val ARPL_BASE_URL = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/"
    val fcstService: FcstService
        get() = getInstance(FCST_BASE_URL).create(
            FcstService::class.java
        )
    val arpltnService: ArpltnService
        get() = getInstance(ARPL_BASE_URL).create(
            ArpltnService::class.java
        )

    private fun getInstance(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }
}