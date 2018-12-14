package com.flyscale.weatherforecast.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flyscale.weatherforecast.R;
import com.flyscale.weatherforecast.util.FTPUtil;

/**
 * Created by bian on 2018/12/10.
 */

public class TestFLowService extends IntentService {
    private static final String TAG = "TestFLowService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public TestFLowService() {
        super("TestFLowService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            FTPUtil.downLoadFileFromDefServer(this, new FTPUtil.FTPDownloadListener() {
                @Override
                public void onSuccess(int times, int successTime, int length) {
                    showToast("下载成功，times=" + times + ",length=" + (successTime * 1024 + length)+"KB");
                }

                @Override
                public void onFailed(int times) {
                    showToast("下载失败，times=" + times);
                }

                @Override
                public void onLoading(int times, int successTime, int length) {
                    showToast("正在下载，times=" + times + ",length=" + (successTime * 1024 + length) + "KB");
                }
            });
            showToast("开始下载");

            return;
        } else {
            showToast("网络未连接");
            Log.e(TAG, "网络未连接，无法下载");
        }
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
}
