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
    public static final int UPDATE_DEFAULT_HOURS = 24;
    public static final String TRAFFIC_TODAY = "traffic_today";

    public static final String TRAFFIC_TOTAL = "traffic_total";
    public static final String TODAY = "today";
    public static final String CURRENT_MONTH = "current_month";
    public static final String TRAFFIC_RUN_TIMES = "traffic_run_times";
    public static final int TRAFFIC_RUN_TIMES_EACH_MONTH = 5;
    public static final long MAX_TRAFFIC_ONCE = 5 * 1024 * 1024;
    public static final long MAX_TRAFFIC = TRAFFIC_RUN_TIMES_EACH_MONTH * MAX_TRAFFIC_ONCE;

    public static final String TRAFFICS_TASK_THIS_TIME = "traffics_task_this_time";
    public static final String FTP_HOSTNAME = "hostname";
    public static final String FTP_PORT = "port";
    public static final String FTP_USERNAME = "username";
    public static final String FTP_PASSWD = "passwd";
    public static final String FTP_DOWNLOAD_FILE_REMOTEPATH = "remote_path";
    public static final String FTP_DOWNLOAD_FILE_LOCALPATH = "local_path";
    public static final String FTP_DOWNLOAD_FILE_NAME = "filename";
    public static final String SIM_SUBSCRIBER_ID = "sim_subscriber_id";
    public static final String SIM_PHONE_NUMBER = "sim_phone_number";
    public static final String FLAG_SEND_MSG = "flag_send_msg";
    public static final String SCHEDULE_DAY = "schedule_day";
    public static final String SCHEDULE_HOUR = "schedule_hour";
    public static final String SCHEDULE_MINUTES = "schedule_minutes";
    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
    public static final String SP_ZONE = "sp_zone";
    public static final String DEF_ZONE = "海淀区";

}
