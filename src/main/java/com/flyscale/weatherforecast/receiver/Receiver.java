package com.flyscale.weatherforecast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.NetworkUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by MrBian on 2017/11/24.
 */

public class Receiver extends BroadcastReceiver {
    private static final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "action=" + action);
        if (TextUtils.equals(action, "android.intent.action.BOOT_COMPLETED")) {
            //启动后更新一次天气
            String city = PreferenceUtil.getString(context, Constants.SP_CITY, Constants.DEF_CITY);
            getWeather(context, city);
        } else if (TextUtils.equals(action, "android.intent.action.ACTION_SHUTDOWN")) {
            int myUid = android.os.Process.myUid();
            long gprsTraficsByUid = NetworkUtil.getGPRSTraficsByUid(myUid);
            PreferenceUtil.put(context, Constants.TRAFFIC_TOTAL, (int)gprsTraficsByUid);
        } else if (TextUtils.equals(action, Constants.WEATHER_BROADCAST)) {
            String city = PreferenceUtil.getString(context, Constants.SP_CITY, Constants.DEF_CITY);
            getWeather(context, city);
        } else if (TextUtils.equals(action, "android.intent.action.ACTION_POWER_CONNECTED")) {
        }
    }

    public String getFromSp(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
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
