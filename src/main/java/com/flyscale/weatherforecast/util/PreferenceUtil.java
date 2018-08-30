package com.flyscale.weatherforecast.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.flyscale.weatherforecast.global.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by MrBian on 2017/11/23.
 */

public class PreferenceUtil {


    @SuppressLint("CommitPrefEdits")
    public static void put(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME,
                Context.MODE_PRIVATE);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public static void put(Context context, String key, long value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME,
                Context.MODE_PRIVATE);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(key, value);
            editor.commit();
        }
    }

    public static void put(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME,
                Context.MODE_PRIVATE);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        int str = sp.getInt(key, defValue);
        return str;
    }
    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        long str = sp.getLong(key, defValue);
        return str;
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME,
                Context.MODE_PRIVATE);
        String str = sp.getString(key, defValue);
        return str;
    }
}
