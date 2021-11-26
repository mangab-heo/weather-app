package com.example.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.weatherapp.databinding.ActivityMainBinding
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import java.util.*

class MainActivity : RxAppCompatActivity() {

    private val mainViewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.setLifecycleOwner { this.lifecycle }
        binding.mainViewModel = mainViewModel

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
        if (LocationUtil.isLocationPermissionGranted(this@MainActivity)) {
            try {
                val gridLocation = LocationUtil.getGridLocation(applicationContext)
                val stationName = LocationUtil.getClosestStation(applicationContext, gridLocation)
                mainViewModel.refreshWeatherData(gridLocation, stationName)
            } catch (exception: Exception) {
                Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@MainActivity, "권한을 획득했습니다.", Toast.LENGTH_LONG).show()
            }
            drawMainView()
        }
    }
}