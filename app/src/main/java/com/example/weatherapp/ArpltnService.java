package com.example.weatherapp;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface ArpltnService {
    @GET("getMsrstnAcctoRltmMesureDnsty")
    Observable<ArpltnResult> getDnsty(@Query("serviceKey") String serviceKey,
                                      @Query("returnType") String returnType,
                                      @Query("numOfRows") String numOfRows,
                                      @Query("pageNo") String pageNo,
                                      @Query("dataTerm") String dataTerm,
                                      @Query("ver") String ver,
                                      @Query("stationName") String stationName);
}
