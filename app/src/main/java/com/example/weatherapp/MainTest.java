package com.example.weatherapp;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainTest {
    public static void main(String[] args) {
        String serviceKey = "Ss2kvjUY0+0ZLASxYBsNWKfosTc4aweCI5ikb/o9Xkg4r6Uq2N0d3mArOsNawHWoxRfVH+0IOkL89ORQPfMPnw==";
        String pageNo = "1";
        String numOfRows = "10";
        String dataType = "JSON";
        String base_date = "20210629";
        String base_time = "1700";
        String nx = "60";
        String ny = "127";

        Call<Object> getTest = RetrofitClient.getApiService().getTest(
                serviceKey,
                pageNo,
                numOfRows,
                dataType,
                base_date,
                base_time,
                nx,
                ny);
        try {
            System.out.println(getTest.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
