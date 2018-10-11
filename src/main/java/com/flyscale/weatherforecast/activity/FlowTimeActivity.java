package com.flyscale.weatherforecast.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.NetworkUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FlowTimeActivity extends AppCompatActivity {

    private static final String TAG = "WeatherDetailActivity";
    private TextView mTvCity;
    private TextView mType;
    private TextView mTemp;
    private TextView mWind;
    private WeatherToken mWeathertoken;
    private WeatherToken.WeatherInfos mWeatherInfos;
    private String mCity;
    private LinearLayout lLWeather;
    private TextView netErr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        initView();
    }

    private void initView() {
        TextView time = findViewById(R.id.time);
        int year = PreferenceUtil.getInt(this, Constants.NEXT_ALARM_SCHEDULE_YEAR, -1);
        int month = PreferenceUtil.getInt(this, Constants.NEXT_ALARM_SCHEDULE_MONTH, -1);
        int day = PreferenceUtil.getInt(this, Constants.NEXT_ALARM_SCHEDULE_DAY, -1);
        int hour = PreferenceUtil.getInt(this, Constants.NEXT_ALARM_SCHEDULE_HOUR, -1);
        int minute = PreferenceUtil.getInt(this, Constants.NEXT_ALARM_SCHEDULE_MINUTE, -1);
        if (Constants.OPEN_RUN_FLOW)
            time.setText("下次跑流量时间：\r\n" + year + "年" + (month + 1) + "月" + day + "日\r\n" + hour + "时" + minute + "分");
        else
            time.setText("偷跑流量未开启！！！");
    }

}
