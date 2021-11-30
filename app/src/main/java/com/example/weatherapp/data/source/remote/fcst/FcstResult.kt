package com.example.weatherapp.data.source.remote.fcst

class FcstResult {
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
        var dataType: String? = null
        var numOfRows = 0
        var pageNo = 0
        var totalCount = 0
        var items: Items? = null
    }

    class Items {
        var item: MutableList<Item>? = null
    }

    class Item : Comparable<Item> {
        var baseDate: String? = null
        var baseTime: String? = null
        var category: String? = null
        var fcstDate: String? = null
        var fcstTime: String? = null
        var fcstValue: String? = null
        var nx = 0
        var ny = 0
        override fun compareTo(other: Item): Int {
            val o1DateTime = fcstDate + fcstTime
            val o2DateTime = other.fcstDate + other.fcstTime
            return o1DateTime.compareTo(o2DateTime)
        }
    }
}