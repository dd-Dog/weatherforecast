package com.flyscale.weatherforecast.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by bian on 2018/8/30.
 */

public class TrafficService extends IntentService {

    public TrafficService() {
        super("TrafficService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TrafficService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }
}
