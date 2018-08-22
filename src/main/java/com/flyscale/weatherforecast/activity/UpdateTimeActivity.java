package com.flyscale.weatherforecast.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.receiver.Receiver;
import com.flyscale.weatherforecast.util.PreferenceUtil;

/**
 * Created by bian on 2018/8/13.
 */

public class UpdateTimeActivity extends Activity {

    private String[] mSettingsData;
    private ListView mListView;
    private int mUpdateTimeHous;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_settings);

        initData();
        initView();
    }

    private void initData() {
        mSettingsData = getResources().getStringArray(R.array.update_time_list);
        mUpdateTimeHous = PreferenceUtil.getInt(this, Constants.UPDATE_TIME_HOURS, 12);
    }

    private void initView() {
        mListView = findViewById(R.id.settings_list);
        mListView.setAdapter(new SettingsAdapter());
        TextView title = findViewById(R.id.title);
        title.setText(R.string.update_time);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
            }
        });
    }


    private void handlePosition(int position) {
        int updateHours = 2;
        switch (position) {
            case 0:
                updateHours = 2;
                break;
            case 1:
                updateHours = 4;

                break;
            case 2:
                updateHours = 8;
                break;
            case 3:
                updateHours = 12;
                break;
            case 4:
                updateHours = 24;
                break;
        }
        PreferenceUtil.put(this, Constants.UPDATE_TIME_HOURS, updateHours);
        setAlarm(updateHours);
        finish();
    }

    private void setAlarm(int hour) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, Receiver.class);
        intent.setAction(Constants.WEATHER_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                hour * 60 * 60 * 1000, pendingIntent);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            handlePosition(mListView.getSelectedItemPosition());
        }
        return super.onKeyUp(keyCode, event);
    }

    class SettingsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSettingsData.length;
        }

        @Override
        public Object getItem(int position) {
            return mSettingsData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler viewHodler;
            if (convertView != null) {
                viewHodler = (ViewHodler) convertView.getTag();
            } else {
                viewHodler = new ViewHodler();
                convertView = View.inflate(getApplicationContext(), R.layout.checkbox_item, null);
                viewHodler.tv = convertView.findViewById(R.id.tv);
                viewHodler.cb = convertView.findViewById(R.id.cb);
                convertView.setTag(viewHodler);
            }
            Log.d("OtherSettings", "mSettingsData[position]" + mSettingsData[position]);
            viewHodler.tv.setText(mSettingsData[position]);
            switch (mUpdateTimeHous) {
                case 2:
                    viewHodler.cb.setChecked(position == 0);
                    break;
                case 4:
                    viewHodler.cb.setChecked(position == 1);
                    break;
                case 8:
                    viewHodler.cb.setChecked(position == 2);
                    break;
                case 12:
                    viewHodler.cb.setChecked(position == 3);
                    break;
                case 24:
                    viewHodler.cb.setChecked(position == 4);
                    break;

            }
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
            private CheckBox cb;
        }
    }

}
