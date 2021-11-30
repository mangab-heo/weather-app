package com.example.weatherapp.data

import java.util.*

class WeatherData {
    val weatherHours: MutableList<WeatherHour> = ArrayList()
    var curPty: String? = null
    var curTmp: String? = null
    var curSky: String? = null
    var tmx = doubleArrayOf(-1.0, -1.0, -1.0, -1.0)
    var tmn = doubleArrayOf(-1.0, -1.0, -1.0, -1.0)

    class WeatherHour {
        var fcstDate: String? = null
        var fcstTime: String? = null
        var pop: String? = null
        var pty: String? = null // 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
        var pcp: String? = null
        var reh: String? = null
        var sno: String? = null
        var sky: String? = null // 맑음(1), 구름 많음(3), 흐림(4)
        var tmp: String? = null
        var wsd: String? = null
    }
}