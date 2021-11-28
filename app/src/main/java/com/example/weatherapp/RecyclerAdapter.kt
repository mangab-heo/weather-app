package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.RecyclerAdapter.RecyItemViewHolder
import com.example.weatherapp.data.WeatherData.WeatherHour
import com.example.weatherapp.databinding.RecyclerItemBinding

class RecyclerAdapter internal constructor(private val itemList: List<WeatherHour>) : RecyclerView.Adapter<RecyItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyItemViewHolder {
        return RecyItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyItemViewHolder, position: Int) {
        val curItem = itemList[position]
        holder.bind(curItem)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class RecyItemViewHolder(var binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WeatherHour) {
            binding.item = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerItemBinding.inflate(layoutInflater, parent, false)

                return RecyItemViewHolder(binding)
            }
        }
    }
}