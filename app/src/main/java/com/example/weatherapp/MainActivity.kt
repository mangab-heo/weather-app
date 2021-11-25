package com.example.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.example.weatherapp.WeatherGrid.LatXLngY
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class MainActivity : RxAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setActionBar()
        setImageButtonListener()
        drawMainView()
    }

    override fun onRestart() {
        super.onRestart()
        drawMainView()
    }

    private fun setImageButtonListener() {
        val imageButton = findViewById<ImageButton>(R.id.gps_button)
        imageButton.setOnClickListener { v: View? ->
            Toast.makeText(applicationContext, "날씨 정보를 불러옵니다.", Toast.LENGTH_LONG).show()
            drawMainView()
        }
    }

    private fun drawMainView() {
        if (LocationUtil.isLocationPermissionGranted(this@MainActivity)) {
            try {
                val gridLocation = LocationUtil.getGridLocation(applicationContext)
                val stationName = LocationUtil.getClosestStation(applicationContext, gridLocation)
                callApiAndUpdateView(gridLocation, stationName)
            } catch (exception: Exception) {
                Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun callApiAndUpdateView(gridLocation: LatXLngY, stationName: String) {
        val requests = getObservables(gridLocation, stationName)
        Observable.zip<Any, List<ViewData>>(requests) { parsedData: Array<Any?> ->
            val viewDataList: MutableList<ViewData> = ArrayList()
            val weatherDataList: MutableList<WeatherData> = ArrayList()
            for (data in parsedData) {
                if (data == null) continue
                if (data is WeatherData) weatherDataList.add(data) else if (data is ViewData) viewDataList.add(data)
            }
            viewDataList.add(WeatherData.combineWeatherData(weatherDataList))
            viewDataList
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe({ viewDataList: List<ViewData> -> for (viewData in viewDataList) viewData.updateView(this@MainActivity) }) { throwable: Throwable? -> Toast.makeText(this@MainActivity, "날씨 정보를 불러오지 못 했습니다.", Toast.LENGTH_LONG).show() }
    }

    private fun setActionBar() {
        val actionBar = supportActionBar
        actionBar?.hide()
    }

    private fun getObservables(gridLocation: LatXLngY, stationName: String): List<Observable<*>> {
        val x = gridLocation.x.toInt()
        val y = gridLocation.y.toInt()
        val baseDateTimeVil = WeatherData.getBaseDateTime(0)
        val baseDateTimeSrt = WeatherData.getBaseDateTime(1)
        val requests: MutableList<Observable<*>> = ArrayList()
        if (stationName != "") {
            val getDnsty = RetrofitClient.getArpltnService().getDnsty(
                    ArpltnClientConstants.SERVICE_KEY, ArpltnClientConstants.RETURN_TYPE,
                    ArpltnClientConstants.NUM_OF_ROWS, ArpltnClientConstants.PAGE_NO,
                    ArpltnClientConstants.DATA_TERM, ArpltnClientConstants.VER,
                    stationName)
                    .map { obj: ArpltnResult -> obj.toAppData() }
            requests.add(getDnsty)
        } else Toast.makeText(this@MainActivity, "미세먼지 관측소 정보를 찾지 못했습니다.", Toast.LENGTH_LONG).show()
        val getVillageFcst = RetrofitClient.getFcstService().getVilageFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDateTimeVil[0], baseDateTimeVil[1], x, y)
                .map { obj: FcstResult -> obj.toAppData() }
        requests.add(getVillageFcst)
        val getUltraSrtFcst = RetrofitClient.getFcstService().getUltraSrtFcst(
                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
                baseDateTimeSrt[0], baseDateTimeSrt[1], x, y)
                .map { obj: FcstResult -> obj.toAppData() }
        requests.add(getUltraSrtFcst)
        return requests
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationUtil.REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "권한을 획득했습니다.", Toast.LENGTH_LONG).show()
            }
            drawMainView()
        }
    }
}