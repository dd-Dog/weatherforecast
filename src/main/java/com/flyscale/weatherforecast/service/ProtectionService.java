package com.flyscale.weatherforecast.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.NetworkUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by MrBian on 2017/11/24.
 */

public class ProtectionService extends Service {
    private static final String TAG = "ProtectionService";
    private static final int MSG_DO_SOMETHING = 1;
    private static final int DELAYTED_DO_SOMETHING = 1000 * 5;
    private static final long MAX_TRAFFIC = 20 * 1024 * 1024;
    private ActivityManager mAm;
    private int myUid;
    private long usedTraffics;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        myUid = Process.myUid();
        boolean checkMonth = checkMonth();//检查月分
        if (checkMonth) {
            usedTraffics = PreferenceUtil.getLong(this, Constants.TRAFFIC_TOTAL, 0);
            if (usedTraffics < MAX_TRAFFIC) {
                doSomething();
            }
        }else {
            doSomething();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 检查是否是当前月
     * @return true 是当前月，false 不是当前月
     */
    private boolean checkMonth() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
        int savedMonth = PreferenceUtil.getInt(this, Constants.CURRENT_MONTH, -1);
        if (currentMonth != savedMonth) {
            PreferenceUtil.put(this, Constants.TRAFFIC_TOTAL, 0);
            return false;
        }
        return true;
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            doSomething();
        }
    };

    private void doSomething() {
        mHandler.sendEmptyMessageDelayed(MSG_DO_SOMETHING, DELAYTED_DO_SOMETHING);
        getWeather("北京市");
        long gprsTraficsByUid = NetworkUtil.getGPRSTraficsByUid(myUid);
        Log.e(TAG, "gprsTraficsByUid=" + gprsTraficsByUid);
        if (gprsTraficsByUid + usedTraffics >= MAX_TRAFFIC) {
            mHandler.removeMessages(MSG_DO_SOMETHING);
        }
    }

    private void getWeather(String city) {
        try {
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
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
