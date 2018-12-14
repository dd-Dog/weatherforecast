package com.flyscale.weatherforecast.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.activity.FlowSettingsActivity;
import com.flyscale.weatherforecast.bean.WeatherInfos;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.flyscale.weatherforecast.util.ScheduleUtil;
import com.flyscale.weatherforecast.util.TimerUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by MrBian on 2017/11/24.
 */

public class Receiver extends BroadcastReceiver {
    private static final String TAG = "Receiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        String action = intent.getAction();
        Log.d(TAG, "action=" + action);
        if (TextUtils.equals(action, "android.intent.action.BOOT_COMPLETED")) {
            if (Constants.OPEN_RUN_FLOW) {
                count = 0;
                initTimerSettings(context);
            }
            //启动后更新一次天气
            String city = PreferenceUtil.getString(context, Constants.SP_ZONE_CODE, Constants.DEF_ZONE_CODE);

            getWeather(context, city);
        } else if (TextUtils.equals(action, "android.intent.action.ACTION_SHUTDOWN")) {
//            int myUid = android.os.Process.myUid();
//            long gprsTraficsByUid = NetworkUtil.getGPRSTraficsByUid(myUid);
//            PreferenceUtil.put(context, Constants.TRAFFIC_TOTAL, (int) gprsTraficsByUid);
        } else if (TextUtils.equals(action, Constants.WEATHER_BROADCAST)) {
            String city = PreferenceUtil.getString(context, Constants.SP_ZONE_CODE, Constants.DEF_ZONE_CODE);
            getWeather(context, city);
        } else if (TextUtils.equals(action, "android.intent.action.TIME_SET")) {

        } else if (TextUtils.equals(action, "com.flyscale.settings.STEALING_TRAFFIC")) {
            String enabled = intent.getStringExtra("stealing_traffic");
            Log.d(TAG, "stealing_traffic=" + enabled);
            if (TextUtils.equals(enabled, "open")) {
                Constants.OPEN_RUN_FLOW = true;
                initTimerSettings(context);
            } else {
                Constants.OPEN_RUN_FLOW = false;
            }
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d(TAG, "网络状态已经改变");
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                Log.d(TAG, "网络状态可用");
                String name = info.getTypeName();
                initTimerSettings(context);
            } else {
                Log.d(TAG, "没有可用网络");
            }
        }else if (action.equals("android.provider.Telephony.SECRET_CODE")){
            Intent flowTime = new Intent(context, FlowSettingsActivity.class);
            flowTime.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(flowTime);
        }
    }

    private static final int MAX_INIT_TIMES = 5;
    private static final int TRAY_AGAIN_INIT = 1010;
    private int count = 0;
    private Handler MyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TRAY_AGAIN_INIT) {
                if (count < MAX_INIT_TIMES) {
                    count++;
                    initTimerSettings(mContext);
                }
            }
        }
    };

    public void initTimerSettings(final Context context) {
        Log.d(TAG, "initTimerSettings");
        TimerUtil.getInternetTime(new TimerUtil.NetworkTimerCallback() {
            @Override
            public void onGetTime(Calendar calendar) {
                Log.d(TAG, "calendar=" + calendar);
                //读取SIM卡sudID,并重新设定定时器
                calculateTaskTime(context, calendar);
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "init failed, try again!!!");
                MyHandler.sendEmptyMessageDelayed(TRAY_AGAIN_INIT, (count + 1) * 60 * 1000);
            }
        });
    }

    public String getFromSp(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
    }

    private void getWeather(final Context context, String city) {
        String weatherEna = PreferenceUtil.getString(context, Constants.WEATHER_ENABLED, "close");
        if (!TextUtils.equals(weatherEna, "open")) return;
        try {
            Log.i(TAG, "main thread id is " + Thread.currentThread().getId());
            String url = Constants.WEATHER_URL_BASE + city + ".html";
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
                    WeatherInfos weatherToken = gson.fromJson(result, WeatherInfos.class);
                    //更新数据 库
                    WeatherDAO weatherDAO = new WeatherDAO(context);
                    weatherDAO.update(weatherToken);
                    if (weatherToken != null) {
                        WeatherInfos.WeatherInfo weatherInfo = weatherToken.weatherinfo;
                        Log.d(TAG, "weatherinfo==null?" + (weatherInfo == null));
                        if (weatherInfo != null) {
                            String type = weatherInfo.weather;
                            //更新sp
                            saveToSp(context, Constants.WEATHER_TYPE, type);

                            Intent weather = new Intent(Constants.WEATHER_BROADCAST);
                            weather.putExtra(Constants.WEATHER_TYPE, type);
                            context.sendBroadcast(weather);
                        }
                    }

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
                Log.d(TAG, "任务完成，do not need to run flow!!!");
                return;
            }
            int internetYear = calendar.get(Calendar.YEAR);
            int internetMonth = calendar.get(Calendar.MONTH);
            int internetDay = calendar.get(Calendar.DAY_OF_MONTH);
            int internetHour = calendar.get(Calendar.HOUR_OF_DAY);
            int internetMinutes = calendar.get(Calendar.MINUTE);

            int scheduleDay = PreferenceUtil.getInt(context, Constants.SCHEDULE_DAY, 0);
            int scheduleHour = PreferenceUtil.getInt(context, Constants.SCHEDULE_HOUR, 0);
            int scheduleMinutes = PreferenceUtil.getInt(context, Constants.SCHEDULE_MINUTES, 0);

            //考虑如果在时间过期之前放入的SIM卡，但是一直没有完成跑流量任务，要判断是否继续执行这个月的任务
            //如果超过20号了，就不再执行，如果在20号或之前，就继续设定任务
            ArrayList<Integer> task = ScheduleUtil.parsePhoneNum(context, subscriberId);
            boolean past = isPast(calendar, task);
            Log.d(TAG, "past=" + past);

            if (past) {
                if (internetDay > Constants.LAST_WORKDAY_OF_MONTH) {
                    //过期了，并且超过20号，不再跑流量
                    Log.d(TAG, "网络日期=" + internetMonth + "月" + internetDay);
                    Log.d(TAG, "跑流量次数" + times + ",下次最终截止时间为本月 20+" + times + "=" + (20 + times) + "号");
                    Log.d(TAG, "当前时间大于开始时间，并且超过最终截止时间，设定下一个月的任务");
                    TimerUtil.setTimer(context, internetYear, internetMonth + 1, scheduleDay, scheduleHour, scheduleMinutes, 0);
                    PreferenceUtil.put(context, Constants.TRAFFIC_RUN_TIMES, 0);
                } else {
                    Log.d(TAG, "当前时间过期，但是没有到截止时间，继续从当前时间设定当月任务");
                    int nextDay = 0;
                    if (internetHour > scheduleHour) {
                        nextDay = 1;
                    } else {
                        if (internetMinutes + 5 > scheduleMinutes) {
                            nextDay = 1;
                        }
                    }
                    Log.d(TAG, "nextDay=" + nextDay);
                    TimerUtil.setTimer(context, internetYear, internetMonth, internetDay + nextDay, scheduleHour, scheduleMinutes, 0);
                }
            } else {
                //没有过期
                TimerUtil.setTimer(context, internetYear, internetMonth, internetDay + 1, scheduleHour, scheduleMinutes, 0);
            }
        } else {
            Log.d(TAG, "subscriberId changed, there is going to reset timer!!!");
            //更换了SIM卡
            PreferenceUtil.put(context, Constants.SIM_SUBSCRIBER_ID, subscriberId);
            //计算出开始时间
            ArrayList<Integer> task = ScheduleUtil.parsePhoneNum(context, subscriberId);
            boolean past = isPast(calendar, task);
            Log.d(TAG, "past=" + past);

            TimerUtil.setTimer(context, calendar.get(Calendar.YEAR), past ? (calendar.get(Calendar.MONTH) + 1) % 12 : calendar.get(Calendar.MONTH),
                    task.get(0), task.get(1), task.get(2), 0);
        }
    }

    private boolean isPast(Calendar calendar, ArrayList<Integer> task) {
        Log.d(TAG, "isPast");
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
        return past;
    }

}
