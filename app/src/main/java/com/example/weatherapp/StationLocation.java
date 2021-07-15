package com.example.weatherapp;

public class StationLocation {
    String name;
    double latitude;
    double longitude;

    public StationLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    double getDistance(double lat1, double lon1) {

        double theta = lon1 - this.longitude;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(this.latitude)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(this.latitude)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1.609344;

        return dist;
    }


    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
