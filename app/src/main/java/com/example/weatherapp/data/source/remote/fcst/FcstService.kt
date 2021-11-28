package com.example.weatherapp.data.source.remote.fcst

import retrofit2.http.GET
import com.example.weatherapp.data.source.remote.fcst.FcstResult
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Query

interface FcstService {
    @GET("getVilageFcst")
    fun getVilageFcst(
        @Query("serviceKey") serviceKey: String?,
        @Query("pageNo") pageNo: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("dataType") dataType: String?,
        @Query("base_date") base_date: String?,
        @Query("base_time") base_time: String?,
        @Query("nx") x: Int,
        @Query("ny") y: Int
    ): Observable<FcstResult>

    @GET("getUltraSrtFcst")
    fun getUltraSrtFcst(
        @Query("serviceKey") serviceKey: String?,
        @Query("pageNo") pageNo: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("dataType") dataType: String?,
        @Query("base_date") base_date: String?,
        @Query("base_time") base_time: String?,
        @Query("nx") x: Int,
        @Query("ny") y: Int
    ): Observable<FcstResult>
}