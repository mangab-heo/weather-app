package com.example.weatherapp

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("app:item")
fun setItems(recyclerView: RecyclerView, item: WeatherData?) {
    item?.let {
        val recyclerAdapter = RecyclerAdapter(item.weatherHours.subList(item.findStartIdx(), item.weatherHours.size))

        recyclerView.adapter = recyclerAdapter

        recyclerAdapter.notifyDataSetChanged()

    }
}
@BindingAdapter("app:customSrc")
fun setImageSrc(imageView: ImageView, item: WeatherData.WeatherHour) {
    val resId = when (item.pty) {
        "없음" -> {
            when (item.sky) {
                "맑음" -> R.drawable.sunny
                "구름 많음" -> R.drawable.cloudy
                "흐림" -> R.drawable.shadowy
                else -> R.drawable.sunny
            }
        }
        "눈" -> R.drawable.snowy
        else -> R.drawable.rainy
    }
    imageView.setImageResource(resId)
}
