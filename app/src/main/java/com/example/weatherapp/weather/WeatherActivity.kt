package com.example.weatherapp.weather

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityWeatherBinding
import com.example.weatherapp.util.LocationUtil
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import java.util.*

class WeatherActivity : RxAppCompatActivity() {

    private val weatherViewModel : WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityWeatherBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_weather
        )
        binding.setLifecycleOwner { this.lifecycle }
        binding.weatherViewModel = weatherViewModel

        setActionBar()
        setImageButtonListener()
        drawMainView()
    }

    override fun onRestart() {
        super.onRestart()
        drawMainView()
    }

    private fun setImageButtonListener() {
        val imageButton = findViewById<ImageButton>(R.id.gps_button)
        imageButton.setOnClickListener { v: View? ->
            Toast.makeText(applicationContext, "날씨 정보를 불러옵니다.", Toast.LENGTH_LONG).show()
            drawMainView()
        }
    }

    private fun drawMainView() {
        if (LocationUtil.isLocationPermissionGranted(this@WeatherActivity)) {
            try {
                val gridLocation = LocationUtil.getGridLocation(applicationContext)
                val stationName = LocationUtil.getClosestStation(applicationContext, gridLocation)
                weatherViewModel.refreshWeatherData(gridLocation, stationName)
            } catch (exception: Exception) {
                Toast.makeText(this@WeatherActivity, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setActionBar() {
        val actionBar = supportActionBar
        actionBar?.hide()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationUtil.REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@WeatherActivity, "권한을 획득했습니다.", Toast.LENGTH_LONG).show()
            }
            drawMainView()
        }
    }
}