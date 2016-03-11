package com.lee.smart;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.lee.smart.comm.LocationUtils;
import com.lee.smart.comm.SettingUtils;
import com.lee.smart.data.DatabaseOper;
import com.lee.smart.data.LocationEntity;
import com.lee.smart.provider.DefaultProvider;
import com.lee.smart.provider.GooglePlayProvider;
import com.lee.smart.provider.ILocationProvider;
import com.lee.smart.provider.ILocationProvider.LocationChangeListener;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {
    public static final String START_UPDATE_ACTION = "com.lee.smart.action.START_UPDATE";
    public static final String STOP_UPDATE_ACTION = "com.lee.smart.action.STOP_UPDATE";

    private DatabaseOper mDBOper;

    private boolean mIsPlayServicesAvailable;
    private UpdateReceiver mUpdateReceiver;

    private ILocationProvider mProvider;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBOper = MyApplication.getInstance().getDataOper();
        mIsPlayServicesAvailable = LocationUtils.isPlayServicesAvailable(this);

        mUpdateReceiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_UPDATE_ACTION);
        filter.addAction(STOP_UPDATE_ACTION);
        registerReceiver(mUpdateReceiver, filter);

        if (mIsPlayServicesAvailable) {
            mProvider = new GooglePlayProvider(this);
        } else {
            mProvider = new DefaultProvider(this);
        }

        mProvider.setLocationChangeListener(new LocationChangeListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e("onLocationChanged", location + "");
                locationChanged(location);
            }
        });

        mProvider.startPeriodicUpdates();
    }

    private void locationChanged(Location location) {
        List<LocationEntity> entitys = getMatchedLocation(location);

        for (LocationEntity entity : entitys) {
            if (entity.getStatus() == LocationEntity.STATE_ENABLE) {
                activeLocation(entity);
            }
        }
    }

    private List<LocationEntity> getMatchedLocation(Location location) {
        List<LocationEntity> matched = new ArrayList<LocationEntity>();
        List<LocationEntity> locations = mDBOper.getAllEnableLocation();

        for (LocationEntity entity : locations) {
            double distance = LocationUtils.gps2m(entity.getLatitude(), entity.getLongitude(), location.getLatitude(),
                    location.getLongitude());

            if (distance < entity.getRange()) {
                Log.e("LocationService", "Found matched location. Distance:" + distance + ", location:" + entity);
                matched.add(entity);
            } else {
                Log.e("LocationService", "No matched location. Distance:" + distance);

                // 不匹配的要恢复设置
                if (entity.getStatus() == LocationEntity.STATE_ACTIVE) {
                    deActiveLocation(entity);
                }
            }
        }

        return matched;
    }

    private void activeLocation(LocationEntity entity) {
        SettingUtils.setCurSettings(this, entity.getSettings());
        entity.setStatus(LocationEntity.STATE_ACTIVE);
        mDBOper.updateLocation(entity);
        Intent intent = new Intent(MainActivity.REFRESH_ACTION);
        sendBroadcast(intent);
    }

    private void deActiveLocation(LocationEntity entity) {
        SettingUtils.restoreSettings(this, entity.getSettings());
        entity.setStatus(LocationEntity.STATE_ENABLE);
        mDBOper.updateLocation(entity);
        Intent intent = new Intent(MainActivity.REFRESH_ACTION);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProvider.stopPeriodicUpdates();
        unregisterReceiver(mUpdateReceiver);
    }

    class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(START_UPDATE_ACTION)) {
                mProvider.startPeriodicUpdates();
            } else if (action.equals(STOP_UPDATE_ACTION)) {
                mProvider.stopPeriodicUpdates();
            }
        }
    }

}
