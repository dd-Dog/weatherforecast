package com.flyscale.weatherforecast.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.flyscale.weatherforecast.bean.City;
import com.flyscale.weatherforecast.bean.Zone;
import com.flyscale.weatherforecast.db.DBHelper;

import java.util.ArrayList;

/**
 * Created by MrBian on 2017/11/23.
 */

public class ZonesActivity extends AppCompatActivity {

    private static final String TAG = "ProActivity";
    private SQLiteDatabase mDb;
    private ZoneAdapter mCityAdapter;
    private DBHelper dbHelper;
    private ArrayList<Zone> mZones;
    private ListView lvPro;
    private String cityName;
    private String citySort;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zones);
        initView();
        initData();
    }

    private void initData() {
        citySort = getIntent().getStringExtra("CitySort");
        cityName = getIntent().getStringExtra("CityName");
        if (TextUtils.isEmpty(cityName) || TextUtils.isEmpty(citySort)) {
            citySort = getIntent().getStringExtra("ProSort");
            cityName = getIntent().getStringExtra("ProName");
        }
        dbHelper = DBHelper.getInstance(this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                mZones = dbHelper.getZones(citySort);
                Log.e(TAG, "mCityies=" + mZones);
                mCityAdapter = new ZoneAdapter();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lvPro.setAdapter(mCityAdapter);
                    }
                });
            }
        }.start();

    }

    private void initView() {
        lvPro = findViewById(R.id.lv_zones);
        lvPro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_MENU:
                int selectedItemPosition = lvPro.getSelectedItemPosition();
                handlePosition(selectedItemPosition);
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void handlePosition(int position) {
        Intent intent = new Intent();
        intent.putExtra("zone", mZones.get(position).name);
        setResult(RESULT_OK, intent);
        finish();
    }

    class ZoneAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mZones.size();
        }

        @Override
        public Object getItem(int position) {
            return mZones.get(position);
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
            Zone zone = mZones.get(position);
            viewHodler.tv.setText(zone.name);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }

}
