package com.flyscale.weatherforecast.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


import com.flyscale.weatherforecast.global.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by MrBian on 2018/2/27.
 */

public class WeatherProvider extends ContentProvider {

    private static final boolean DEBUG = true;

    private static final String AUTHORITY = "com.flyscale.broadcast.provider";
    //定义URI和对应的URI_CODE，并进行绑定
    private static final int WEATHER_TYPE_CODE = 6001;
    private static final String URI_WEATHER_TYPE = "weathertype";

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private String[] mColumnNames = null;//构建cursor时必须先传入列数组以规定列数

    static {
        //对URI和URI_CODE进行绑定，之后在匹配URI的时候就可以根据URI进行判断，返回对应的URI_CODE
        uriMatcher.addURI(AUTHORITY, URI_WEATHER_TYPE, WEATHER_TYPE_CODE);
    }

    private HashMap<String, ArrayList<String>> allDatas = new HashMap<String, ArrayList<String>>();


    private MyCursor cursor;


    /**
     * 加载我们的数据信息
     */
    public void loadAllData() {
        allDatas.clear();
        ArrayList<String> dataList = null;
        dataList = new ArrayList<String>();
        int columnCount = 0;
        if (mColumnNames != null)
            columnCount = mColumnNames.length;
        //目前数据只有一行，columnCount列
        SharedPreferences sp = getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        for (int pos = 0; pos < columnCount; pos++) {
            if (pos == 0) {
                String weatherType = sp.getString(Constants.WEATHER_TYPE, "invalid");
                dataList.add(weatherType);
            } else if (pos == 1) {
                String weatherEna = sp.getString(Constants.WEATHER_ENABLED, "close");
                dataList.add(weatherEna);
            }
            allDatas.put(pos + "", dataList);
        }
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uriMatcher.match(uri) == WEATHER_TYPE_CODE) {
            cursor = new MyCursor();
            loadAllData();
            cursor.updateAllData(allDatas.values());
            return cursor;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        getContext().getContentResolver().notifyChange(uri, null);
        return null;
    }


    @Override
    public int delete(Uri uri, String selection, String[]
            selectionArgs) {
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }


    private static final String TAG = "MyCursor";

    public class MyCursor extends AbstractCursor {


        /**
         * 数据区域
         */
        //所有的数据
        private ArrayList<ArrayList<String>> allDatas = new ArrayList<ArrayList<String>>();//在构造的时候填充数据，里层数据的size=mColumnNames.leng
        //当前一项的数据
        private ArrayList<String> oneLineData = null;//onMove时填充

        public MyCursor() {
            //必须构建完整列信息
            mColumnNames = new String[]{Constants.WEATHER_TYPE, Constants.WEATHER_ENABLED};
        }

        /**
         * 加载所有的数据信息
         */
        public void updateAllData(Collection<ArrayList<String>> data) {
            mPos = -1;
            allDatas.clear();
            allDatas.addAll(data);
        }

        /**
         * 加载单个的数据信息
         */
        public void updateUserData(ArrayList<String> data) {
            mPos = -1;
            allDatas.clear();
            allDatas.add(data);
        }

        /**
         * 获取当前行对象，为一个oneLineDatastring[]
         */
        @Override
        public boolean onMove(int oldPosition, int newPosition) {
            if (newPosition < 0 || newPosition >= getCount()) {
                oneLineData = null;
                return false;
            }

            int index = newPosition;
            if (index < 0 || index >= allDatas.size()) {
                return false;
            }
            oneLineData = allDatas.get(index);
            return super.onMove(oldPosition, newPosition);
        }

        /**
         * 获取游标行数
         */
        @Override
        public int getCount() {
            return allDatas.size();
        }

        /**
         * 获取列名称
         */
        @Override
        public String[] getColumnNames() {
            return mColumnNames;
        }


        @Override
        public String getString(int column) {
            if (oneLineData == null) {
                return null;
            }
            if (DEBUG)
                Log.i(TAG, "column=" + column + ",getString()=" + oneLineData.get(column));
            return oneLineData.get(column);
        }

        @Override
        public int getInt(int column) {
            if (DEBUG)
                Log.i("MyCursor", "getInt, column=" + column);
            Object value = getString(column);
            try {
                return value != null ? ((Number) value).intValue() : null;
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Integer.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        if (DEBUG)
                            Log.e(TAG, "Cannotparse int value for " + value + "at key " + column);
                        return 0;
                    }
                } else {
                    if (DEBUG)
                        Log.e(TAG, "Cannotcast value for " + column + "to a int: " + value, e);
                    return 0;
                }
            }
        }

        /**
         * 以下参考getInt(int column)
         */
        @Override
        public short getShort(int column) {
            return 0;
        }

        @Override
        public long getLong(int column) {
            return 0;
        }

        @Override
        public float getFloat(int column) {
            return 0;
        }

        @Override
        public double getDouble(int column) {
            return 0;
        }

        @Override
        public boolean isNull(int column) {
            return false;
        }
    }
}
