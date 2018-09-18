package com.flyscale.weatherforecast.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.bean.City;
import com.flyscale.weatherforecast.db.DBHelper;
import com.flyscale.weatherforecast.global.Constants;

import java.util.ArrayList;

/**
 * Created by MrBian on 2017/11/23.
 */

public class CitiesActivity extends AppCompatActivity {

    private static final String TAG = "ProActivity";
    public static final int GET_ZONE = 20;
    private SQLiteDatabase mDb;
    private CityAdapter mCityAdapter;
    private DBHelper dbHelper;
    private ArrayList<City> mCityies;
    private ListView lvPro;
    private String proSort;
    private String proName;
    private int mPosition;
    private LinearLayout mContent;
    private TextView mLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        initView();
        initData();
    }

    private void initData() {
        proSort = getIntent().getStringExtra("ProSort");
        proName = getIntent().getStringExtra("ProName");
        dbHelper = DBHelper.getInstance(this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                mCityies = dbHelper.getCities(proSort);
                Log.e(TAG, "mCityies=" + mCityies);
                mCityAdapter = new CityAdapter();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.GONE);
                        mContent.setVisibility(View.VISIBLE);
                        lvPro.setAdapter(mCityAdapter);
                    }
                });
            }
        }.start();

    }

    private void initView() {
        lvPro = findViewById(R.id.lv_cities);
        mContent = findViewById(R.id.ll_content);
        mLoading = findViewById(R.id.loading);
        lvPro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
            }
        });
    }

    private void handlePosition(int position) {
        Intent city = new Intent(getApplicationContext(), ZonesActivity.class);
        city.putExtra("CitySort", mCityies.get(position).sort);
        city.putExtra("CityName", mCityies.get(position).name);
        mPosition = position;
        startActivityForResult(city, GET_ZONE);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                int selectedItemPosition = lvPro.getSelectedItemPosition();
                handlePosition(selectedItemPosition);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void saveToSp(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_ZONE:
                    String zone = data.getStringExtra("zone");
                    City city = mCityies.get(mPosition);
                    Intent intent = new Intent();
                    intent.putExtra("city", city.name);
                    intent.putExtra("zone", zone);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
        }
    }

    class CityAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCityies.size();
        }

        @Override
        public Object getItem(int position) {
            return mCityies.get(position);
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
            City city = mCityies.get(position);
            viewHodler.tv.setText(city.name);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }

}
