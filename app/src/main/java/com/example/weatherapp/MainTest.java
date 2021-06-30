package com.example.weatherapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainTest {
    public static void main(String[] args) {
        String serviceKey = "Ss2kvjUY0+0ZLASxYBsNWKfosTc4aweCI5ikb/o9Xkg4r6Uq2N0d3mArOsNawHWoxRfVH+0IOkL89ORQPfMPnw==";
        String pageNo = "1";
        String numOfRows = "30";
        String dataType = "JSON";
        String base_date = "20210630";
        String base_time = "1100";
        String nx = "60";
        String ny = "127";

        Call<FcstResult> getTest = RetrofitClient.getApiService().getTest(
                serviceKey,
                pageNo,
                numOfRows,
                dataType,
                base_date,
                base_time,
                nx,
                ny);

        getTest.enqueue(new Callback<FcstResult>() {
            @Override
            public void onResponse(@NonNull Call<FcstResult> call, @NonNull Response<FcstResult> response) {
                System.out.println(new Gson().toJson(response.body()));
                FcstResult fcstResult = response.body();
//                MyData myData = new MyData("hi", 3.14);
//                System.out.println(new Gson().toJson(myData));
            }

            @Override
            public void onFailure(@NonNull Call<FcstResult> call, @NonNull Throwable t) {
                System.out.println("Bye");
            }
        });
        System.out.println();
    }
}
