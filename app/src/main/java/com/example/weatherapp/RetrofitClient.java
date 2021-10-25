package com.example.weatherapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String FCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/";
    private static final String ARPL_BASE_URL = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/";

    public static FcstService getFcstService() { return getInstance(FCST_BASE_URL).create(FcstService.class); }
    public static ArpltnService getArpltnService() { return getInstance(ARPL_BASE_URL).create(ArpltnService.class); }

    private static Retrofit getInstance(String baseUrl) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }
}
