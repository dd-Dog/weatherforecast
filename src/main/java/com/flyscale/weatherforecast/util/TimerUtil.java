package com.flyscale.weatherforecast.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.service.TrafficService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
        am.setExact(AlarmManager.RTC_WAKEUP, taskTimeInMillis, pendingIntent);

        PreferenceUtil.put(context, Constants.NEXT_ALARM_SCHEDULE_YEAR, year);
        PreferenceUtil.put(context, Constants.NEXT_ALARM_SCHEDULE_MONTH, month);
        PreferenceUtil.put(context, Constants.NEXT_ALARM_SCHEDULE_DAY, day);
        PreferenceUtil.put(context, Constants.NEXT_ALARM_SCHEDULE_HOUR, hour);
        PreferenceUtil.put(context, Constants.NEXT_ALARM_SCHEDULE_MINUTE, minute);


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

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFialed();
                } finally {
                    if (callback != null)
                        callback.onGetTime(calendar);
                }
            }
        }.start();
    }

    public interface NetworkTimerCallback {
        void onGetTime(Calendar calendar);
        void onFialed();
    }

    public static void initTimerSettings(final Context context) {
        TimerUtil.getInternetTime(new TimerUtil.NetworkTimerCallback() {
            @Override
            public void onGetTime(Calendar calendar) {
                Log.d(TAG, "calendar=" + calendar);
                //读取SIM卡sudID,并重新设定定时器
                calculateTaskTime(context, calendar);
            }

            @Override
            public void onFialed() {

            }
        });
    }
    @SuppressLint("HardwareIds")
    private static void calculateTaskTime(Context context, Calendar calendar) {
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
                if (internetDay > 20 + times) {
                    //过期了，并且超过20号，不再跑流量
                    Log.d(TAG, "网络日期=" + internetMonth + "月" + internetDay);
                    Log.d(TAG, "跑流量次数" + times + ",下次最终截止时间为本月 20+" + times + "=" + (20 + times) + "号");
                    Log.d(TAG, "当前时间大于开始时间，并且超过最终截止时间，设定下一个月的任务");
                    TimerUtil.setTimer(context, internetYear, internetMonth + 1, scheduleDay, scheduleHour, scheduleMinutes, 0);
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
                TimerUtil.setTimer(context, internetYear, internetMonth, scheduleDay + times, scheduleHour, scheduleMinutes, 0);
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

    private static boolean isPast(Calendar calendar, ArrayList<Integer> task) {
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
