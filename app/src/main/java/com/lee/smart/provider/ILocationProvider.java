package com.lee.smart.provider;

import android.location.Location;

public interface ILocationProvider {
    // 更新间隔时间5分钟
    long UPDATE_INTERVAL = 5 * 1000 * 60;

    // 当界面可见时的最快更新间隔时间
    long FAST_INTERVAL_CEILING = 5 * 1000;

    float UPDATE_MIN_DISTANCE = 0;

    void startPeriodicUpdates();

    void stopPeriodicUpdates();

    void setLocationChangeListener(LocationChangeListener listener);

    interface LocationChangeListener {
        void onLocationChanged(Location location);
    }
}
