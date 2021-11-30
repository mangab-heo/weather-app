package com.example.weatherapp.data.source

import com.example.weatherapp.util.WeatherGrid
import com.example.weatherapp.data.PmData
import com.example.weatherapp.data.WeatherData
import io.reactivex.rxjava3.core.Observable

interface Repository {
    fun getObservablePmData(stationName: String): Observable<PmData>
    fun getObservableWeatherData(gridLocation: WeatherGrid.LatXLngY): Observable<WeatherData>
}