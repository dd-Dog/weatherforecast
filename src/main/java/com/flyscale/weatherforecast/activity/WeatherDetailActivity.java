package com.flyscale.weatherforecast.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.NetworkUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class WeatherDetailActivity extends AppCompatActivity {

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
    private FrameLayout mContent;
    private String mZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mCity = getIntent().getStringExtra("city");
        mZone = getIntent().getStringExtra("zone");
        Log.d(TAG, "mCity=" + mCity + ",mZone=" + mZone);

        if (NetworkUtil.isOpenNetwork(this)) {
            getWeather(mCity);
            lLWeather.setVisibility(View.INVISIBLE);
            netErr.setVisibility(View.VISIBLE);
            netErr.setText(R.string.loading);
        } else {
            Log.e(TAG, "未连接到网络，请检查网络连接");
            lLWeather.setVisibility(View.INVISIBLE);
            netErr.setVisibility(View.VISIBLE);
            netErr.setText(R.string.net_err);
        }
    }

    private void initView() {
        mContent = findViewById(R.id.content);
        mTvCity = findViewById(R.id.city);
        mType = findViewById(R.id.type);
        mTemp = findViewById(R.id.temp);
        mWind = findViewById(R.id.wind);
        lLWeather = findViewById(R.id.ll_weather);
        netErr = findViewById(R.id.net_err);
    }

    private void getWeather(String city) {
        try {
            Log.i(TAG, "main thread id is " + Thread.currentThread().getId());
            String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + city;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws
                        IOException {
                    String result = response.body().string();
                    Log.i(TAG, result);
                    refresh(result);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refresh(String result) {
        Gson gson = new Gson();
        mWeathertoken = gson.fromJson(result, WeatherToken.class);
        if (mWeathertoken == null) {
            return;
        }

        //更新数据库
        WeatherDAO weatherDAO = new WeatherDAO(this);
        weatherDAO.update(mWeathertoken);

        mWeatherInfos = mWeathertoken.data;
        if (mWeatherInfos == null) {
            return;
        }
        Log.e(TAG, "mWeathertoken=" + mWeathertoken);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lLWeather.setVisibility(View.VISIBLE);
                netErr.setVisibility(View.INVISIBLE);
                String city = mWeatherInfos.city;
                WeatherToken.WeatherInfos.Forecast forecast = mWeatherInfos.forecast.get(0);
                mTvCity.setText(city + " " + mZone);
                mType.setText(forecast.type);
                mTemp.setText(forecast.low + " " + forecast.high);
                mWind.setText(forecast.fengxiang + forecast.fengli.substring(10, 12));
            }
        });
    }


}
