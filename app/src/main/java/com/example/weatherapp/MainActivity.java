package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.example.weatherapp.RetrofitClient.getArpltnService;
import static com.example.weatherapp.WeatherData.combineWeatherData;
import static com.example.weatherapp.WeatherGrid.TO_GRID;
import static com.example.weatherapp.WeatherGrid.convertGRID_GPS;

public class MainActivity extends RxAppCompatActivity {
    List<StationLocation> stationLocations = new ArrayList<>();

    static final int REQUEST_CODE = 101;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBar();

        setLocationsFromFile(this);

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
        imageButton.setOnClickListener(v -> drawMainView());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMainView() {
        if (isLocationPermissionGranted()) {
            try {
                WeatherGrid.LatXLngY gridLocation = getGridLocation();
                String stationName = getClosestStation(gridLocation);

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

    private String getClosestStation(WeatherGrid.LatXLngY gridLocation) {
        double latitude = gridLocation.lat;
        double longitude = gridLocation.lng;

        String stationName = "";
        double minDist = Double.MAX_VALUE;

        for (StationLocation stationLocation : stationLocations) {
            double dist = stationLocation.getDistance(latitude, longitude);
            if (dist < minDist) {
                stationName = stationLocation.name;
                minDist = dist;
            }
        }

        return stationName;
    }

    private static void setLocationsFromFile(MainActivity mainActivity) {
        AssetManager assetManager = mainActivity.getResources().getAssets();
        InputStream inputStream;
        String stationLine;

        try {
            inputStream = assetManager.open("stationLocation.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            while ((stationLine = bufferedReader.readLine()) != null) {
                String[] stationLineArr = stationLine.split(" ");

                String stationName = stationLineArr[0];
                double latitude = Double.parseDouble(stationLineArr[1]);
                double longitude = Double.parseDouble(stationLineArr[2]);

                mainActivity.stationLocations.add(new StationLocation(stationName, latitude, longitude));
            }

            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            mainActivity.stationLocations.clear();
        }
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @NotNull
    private String[] getBaseDateTime(int fcstConstantsIdx) {
        final int[][] fcstConstants = {{300, 210, 2300, 100, 200}, {100, 45, 2330, 70, 30}};

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        String curDateTimeStr = sdf.format(new Date(System.currentTimeMillis()));
        String[] curDateTime = curDateTimeStr.split(" ");

        String[] baseDateTime = new String[2];
        baseDateTime[0] = curDateTime[0];

        int quotient = Integer.parseInt(curDateTime[1]) / fcstConstants[fcstConstantsIdx][0];
        int remain = Integer.parseInt(curDateTime[1]) % fcstConstants[fcstConstantsIdx][0];

        if (quotient == 0 && remain <= fcstConstants[fcstConstantsIdx][1]) {
            String[] yesterDateTime = sdf.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))
                    .split(" ");
            baseDateTime[0] = yesterDateTime[0];
            baseDateTime[1] = String.valueOf(fcstConstants[fcstConstantsIdx][2]);
        } else {
            if (remain <= fcstConstants[fcstConstantsIdx][1]) {
                baseDateTime[1] = String.valueOf(Integer.parseInt(curDateTime[1]) - remain - fcstConstants[fcstConstantsIdx][3]);
            } else {
                baseDateTime[1] = String.valueOf(Integer.parseInt(curDateTime[1]) - remain + fcstConstants[fcstConstantsIdx][4]);
            }
        }

        if (baseDateTime[1].length() < 4) {
            int zeroCnts = 4 - baseDateTime[1].length();
            StringBuilder zeros = new StringBuilder();
            while (zeroCnts > 0) {
                zeroCnts -= 1;
                zeros.append("0");
            }
            baseDateTime[1] = zeros + baseDateTime[1];
        }
        return baseDateTime;
    }

    private WeatherGrid.LatXLngY getGridLocation() throws Exception {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null)
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastKnownLocation == null) {
                Log.e("MainActivity", "getLastKnownLocation return null exception");
                throw new Exception("현재 위치를 찾지 못했습니다.");
            }
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();

            return convertGRID_GPS(TO_GRID, lat, lng);
        }
        else throw new Exception("위치 권한을 얻지 못했습니다.");
    }

    @NotNull
    private List<Observable<?>> getObservables(WeatherGrid.LatXLngY gridLocation, String stationName) {
        int x = (int) gridLocation.x;
        int y = (int) gridLocation.y;

        String[] baseDateTimeVil = getBaseDateTime(0);
        String[] baseDateTimeSrt = getBaseDateTime(1);

        List<Observable<?>> requests = new ArrayList<>();

        if (!stationName.equals("")) {
            Observable<PmData> getDnsty = getArpltnService().getDnsty(
                    ArpltnClientConstants.SERVICE_KEY, ArpltnClientConstants.RETURN_TYPE,
                    ArpltnClientConstants.NUM_OF_ROWS, ArpltnClientConstants.PAGE_NO,
                    ArpltnClientConstants.DATA_TERM, ArpltnClientConstants.VER,
                    stationName)
                    .map(ArpltnResult::toAppData)
                    .onErrorResumeNext(throwable -> { Toast.makeText(MainActivity.this, "미세먼지 정보를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();return null; });
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
                .map(FcstResult::toAppData)
                .onErrorResumeNext(throwable -> null);
        requests.add(getUltraSrtFcst);

        return requests;
    }

    private boolean isLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
                showSnackbarForDenial();
            else if (isFirstCheck())
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            else
                showSnackbarForAlwaysDenial();

            return false;
        }
        return true;
    }

    private void showSnackbarForDenial() {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.layout_main), R.string.suggest_permission_grant, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("권한승인", v ->
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE));
        snackBar.show();
    }

    private void showSnackbarForAlwaysDenial() {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.layout_main), R.string.suggest_permission_grant_in_setting, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("확인", v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        snackBar.show();
    }

    private boolean isFirstCheck() {
        SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean isFirstCheck = pref.getBoolean("isFirstPermissionCheck", true);
        if (isFirstCheck)
            pref.edit().putBoolean("isFirstPermissionCheck", false).apply();
        return isFirstCheck;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "권한을 획득했습니다.", Toast.LENGTH_LONG).show();
            }
            drawMainView();
        }
    }
}