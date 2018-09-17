package com.flyscale.weatherforecast.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by bian on 2018/9/3.
 */

public class UpdateWeatherService extends IntentService {

    private static final String TAG = "UpdateWeatherService";

    public UpdateWeatherService() {
        super("UpdateWeatherService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateWeatherService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        String city = PreferenceUtil.getString(this, Constants.SP_CITY, Constants.DEF_CITY);
        getWeather(this, city);
    }


    private void getWeather(final Context context, String city) {
        try {
            Log.i(TAG, "main thread id is " + Thread.currentThread().getId());
            String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + city;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws
                        IOException {
                    String result = response.body().string();
                    Log.i(TAG, result);
                    Gson gson = new Gson();
                    WeatherToken weatherToken = gson.fromJson(result, WeatherToken.class);
                    //更新数据 库
                    WeatherDAO weatherDAO = new WeatherDAO(context);
                    weatherDAO.update(weatherToken);

                    String type = weatherToken.data.forecast.get(0).type;
                    //更新sp
                    saveToSp(context, Constants.WEATHER_TYPE, type);

                    Intent weather = new Intent(Constants.WEATHER_BROADCAST);
                    weather.putExtra(Constants.WEATHER_TYPE, type);
                    context.sendBroadcast(weather);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToSp(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
