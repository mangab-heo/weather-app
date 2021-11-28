package com.example.weatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.weatherapp.data.PmData
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.source.DefaultRepository
import com.example.weatherapp.data.source.Repository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class MainViewModel : ViewModel() {
    private val repository: Repository = DefaultRepository()

    val weatherData: LiveData<WeatherData> by lazy { _weatherData }
    private val _weatherData: MutableLiveData<WeatherData> = MutableLiveData()

    val pmData: LiveData<PmData> by lazy { _pmData }
    private val _pmData: MutableLiveData<PmData> = MutableLiveData()

    val weatherText: LiveData<String> = Transformations.map(_weatherData) {
        if (it.curPty == "없음") it.curSky else it.curPty
    }

    fun refreshWeatherData(gridLocation: WeatherGrid.LatXLngY, stationName: String) {
        try {
            // loading flag live data
            repository.getObservablePmData(stationName).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe{
                _pmData.value = it
            }
            repository.getObservableWeatherData(gridLocation).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe{
                _weatherData.value = it
            }
        } catch (exception: Exception) {
            // to set exception flag live data  false
        }
    }
}