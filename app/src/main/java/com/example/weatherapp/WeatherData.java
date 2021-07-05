package com.example.weatherapp;

import java.util.ArrayList;
import java.util.List;

public class WeatherData {
    private List<WeatherHour> weatherHours = new ArrayList<>();

    double[] tmx = new double[] { -1, -1, -1, -1 };
    double[] tmn = new double[] { -1, -1, -1, -1 };

    List<WeatherHour> getWeatherHours() { return this.weatherHours; }

    void addWeatherHour(WeatherHour weatherHour) { weatherHours.add(weatherHour); }
}

class WeatherHour {
    String fcstDate = "Missing";
    String fcstTime = "Missing";
    String pop = "Missing";
    String pty = "Missing"; // 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
    String pcp = "Missing";
    String reh = "Missing";
    String sno = "Missing";
    String sky = "Missing"; // 맑음(1), 구름 많음(3), 흐림(4)
    String tmp = "Missing";
    String wsd = "Missing";

    public void setFcstValue(String category, String fcstValue) {
        if (category.equals(Category.POP.getValue())) {
            this.pop = fcstValue + " %";
        }
        else if (category.equals(Category.PTY.getValue())) {
            int ptyCode = Integer.parseInt(fcstValue);
            switch (ptyCode) {
                case 0: this.pty = "없음"; break;
                case 1: this.pty = "비"; break;
                case 2: this.pty = "비/눈"; break;
                case 3: this.pty = "눈"; break;
                case 4: this.pty = "소나기"; break;
            }
        }
        else if (category.equals(Category.PCP.getValue())) {
            this.pcp = fcstValue;
        }
        else if (category.equals(Category.REH.getValue())) {
            this.reh = fcstValue + " %";
        }
        else if (category.equals(Category.SNO.getValue())) {
            this.sno = fcstValue ;
        }
        else if (category.equals(Category.SKY.getValue())) {
            int skyCode = Integer.parseInt(fcstValue);
            switch (skyCode) {
                case 1: this.sky = "맑음"; break;
                case 3: this.sky = "구름 많음"; break;
                case 4: this.sky = "흐림"; break;
            }
        }
        else if (category.equals(Category.TMP.getValue())) {
            this.tmp = fcstValue + " ℃";
        }
        else if (category.equals(Category.WSD.getValue())) {
            this.wsd = fcstValue + " m/s";
        }
    }
}

enum Category {
    POP("POP"), PTY("PTY"), PCP("PCP"), REH("REH"), SNO("SNO"),
    SKY("SKY"), TMP("TMP"), TMN("TMN"), TMX("TMX"), WSD("WSD");

    private final String value;
    Category(String str) {
        value = str;
    }

    public String getValue() {
        return value;
    }
}

enum Day {
    TODAY(0), TOMM(1), DAT(2), DADAT(3);

    private final int value;
    Day(int value) { this.value = value; }

    public int getValue() { return value; }
}