package com.example.weatherapp;

import java.util.List;

public class FcstResult {
    Response response;
}

class Response {
    Header header;
    Body body;
}

class Header {
    String resultCode;
    String resultMsg;
}

class Body {
    String dataType;
    Items items;
}

class Items {
    List<Item> item;
}

class Item {
    String baseDate;
    String baseTime;
    String category;
    String fcstDate;
    String fcstTime;
    String fcstValue;
    double nx;
    double ny;
}