package com.example.weatherapp.data.source.remote

import com.example.weatherapp.WeatherGrid
import com.example.weatherapp.data.PmData
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.source.DataSource
import com.example.weatherapp.data.source.remote.alpltn.ArpltnClientConstants
import com.example.weatherapp.data.source.remote.alpltn.ArpltnResult
import com.example.weatherapp.data.source.remote.fcst.FcstClientConstants
import com.example.weatherapp.data.source.remote.fcst.FcstResult
import com.example.weatherapp.data.source.remote.fcst.FcstService
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.exceptions.Exceptions
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class RemoteDataSource : DataSource {
    override fun getObservablePmData(stationName: String): Observable<PmData> {
        return RetrofitClient.arpltnService.getDnsty(
            ArpltnClientConstants.SERVICE_KEY, ArpltnClientConstants.RETURN_TYPE,
            ArpltnClientConstants.NUM_OF_ROWS, ArpltnClientConstants.PAGE_NO,
            ArpltnClientConstants.DATA_TERM, ArpltnClientConstants.VER,
            stationName)
            .map { arpltnResult: ArpltnResult ->
                try {
                    toPmData(arpltnResult)
                } catch (exception: Exception) {
                    throw Exceptions.propagate(exception)
                }
            }
    }

    override fun getObservableWeatherData(gridLocation: WeatherGrid.LatXLngY): Observable<WeatherData> {
        val x = gridLocation.x.toInt()
        val y = gridLocation.y.toInt()
        val baseDateTimeVil = getBaseDateTime(0)
        val baseDateTimeSrt = getBaseDateTime(1)

        val fcstService: FcstService = RetrofitClient.fcstService
        val vilageFcstResult: Observable<FcstResult> = fcstService.getVilageFcst(
            FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
            baseDateTimeVil[0], baseDateTimeVil[1], x, y)

        val ultraSrtFcstResult: Observable<FcstResult> = fcstService.getUltraSrtFcst(
            FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
            baseDateTimeSrt[0], baseDateTimeSrt[1], x, y)

        return Observable.zip(listOf(vilageFcstResult, ultraSrtFcstResult)) {
            try {
                val weatherDatalist: List<WeatherData> = it.asList().map { fcstResult -> toWeatherData(fcstResult as FcstResult) }
                combineWeatherData(weatherDatalist)
            } catch (exception: Exception) {
                throw Exceptions.propagate(exception)
            }
        }
    }

    private fun getBaseDateTime(fcstConstantsIdx: Int): Array<String?> {
        val fcstConstants =
            arrayOf(intArrayOf(300, 210, 2300, 100, 200), intArrayOf(100, 45, 2330, 70, 30))
        val sdf = SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA)
        val curDateTimeStr = sdf.format(Date(System.currentTimeMillis()))
        val curDateTime = curDateTimeStr.split(" ").toTypedArray()
        val baseDateTime = arrayOfNulls<String>(2)
        baseDateTime[0] = curDateTime[0]
        val quotient = curDateTime[1].toInt() / fcstConstants[fcstConstantsIdx][0]
        val remain = curDateTime[1].toInt() % fcstConstants[fcstConstantsIdx][0]
        if (quotient == 0 && remain <= fcstConstants[fcstConstantsIdx][1]) {
            val yesterDateTime = sdf.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))
                .split(" ").toTypedArray()
            baseDateTime[0] = yesterDateTime[0]
            baseDateTime[1] = fcstConstants[fcstConstantsIdx][2].toString()
        } else {
            if (remain <= fcstConstants[fcstConstantsIdx][1]) {
                baseDateTime[1] =
                    (curDateTime[1].toInt() - remain - fcstConstants[fcstConstantsIdx][3]).toString()
            } else {
                baseDateTime[1] =
                    (curDateTime[1].toInt() - remain + fcstConstants[fcstConstantsIdx][4]).toString()
            }
        }
        if (baseDateTime[1]!!.length < 4) {
            var zeroCnts = 4 - baseDateTime[1]!!.length
            val zeros = StringBuilder()
            while (zeroCnts > 0) {
                zeroCnts -= 1
                zeros.append("0")
            }
            baseDateTime[1] = zeros.append(baseDateTime[1]).toString()
        }
        return baseDateTime
    }

}