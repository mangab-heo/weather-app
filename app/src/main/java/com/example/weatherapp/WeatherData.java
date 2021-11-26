package com.example.weatherapp;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherData {
    private final List<WeatherHour> weatherHours = new ArrayList<>();

    public String curPty = "Missing";
    public String curTmp = "Missing";
    public String curSky = "Missing";

    double[] tmx = new double[] { -1, -1, -1, -1 };
    double[] tmn = new double[] { -1, -1, -1, -1 };

    List<WeatherHour> getWeatherHours() { return this.weatherHours; }

    void addWeatherHour(WeatherHour weatherHour) { weatherHours.add(weatherHour); }

    int findStartIdx() {
        SimpleDateFormat sbf = new SimpleDateFormat("yyyyMMdd HH00", Locale.KOREA);
        String[] curDateTime = sbf.format(new Date(System.currentTimeMillis())).split(" ");
        int curDateInt = Integer.parseInt(curDateTime[0]);
        int curTimeInt = Integer.parseInt(curDateTime[1]);

        int surviveIdx = 0;
        for (int i = 0; i < weatherHours.size(); i++) {
            WeatherHour weatherHour = weatherHours.get(i);
            int fcstDateInt = Integer.parseInt(weatherHour.fcstDate);
            int fcstTimeInt = Integer.parseInt(weatherHour.fcstTime.substring(0, 2)
                    + weatherHour.fcstTime.substring(3));

            if (fcstDateInt >= curDateInt && fcstTimeInt > curTimeInt) {
                surviveIdx = i;
                break;
            }
        }
        return surviveIdx;
    }

    static WeatherData combineWeatherData(List<WeatherData> list) {
        if (list.size() == 1) {
            WeatherData weatherDataVillage = list.get(0);
            List<WeatherHour> villageHours = weatherDataVillage.getWeatherHours();
            int villageIdx = weatherDataVillage.findStartIdx();

            weatherDataVillage.curPty = villageHours.get(villageIdx).pty;
            weatherDataVillage.curTmp = villageHours.get(villageIdx).tmp;
            weatherDataVillage.curSky = villageHours.get(villageIdx).sky;

            return weatherDataVillage;
        }
        else {
            WeatherData weatherDataVillage;
            WeatherData weatherDataSrt;
            if (list.get(0).getWeatherHours().size() >= list.get(1).getWeatherHours().size()) {
                weatherDataVillage = list.get(0);
                weatherDataSrt = list.get(1);
            }
            else {
                weatherDataVillage = list.get(1);
                weatherDataSrt = list.get(0);
            }

            List<WeatherHour> villageHours = weatherDataVillage.getWeatherHours();
            int villageIdx = weatherDataVillage.findStartIdx();

            List<WeatherHour> srtHours = weatherDataSrt.getWeatherHours();
            int srtIdx = weatherDataSrt.findStartIdx();

            weatherDataVillage.curPty = srtHours.get(0).pty;
            weatherDataVillage.curTmp = srtHours.get(0).tmp;
            weatherDataVillage.curSky = srtHours.get(0).sky;

            for (; srtIdx < srtHours.size(); srtIdx++, villageIdx++) {
                WeatherHour srtHour = srtHours.get(srtIdx);
                WeatherHour villageHour = villageHours.get(villageIdx);
                villageHour.tmp = srtHour.tmp;
                villageHour.pcp = srtHour.pcp;
                villageHour.sky = srtHour.sky;
                villageHour.reh = srtHour.reh;
                villageHour.pty = srtHour.pty;
                villageHour.wsd = srtHour.wsd;
            }

            return weatherDataVillage;
        }
    }

    @NotNull
    public static String[] getBaseDateTime(int fcstConstantsIdx) {
        final int[][] fcstConstants = {{300, 210, 2300, 100, 200}, {100, 45, 2330, 70, 30}};

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        String curDateTimeStr = sdf.format(new Date(System.currentTimeMillis()));
        String[] curDateTime = curDateTimeStr.split(" ");

        String[] baseDateTime = new String[2];
        baseDateTime[0] = curDateTime[0];

        int quotient = Integer.parseInt(curDateTime[1]) / fcstConstants[fcstConstantsIdx][0];
        int remain = Integer.parseInt(curDateTime[1]) % fcstConstants[fcstConstantsIdx][0];

        if (quotient == 0 && remain <= fcstConstants[fcstConstantsIdx][1]) {
            String[] yesterDateTime = sdf.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))
                    .split(" ");
            baseDateTime[0] = yesterDateTime[0];
            baseDateTime[1] = String.valueOf(fcstConstants[fcstConstantsIdx][2]);
        } else {
            if (remain <= fcstConstants[fcstConstantsIdx][1]) {
                baseDateTime[1] = String.valueOf(Integer.parseInt(curDateTime[1]) - remain - fcstConstants[fcstConstantsIdx][3]);
            } else {
                baseDateTime[1] = String.valueOf(Integer.parseInt(curDateTime[1]) - remain + fcstConstants[fcstConstantsIdx][4]);
            }
        }

        if (baseDateTime[1].length() < 4) {
            int zeroCnts = 4 - baseDateTime[1].length();
            StringBuilder zeros = new StringBuilder();
            while (zeroCnts > 0) {
                zeroCnts -= 1;
                zeros.append("0");
            }
            baseDateTime[1] = zeros + baseDateTime[1];
        }
        return baseDateTime;
    }

    public static class WeatherHour {
        public String fcstDate = "Missing";
        public String fcstTime = "Missing";
        public String pop = "Missing";
        public String pty = "Missing"; // 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
        public String pcp = "Missing";
        public String reh = "Missing";
        public String sno = "Missing";
        public String sky = "Missing"; // 맑음(1), 구름 많음(3), 흐림(4)
        public String tmp = "Missing";
        public String wsd = "Missing";

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
            else if (category.equals(Category.PCP.getValue())
                    || category.equals(Category.RN1.getValue())) {
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
            else if (category.equals(Category.TMP.getValue())
                    || category.equals(Category.T1H.getValue()) ) {
                this.tmp = fcstValue + " ℃";
            }
            else if (category.equals(Category.WSD.getValue())) {
                this.wsd = fcstValue + " m/s";
            }
        }
    }
}

enum Category {
    POP("POP"), PTY("PTY"), PCP("PCP"), REH("REH"), SNO("SNO"),
    SKY("SKY"), TMP("TMP"), TMN("TMN"), TMX("TMX"), WSD("WSD"),
    T1H("T1H"), RN1("RN1");

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