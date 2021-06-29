package com.example.weatherapp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TestService {
    @GET("getVilageFcst")
    Call<Object> getTest(@Query("ServiceKey") String serviceKey,
                         @Query("pageNo") String pageNo,
                         @Query("numOfRows") String numOfRows,
                         @Query("dataType") String dataTye,
                         @Query("base_date") String base_date,
                         @Query("base_time") String base_time,
                         @Query("nx") String nx,
                         @Query("ny") String ny);
}
