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
 */

public class TrafficService extends IntentService {

    public static final String TAG = "TrafficService";

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
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = PreferenceUtil.getInt(this, Constants.SCHEDULE_DAY, 0);
        int hour = PreferenceUtil.getInt(this, Constants.SCHEDULE_HOUR, 0);
        int minutes = PreferenceUtil.getInt(this, Constants.SCHEDULE_MINUTES, 0);

        int times = PreferenceUtil.getInt(this, Constants.TRAFFIC_RUN_TIMES, 0);
        Log.d(TAG, "已经运行了" + times + "次");
        if (checkMonth()) {
            if (times >= Constants.TRAFFIC_RUN_TIMES_EACH_MONTH) {
                return;
            }
            //是当月
            boolean download = download();
            //下载完成或者失败返回后再进行设置
            TimerUtil.setTimer(this, month, day + times, hour, minutes, 0);
            PreferenceUtil.put(this, Constants.TRAFFIC_RUN_TIMES, times + (download ? 1 : 0));
        } else {
            //新的一个月的开始
            boolean download = download();
            TimerUtil.setTimer(this, (month + 1) / 12, day, hour, minutes, 0);
            PreferenceUtil.put(this, Constants.TRAFFIC_RUN_TIMES, times + (download ? 1 : 0));
        }
    }

    private boolean download() {
        return FTPUtil.downLoadFileFromDefServer(this);
    }

    /**
     * 检查是否是当前月
     *
     * @return true 是当前月，false 不是当前月
     */
    private boolean checkMonth() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
        int savedMonth = PreferenceUtil.getInt(this, Constants.CURRENT_MONTH, -1);
        if (currentMonth != savedMonth) {
            PreferenceUtil.put(this, Constants.TRAFFIC_TOTAL, 0);
            Log.d(TAG, "checkMonth=false");
            return false;
        }
        Log.d(TAG, "checkMonth=true");
        return true;
    }
}
