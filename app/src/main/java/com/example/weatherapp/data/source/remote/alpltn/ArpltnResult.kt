package com.example.weatherapp.data.source.remote.alpltn

class ArpltnResult {
    var response: Response? = null

    class Response {
        var header: Header? = null
        var body: Body? = null
    }

    class Header {
        var resultCode: String? = null
        var resultMsg: String? = null
    }

    class Body {
        var numOfRows = 0
        var pageNo = 0
        var totalCount = 0
        var items: List<Item>? = null
    }

    class Item {
        var pm10Value: String? = null
        var pm10Value24: String? = null
        var pm25Value: String? = null
        var pm25Value24: String? = null
        var pm10Flag: String? = null
        var pm25Flag: String? = null
        var pm10Grade1h: String? = null
        var pm10Grade: String? = null
        var pm25Grade1h: String? = null
        var pm25Grade: String? = null
    }
}