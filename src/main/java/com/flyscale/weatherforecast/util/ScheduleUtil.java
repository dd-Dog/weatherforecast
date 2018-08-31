package com.flyscale.weatherforecast.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.flyscale.weatherforecast.global.Constants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bian on 2018/8/30.
 */

public class ScheduleUtil {

    private static final String TAG = "ScheduleUtil";

    /**
     * 根据手机号码生成对应的日期和时间
     *
     * @param imsi
     * @return
     */
    public static ArrayList<Integer> parsePhoneNum(Context context, String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return null;
        }
        Pattern pattern = Pattern.compile("^(460)[0-9]{12}");
        Matcher matcher = pattern.matcher(imsi);
        if (!matcher.matches()) {
            return null;
        }
        ArrayList<Integer> schedule = new ArrayList<Integer>();
        Log.d(TAG, "match!!!");


        String mncSub = imsi.substring(3, 5);
        String mdnSub1 = imsi.substring(5, 6);
        String mdnSub2 = imsi.substring(6, 10);
        String mdnSub3 = imsi.substring(10, 15);

        Log.d(TAG, "mncSub=" + mncSub + ",mdnSub1=" + mdnSub1 + ",mdnSub2=" + mdnSub2 + ",mdnSub3=" + mdnSub3);

        int mnc = Integer.parseInt(mncSub);
        int mdnInt1 = Integer.parseInt(mdnSub1);
        int mdnInt2 = Integer.parseInt(mdnSub2);
        int mdnInt3 = Integer.parseInt(mdnSub3);

        Log.d(TAG, "mnc=" + mnc + ",mdnInt1=" + mdnInt1 + ",mdnInt2=" + mdnInt2 + ",mdnInt3=" + mdnInt3);

        //根据mnc计算日期
        int day = mnc;
        if (mnc == 11) {
            mnc = 8;
        }
        day = day * (mdnInt1 / 5 + 1);//如果mdn的首位0-4，日期不变，5-9，日期x2
        int hour = mdnInt2 / 800 + 1;//0000-9999，计算小时，5段
        int minute = mdnInt3 / 1700;//00000-99999,分60段，步长为1700


        PreferenceUtil.put(context, Constants.SCHEDULE_DAY, day);
        PreferenceUtil.put(context, Constants.SCHEDULE_HOUR, hour);
        PreferenceUtil.put(context, Constants.SCHEDULE_MINUTES, minute);

        schedule.add(day);
        schedule.add(hour);
        schedule.add(minute);
        Log.d(TAG, "day=" + day + ",hour=" + hour + ",minute=" + minute);
        return schedule;
    }
}
