package com.flyscale.weatherforecast.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.flyscale.weatherforecast.global.Constants;

/**
 * Created by MrBian on 2018/1/18.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context, int version) {
        super(context, Constants.WEATHER_DB_NAME, null, version);
    }

    private static final String INIT_TABLE = "insert into " +
            Constants.CURRENT_WEATHER_TABLE + " values(0, null, null, null, null, null ,null, null);";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.SQL_CREATE_WEATHER_STATE_TABLE);
        db.execSQL(INIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
