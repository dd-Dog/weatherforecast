package com.flyscale.weatherforecast.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flyscale.weatherforecast.service.TrafficService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by bian on 2018/9/12.
 */

public class TimerUtil {
    private static final String TAG = "TimerUtil";

    public static void setTimer(Context context, int year, int month, int day, int hour, int minute, int second) {
        Log.d(TAG, "setTimer,year=" + year + ",month=" + month + ",day=" + day + ",hour=" + hour + ",minute=" + minute + ",second=" + second);
        if (month < 0 || month > 11 || day < 0 || day > 31 || hour < 0 || hour > 24 || minute < 0 || minute > 60 || second < 0 || second > 60) {
            Log.d(TAG, "invalid params!!!");
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        long taskTimeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "calendar=" + calendar);
        Log.d(TAG, "taskTimeInMillis=" + taskTimeInMillis);
        Intent intent = new Intent(context, TrafficService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1003, intent, 0);
        assert am != null;
        am.set(AlarmManager.RTC_WAKEUP, taskTimeInMillis, pendingIntent);
    }

    public static void getInternetTime(final NetworkTimerCallback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                URL url = null;//取得资源对象
                Date date = null;
                Calendar calendar = null;
                try {
                    url = new URL("http://www.baidu.com");
                    URLConnection uc = url.openConnection();//生成连接对象
                    uc.connect(); //发出连接
                    long ld = uc.getDate(); //取得网站日期时间
                    date = new Date(ld); //转换为标准时间对象
                    calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    //分别取得时间中的小时，分钟和秒，并输出
                    Log.d(TAG, "internet calender=" + calendar);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (callback != null)
                        callback.onGetTime(calendar);
                }
            }
        }.start();
    }

    public interface NetworkTimerCallback {
        void onGetTime(Calendar calendar);
    }
}
