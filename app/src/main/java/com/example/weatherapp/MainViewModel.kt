package com.example.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.ArrayList

class MainViewModel : ViewModel() {
    val weatherData: LiveData<WeatherData> by lazy { _weatherData }
    private val _weatherData: MutableLiveData<WeatherData> = MutableLiveData()

    val pmData: LiveData<PmData> by lazy { _pmData }
    private val _pmData: MutableLiveData<PmData> = MutableLiveData()

    val weatherText: LiveData<String> = Transformations.map(_weatherData) {
        if (it.curPty == "없음") it.curSky else it.curPty
    }

    fun refreshWeatherData(gridLocation: WeatherGrid.LatXLngY, stationName: String) {
        val requests = getObservables(gridLocation, stationName)
        Observable.zip<Any, List<Any>>(requests) { parsedData: Array<Any?> ->
            val viewDataList: MutableList<Any> = ArrayList()
            val weatherDataList: MutableList<WeatherData> = ArrayList()
            for (data in parsedData) {
                if (data == null) continue
                if (data is WeatherData) weatherDataList.add(data) else viewDataList.add(data)
            }
            viewDataList.add(WeatherData.combineWeatherData(weatherDataList))
            viewDataList
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { viewDataList: List<Any> ->
                    for (viewData in viewDataList)
                        if (viewData is WeatherData) {
                            _weatherData.value = viewData
                        }
                        else if (viewData is PmData) {
                            _pmData.value = viewData
                        }
                }
        //{ throwable: Throwable? -> Toast.makeText(this@MainActivity, "날씨 정보를 불러오지 못 했습니다.", Toast.LENGTH_LONG).show() }

    }

    private fun getObservables(gridLocation: WeatherGrid.LatXLngY, stationName: String): List<Observable<*>> {
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
        }
        // else Toast.makeText(this@MainActivity, "미세먼지 관측소 정보를 찾지 못했습니다.", Toast.LENGTH_LONG).show()
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
}