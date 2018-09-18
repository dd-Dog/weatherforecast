package com.flyscale.weatherforecast.global;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.service.TrafficService;
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
        if (Constants.OPEN_RUN_FLOW) {
            initProperties();
        }
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
