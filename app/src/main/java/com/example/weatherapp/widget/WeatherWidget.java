package com.example.weatherapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.weatherapp.R;
import com.example.weatherapp.data.WeatherData;

import java.util.List;

public class WeatherWidget extends AppWidgetProvider {
    static String ACTION_REFRESH_WIDGET = "refresh_widget";

    static List<WeatherData.WeatherHour> weatherHours;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
//        new Thread(() -> {
//            try {
//                WeatherGrid.LatXLngY gridLocation = LocationUtil.getGridLocation(context);
//                List<Observable<?>> requests = getObservables(gridLocation);
//                Observable.zip(requests, parsedData -> {
//                    List<WeatherData> weatherDataList = new ArrayList<>();
//                    for (Object data : parsedData) {
//                        weatherDataList.add((WeatherData) data);
//                    }
//                    return WeatherData.combineWeatherData(weatherDataList);
//                })
//                        .subscribe(weatherData -> {
//                            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
//
//                            setWeatherHours(weatherData);
//
//                            setTextViewsText(weatherData, views);
//
//                            setRefreshIntent(context, views);
//
//                            setRemoteViewsServiceIntent(views, context, appWidgetId);
//
//                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);
//
//                            appWidgetManager.updateAppWidget(appWidgetId, views);
//                        }, throwable -> new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context.getApplicationContext(), "날씨 정보를 불러오지 못했습니다", Toast.LENGTH_LONG).show()));
//            } catch (Exception exception) {
//                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show());
//            }
//        }).start();
    }
//
//    private static void setWeatherHours(WeatherData weatherData) {
//        int startIdx = weatherData.findStartIdx();
//        weatherHours = weatherData.getWeatherHours().subList(startIdx, weatherData.getWeatherHours().size());
//    }

    private static void setRemoteViewsServiceIntent(RemoteViews views, Context context, int appWidgetId) {
        Intent serviceIntent = new Intent(context, WeatherRemoteViewsService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list_view, serviceIntent);
    }

    private static void setRefreshIntent(Context context, RemoteViews views) {
        Intent refreshIntent = new Intent(context, WeatherWidget.class);
        refreshIntent.setAction(ACTION_REFRESH_WIDGET);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.refresh_widget, pendingIntent);
    }

//    private static void setTextViewsText(WeatherData weatherData, RemoteViews views) {
//        views.setTextViewText(R.id.tmpViewWidget, weatherData.curTmp);
//
//        String wsd = weatherHours.get(0).wsd;
//        views.setTextViewText(R.id.wsdViewWidget, wsd);
//
//        SimpleDateFormat sbf = new SimpleDateFormat("HH:mm", Locale.KOREA);
//        String curTime = sbf.format(new Date(System.currentTimeMillis()));
//        views.setTextViewText(R.id.fcstTimeViewWidget, curTime);
//
//        if (weatherData.curPty.equals("없음")) {
//            switch (weatherData.curSky) {
//                case "맑음": views.setImageViewResource(R.id.weatherImageWidget, R.drawable.sunny);break;
//                case "구름 많음": views.setImageViewResource(R.id.weatherImageWidget, R.drawable.cloudy);break;
//                case "흐림": views.setImageViewResource(R.id.weatherImageWidget, R.drawable.shadowy);break;
//            }
//        } else if (weatherData.curPty.equals("눈"))
//            views.setImageViewResource(R.id.weatherImageWidget, R.drawable.snowy);
//        else views.setImageViewResource(R.id.weatherImageWidget, R.drawable.rainy);
//    }

//    @NotNull
//    private static List<Observable<?>> getObservables(WeatherGrid.LatXLngY gridLocation) {
//        int x = (int) gridLocation.x;
//        int y = (int) gridLocation.y;
//
//        String[] baseDateTimeVil = WeatherData.getBaseDateTime(0);
//        String[] baseDateTimeSrt = WeatherData.getBaseDateTime(1);
//
//        List<Observable<?>> requests = new ArrayList<>();
//
//        Observable<WeatherData> getVillageFcst = RetrofitClient.getFcstService().getVilageFcst(
//                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
//                baseDateTimeVil[0], baseDateTimeVil[1], x, y)
//                .map(FcstResult::toAppData);
//        requests.add(getVillageFcst);
//
//        Observable<WeatherData> getUltraSrtFcst = RetrofitClient.getFcstService().getUltraSrtFcst(
//                FcstClientConstants.SERVICE_KEY, FcstClientConstants.PAGE_NO, FcstClientConstants.NUM_OF_ROWS, FcstClientConstants.DATA_TYPE,
//                baseDateTimeSrt[0], baseDateTimeSrt[1], x, y)
//                .map(FcstResult::toAppData);
//        requests.add(getUltraSrtFcst);
//        return requests;
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH_WIDGET.equals(intent.getAction())) {
            Toast.makeText(context, "날씨 정보를 불러옵니다.", Toast.LENGTH_LONG).show();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidget.class));
            for (int widgetId : widgetIds)
                updateAppWidget(context, appWidgetManager, widgetId);
        }
        else super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(android.content.Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onEnabled(android.content.Context context) { }

    @Override
    public void onDisabled(android.content.Context context) { }
}