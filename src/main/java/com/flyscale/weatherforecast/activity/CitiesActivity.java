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

    private static final String TAG = "CitiesActivity";
    public static final int GET_ZONE = 20;
    private SQLiteDatabase mDb;
    private CityAdapter mCityAdapter;
    private DBHelper dbHelper;
    private ArrayList<String> mCityies;
    private ListView lvPro;
    private String proSort;
    private String proName;
    private int mPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        initView();
        initData();
    }

    private void initData() {
        proSort = getIntent().getStringExtra("ProSort");
        proName = getIntent().getStringExtra("province");
        dbHelper = DBHelper.getInstance(this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                mCityies = dbHelper.getCities2(proName);
                Log.d(TAG, "mCityies=" + mCityies);
                mCityAdapter = new CityAdapter();
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
        lvPro = findViewById(R.id.lv_cities);
        lvPro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
            }
        });
    }

    private void handlePosition(int position) {
        Intent city = new Intent(getApplicationContext(), ZonesActivity.class);
        city.putExtra("city", mCityies.get(position));
        mPosition = position;
        startActivityForResult(city, GET_ZONE);
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

    public void saveToSp(Context context, String key , String value){
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_ZONE:
                    String city = mCityies.get(mPosition);
                    Intent intent = new Intent();
                    intent.putExtra("city", city);
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
            String city = mCityies.get(position);
            viewHodler.tv.setText(city);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }

}
