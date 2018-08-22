package com.flyscale.weatherforecast.util;

/**
 * Created by bian on 2018/8/13.
 */

import android.content.Context;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;


public class TrafficUtil {
    private static final String TAG = "TrafficUtil";

    public static void main(String[] args) throws Exception {
        ArrayList<String> packages = getpackage();
        for (int i = 0; i < packages.size(); i++) {
            String uid = getuid(packages.get(i));
            System.out.println("包名为" + packages.get(i) + "的WIFI流量消耗为：" + getliuliangwifi(uid) / 1024 + "KB");
            System.out.println("包名为" + packages.get(i) + "的GPRS流量消耗为：" + getliulianggprs(uid) / 1024 + "KB");
            System.out.println("......");
        }
        System.out.println("所有的应用流量消耗已获取完成");
    }

    public static void printAllTraffics() {
        ArrayList<String> packages = null;
        try {
            packages = getpackage();
            for (int i = 0; i < packages.size(); i++) {
                String uid = getuid(packages.get(i));
                System.out.println("包名为" + packages.get(i) + "的WIFI流量消耗为：" + getliuliangwifi(uid) / 1024 + "KB");
                System.out.println("包名为" + packages.get(i) + "的GPRS流量消耗为：" + getliulianggprs(uid) / 1024 + "KB");
                System.out.println("......");
            }
            System.out.println("所有的应用流量消耗已获取完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTraffic(int uid) {
        try {
            String result = getliulianggprs(uid + "") / 1024 + "KB";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> getpackage() throws Exception {

        //adb shell pm list packages 输出所有包名
        // -3,只输出第三方的包
        Process p = Runtime.getRuntime().exec("pm list packages -3");
        InputStream in = p.getInputStream();
        InputStreamReader ir = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(ir);
        String str;
        ArrayList<String> list = new ArrayList<>();
        while ((str = br.readLine()) != null) {
            Log.d(TAG, "str=" + str);
            list.add(str.trim().split(":")[1]);
            str = br.readLine();

        }
        return list;
    }

    public static String getuid(String packagename) throws Exception {
        Process p = Runtime.getRuntime().exec("dumpsys package " + packagename + " |grep userId");
        InputStream in = p.getInputStream();
        InputStreamReader ir = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(ir);
        String uid = br.readLine().split("=")[1].split(" ")[0];
        return uid;
    }

    public static float getliuliangwifi(String uid) throws IOException {
        Process p = Runtime.getRuntime().exec("adb shell cat /proc/net/xt_qtaguid/stats |grep " + uid + " |grep wlan0");
        InputStream in = p.getInputStream();
        InputStreamReader ir = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(ir);
        String str;
        float total = 0;
        while ((str = br.readLine()) != null) {
            total = total + Integer.parseInt(str.split(" ")[5]) + Integer.parseInt(str.split(" ")[7]);
            str = br.readLine();
        }
        return total;
    }

    public static float getliulianggprs(String uid) throws IOException {
        String cmd = "cat /proc/net/xt_qtaguid/stats | grep " + uid;
        Log.d(TAG, "getliulianggprs, uid=" + uid + ",cmd=" + cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        InputStream in = p.getInputStream();
        InputStreamReader ir = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(ir);
        String str;
        float total = 0;
        int first = 0;
        while ((str = br.readLine()) != null) {
            if (first == 0) {
                first++;
                continue;
            }
            Log.d(TAG, "readline,str=" + str);
            total = total + Integer.parseInt(str.split(" ")[5]) + Integer.parseInt(str.split(" ")[7]);
            str = br.readLine();
        }
        return total;
    }

    //1.today
    //2.month
    //3.times
    //4.today_traffics
    //5.month_traffics
    /**
     * 1.判断月份
     * 2.判断次数
     * 3.判断流量
     */
    /**
     * 计算本次要跑的流量
     *
     * @return
     */
    public static long caculateBytes(Context context) {
        //判断月份，如果是本月第一次，则为固定一次的流量.
        //如果不是，在第二次至倒数第二次，每次跑固定的量
        //最后一次，跑完剩余需要跑的流量
        boolean checkMonth = checkMonth(context);
        Log.d(TAG, "checkMonth=" + checkMonth);
        if (checkMonth) {
            //如果是当月，再判断次数
            int times = PreferenceUtil.getInt(context, Constants.TRAFFIC_RUN_TIMES, 0);
            if (times + 1 < Constants.TRAFFIC_RUN_TIMES_EACH_MONTH) {
                return Constants.MAX_TRAFFIC_ONCE;
            } else if (times + 1 == Constants.TRAFFIC_RUN_TIMES_EACH_MONTH) {
                int trafficsAlready = PreferenceUtil.getInt(context, Constants.TRAFFIC_TOTAL, 0);
                return Constants.MAX_TRAFFIC - trafficsAlready;
            } else {
                return -1;
            }
        }else {
            return Constants.MAX_TRAFFIC_ONCE;
        }
    }

    /**
     * 检查是否是当前月
     *
     * @return true 是当前月，false 不是当前月
     */
    private static boolean checkMonth(Context context) {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
        Log.d(TAG, "currentMonth=" + currentMonth);
        int savedMonth = PreferenceUtil.getInt(context, Constants.CURRENT_MONTH, -1);
        Log.d(TAG, "savedMonth=" + savedMonth);
        if (currentMonth != savedMonth) {
            //新月份，重置月消耗总流量,次数
            PreferenceUtil.put(context, Constants.TRAFFIC_TOTAL, 0);
            PreferenceUtil.put(context, Constants.CURRENT_MONTH, currentMonth);
            PreferenceUtil.put(context, Constants.TRAFFIC_RUN_TIMES, 0);
            return false;
        }
        return true;
    }
}
