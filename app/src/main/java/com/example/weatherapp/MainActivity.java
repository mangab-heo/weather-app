package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    WeatherData weatherData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String baseDate = "20210704";
        String baseTime = "1100";
        String nx = "60";
        String ny = "127";

        RetrofitClient.getApiService().getVilageFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDate, baseTime, nx, ny)
                .enqueue(new Callback<FcstResult>() {
                    @Override
                    public void onResponse(@NonNull Call<FcstResult> call,@NonNull Response<FcstResult> response) {
                        FcstResult fcstResult = response.body();

                        if (fcstResult != null) {
                            weatherData = fcstResult.toWeatherData();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FcstResult> call, @NonNull Throwable t) {
                    }
                });
    }
}