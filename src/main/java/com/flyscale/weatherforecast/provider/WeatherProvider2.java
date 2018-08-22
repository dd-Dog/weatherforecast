package com.flyscale.weatherforecast.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.flyscale.weatherforecast.db.SQLiteHelper;
import com.flyscale.weatherforecast.global.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by MrBian on 2018/2/27.
 */

public class WeatherProvider2 extends ContentProvider {

    private static final boolean DEBUG = true;

    private static final String AUTHORITY = "com.flyscale.broadcast.provider";
    //定义URI和对应的URI_CODE，并进行绑定
    private static final int WEATHER_TYPE_CODE = 6002;
    private static final String URI_WEATHER_TYPE = "weatherinfo";

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        //对URI和URI_CODE进行绑定，之后在匹配URI的时候就可以根据URI进行判断，返回对应的URI_CODE
        uriMatcher.addURI(AUTHORITY, URI_WEATHER_TYPE, WEATHER_TYPE_CODE);
    }

    private HashMap<String, ArrayList<String>> allDatas = new HashMap<String, ArrayList<String>>();
    private SQLiteHelper mSQLiteOpenHelper;

    @Override
    public boolean onCreate() {
        mSQLiteOpenHelper = new SQLiteHelper(getContext(), Constants.VERSION);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();

        Cursor cursor = null;
        if (uriMatcher.match(uri) == WEATHER_TYPE_CODE) {
            cursor = db.query(Constants.CURRENT_WEATHER_TABLE, projection, selection, selectionArgs, sortOrder, null, null);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();

        if (db != null && uriMatcher.match(uri) == WEATHER_TYPE_CODE) {
            long insert = db.insert(Constants.CURRENT_WEATHER_TABLE, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return null;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();

        if (db != null && uriMatcher.match(uri) == WEATHER_TYPE_CODE) {
            db.delete(Constants.CURRENT_WEATHER_TABLE, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteOpenHelper.getReadableDatabase();
        if (db != null && uriMatcher.match(uri) == WEATHER_TYPE_CODE) {
            db.update(Constants.CURRENT_WEATHER_TABLE, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }


}
