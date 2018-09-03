package com.flyscale.weatherforecast.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.FTPUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;

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
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int times = PreferenceUtil.getInt(this, Constants.TRAFFIC_RUN_TIMES, 0);
        Log.d(TAG, "已经运行了" + Constants.TRAFFIC_RUN_TIMES_EACH_MONTH + "次");
        if (checkMonth()) {
            if (times >= Constants.TRAFFIC_RUN_TIMES_EACH_MONTH) {
                return;
            }
            PreferenceUtil.put(this, Constants.TRAFFIC_RUN_TIMES, times + 1);
            download();
        } else {
            download();
            PreferenceUtil.put(this, Constants.TRAFFIC_RUN_TIMES, 1);
        }
    }

    private void download() {
        FTPUtil.downLoadFileFromDefServer(this);
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
