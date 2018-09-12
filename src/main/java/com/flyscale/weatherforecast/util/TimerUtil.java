package com.flyscale.weatherforecast.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flyscale.weatherforecast.service.TrafficService;

import java.util.Calendar;

/**
 * Created by bian on 2018/9/12.
 */

public class TimerUtil {
    private static final String TAG = "TimerUtil";

    public static void setTimer(Context context, int month, int day, int hour, int minute, int second) {
        Log.d(TAG, "setTimer,month=" + month + ",day=" + day + ",hour=" + hour + ",minute=" + minute + ",second=" + second);
        if (month < 0 || month > 11 || day < 0 || day > 31 || hour < 0 || hour > 24 || minute < 0 || minute > 60 || second < 0 || second > 60) {
            Log.d(TAG, "invalid params!!!");
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long timeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "timeInMillis=" + timeInMillis);
        int year = calendar.get(Calendar.YEAR);
        calendar.set(year, month, day, hour, minute, second);
        long taskTimeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "calendar=" + calendar);
        Log.d(TAG, "taskTimeInMillis=" + taskTimeInMillis);
        Intent intent = new Intent(context, TrafficService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1003, intent, 0);
        assert am != null;
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, taskTimeInMillis, pendingIntent);
    }
}
