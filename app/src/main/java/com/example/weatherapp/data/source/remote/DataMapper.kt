package com.example.weatherapp.data.source.remote

import com.example.weatherapp.data.PmData
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherData.WeatherHour
import com.example.weatherapp.data.source.remote.alpltn.ArpltnResult
import com.example.weatherapp.data.source.remote.fcst.FcstResult
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

@Throws(Exception::class)
fun toPmData(arpltnResult: ArpltnResult): PmData {
    try {
        if (arpltnResult.response!!.header!!.resultCode != "00") throw Exception()
        val item: ArpltnResult.Item = arpltnResult.response!!.body!!.items!![0]
        val pmData = PmData()
        setFields(pmData, item)
        return pmData
    } catch (_: Exception) {
        throw Exception("미세먼지 정보를 불러오지 못 했습니다.")
    }
}


@Throws(Exception::class)
fun toWeatherData(fcstResult: FcstResult): WeatherData {
    try {
        if (fcstResult.response!!.header!!.resultCode != "00") throw Exception()
        val weatherData = WeatherData()
        val listItem: MutableList<FcstResult.Item> = fcstResult.response!!.body!!.items!!.item!!
        listItem.sort()
        var fcstTime = listItem[0].fcstTime
        var weatherHour = WeatherHour()
        weatherHour.fcstDate = listItem[0].fcstDate
        weatherHour.fcstTime =
            listItem[0].fcstTime!!.substring(0, 2) + ":" + listItem[0].fcstTime!!.substring(2)
        for (item in listItem) {
            if (fcstTime != item.fcstTime) {
                weatherData.weatherHours.add(weatherHour)
                fcstTime = item.fcstTime
                weatherHour = WeatherHour()
                weatherHour.fcstDate = item.fcstDate
                weatherHour.fcstTime = item.fcstTime!!.substring(0, 2) + ":" + item.fcstTime!!.substring(2)
            }
            if (item.category == Category.TMN.value) {
                val dayDiff = item.fcstDate!!.toInt() - item.baseDate!!.toInt()
                //                weatherData.tmn[dayDiff] = Double.parseDouble(item.fcstValue);
            } else if (item.category == Category.TMX.value) {
                val dayDiff = item.fcstDate!!.toInt() - item.baseDate!!.toInt()
                //                weatherData.tmx[dayDiff] = Double.parseDouble(item.fcstValue);
            } else {
                setFcstValue(weatherHour, item.category, item.fcstValue)
            }
        }
        weatherData.weatherHours.add(weatherHour)
        return weatherData
    } catch (_: Exception) {
        throw Exception("날씨 정보를 불러오지 못 했습니다.")
    }
}


private fun setFields(pmData: PmData, item: ArpltnResult.Item) {
    for (pmIdx: Int in 0..1) {
        val pmValues: Array<String?> = if (pmIdx == 0) arrayOf(
            item.pm10Value,
            item.pm10Grade1h,
            item.pm10Value24,
            item.pm10Grade,
            item.pm10Flag
        ) else arrayOf(
            item.pm25Value,
            item.pm25Grade1h,
            item.pm25Value24,
            item.pm25Grade,
            item.pm25Flag
        )
        when {
            pmValues[0] != "-" -> {
                pmData.pmStrs[pmIdx] = pmValues[0]
                pmData.pmGrade[pmIdx] = pmValues[1]?.toInt() ?: 0
            }
            pmValues[2] != "-" -> {
                pmData.pmStrs[pmIdx] = pmValues[2]
                pmData.pmGrade[pmIdx] = pmValues[3]?.toInt() ?: 0
            }
            else -> {
                pmData.pmStrs[pmIdx] = pmValues[4]
                pmData.isUnitHidden[pmIdx] = true
            }
        }
    }
}

