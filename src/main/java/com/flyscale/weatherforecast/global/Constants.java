package com.flyscale.weatherforecast.global;

/**
 * Created by MrBian on 2017/11/23.
 */

public class Constants {

    public static final String SP_NAME = "weather_sp";
    public static final String SP_CITY = "sp_city";

    public static final String DEF_CITY = "北京市";

    public static final String WEATHER_TYPE = "weather_type";
    public static final String WEATHER_BROADCAST = "com.flyscale.weatherforecast.WEATHER";
    public static final String WEATHER_DB_NAME = "weather_db";
    public static final String DATE = "date";

    public static final String CURRENT_WEATHER_TABLE = "current_weather";
    public static final String CITY = "city";
    public static final String FENGLI = "fengli";
    public static final String FENGXIANG = "fengxiang";
    public static final String HIGH = "high";
    public static final String LOW = "low";
    public static final String TYPE = "type";

    public static final String SQL_CREATE_WEATHER_STATE_TABLE =
            "CREATE TABLE " + CURRENT_WEATHER_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CITY + " TEXT," +
                    DATE + " TEXT," +
                    FENGLI + " TEXT," +
                    FENGXIANG + " TEXT," +
                    HIGH + " TEXT," +
                    LOW + " TEXT," +
                    TYPE + " TEXT)";
    public static final int VERSION = 1;
    public static final String UPDATE_TIME_HOURS = "update_time_hours";
    public static final String TRAFFIC_TODAY = "traffic_today";

    public static final String TRAFFIC_TOTAL = "traffic_total";
    public static final String TRAFFIC_TOTAL_EXTRA = "traffic_total";
    public static final String TODAY = "today";
    public static final String CURRENT_MONTH = "current_month";
    public static final String TRAFFIC_RUN_TIMES = "traffic_run_times";
    public static final int TRAFFIC_RUN_TIMES_EACH_MONTH = 5;
    public static final long MAX_TRAFFIC_ONCE = 5 * 1024 * 1024;
    public static final long MAX_TRAFFIC = TRAFFIC_RUN_TIMES_EACH_MONTH * MAX_TRAFFIC_ONCE;

}
