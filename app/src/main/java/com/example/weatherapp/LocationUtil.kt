package com.example.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.example.weatherapp.WeatherGrid.convertGRID_GPS;

public class LocationUtil {
    static final int REQUEST_CODE = 101;

    static List<StationLocation> stationLocations = new ArrayList<>();

    public static String getClosestStation(Context context, WeatherGrid.LatXLngY gridLocation) {
        double latitude = gridLocation.lat;
        double longitude = gridLocation.lng;

        if (stationLocations.size() == 0)
            setLocationsFromFile(context);

        String stationName = "";
        double minDist = Double.MAX_VALUE;

        for (StationLocation stationLocation : stationLocations) {
            double dist = stationLocation.getDistance(latitude, longitude);
            if (dist < minDist) {
                stationName = stationLocation.name;
                minDist = dist;
            }
        }

        SharedPreferences pref = context.getSharedPreferences("pref", android.content.Context.MODE_PRIVATE);
        if (stationName.equals("")) {
            String lastStation = pref.getString("lastStation", "");
            stationName = lastStation.equals("") ? getDefaultStation(gridLocation) : lastStation;
        }
        else pref.edit().putString("lastStation", stationName).apply();

        return stationName;
    }

    private static String getDefaultStation(WeatherGrid.LatXLngY gridLocation) {
        Field[] constants = DefaultStationLocation.class.getFields();
        DefaultStationLocation defaultStationLocation = new DefaultStationLocation();
        double minDist = Double.MAX_VALUE;
        String closestStation = "";
        for (Field field : constants) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == StationLocation.class) {
                try {
                    StationLocation stationLocation = (StationLocation) field.get(defaultStationLocation);
                    if (stationLocation != null) {
                        double dist = stationLocation.getDistance(gridLocation.lat, gridLocation.lng);
                        if (dist < minDist) {
                            minDist = dist;
                            closestStation = stationLocation.name;
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return closestStation;
    }

    private static void setLocationsFromFile(Context context) {
        AssetManager assetManager = context.getResources().getAssets();
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

                stationLocations.add(new StationLocation(stationName, latitude, longitude));
            }

            inputStream.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            stationLocations.clear();

            Toast.makeText(context, "마지막으로 사용한 미세먼지 관측소를 이용하여 미세먼지 정보를 불러옵니다.", Toast.LENGTH_LONG).show();
        }
    }

    public static WeatherGrid.LatXLngY getGridLocation(Context context) throws Exception {
        LocationManager locationManager = (LocationManager) context.getSystemService(android.content.Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("perf", Context.MODE_PRIVATE);
            WeatherGrid.LatXLngY latXLngY;
            Gson gson = new Gson();

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation == null) {
                if (sharedPreferences.contains("lastLocation")) {
                    latXLngY = gson.fromJson(sharedPreferences.getString("lastLocation", ""), WeatherGrid.LatXLngY.class);
                }
                else throw new Exception("현재 위치를 찾지 못했습니다.");
            }
            else {
                latXLngY = convertGRID_GPS(0, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                sharedPreferences.edit().putString("lastLocation", gson.toJson(latXLngY)).apply();
            }

            return latXLngY;
        } else throw new Exception("위치 권한을 얻지 못했습니다.");
    }

    public static boolean isLocationPermissionGranted(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION))
                showSnackbarForDenial(activity);
            else if (isFirstCheck(activity))
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            else
                showSnackbarForAlwaysDenial(activity);

            return false;
        }
        return true;
    }

    private static void showSnackbarForDenial(Activity activity) {
        Snackbar snackBar = Snackbar.make(activity.findViewById(R.id.layout_main), R.string.suggest_permission_grant, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("권한승인", v ->
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE));
        snackBar.show();
    }

    private static void showSnackbarForAlwaysDenial(Activity activity) {
        Snackbar snackBar = Snackbar.make(activity.findViewById(R.id.layout_main), R.string.suggest_permission_grant_in_setting, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("확인", v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        });
        snackBar.show();
    }

    private static boolean isFirstCheck(Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences("pref", android.content.Context.MODE_PRIVATE);
        boolean isFirstCheck = pref.getBoolean("isFirstPermissionCheck", true);
        if (isFirstCheck)
            pref.edit().putBoolean("isFirstPermissionCheck", false).apply();
        return isFirstCheck;
    }

    public static class DefaultStationLocation {
        public static final StationLocation seoulStation = new StationLocation("강남구", 37.5175623, 127.0472893);
        public static final StationLocation gyeonggiStation = new StationLocation("복정동", 37.4563598, 127.1304652);
        public static final StationLocation incheonStation = new StationLocation("구월동", 37.449722, 126.724167);
        public static final StationLocation gangwonStation = new StationLocation("옥천동", 37.7601328, 128.9028489);
        public static final StationLocation chungnamStation = new StationLocation("공주", 36.446611, 127.11915);
        public static final StationLocation chungBukStation = new StationLocation("가덕면", 36.553449, 127.546407);
        public static final StationLocation gyeongbukStation = new StationLocation("3공단", 35.963042, 129.376848);
        public static final StationLocation jeonbukStation = new StationLocation("개정동", 35.964433, 126.754533);
        public static final StationLocation jeonnamStation = new StationLocation("강진읍", 34.6446579, 126.7711918);
        public static final StationLocation gyeongnamStation = new StationLocation("가야읍", 35.2720132, 128.408105);
        public static final StationLocation busanStation = new StationLocation("개금동", 35.1552633, 129.0225679);
    }

    public static class StationLocation {
        String name;
        double latitude;
        double longitude;

        public StationLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }


        double getDistance(double lat1, double lon1) {

            double theta = lon1 - this.longitude;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(this.latitude)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(this.latitude)) * Math.cos(deg2rad(theta));

            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;

            dist = dist * 1.609344;

            return dist;
        }


        // This function converts decimal degrees to radians
        private static double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        // This function converts radians to decimal degrees
        private static double rad2deg(double rad) {
            return (rad * 180 / Math.PI);
        }
    }
}
