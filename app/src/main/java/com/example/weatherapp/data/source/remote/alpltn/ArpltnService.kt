package com.example.weatherapp.data.source.remote.alpltn

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ArpltnService {
    @GET("getMsrstnAcctoRltmMesureDnsty")
    fun getDnsty(
        @Query("serviceKey") serviceKey: String?,
        @Query("returnType") returnType: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("pageNo") pageNo: String?,
        @Query("dataTerm") dataTerm: String?,
        @Query("ver") ver: String?,
        @Query("stationName") stationName: String?
    ): Observable<ArpltnResult>
}