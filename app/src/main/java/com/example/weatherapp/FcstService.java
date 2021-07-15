package com.example.weatherapp;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FcstService {
    @GET("getVilageFcst")
    Observable<FcstResult> getVilageFcst(@Query("serviceKey") String serviceKey,
                                         @Query("pageNo") String pageNo,
                                         @Query("numOfRows") String numOfRows,
                                         @Query("dataType") String dataType,
                                         @Query("base_date") String base_date,
                                         @Query("base_time") String base_time,
                                         @Query("nx") String nx,
                                         @Query("ny") String ny);
    @GET("getUltraSrtFcst")
    Observable<FcstResult> getUltraSrtFcst(@Query("serviceKey") String serviceKey,
                                        @Query("pageNo") String pageNo,
                                        @Query("numOfRows") String numOfRows,
                                        @Query("dataType") String dataType,
                                        @Query("base_date") String base_date,
                                        @Query("base_time") String base_time,
                                        @Query("nx") String nx,
                                        @Query("ny") String ny);
}

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