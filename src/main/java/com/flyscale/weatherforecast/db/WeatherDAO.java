package com.flyscale.weatherforecast.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;


import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.global.Constants;

import java.util.ArrayList;

/**
 * Created by MrBian on 2018/1/18.
 */

public class WeatherDAO {
    private SQLiteHelper dbHelper;
    private static final String TAG = "WeatherDAO";

    public WeatherDAO(Context context) {
        dbHelper = new SQLiteHelper(context, Constants.VERSION);
    }

    public boolean delete() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delete = 0;
        if (null != db) {
            delete = db.delete(Constants.CURRENT_WEATHER_TABLE, null, null);
        }
        return delete != 0;
    }


    public boolean insert(WeatherToken token) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int insert = 0;
        if (token == null || token.data == null || token.data.forecast == null ||
                token.data.forecast.get(0) == null) {
            return insert != 0;
        }
        WeatherToken.WeatherInfos.Forecast forecast = token.data.forecast.get(0);
        if (null != db) {
            ContentValues values = new ContentValues();
            values.put(Constants.CITY, token.data.city);
            values.put(Constants.DATE, forecast.date);
            values.put(Constants.FENGLI, forecast.fengli);
            values.put(Constants.FENGXIANG, forecast.fengxiang);
            values.put(Constants.HIGH, forecast.high);
            values.put(Constants.LOW, forecast.low);
            values.put(Constants.TYPE, forecast.type);
            insert = (int) db.insert(Constants.CURRENT_WEATHER_TABLE, null, values);
        }
        db.close();
        return insert != 0;
    }


    public boolean update(WeatherToken token) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int update = 0;
        if (token == null || token.data == null || token.data.forecast == null ||
                token.data.forecast.get(0) == null) {
            return false;
        }
        WeatherToken.WeatherInfos.Forecast forecast = token.data.forecast.get(0);
        if (null != db) {
            ContentValues values = new ContentValues();
            values.put(Constants.CITY, token.data.city);
            values.put(Constants.DATE, forecast.date);
            values.put(Constants.FENGLI, forecast.fengli);
            values.put(Constants.FENGXIANG, forecast.fengxiang);
            values.put(Constants.HIGH, forecast.high);
            values.put(Constants.LOW, forecast.low);
            values.put(Constants.TYPE, forecast.type);
            update = db.update(Constants.CURRENT_WEATHER_TABLE, values, "_id=?", new String[]{"0"});
        }
        db.close();
        return update != 0;

    }

}
