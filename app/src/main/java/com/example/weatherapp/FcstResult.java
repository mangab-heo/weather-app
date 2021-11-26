package com.example.weatherapp;

import java.util.Collections;
import java.util.List;

public class FcstResult implements APIResult<WeatherData> {
    Response response;

    @Override
    public WeatherData toAppData() throws Exception {
        if (!response.header.resultCode.equals("00")) throw new Exception();

        WeatherData weatherData = new WeatherData();

        List<Item> listItem = response.body.items.item;

        Collections.sort(listItem);

        String fcstTime = listItem.get(0).fcstTime;
        WeatherData.WeatherHour weatherHour = new WeatherData.WeatherHour();
        weatherHour.fcstDate = listItem.get(0).fcstDate;
        weatherHour.fcstTime = listItem.get(0).fcstTime.substring(0, 2) + ":" + listItem.get(0).fcstTime.substring(2);

        for (Item item : listItem) {
            if (!fcstTime.equals(item.fcstTime)) {
                weatherData.addWeatherHour(weatherHour);
                fcstTime = item.fcstTime;

                weatherHour = new WeatherData.WeatherHour();
                weatherHour.fcstDate = item.fcstDate;
                weatherHour.fcstTime = item.fcstTime.substring(0, 2) + ":" + item.fcstTime.substring(2);
            }

            if (item.category.equals(Category.TMN.getValue())) {
                int dayDiff = Integer.parseInt(item.fcstDate) - Integer.parseInt(item.baseDate);
//                weatherData.tmn[dayDiff] = Double.parseDouble(item.fcstValue);
            }
            else if (item.category.equals(Category.TMX.getValue())) {
                int dayDiff = Integer.parseInt(item.fcstDate) - Integer.parseInt(item.baseDate);
//                weatherData.tmx[dayDiff] = Double.parseDouble(item.fcstValue);
            }
            else {
                weatherHour.setFcstValue(item.category, item.fcstValue);
            }
        }

        weatherData.addWeatherHour(weatherHour);
        return weatherData;
    }

    static class Response {
        Header header;
        Body body;
    }

    static class Header {
        String resultCode;
        String resultMsg;
    }

    static class Body {
        String dataType;
        int numOfRows;
        int pageNo;
        int totalCount;
        Items items;
    }

    static class Items {
        List<Item> item;
    }

    static class Item implements Comparable<Item> {
        String baseDate;
        String baseTime;
        String category;
        String fcstDate;
        String fcstTime;
        String fcstValue;
        int nx;
        int ny;

        @Override
        public int compareTo(Item o) {
            String o1DateTime = fcstDate + fcstTime;
            String o2DateTime = o.fcstDate + o.fcstTime;
            return o1DateTime.compareTo(o2DateTime);
        }
    }
}
