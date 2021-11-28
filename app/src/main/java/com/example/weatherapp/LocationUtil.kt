package com.example.weatherapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.WeatherGrid.LatXLngY
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.*
import java.lang.reflect.Modifier
import java.util.*

object LocationUtil {
    const val REQUEST_CODE = 101
    var stationLocations: MutableList<StationLocation> = ArrayList()
    fun getClosestStation(context: Context, gridLocation: LatXLngY): String {
        val latitude = gridLocation.lat
        val longitude = gridLocation.lng
        if (stationLocations.size == 0) setLocationsFromFile(context)
        var stationName = ""
        var minDist = Double.MAX_VALUE
        for (stationLocation in stationLocations) {
            val dist = stationLocation.getDistance(latitude, longitude)
            if (dist < minDist) {
                stationName = stationLocation.name
                minDist = dist
            }
        }
        val pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE)
        if (stationName == "") {
            val lastStation = pref.getString("lastStation", "")
            stationName = if (lastStation == "") getDefaultStation(gridLocation) else lastStation!!
        } else pref.edit().putString("lastStation", stationName).apply()
        return stationName
    }

    private fun getDefaultStation(gridLocation: LatXLngY): String {
        val constants = DefaultStationLocation::class.java.fields
        var minDist = Double.MAX_VALUE
        var closestStation = ""
        for (field in constants) {
            if (Modifier.isStatic(field.modifiers) && field.type == StationLocation::class.java) {
                try {
                    val stationLocation =
                        field[DefaultStationLocation::class.java] as StationLocation
                    val dist = stationLocation.getDistance(gridLocation.lat, gridLocation.lng)
                    if (dist < minDist) {
                        minDist = dist
                        closestStation = stationLocation.name
                    }
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        }
        return closestStation
    }

    private fun setLocationsFromFile(context: Context) {
        val assetManager = context.resources.assets
        val inputStream: InputStream
        var stationLine: String?
        try {
            inputStream = assetManager.open("stationLocation.txt")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            while (bufferedReader.readLine().also { stationLine = it } != null) {
                val stationLineArr = stationLine!!.split(" ").toTypedArray()
                val stationName = stationLineArr[0]
                val latitude = stationLineArr[1].toDouble()
                val longitude = stationLineArr[2].toDouble()
                stationLocations.add(StationLocation(stationName, latitude, longitude))
            }
            inputStream.close()
            bufferedReader.close()
        } catch (e: Exception) {
            stationLocations.clear()
            Toast.makeText(context, "마지막으로 사용한 미세먼지 관측소를 이용하여 미세먼지 정보를 불러옵니다.", Toast.LENGTH_LONG)
                .show()
        }
    }

    @Throws(Exception::class)
    fun getGridLocation(context: Context): LatXLngY {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val sharedPreferences =
                context.getSharedPreferences("perf", Context.MODE_PRIVATE)
            val latXLngY: LatXLngY
            val gson = Gson()
            var lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation == null) {
                lastKnownLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (lastKnownLocation == null) {
                latXLngY = if (sharedPreferences.contains("lastLocation")) {
                    gson.fromJson(
                        sharedPreferences.getString("lastLocation", ""),
                        LatXLngY::class.java
                    )
                } else throw Exception("현재 위치를 찾지 못했습니다.")
            } else {
                latXLngY = WeatherGrid.convertGRID_GPS(
                    0,
                    lastKnownLocation.latitude,
                    lastKnownLocation.longitude
                )
                sharedPreferences.edit().putString("lastLocation", gson.toJson(latXLngY)).apply()
            }
            latXLngY
        } else throw Exception("위치 권한을 얻지 못했습니다.")
    }

    fun isLocationPermissionGranted(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) showSnackbarForDenial(activity) else if (isFirstCheck(activity)) ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_CODE
            ) else showSnackbarForAlwaysDenial(activity)
            return false
        }
        return true
    }

    private fun showSnackbarForDenial(activity: Activity) {
        val snackBar = Snackbar.make(
            activity.findViewById(R.id.layout_main),
            R.string.suggest_permission_grant,
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction("권한승인") { v: View? ->
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_CODE
            )
        }
        snackBar.show()
    }

    private fun showSnackbarForAlwaysDenial(activity: Activity) {
        val snackBar = Snackbar.make(
            activity.findViewById(R.id.layout_main),
            R.string.suggest_permission_grant_in_setting,
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction("확인") { v: View? ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
        snackBar.show()
    }

    private fun isFirstCheck(activity: Activity): Boolean {
        val pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE)
        val isFirstCheck = pref.getBoolean("isFirstPermissionCheck", true)
        if (isFirstCheck) pref.edit().putBoolean("isFirstPermissionCheck", false).apply()
        return isFirstCheck
    }

    private object DefaultStationLocation {
        private val seoulStation = StationLocation("강남구", 37.5175623, 127.0472893)
        private val gyeonggiStation = StationLocation("복정동", 37.4563598, 127.1304652)
        private val incheonStation = StationLocation("구월동", 37.449722, 126.724167)
        private val gangwonStation = StationLocation("옥천동", 37.7601328, 128.9028489)
        private val chungnamStation = StationLocation("공주", 36.446611, 127.11915)
        private val chungBukStation = StationLocation("가덕면", 36.553449, 127.546407)
        private val gyeongbukStation = StationLocation("3공단", 35.963042, 129.376848)
        private val jeonbukStation = StationLocation("개정동", 35.964433, 126.754533)
        private val jeonnamStation = StationLocation("강진읍", 34.6446579, 126.7711918)
        private val gyeongnamStation = StationLocation("가야읍", 35.2720132, 128.408105)
        private val busanStation = StationLocation("개금동", 35.1552633, 129.0225679)
    }

    class StationLocation(var name: String, var latitude: Double, var longitude: Double) {
        fun getDistance(lat1: Double, lon1: Double): Double {
            val theta = lon1 - longitude
            var dist = Math.sin(deg2rad(lat1)) * Math.sin(
                deg2rad(
                    latitude
                )
            ) + Math.cos(deg2rad(lat1)) * Math.cos(
                deg2rad(
                    latitude
                )
            ) * Math.cos(deg2rad(theta))
            dist = Math.acos(dist)
            dist = rad2deg(dist)
            dist = dist * 60 * 1.1515
            dist = dist * 1.609344
            return dist
        }

        companion object {
            // This function converts decimal degrees to radians
            private fun deg2rad(deg: Double): Double {
                return deg * Math.PI / 180.0
            }

            // This function converts radians to decimal degrees
            private fun rad2deg(rad: Double): Double {
                return rad * 180 / Math.PI
            }
        }
    }
}