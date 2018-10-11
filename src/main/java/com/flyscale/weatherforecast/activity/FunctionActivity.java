package com.flyscale.weatherforecast.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.db.WeatherDAO;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.FTPUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;
import com.flyscale.weatherforecast.util.TimerUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by MrBian on 2017/11/23.
 */

public class FunctionActivity extends AppCompatActivity {
    private String mCity;
    public static final int CODE_SET_CITY = 10;
    public static final int CODE_GET_CITY = 40;
    private static final String TAG = "FunctionActivity";
    private ListView mListView;
    private String[] mMainData;
    private MainAdapter mMainAdapter;
    private String mZone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);
        initData();
        initView();
        mCity = PreferenceUtil.getString(this, Constants.SP_CITY, Constants.DEF_CITY);
        mZone = PreferenceUtil.getString(this, Constants.SP_ZONE, Constants.DEF_ZONE);

        WeatherDAO weatherDAO = new WeatherDAO(this);

        WeatherToken weatherToken = new WeatherToken();

        new Thread() {
            @Override
            public void run() {
                super.run();
//                FTPUtil.downLoadFileFromDefServer(FunctionActivity.this);
            }
        }.start();
    }

    private void initData() {
        mMainData = getResources().getStringArray(R.array.main);
    }


    private void initView() {
        mListView = findViewById(R.id.lv_main);
        mMainAdapter = new MainAdapter();
        mListView.setAdapter(mMainAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
            }
        });

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.app_name);

    }

    @Override
    protected void onResume() {
        super.onResume();
        String city = PreferenceUtil.getString(this, Constants.SP_CITY, Constants.DEF_CITY);
        getWeather(this, city);

    }

    private void getWeather(final Context context, String city) {
        String weatherEna = PreferenceUtil.getString(context, Constants.WEATHER_ENABLED, "close");
        if (!TextUtils.equals(weatherEna, "open")) return;
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
                    Gson gson = new Gson();
                    WeatherToken weatherToken = gson.fromJson(result, WeatherToken.class);
                    //更新数据 库
                    WeatherDAO weatherDAO = new WeatherDAO(context);
                    weatherDAO.update(weatherToken);

                    String type = weatherToken.data.forecast.get(0).type;
                    //更新sp
                    saveToSp(context, Constants.WEATHER_TYPE, type);

                    Intent weather = new Intent(Constants.WEATHER_BROADCAST);
                    weather.putExtra(Constants.WEATHER_TYPE, type);
                    context.sendBroadcast(weather);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToSp(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp,keyCode=" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                int selectedItemPosition = mListView.getSelectedItemPosition();
                handlePosition(selectedItemPosition);
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void handlePosition(int position) {
        if (position == 0) {
            Intent weather = new Intent(this, WeatherDetailActivity.class);
            weather.putExtra("zone", mZone);
            weather.putExtra("city", mCity);
            startActivity(weather);
        } else if (position == 1) {
            startActivityForResult(new Intent(this, ProActivity.class), CODE_GET_CITY);
        } else if (position == 2) {
            startActivity(new Intent(this, OtherSettingsActivity.class));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_SET_CITY:
                    mCity = data.getStringExtra("city");
                    mZone = data.getStringExtra("zone");
                    PreferenceUtil.put(this, Constants.SP_CITY, mCity);
                    PreferenceUtil.put(this, Constants.SP_ZONE, mZone);
                    Log.e(TAG, "city=" + mCity + ",zone=" + mZone);
                    break;
                case CODE_GET_CITY:
                    mCity = data.getStringExtra("city");
                    mZone = data.getStringExtra("zone");
                    PreferenceUtil.put(this, Constants.SP_CITY, mCity);
                    PreferenceUtil.put(this, Constants.SP_ZONE, mZone);
                    Log.e(TAG, "city=" + mCity + ",zone=" + mZone);
                    break;
            }
        }
    }


    class MainAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMainData.length;
        }

        @Override
        public Object getItem(int position) {
            return mMainData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler viewHodler = null;
            if (convertView != null) {
                viewHodler = (ViewHodler) convertView.getTag();
            } else {
                viewHodler = new ViewHodler();
                convertView = View.inflate(getApplicationContext(), R.layout.city_item, null);
                viewHodler.tv = convertView.findViewById(R.id.tv);
                convertView.setTag(viewHodler);
            }
            viewHodler.tv.setText(mMainData[position]);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }

}
