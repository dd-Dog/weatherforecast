package com.flyscale.weatherforecast.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flyscale.weatherforecast.bean.City;
import com.flyscale.weatherforecast.bean.Province;
import com.flyscale.weatherforecast.bean.Zone;

import java.util.ArrayList;

/**
 * Created by MrBian on 2017/11/24.
 */

public class DBHelper {
    private SQLiteDatabase mDb;
    private DBManager mDBManager;
    private ArrayList<City> cities;
    private static DBHelper dbHelper;

    private DBHelper(Context context) {
        mDBManager = new DBManager(context);
    }

    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    /**
     * {@link #getAllPros2()}
     *
     * @return
     */
    @Deprecated
    public ArrayList<Province> getAllPros() {
        mDb = mDBManager.openDatabase();
        Cursor t_province = mDb.query("T_province", new String[]{"ProName, ProSort, ProRemark"}, null, null,
                null, null, null);
        ArrayList<Province> pros = new ArrayList<>();
        if (t_province != null) {
            while (t_province.moveToNext()) {
                String name = t_province.getString(0);
                String sort = t_province.getString(1);
                String remark = t_province.getString(2);
                pros.add(new Province(name, sort, remark));
            }
            t_province.close();
            mDBManager.closeDatabase();
        } else {
            mDBManager.closeDatabase();
        }
        return pros;
    }

    /**
     * use new database
     *
     * @return
     */
    public ArrayList<String> getAllPros2() {
        mDb = mDBManager.openDatabase();
        Cursor t_province = mDb.query("china_provinces_code", new String[]{"name"}, null, null,
                null, null, null);
        ArrayList<String> pros = new ArrayList<>();
        if (t_province != null) {
            while (t_province.moveToNext()) {
                String name = t_province.getString(0);
                pros.add(name);
            }
            t_province.close();
            mDBManager.closeDatabase();
        } else {
            mDBManager.closeDatabase();
        }
        return pros;
    }

    /**
     * {@link #getCities2(String)}
     *
     * @param proSort
     * @return
     */
    @Deprecated
    public ArrayList<City> getCities(String proSort) {
        mDb = mDBManager.openDatabase();
        String sql = "select " + "CityName, CitySort" + " from " + "T_City where ProID=" + proSort;
        Cursor t_city = mDb.rawQuery(sql, null);
        ArrayList<City> cities = new ArrayList<>();
        if (t_city != null) {
            while (t_city.moveToNext()) {
                String name = t_city.getString(0);
                String sort = t_city.getString(1);
                cities.add(new City(name, sort));
            }
            t_city.close();
            mDBManager.closeDatabase();
        } else {
            mDBManager.closeDatabase();
        }
        return cities;
    }

    /**
     * 使用新数据库
     *
     * @param proName
     * @return
     */
    public ArrayList<String> getCities2(String proName) {
        mDb = mDBManager.openDatabase();
        String sql = "select " + "city" + " from " + "china_city_code where province='" + proName + "'";
        Cursor t_city = mDb.rawQuery(sql, null);
        ArrayList<String> cities = new ArrayList<>();
        if (t_city != null) {
            while (t_city.moveToNext()) {
                String name = t_city.getString(0);
                if (!cities.contains(name))
                    cities.add(name);
            }
            t_city.close();
            mDBManager.closeDatabase();
        } else {
            mDBManager.closeDatabase();
        }
        return cities;
    }

    public ArrayList<Zone> getZones2(String cityName) {
        mDb = mDBManager.openDatabase();
        String sql = "select " + "county,code" + " from " + "china_city_code where city='" + cityName + "'";
        Cursor t_zone = mDb.rawQuery(sql, null);
        ArrayList<Zone> zones = new ArrayList<>();
        if (t_zone != null) {
            while (t_zone.moveToNext()) {
                String name = t_zone.getString(0);
                String code = t_zone.getString(1);
                zones.add(new Zone(name, code));
            }
            t_zone.close();
            mDBManager.closeDatabase();
        } else {
            mDBManager.closeDatabase();
        }
        return zones;
    }

    /**
     * {@link #getZones2(String)}
     *
     * @param citySort
     * @return
     */
    @Deprecated
    public ArrayList<Zone> getZones(String citySort) {
        mDb = mDBManager.openDatabase();
        String sql = "select " + "ZoneName" + " from " + "T_Zone where CityID=" + citySort;
        Cursor t_zone = mDb.rawQuery(sql, null);
        ArrayList<Zone> zones = new ArrayList<>();
        if (t_zone != null) {
            while (t_zone.moveToNext()) {
                String name = t_zone.getString(0);
                zones.add(new Zone(name));
            }
            t_zone.close();
            mDBManager.closeDatabase();
        } else {
            mDBManager.closeDatabase();
        }
        return zones;
    }
}
