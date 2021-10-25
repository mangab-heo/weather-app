package com.example.weatherapp;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;

import com.trello.rxlifecycle4.components.support.RxAppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.example.weatherapp.RetrofitClient.getArpltnService;
import static com.example.weatherapp.WeatherData.combineWeatherData;

public class MainActivity extends RxAppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBar();

        setImageButtonListener();

        drawMainView();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onRestart() {
        super.onRestart();
        drawMainView();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setImageButtonListener() {
        ImageButton imageButton = findViewById(R.id.gps_button);
        imageButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "날씨 정보를 불러옵니다.", Toast.LENGTH_LONG).show();
            drawMainView();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMainView() {
        if (LocationUtil.isLocationPermissionGranted(MainActivity.this)) {
            try {
                WeatherGrid.LatXLngY gridLocation = LocationUtil.getGridLocation(getApplicationContext());
                String stationName = LocationUtil.getClosestStation(getApplicationContext(), gridLocation);

                callApiAndUpdateView(gridLocation, stationName);
            } catch (Exception exception) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void callApiAndUpdateView(WeatherGrid.LatXLngY gridLocation, String stationName) {
        List<Observable<?>> requests = getObservables(gridLocation, stationName);

        Observable.zip(requests, parsedData -> {
            List<ViewData> viewDataList = new ArrayList<>();
            List<WeatherData> weatherDataList = new ArrayList<>();

            for (Object data : parsedData) {
                if (data == null) continue;
                if (data instanceof WeatherData)
                    weatherDataList.add((WeatherData) data);
                else if (data instanceof ViewData) viewDataList.add((ViewData) data);
            }
            viewDataList.add(combineWeatherData(weatherDataList));

            return viewDataList;
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(viewDataList -> {
                    for (ViewData viewData : viewDataList)
                        viewData.updateView(MainActivity.this);
                }, throwable -> Toast.makeText(MainActivity.this, "날씨 정보를 불러오지 못 했습니다.", Toast.LENGTH_LONG).show());
    }


    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @NotNull
    private List<Observable<?>> getObservables(WeatherGrid.LatXLngY gridLocation, String stationName) {
        int x = (int) gridLocation.x;
        int y = (int) gridLocation.y;

        String[] baseDateTimeVil = WeatherData.getBaseDateTime(0);
        String[] baseDateTimeSrt = WeatherData.getBaseDateTime(1);

        List<Observable<?>> requests = new ArrayList<>();

        if (!stationName.equals("")) {
            Observable<PmData> getDnsty = getArpltnService().getDnsty(
                    ArpltnClientConstants.SERVICE_KEY, ArpltnClientConstants.RETURN_TYPE,
                    ArpltnClientConstants.NUM_OF_ROWS, ArpltnClientConstants.PAGE_NO,
                    ArpltnClientConstants.DATA_TERM, ArpltnClientConstants.VER,
                    stationName)
                    .map(ArpltnResult::toAppData);
            requests.add(getDnsty);
        } else Toast.makeText(MainActivity.this, "미세먼지 관측소 정보를 찾지 못했습니다.", Toast.LENGTH_LONG).show();

        Observable<WeatherData> getVillageFcst = RetrofitClient.getFcstService().getVilageFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDateTimeVil[0], baseDateTimeVil[1], x, y)
                .map(FcstResult::toAppData);
        requests.add(getVillageFcst);

        Observable<WeatherData> getUltraSrtFcst = RetrofitClient.getFcstService().getUltraSrtFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDateTimeSrt[0], baseDateTimeSrt[1], x, y)
                .map(FcstResult::toAppData);
        requests.add(getUltraSrtFcst);

        return requests;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtil.REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "권한을 획득했습니다.", Toast.LENGTH_LONG).show();
            }
            drawMainView();
        }
    }
}