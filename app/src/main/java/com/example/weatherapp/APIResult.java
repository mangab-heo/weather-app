package com.example.weatherapp;

public interface APIResult<T> {
    T toAppData() throws Exception;
}