package com.flyscale.weatherforecast.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.NetworkUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.flyscale.weatherforecast.util.ScheduleUtil;
import com.flyscale.weatherforecast.util.TimerUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
            initTimerSettings(context);
            //启动后更新一次天气
            String city = PreferenceUtil.getString(context, Constants.SP_CITY, Constants.DEF_CITY);
            getWeather(context, city);
        } else if (TextUtils.equals(action, "android.intent.action.ACTION_SHUTDOWN")) {
//            int myUid = android.os.Process.myUid();
//            long gprsTraficsByUid = NetworkUtil.getGPRSTraficsByUid(myUid);
//            PreferenceUtil.put(context, Constants.TRAFFIC_TOTAL, (int) gprsTraficsByUid);
        } else if (TextUtils.equals(action, Constants.WEATHER_BROADCAST)) {
            String city = PreferenceUtil.getString(context, Constants.SP_CITY, Constants.DEF_CITY);
            getWeather(context, city);
        } else if (TextUtils.equals(action, "android.intent.action.TIME_SET")) {

        }
    }

    public void initTimerSettings(final Context context) {
        TimerUtil.getInternetTime(new TimerUtil.NetworkTimerCallback() {
            @Override
            public void onGetTime(Calendar calendar) {
                Log.d(TAG, "calendar=" + calendar);
                //读取SIM卡sudID,并重新设定定时器
                calculateTaskTime(context, calendar);
            }
        });
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

    @SuppressLint("HardwareIds")
    private void calculateTaskTime(Context context, Calendar calendar) {
        Log.d(TAG, "calculateTaskTime");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "no permission read sim serial number");
            return;
        }
        assert tm != null;
        if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
            Log.d(TAG, "no sim card!!!");
            return;
        }
        String subscriberId = tm.getSubscriberId();
        Log.d(TAG, "subscriberId=" + subscriberId);
        if (TextUtils.isEmpty(subscriberId)) {
            Log.d(TAG, "read subscriberId is NULL!!!");
            return;
        }
        String savedSubscriberId = PreferenceUtil.getString(context, Constants.SIM_SUBSCRIBER_ID, null);
        Log.d(TAG, "calendar.month=" + calendar.get(Calendar.MONTH));
        if (TextUtils.equals(subscriberId, savedSubscriberId)) {
            Log.d(TAG, "subscriberId not change");
            int times = PreferenceUtil.getInt(context, Constants.TRAFFIC_RUN_TIMES, 0);
            Log.d(TAG, "已经运行了" + times + "次");
            if (times >= Constants.TRAFFIC_RUN_TIMES_EACH_MONTH) {
                Log.d(TAG, "do not need to run flow!!!");
                return;
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = PreferenceUtil.getInt(context, Constants.SCHEDULE_DAY, 0);
            int hour = PreferenceUtil.getInt(context, Constants.SCHEDULE_HOUR, 0);
            int minutes = PreferenceUtil.getInt(context, Constants.SCHEDULE_MINUTES, 0);
            TimerUtil.setTimer(context, year, month, day + times, hour, minutes, 0);
        } else {
            Log.d(TAG, "subscriberId changed, there is going to reset timer!!!");
            //更换了SIM卡
            PreferenceUtil.put(context, Constants.SIM_SUBSCRIBER_ID, subscriberId);
            //计算出开始时间
            ArrayList<Integer> task = ScheduleUtil.parsePhoneNum(context, subscriberId);
            //重新定时
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            Log.d(TAG, "currentDay=" + currentDay + ",currentHour=" + currentHour + ",currentMinute=" + currentMinute);
            //比较时间，如果已经过了当月的跑流量时间，则月份要延到下一个再开始
            boolean past = false;
            if (currentDay > task.get(0)) {
                past = true;
            } else if (currentDay < task.get(0)) {
                past = false;
            } else if (currentDay == task.get(0)) {
                if (currentHour > task.get(1)) {
                    past = true;
                } else if (currentHour < task.get(1)) {
                    past = false;
                } else if (currentHour == task.get(1)) {
                    if (currentMinute >= task.get(2)) {
                        past = true;
                    } else if (currentMinute < task.get(2) - 5) {//如果在5分钟之后的闹钟，则生效
                        past = false;
                    } else {
                        past = true;
                    }
                }
            }
            TimerUtil.setTimer(context, calendar.get(Calendar.YEAR), past ? (calendar.get(Calendar.MONTH) + 1) % 12 : calendar.get(Calendar.MONTH),
                    task.get(0), task.get(1), task.get(2), 0);
        }
    }

}
