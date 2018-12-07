package com.flyscale.weatherforecast.activity;

import android.content.Intent;
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
import com.flyscale.weatherforecast.bean.Province;
import com.flyscale.weatherforecast.db.DBHelper;

import java.util.ArrayList;

/**
 * Created by MrBian on 2017/11/23.
 */

public class ProActivity extends AppCompatActivity {

    private static final String TAG = "ProActivity";
    public static final int GET_CITY = 10;
    public static final int GET_ZONE_DIREC = 30;
    private SQLiteDatabase mDb;
    private ProAdapter mCityAdapter;
    private DBHelper dbHelper;
    private ArrayList<Province> mAllPros;
    private ListView lvPro;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pros);
        initView();
        initData();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                mAllPros = dbHelper.getAllPros();
                Log.e(TAG, "mAllPros=" + mAllPros);
                mCityAdapter = new ProAdapter();
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
        lvPro = findViewById(R.id.lv_pro);
        TextView title = findViewById(R.id.title);
        title.setText(R.string.select_city);
        lvPro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePosition(position);
//                if (mAllPros.get(position).isPro()) {
//                    startActivityForResult(pro, GET_CITY);
//                    Log.e(TAG, mAllPros.get(position).name + "是一个省份");
//                }else {
//                    startActivityForResult(pro, GET_ZONE_DIREC);
//                }
            }
        });
    }

    private void handlePosition(int position) {
        Intent pro = new Intent(getApplicationContext(), CitiesActivity.class);
        pro.putExtra("ProSort", mAllPros.get(position).sort);
        pro.putExtra("ProName", mAllPros.get(position).name);
        startActivityForResult(pro, GET_CITY);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_CITY:
                    String city = data.getStringExtra("city");
                    String zone1 = data.getStringExtra("zone");
                    Intent intent1 = new Intent();
                    intent1.putExtra("city", city);
                    intent1.putExtra("zone", zone1);
                    Log.d(TAG, "GET_CITY,city=" + city + "zone=" + zone1);
                    setResult(RESULT_OK, intent1);
                    finish();
                    break;
                case GET_ZONE_DIREC:
                    String zone2 = data.getStringExtra("zone");
                    Intent intent2 = new Intent();
                    intent2.putExtra("zone", zone2);
                    Log.d(TAG, "GET_ZONE_DIREC," + "zone=" + zone2);
                    setResult(RESULT_OK, intent2);
                    finish();
                    break;
            }
        }

    }

    class ProAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAllPros.size();
        }

        @Override
        public Object getItem(int position) {
            return mAllPros.get(position);
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
            Province province = mAllPros.get(position);
            viewHodler.tv.setText(province.name);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }

}
