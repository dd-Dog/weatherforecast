package com.flyscale.weatherforecast.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.FTPUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.flyscale.weatherforecast.util.TimerUtil;

import java.util.Calendar;

/**
 * Created by bian on 2018/8/30.
 * <p>
 * 由定时器，在每天的固定时间启动该Service
 * <p>
 * 根据网络时间定时，根据系统时间触发定时器
 */

public class TrafficService extends IntentService {

    public static final String TAG = "TrafficService";
    private static final int CHECK_MONTH_EQUALS = 0;
    private static final int CHECK_MONTH_FORWARD = 1;
    private static final int CHECK_MONTH_BACKWARD = 2;
    private static final int CHECK_MONTH_INVALID = -1;

    public TrafficService() {
        super("TrafficService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TrafficService(String name) {
        super(name);
    }

    /**
     * IntentService会使用单独的线程来执行该方法的代码
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //设定定时器使用的都是网络时间
        TimerUtil.getInternetTime(new TimerUtil.NetworkTimerCallback() {
            @Override
            public void onGetTime(Calendar calendar) {
                int internetYear = calendar.get(Calendar.YEAR);
                int internetMonth = calendar.get(Calendar.MONTH);
                int internetDay = calendar.get(Calendar.DAY_OF_MONTH);
                int internetHour = calendar.get(Calendar.HOUR_OF_DAY);
                int internetMinute = calendar.get(Calendar.MONTH);

                int scheduleMonth = PreferenceUtil.getInt(TrafficService.this, Constants.NEXT_ALARM_SCHEDULE_MONTH, -1);

                int scheduleDay = PreferenceUtil.getInt(TrafficService.this, Constants.SCHEDULE_DAY, 0);
                int scheduleHour = PreferenceUtil.getInt(TrafficService.this, Constants.SCHEDULE_HOUR, 0);
                int scheduleMinutes = PreferenceUtil.getInt(TrafficService.this, Constants.SCHEDULE_MINUTES, 0);

                int times = PreferenceUtil.getInt(TrafficService.this, Constants.TRAFFIC_RUN_TIMES, 0);
                Log.d(TAG, "已经运行了" + times + "次,month=" + internetMonth);
                boolean download = download();
                int newTimes = times + (download ? 1 : 0);
                Log.d(TAG, "newTimes=" + newTimes + "次");
                //下载完成或者失败返回后再进行设置
                if (newTimes == Constants.TRAFFIC_RUN_TIMES_EACH_MONTH) {
                    Log.d(TAG, "本月任务已经完成，设定下个月任务，重置次数记录,任务时间根据网络年月时间进行更新");
                    TimerUtil.setTimer(TrafficService.this, internetYear, (internetMonth + 1) % 12, scheduleDay, scheduleHour, scheduleMinutes, 0);
                    PreferenceUtil.put(TrafficService.this, Constants.TRAFFIC_RUN_TIMES, 0);
                } else {
                    if (internetDay > Constants.LAST_WORKDAY_OF_MONTH) {
                        Log.d(TAG, "超过25号，到了截止时间,设定下个月的任务，重置次数记录,任务时间根据网络年月时间进行更新");
                        //这里月份使用网络时间
                        TimerUtil.setTimer(TrafficService.this, internetYear, (internetMonth + 1) % 12, scheduleDay, scheduleHour, scheduleMinutes, 0);
                        PreferenceUtil.put(TrafficService.this, Constants.TRAFFIC_RUN_TIMES, 0);
                    } else {
                        Log.d(TAG, "本月完成" + newTimes + "次，设定下次任务，日期依据上次日期");
                        //在执行本月2-4次任务时，依据系统年月日时间设定
                        int sysYear = Calendar.getInstance().get(Calendar.YEAR);
                        int sysMonth = Calendar.getInstance().get(Calendar.MONTH);
                        int sysDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        TimerUtil.setTimer(TrafficService.this, sysYear, sysMonth, sysDay + 1, scheduleHour, scheduleMinutes, 0);
                        PreferenceUtil.put(TrafficService.this, Constants.TRAFFIC_RUN_TIMES, newTimes);
                    }
                }

//                if (internetMonth > scheduleMonth) {
//                    Log.d(TAG, "任务计划月份落后当前时间月份，需要重新设定任务时间");
//                    TimerUtil.initTimerSettings(TrafficService.this);
//                }
            }
        });
    }

    private boolean download() {
        return FTPUtil.downLoadFileFromDefServer(this);
    }

    /**
     * 检查是否是当前月
     *
     * @return 0：是当前月
     * 1：当前月领先计划月份，即新的一个月到了
     * 2：当前月落后计划月份，也就是要等到下个月才能执行任务
     * -1: invalid
     */
    private int checkMonth(int internetMonth) {
        Log.d(TAG, "checkMonth, curMonth=" + internetMonth);
        int savedMonth = PreferenceUtil.getInt(this, Constants.NEXT_ALARM_SCHEDULE_MONTH, -1);
        if (internetMonth == savedMonth) {
            return 0;
        } else if (internetMonth > savedMonth) {
            PreferenceUtil.put(this, Constants.TRAFFIC_TOTAL, 0);
            return 1;
        } else if (internetMonth < savedMonth) {
            return 2;
        }
        return -1;
    }


}
