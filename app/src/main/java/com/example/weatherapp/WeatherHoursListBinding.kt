package com.example.weatherapp

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
