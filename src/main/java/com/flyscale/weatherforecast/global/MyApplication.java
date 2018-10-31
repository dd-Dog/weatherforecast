package com.flyscale.weatherforecast.global;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.service.TrafficService;
import com.flyscale.weatherforecast.service.UpdateWeatherService;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.flyscale.weatherforecast.util.ScheduleUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by bian on 2018/8/30.
 */

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static String SMS_SEND_ACTION = "SMS_SEND_ACTIOIN";

    @Override
    public void onCreate() {
        super.onCreate();
        setUpdateWeatherAlarm();
    }

    private void setUpdateWeatherAlarm() {
        int updateHours = PreferenceUtil.getInt(this, Constants.UPDATE_TIME_HOURS, Constants.UPDATE_DEFAULT_HOURS);
        Log.d(TAG, "setUpdateWeatherAlarm,updateHours=" + updateHours);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateWeatherService.class);
        intent.setAction(Constants.WEATHER_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getService(this, 2002, intent, 0);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                updateHours * 60 * 60 * 1000, pendingIntent);
    }

    @SuppressLint("HardwareIds")
    private void calculateTaskTime() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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
        String savedSubscriberId = PreferenceUtil.getString(this, Constants.SIM_SUBSCRIBER_ID, null);
        if (TextUtils.equals(subscriberId, savedSubscriberId)) {
            return;
        }
        //更换了SIM卡
        PreferenceUtil.put(this, Constants.SIM_SUBSCRIBER_ID, subscriberId);
        ArrayList<Integer> task = ScheduleUtil.parsePhoneNum(this, subscriberId);
        //重新定时
        resetTimer(task);
    }

    private void resetTimer(ArrayList<Integer> task) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long timeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "timeInMillis=" + timeInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(year, month, task.get(0), task.get(1), task.get(2), 0);
        long taskTimeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "taskTimeInMillis=" + taskTimeInMillis);

        Intent intent = new Intent(this, TrafficService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1003, intent, 0);

        assert am != null;
        am.cancel(pendingIntent);

        am.setRepeating(AlarmManager.RTC_WAKEUP, taskTimeInMillis, Constants.DAY_MILLIS, pendingIntent);

    }

    private void initProperties() {
        Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = PreferenceUtil.class.getClassLoader().getResourceAsStream("ftp.properties");
        // 使用properties对象加载输入流
        try {
            properties.load(getResources().openRawResource(R.raw.ftp));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取key对应的value值
        String hostname = properties.getProperty(Constants.FTP_HOSTNAME);
        String port = properties.getProperty(Constants.FTP_PORT);
        String username = properties.getProperty(Constants.FTP_USERNAME);
        String passwd = properties.getProperty(Constants.FTP_PASSWD);
        String remotePath = properties.getProperty(Constants.FTP_DOWNLOAD_FILE_REMOTEPATH);
        String localRelaPath = properties.getProperty(Constants.FTP_DOWNLOAD_FILE_LOCALPATH);
        String localPath = getFilesDir() + File.separator + localRelaPath;
        String fileName = properties.getProperty(Constants.FTP_DOWNLOAD_FILE_NAME);
        Log.d(TAG, "hostname=" + hostname + ",port=" + port + ",username=" + username + ",passwd=" + passwd
                + ",remotePath=" + remotePath + ",localPath=" + localPath + ",fileName=" + fileName);
        PreferenceUtil.put(this, Constants.FTP_HOSTNAME, hostname);
        PreferenceUtil.put(this, Constants.FTP_PORT, port);
        PreferenceUtil.put(this, Constants.FTP_USERNAME, username);
        PreferenceUtil.put(this, Constants.FTP_PASSWD, passwd);
        PreferenceUtil.put(this, Constants.FTP_DOWNLOAD_FILE_REMOTEPATH, remotePath);
        PreferenceUtil.put(this, Constants.FTP_DOWNLOAD_FILE_LOCALPATH, localPath);
        PreferenceUtil.put(this, Constants.FTP_DOWNLOAD_FILE_NAME, fileName);
    }
}
