package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.example.weatherapp.WeatherGrid.TO_GRID;
import static com.example.weatherapp.WeatherGrid.convertGRID_GPS;

public class MainActivity extends RxAppCompatActivity {
    RecyclerView recyclerView;
    TextView weatherTextView;
    TextView tmpTextView;

    static final int REQUEST_CODE = 101;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ImageButton imageButton = findViewById(R.id.gps_button);
        imageButton.setOnClickListener(v -> {

            Toast.makeText(MainActivity.this, "업데이트", Toast.LENGTH_LONG).show();

            if (isLocationPermissionGranted()) {
                WeatherGrid.LatXLngY gridLocation = getGridLocation();

                if (gridLocation != null) {
                    String x = Integer.valueOf((int)gridLocation.x).toString();
                    String y = Integer.valueOf((int)gridLocation.y).toString();

                    getFcstAndUpdateView(x, y);
                }
            }
        });

        if (isLocationPermissionGranted()) {
            WeatherGrid.LatXLngY gridLocation = getGridLocation();

            if (gridLocation != null) {
                String x = Integer.valueOf((int) gridLocation.x).toString();
                String y = Integer.valueOf((int) gridLocation.y).toString();

                getFcstAndUpdateView(x, y);
            }
        }
    }

    private String[] findBaseDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        String curDateTimeStr = sdf.format(new Date(System.currentTimeMillis()));
        String[] curDateTime = curDateTimeStr.split(" ");

        String curDate = curDateTime[0];
        String curTime = curDateTime[1];

        String baseDateFcst = curDate;
        String baseTimeFcst;

        String baseDateSrt = curDate;
        String baseTimeSrt;

        // getVilageFcst의 baseDate, baseTime 구하기
        int quotient = Integer.parseInt(curTime) / 300;
        int remain = Integer.parseInt(curTime) % 300;

        if (quotient == 0 && remain <= 210) {
            // 00시 00분 ~ 02시 10분
            // 전날 23시로 호출
            String[] yesterDateTime = sdf.format(new Date(System.currentTimeMillis() - 1000*60*60*24*-1))
                    .split(" ");
            baseDateFcst = yesterDateTime[0];
            baseTimeFcst = "2300";
        }
        else {
            // 02시 11분 ~ 23시 59분
            if (remain <= 210) {
                // - remain - 100
                baseTimeFcst = String.valueOf(Integer.parseInt(curTime) - remain - 100);
            }
            else {
                // - remain + 200
                baseTimeFcst = String.valueOf(Integer.parseInt(curTime) - remain + 200);
            }
        }

        if (baseTimeFcst.length() < 4) {
            baseTimeFcst = "0" + baseTimeFcst;
        }

        // getUltraSrtFcst의 baseDate, baseTime 구하기
        int quotientSrt = Integer.parseInt(curTime) / 100;
        int remainSrt = Integer.parseInt(curTime) % 100;

        if (quotientSrt == 0 && remainSrt <= 45) {
            // 전날 23:30으로 호출
            String[] yesterDateTime = sdf.format(new Date(System.currentTimeMillis() - 1000*60*60*24*-1))
                    .split(" ");
            baseDateSrt = yesterDateTime[0];
            baseTimeSrt = "2330";
        }
        else {
            if (remainSrt <= 45) {
                // 이전 시각 30분
                baseTimeSrt = String.valueOf(Integer.parseInt(curTime) - remainSrt - 70);
            }
            else {
                // 같은 시각 30분
                baseTimeSrt = String.valueOf(Integer.parseInt(curTime) - remainSrt + 30);
            }
        }

        if (baseTimeSrt.length() < 4) {
            if (baseTimeSrt.length() == 2)
                baseTimeSrt = "00" + baseTimeSrt;
            else baseTimeSrt = "0" + baseTimeSrt;
        }

        return new String[] {baseDateFcst, baseTimeFcst, baseDateSrt, baseTimeSrt};
    }

    private WeatherGrid.LatXLngY getGridLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        WeatherGrid.LatXLngY latXLngY = null;

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "권한이 필요합니다.", Toast.LENGTH_LONG).show();
        }
        else {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null)
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastKnownLocation != null) {
                double lng = lastKnownLocation.getLongitude();
                double lat = lastKnownLocation.getLatitude();

                latXLngY = convertGRID_GPS(TO_GRID, lat, lng);
            }
        }

        return latXLngY;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getFcstAndUpdateView(String nx, String ny) {
        String[] baseDateTimes = findBaseDateTime();

        Observable<FcstResult> getVilageFcst = RetrofitClient.getFcstService().getVilageFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDateTimes[0], baseDateTimes[1], nx, ny);

        Observable<FcstResult> getUltraSrtFcst = RetrofitClient.getFcstService().getUltraSrtFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDateTimes[2], baseDateTimes[3], nx, ny);

        List<Observable<?>> requests = new ArrayList<>();
        requests.add(getVilageFcst);
        requests.add(getUltraSrtFcst);

        Observable.zip(requests, objects -> {

            FcstResult vilageFcst = (FcstResult) objects[0];
            FcstResult srtFcst = (FcstResult) objects[1];

            WeatherData weatherDataVilage = vilageFcst.toWeatherData();
            WeatherData weatherDataSrt = srtFcst.toWeatherData();
            if (weatherDataVilage == null)
                return null;

            List<WeatherHour> vilageHours = weatherDataVilage.getWeatherHours();
            int vilageIdx = weatherDataVilage.findStartIdx();

            weatherDataVilage.curPty = vilageHours.get(vilageIdx).pty;
            weatherDataVilage.curTmp = vilageHours.get(vilageIdx).tmp;
            weatherDataVilage.curSky = vilageHours.get(vilageIdx).sky;

            if (weatherDataSrt != null) {
                List<WeatherHour> srtHours = weatherDataSrt.getWeatherHours();
                int srtIdx = weatherDataSrt.findStartIdx();

                weatherDataVilage.curPty = srtHours.get(0).pty;
                weatherDataVilage.curTmp = srtHours.get(0).tmp;
                weatherDataVilage.curSky = srtHours.get(0).sky;

                for (; srtIdx < srtHours.size(); srtIdx++, vilageIdx++) {
                    WeatherHour srtHour = srtHours.get(srtIdx);
                    WeatherHour vilageHour = vilageHours.get(vilageIdx);
                    vilageHour.tmp = srtHour.tmp;
                    vilageHour.pcp = srtHour.pcp;
                    vilageHour.sky = srtHour.sky;
                    vilageHour.reh = srtHour.reh;
                    vilageHour.pty = srtHour.pty;
                    vilageHour.wsd = srtHour.wsd;
                }
            }

            return weatherDataVilage;
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(weatherData -> {
                    if (weatherData != null) {
                        weatherTextView = findViewById(R.id.weather_text);
                        String weatherText;
                        if (weatherData.curPty.equals("없음")) {
                            weatherText = weatherData.curSky;
                        }
                        else  {
                            weatherText = weatherData.curPty;
                        }
                        weatherTextView.setText(weatherText);

                        tmpTextView = findViewById(R.id.temperature_text);
                        tmpTextView.setText(weatherData.curTmp);

                        List<WeatherHour> weatherHours = weatherData.getWeatherHours();
                        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(weatherHours.subList(weatherData.findStartIdx(), weatherHours.size()));

                        recyclerView = findViewById(R.id.recyclerView);
                        recyclerView.setAdapter(recyclerAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                        recyclerAdapter.notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "api 호출 오류", Toast.LENGTH_LONG).show();
                    }
                }, throwable -> Toast.makeText(MainActivity.this, throwable.toString(), Toast.LENGTH_LONG).show());
    }

    private boolean isLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // "앱 사용 중에만 허용", "거부", "거부 및 다시 묻지 않음"
                Snackbar snackBar = Snackbar.make(findViewById(R.id.layout_main), R.string.suggest_permission_grant, Snackbar.LENGTH_INDEFINITE).setDuration(8000);
                snackBar.setAction("권한승인", v ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE));
                snackBar.show();
            }
            else {
                SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                boolean isFirstCheck = pref.getBoolean("isFirstPermissionCheck", true);
                if (isFirstCheck) {
                    // "앱 사용 중에만 허용", "거부"
                    pref.edit().putBoolean("isFirstPermissionCheck", false).apply();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                }
                else {
                    Snackbar snackBar = Snackbar.make(findViewById(R.id.layout_main), R.string.suggest_permission_grant_in_setting, Snackbar.LENGTH_LONG).setDuration(8000);
                    snackBar.setAction("확인", v -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    });
                    snackBar.show();
                }
            }
            return false;
        }
        else {
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "권한 획득", Toast.LENGTH_LONG).show();

                WeatherGrid.LatXLngY gridLocation = getGridLocation();
                if (gridLocation != null) {
                    String x = Integer.valueOf((int)gridLocation.x).toString();
                    String y = Integer.valueOf((int)gridLocation.y).toString();

                    getFcstAndUpdateView(x, y);
                }
            } else {
                Toast.makeText(MainActivity.this, "권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}