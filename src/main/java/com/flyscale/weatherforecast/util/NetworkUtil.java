package com.flyscale.weatherforecast.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.TrafficStats;

import com.flyscale.weatherforecast.global.Constants;

/**
 * Created by MrBian on 2017/11/24.
 */

public class NetworkUtil {
    public static final int UNIT_BYTE = 1;
    public static final int UNIT_MB = 1024;

    public static boolean isOpenNetwork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    public static long getGPRSTraficsByUid(int uid) {
        long rxBytes = TrafficStats.getUidRxBytes(uid);
        long txBytes = TrafficStats.getUidTxBytes(uid);
        long total = rxBytes + txBytes;
        return total;
    }

    public static long getGPRSTraficsByUid(int unit, int uid) {
        long rxBytes = TrafficStats.getUidRxBytes(uid);
        long txBytes = TrafficStats.getUidTxBytes(uid);
        long total = rxBytes + txBytes;
        if (unit == UNIT_BYTE) {
            return total;
        } else if (unit == UNIT_MB) {
            return total / unit;
        }
        return total;
    }
}
