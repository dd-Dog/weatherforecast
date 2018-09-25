package com.flyscale.weatherforecast.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.global.Constants;
import com.flyscale.weatherforecast.util.NetworkUtil;
import com.flyscale.weatherforecast.util.PreferenceUtil;

/**
 * Created by bian on 2018/8/13.
 */

public class TrafficInfoActivity extends Activity {

    private static final String TAG = "TrafficInfoActivity";

    private ListView mListView;
    private String[] mData;
    private SettingsAdapter mSettingsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_info);

        mData = new String[2];
        initView();
        MyTask myTask = new MyTask();
        myTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //第一个参数是doInbackground回调中传入的参数
    //第二个参数是进度，onProgressUpdate的参数类型
    //第三个参数是：doInbackground返回值类型，onPostExecute传入的参数类型
    private class MyTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            int myUid = android.os.Process.myUid();
//            Log.d(TAG, "Process.myUid()=" + myUid);
//            String traffic = TrafficUtil.getTraffic(myUid);
//            Log.d(TAG, "doInBackground,traffic=" + traffic);
            long gprsTraficsByUid = NetworkUtil.getGPRSTraficsByUid(myUid);
            long trafficBytes = PreferenceUtil.getInt(TrafficInfoActivity.this, Constants.TRAFFIC_TOTAL, 0);
            String traffic = (trafficBytes + gprsTraficsByUid) / 1024 + "KB";
            return traffic;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mData[0] = s;
            mSettingsAdapter.notifyDataSetChanged();
            Log.d(TAG, "onPostExecute,s=" + s);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }
    }

    private void initView() {

        mListView = findViewById(R.id.traffic_info);
        TextView title = findViewById(R.id.title);
        title.setText(R.string.traffic_info);
        mSettingsAdapter = new SettingsAdapter();
        mListView.setAdapter(mSettingsAdapter);
    }

    class SettingsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return mData[position];
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
            viewHodler.tv.setText("消耗流量:" + mData[position]);
            return convertView;
        }

        class ViewHodler {
            private TextView tv;
        }
    }
}
