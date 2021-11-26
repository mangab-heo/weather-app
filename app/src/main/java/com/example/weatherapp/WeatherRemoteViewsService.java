package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

public class WeatherRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WeatherRemoteViewsFactory(this.getApplicationContext());
    }
}

class WeatherRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public Context context;
    public List<WeatherData.WeatherHour> itemList;

    WeatherRemoteViewsFactory(Context context) { this.context = context; }

    @Override
    public void onCreate() { }

    @Override
    public void onDataSetChanged() {
        itemList = WeatherWidget.weatherHours;
    }

    @Override
    public void onDestroy() { }

    @Override
    public int getCount() { return itemList.size(); }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.recycler_item);
        WeatherData.WeatherHour curItem = itemList.get(position);

        if ("없음".equals(curItem.pty)) {
            switch (curItem.sky) {
                case "맑음": remoteViews.setImageViewResource(R.id.weather_image_view, R.drawable.sunny);break;
                case "구름 많음": remoteViews.setImageViewResource(R.id.weather_image_view, R.drawable.cloudy);break;
                case "흐림": remoteViews.setImageViewResource(R.id.weather_image_view, R.drawable.shadowy);break;
            }
        }
        else if (curItem.pty.equals("눈")) remoteViews.setImageViewResource(R.id.weather_image_view, R.drawable.snowy);
        else remoteViews.setImageViewResource(R.id.weather_image_view, R.drawable.rainy);

        remoteViews.setTextViewText(R.id.tmpViewWidget, curItem.tmp);
        remoteViews.setTextViewText(R.id.skyView, curItem.sky);
        remoteViews.setTextViewText(R.id.popView, curItem.pop);
        remoteViews.setTextViewText(R.id.wsdView, curItem.wsd);
        remoteViews.setTextViewText(R.id.ptyView, curItem.pty);
        remoteViews.setTextViewText(R.id.pcpView, curItem.pcp);
        remoteViews.setTextViewText(R.id.snoView, curItem.sno);
        remoteViews.setTextViewText(R.id.rehView, curItem.reh);
        remoteViews.setTextViewText(R.id.fcstTimeView, curItem.fcstTime);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
