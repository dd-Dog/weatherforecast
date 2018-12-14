package com.flyscale.weatherforecast.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.bean.WeatherToken;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.service.TestFLowService;
import com.flyscale.weatherforecast.service.UpdateWeatherService;
import com.flyscale.weatherforecast.util.PreferenceUtil;

public class FlowTestActivity extends AppCompatActivity {

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
    private EditText mET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_flow);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        mET = findViewById(R.id.time);
        TextView title = findViewById(R.id.title);
        title.setText(R.string.test_flow);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MENU) {
            if (mET != null) {
                String text = mET.getText().toString();
                int minutes = Integer.parseInt(text);
                if (minutes < 1) {
                    showToast("时间间隔应大于1分钟");
                } else {
                    setAlarm(minutes);
                    finish();
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void showToast(String msg) {
        Log.d(TAG, "showToast");
        View layout = LayoutInflater.from(this).inflate(R.layout.toast, null);
        Toast toast = new Toast(getApplicationContext());
        TextView content = layout.findViewById(R.id.content);
        content.setText(msg);
        toast.setView(layout);
        toast.show();
    }

    private void setAlarm(int minutes) {
        Log.d(TAG, "setAlarm,minutes=" + minutes);
        if (minutes <= 0) return;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TestFLowService.class);
        intent.setAction(Constants.WEATHER_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getService(this, 2005, intent, 0);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes * 60 * 1000, pendingIntent);
    }
}
