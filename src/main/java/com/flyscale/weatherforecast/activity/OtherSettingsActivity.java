package com.flyscale.weatherforecast.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;

/**
 * Created by bian on 2018/8/13.
 */

public class OtherSettingsActivity extends Activity {

    private String[] mSettingsData;
    private ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_settings);

        initData();
        initView();
    }

    private void initData() {
        mSettingsData = getResources().getStringArray(R.array.settings);
        Log.d("OtherSettings", "mSettingsData=" + mSettingsData[0]);
    }

    private void initView() {
        mListView = findViewById(R.id.settings_list);
        mListView.setAdapter(new SettingsAdapter());
        TextView title = findViewById(R.id.title);
        title.setText(R.string.other_settings);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
            }
        });
    }


    private void handlePosition(int position) {
        if (position == 0) {
            startActivity(new Intent(this, UpdateTimeActivity.class));
        }else if(position == 1) {
            startActivity(new Intent(this, TrafficInfoActivity.class));
        }
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
//            return mSettingsData.length;
            return 1;
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
                convertView = View.inflate(getApplicationContext(), R.layout.city_item, null);
                viewHodler.tv = convertView.findViewById(R.id.tv);
                convertView.setTag(viewHodler);
            }
            Log.d("OtherSettings", "mSettingsData[position]" + mSettingsData[position]);
            viewHodler.tv.setText(mSettingsData[position]);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }

}
