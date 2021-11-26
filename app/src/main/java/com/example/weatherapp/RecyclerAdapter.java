package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyItemViewHolder> {
    private List<WeatherData.WeatherHour> itemList;

    RecyclerAdapter(List<WeatherData.WeatherHour> weatherHours) {
        this.itemList = weatherHours;
    }

    @NonNull
    @NotNull
    @Override
    public RecyItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.recycler_item, parent, false);
        return new RecyItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerAdapter.RecyItemViewHolder holder, int position) {
        WeatherData.WeatherHour curItem = itemList.get(position);

        if (curItem.pty.equals("없음")) {
            switch (curItem.sky) {
                case "맑음": holder.weatherImageView.setImageResource(R.drawable.sunny);break;
                case "구름 많음": holder.weatherImageView.setImageResource(R.drawable.cloudy);break;
                case "흐림": holder.weatherImageView.setImageResource(R.drawable.shadowy);break;
            }
        }
        else if (curItem.pty.equals("눈")) holder.weatherImageView.setImageResource(R.drawable.snowy);
        else holder.weatherImageView.setImageResource(R.drawable.rainy);

        holder.tmpView.setText(curItem.tmp);
        holder.skyView.setText(curItem.sky);
        holder.popView.setText(curItem.pop);
        holder.wsdView.setText(curItem.wsd);
        holder.ptyView.setText(curItem.pty);
        holder.pcpView.setText(curItem.pcp);
        holder.snoView.setText(curItem.sno);
        holder.rehView.setText(curItem.reh);
        holder.fcstTimeView.setText(curItem.fcstTime);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class RecyItemViewHolder extends RecyclerView.ViewHolder {
        ImageView weatherImageView;
        TextView fcstTimeView;
        TextView popView;
        TextView ptyView;
        TextView pcpView;
        TextView snoView;
        TextView skyView;
        TextView tmpView;
        TextView wsdView;
        TextView rehView;

        public RecyItemViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            weatherImageView = itemView.findViewById(R.id.weather_image_view);
            fcstTimeView = itemView.findViewById(R.id.fcstTimeView);
            popView = itemView.findViewById(R.id.popView);
            ptyView = itemView.findViewById(R.id.ptyView);
            pcpView = itemView.findViewById(R.id.pcpView);
            snoView = itemView.findViewById(R.id.snoView);
            skyView = itemView.findViewById(R.id.skyView);
            tmpView = itemView.findViewById(R.id.tmpView);
            wsdView = itemView.findViewById(R.id.wsdView);
            rehView = itemView.findViewById(R.id.rehView);
        }
    }
}