fun combineWeatherData(list: List<WeatherData>): WeatherData {
    return if (list.size == 1) {
        val weatherDataVillage = list[0]
        val villageHours = weatherDataVillage.weatherHours
        val villageIdx = findStartIdx(weatherDataVillage)
        weatherDataVillage.curPty = villageHours[villageIdx].pty
        weatherDataVillage.curTmp = villageHours[villageIdx].tmp
        weatherDataVillage.curSky = villageHours[villageIdx].sky
        weatherDataVillage
    } else {
        val weatherDataVillage: WeatherData
        val weatherDataSrt: WeatherData
        if (list[0].weatherHours.size >= list[1].weatherHours.size) {
            weatherDataVillage = list[0]
            weatherDataSrt = list[1]
        } else {
            weatherDataVillage = list[1]
            weatherDataSrt = list[0]
        }
        val villageHours = weatherDataVillage.weatherHours
        var villageIdx = findStartIdx(weatherDataVillage)
        val srtHours = weatherDataSrt.weatherHours
        var srtIdx = findStartIdx(weatherDataSrt)
        weatherDataVillage.curPty = srtHours[0].pty
        weatherDataVillage.curTmp = srtHours[0].tmp
        weatherDataVillage.curSky = srtHours[0].sky
        while (srtIdx < srtHours.size) {
            val srtHour = srtHours[srtIdx]
            val villageHour = villageHours[villageIdx]
            villageHour.tmp = srtHour.tmp
            villageHour.pcp = srtHour.pcp
            villageHour.sky = srtHour.sky
            villageHour.reh = srtHour.reh
            villageHour.pty = srtHour.pty
            villageHour.wsd = srtHour.wsd
            srtIdx++
            villageIdx++
        }
        weatherDataVillage
    }
}

fun findStartIdx(weatherData: WeatherData): Int {
    val weatherHours = weatherData.weatherHours
    val sbf = SimpleDateFormat("yyyyMMdd HH00", Locale.KOREA)
    val curDateTime = sbf.format(Date(System.currentTimeMillis())).split(" ").toTypedArray()
    val curDateInt = curDateTime[0].toInt()
    val curTimeInt = curDateTime[1].toInt()
    var surviveIdx = 0
    for (i in weatherHours.indices) {
        val weatherHour: WeatherHour = weatherHours[i]
        val fcstDateInt = weatherHour.fcstDate!!.toInt()
        val fcstTimeInt = (weatherHour.fcstTime!!.substring(0, 2)
                + weatherHour.fcstTime!!.substring(3)).toInt()
        if (fcstDateInt >= curDateInt && fcstTimeInt > curTimeInt) {
            surviveIdx = i
            break
        }
    }
    return surviveIdx
}

fun setFcstValue(weatherHour: WeatherHour, category: String?, fcstValue: String?) {
    if (category == Category.POP.value) {
        weatherHour.pop = "$fcstValue %"
    } else if (category == Category.PTY.value) {
        when (fcstValue?.toInt() ?: 0) {
            0 -> weatherHour.pty = "없음"
            1 -> weatherHour.pty = "비"
            2 -> weatherHour.pty = "비/눈"
            3 -> weatherHour.pty = "눈"
            4 -> weatherHour.pty = "소나기"
        }
    } else if (category == Category.PCP.value || category == Category.RN1.value) {
        weatherHour.pcp = fcstValue
    } else if (category == Category.REH.value) {
        weatherHour.reh = "$fcstValue %"
    } else if (category == Category.SNO.value) {
        weatherHour.sno = fcstValue
    } else if (category == Category.SKY.value) {
        when (fcstValue?.toInt() ?: 1) {
            1 -> weatherHour.sky = "맑음"
            3 -> weatherHour.sky = "구름 많음"
            4 -> weatherHour.sky = "흐림"
        }
    } else if (category == Category.TMP.value || category == Category.T1H.value) {
        weatherHour.tmp = "$fcstValue ℃"
    } else if (category == Category.WSD.value) {
        weatherHour.wsd = "$fcstValue m/s"
    }
}

enum class Category(val value: String) {
    POP("POP"), PTY("PTY"), PCP("PCP"), REH("REH"), SNO("SNO"), SKY("SKY"), TMP("TMP"), TMN("TMN"), TMX(
        "TMX"
    ),
    WSD("WSD"), T1H("T1H"), RN1("RN1");

}
