package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.weatherapp.WeatherGrid.TO_GRID;
import static com.example.weatherapp.WeatherGrid.convertGRID_GPS;

public class MainActivity extends AppCompatActivity {
    WeatherData weatherData;
    RecyclerView recyclerView;

    static final int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        String[] baseDateTime = findBaseDateTime();

        String baseDate = baseDateTime[0];
        String baseTime = baseDateTime[1];

        if (baseDate == null || baseTime == null) {
            Toast.makeText(MainActivity.this, "현재 시각을 구할 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        ImageButton imageButton = findViewById(R.id.gps_button);
        imageButton.setOnClickListener(v -> {

            Toast.makeText(MainActivity.this, "업데이트", Toast.LENGTH_LONG).show();

            if (isLocationPermissionGranted()) {
                WeatherGrid.LatXLngY gridLocation = getGridLocation();

                if (gridLocation != null) {
                    String x = Integer.valueOf((int)gridLocation.x).toString();
                    String y = Integer.valueOf((int)gridLocation.y).toString();

                    getFcstAndUpdateView(baseDate, baseTime, x, y);
                }
            }
        });

        if (isLocationPermissionGranted()) {
            WeatherGrid.LatXLngY gridLocation = getGridLocation();

            if (gridLocation != null) {
                String x = Integer.valueOf((int) gridLocation.x).toString();
                String y = Integer.valueOf((int) gridLocation.y).toString();

                getFcstAndUpdateView(baseDate, baseTime, x, y);
            }
        }
    }

    private String[] findBaseDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        String curDateTimeStr = sdf.format(new Date(System.currentTimeMillis()));
        String[] curDateTime = curDateTimeStr.split(" ");

        String curTime = curDateTime[1];
        String baseDate = curDateTime[0];
        String baseTime;

        int quotient = Integer.parseInt(curTime) / 300;
        int remain = Integer.parseInt(curTime) % 300;

        if (quotient == 0) {
            // 00시 00분 ~ 02시 59분
            if (remain <= 210) {
                // 전날 23시
                String[] yesterDateTime = sdf.format(new Date(System.currentTimeMillis() - 1000*60*60*24*-1))
                        .split(" ");
                baseDate = yesterDateTime[0];
                baseTime = "2300";
            }
            else {
                // 오늘 0200
                baseTime = "0200";
            }
        }
        else {
            // 03시 00분 ~ 23시 59분
            if (remain <= 210) {
                // - remain - 100
                baseTime = String.valueOf(Integer.parseInt(curTime) - remain - 100);
            }
            else {
                // - remain + 200
                baseTime = String.valueOf(Integer.parseInt(curTime) - remain + 200);
            }
        }

        return new String[] { baseDate, baseTime };
    }

    private WeatherGrid.LatXLngY getGridLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        WeatherGrid.LatXLngY latXLngY = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    private void getFcstAndUpdateView(String baseDate, String baseTime, String nx, String ny) {
        RetrofitClient.getApiService().getVilageFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDate, baseTime, nx, ny)
                .enqueue(new Callback<FcstResult>() {
                    @Override
                    public void onResponse(@NonNull Call<FcstResult> call, @NonNull Response<FcstResult> response) {
                        FcstResult fcstResult = response.body();

                        if (fcstResult != null) {
                            weatherData = fcstResult.toWeatherData();

                            if (weatherData.getWeatherHours().size() == 0) {
                                Toast.makeText(MainActivity.this, "데이터 없음", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            SimpleDateFormat sbf = new SimpleDateFormat("yyyyMMdd HH00", Locale.KOREA);
                            String[] curDateTime = sbf.format(new Date(System.currentTimeMillis())).split(" ");
                            int curDateInt = Integer.parseInt(curDateTime[0]);
                            int curTimeInt = Integer.parseInt(curDateTime[1]);

                            List<WeatherHour> weatherHours = weatherData.getWeatherHours();
                            int surviveIdx = 0;
                            for (int i = 0; i < weatherHours.size(); i++) {
                                WeatherHour weatherHour = weatherHours.get(i);
                                int fcstDateInt = Integer.parseInt(weatherHour.fcstDate);
                                int fcstTimeInt = Integer.parseInt(weatherHour.fcstTime.substring(0, 2)
                                        + weatherHour.fcstTime.substring(3));

                                if (fcstDateInt >= curDateInt && fcstTimeInt > curTimeInt) {
                                    surviveIdx = i;
                                    break;
                                }
                            }

                            RecyclerAdapter recyclerAdapter = new RecyclerAdapter(weatherHours.subList(surviveIdx, weatherHours.size()));

                            recyclerView = findViewById(R.id.recyclerView);
                            recyclerView.setAdapter(recyclerAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FcstResult> call, @NonNull Throwable t) {
                    }
                });
    }

    private boolean isLocationPermissionGranted() {
        SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean isFirstCheck = pref.getBoolean("isFirstPermissionCheck", true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar snackBar = Snackbar.make(findViewById(R.id.layout_main), R.string.suggest_permission_grant, Snackbar.LENGTH_INDEFINITE).setDuration(8000);
                snackBar.setAction("권한승인", v ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE));
                snackBar.show();
            }
            else {
                if (isFirstCheck) {
                    pref.edit().putBoolean("isFirstPermissionCheck", false).apply();
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "권한 획득", Toast.LENGTH_LONG).show();

                String[] baseDateTime = findBaseDateTime();
                String baseDate = baseDateTime[0];
                String baseTime = baseDateTime[1];

                if (baseDate == null || baseTime == null) {
                    Toast.makeText(MainActivity.this, "현재 시각을 구할 수 없습니다.", Toast.LENGTH_LONG).show();
                }
                else {
                    WeatherGrid.LatXLngY gridLocation = getGridLocation();
                    if (gridLocation != null) {
                        String x = Integer.valueOf((int)gridLocation.x).toString();
                        String y = Integer.valueOf((int)gridLocation.y).toString();

                        getFcstAndUpdateView(baseDate, baseTime, x, y);
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}