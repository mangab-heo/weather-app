package com.example.weatherapp;

import android.content.Intent;

import java.util.List;

public class FcstResult {
    Response response;

    WeatherData toWeatherData() {
        WeatherData weatherData = new WeatherData();

        List<Item> listItem = response.body.items.item;

        String fcstTime = listItem.get(0).fcstTime;
        WeatherHour weatherHour = new WeatherHour();
        weatherHour.fcstDate = listItem.get(0).fcstDate;
        weatherHour.fcstTime = listItem.get(0).fcstTime;
        int cnt = 0;
        for (Item item : listItem) {
            if (!fcstTime.equals(item.fcstTime)) {
                weatherData.addWeatherHour(weatherHour);
                fcstTime = item.fcstTime;

                weatherHour = new WeatherHour();
                weatherHour.fcstDate = item.fcstDate;
                weatherHour.fcstTime = item.fcstTime;
            }

            if (item.category.equals(Category.TMN.getValue())) {
                // check TODAY TOMM DAT DADAT given baseDate and fcstDate
                int dayDiff = Integer.parseInt(item.fcstDate) - Integer.parseInt(item.baseDate);
                weatherData.tmn[dayDiff] = Double.parseDouble(item.fcstValue);
            }
            else if (item.category.equals(Category.TMX.getValue())) {
                int dayDiff = Integer.parseInt(item.fcstDate) - Integer.parseInt(item.baseDate);
                weatherData.tmx[dayDiff] = Double.parseDouble(item.fcstValue);
            }
            else {
                weatherHour.setFcstValue(item.category, item.fcstValue);
            }
        }

        weatherData.addWeatherHour(weatherHour);

        return weatherData;
    }
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
    int numOfRows;
    int pageNo;
    int totalCount;
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
    int nx;
    int ny;
}
