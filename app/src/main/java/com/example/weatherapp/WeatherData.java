package com.example.weatherapp;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.weatherapp.FcstClientConstants.DATA_TYPE;
import static com.example.weatherapp.FcstClientConstants.NUM_OF_ROWS;
import static com.example.weatherapp.FcstClientConstants.PAGE_NO;
import static com.example.weatherapp.FcstClientConstants.SERVICE_KEY;

public class WeatherData {
    List<WeatherHour> weatherHours = new ArrayList<>();
    double[] tmx = new double[] { -1, -1, -1, -1 };
    double[] tmn = new double[] { -1, -1, -1, -1 };

    void addWeatherHour(WeatherHour weatherHour) {
        weatherHours.add(weatherHour);
    }

    public static void main(String[] args) {
        String baseDate = "20210703";
        String baseTime = "0800";
        String nx = "60";
        String ny = "127";

        Call<FcstResult> getVilageFcst = RetrofitClient.getApiService().getVilageFcst(SERVICE_KEY, PAGE_NO, NUM_OF_ROWS, DATA_TYPE,
                baseDate, baseTime, nx, ny);

        getVilageFcst.enqueue(new Callback<FcstResult>() {
            @Override
            public void onResponse(@NonNull Call<FcstResult> call, @NonNull Response<FcstResult> response) {
                FcstResult fcstResult = response.body();
                if (fcstResult != null) {
                    WeatherData weatherData = fcstResult.toWeatherData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FcstResult> call, @NonNull Throwable t) {
            }
        });
    }
}

class WeatherHour {
    String fcstDate = "Missing";
    String fcstTime = "Missing";
    int pop = -1;
    String pty = "Missing";
    String pcp = "Missing";
    int reh = -1;
    String sno = "Missing";
    String sky = "Missing";
    double tmp = -1;
    double wsd = -1;

    public void setFcstValue(String category, String fcstValue) {
        if (category.equals(Category.POP.getValue())) {
            this.pop = Integer.parseInt(fcstValue);
        }
        else if (category.equals(Category.PTY.getValue())) {
            this.pty = fcstValue;
        }
        else if (category.equals(Category.PCP.getValue())) {
            this.pcp = fcstValue;
        }
        else if (category.equals(Category.REH.getValue())) {
            this.reh = Integer.parseInt(fcstValue);
        }
        else if (category.equals(Category.SNO.getValue())) {
            this.sno = fcstValue ;
        }
        else if (category.equals(Category.SKY.getValue())) {
            this.sky = fcstValue;
        }
        else if (category.equals(Category.TMP.getValue())) {
            this.tmp = Double.parseDouble(fcstValue);
        }
        else if (category.equals(Category.WSD.getValue())) {
            this.wsd = Double.parseDouble(fcstValue);
        }
    }
}

enum Category {
    POP("POP"), PTY("PTY"), PCP("PCP"), REH("REH"), SNO("SNO"),
    SKY("SKY"), TMP("TMP"), TMN("TMN"), TMX("TMX"), WSD("WSD");

    private final String value;
    Category(String str) {
        value = str;
    }

    public String getValue() {
        return value;
    }
}

enum Day {
    TODAY(0), TOMM(1), DAT(2), DADAT(3);

    private final int value;
    Day(int value) { this.value = value; }

    public int getValue() { return value; }
}