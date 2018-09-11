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
        int day = PreferenceUtil.getInt(this, Constants.SCHEDULE_DAY, 0);
        int hour = PreferenceUtil.getInt(this, Constants.SCHEDULE_HOUR, 0);
        int minutes = PreferenceUtil.getInt(this, Constants.SCHEDULE_MINUTES, 0);
        time.setText("从" + day + "号开始，每天的" + hour +"点" + minutes + "分开始偷跑流量，每次5M");
    }




}
