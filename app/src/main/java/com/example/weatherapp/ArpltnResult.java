package com.example.weatherapp;

import java.util.List;

public class ArpltnResult implements APIResult<PmData> {
    Response response;

    @Override
    public PmData toAppData() throws Exception {
        if (!response.header.resultCode.equals("00")) throw new Exception();
        ArpltnResult.Item item = this.response.body.items.get(0);

        PmData pmData = new PmData();
        pmData.setFields(item, 0);
        pmData.setFields(item, 1);

        return pmData;
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
        int numOfRows;
        int pageNo;
        int totalCount;
        List<Item> items;
    }

    static class Item {
        String pm10Value;
        String pm10Value24;
        String pm25Value;
        String pm25Value24;
        String pm10Flag;
        String pm25Flag;
        String pm10Grade1h;
        String pm10Grade;
        String pm25Grade1h;
        String pm25Grade;
    }
}