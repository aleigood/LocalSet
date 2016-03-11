package com.lee.smart.provider;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.lee.smart.R;
import com.lee.smart.comm.LocationUtils;

public class DefaultProvider implements ILocationProvider {
    private String TAG = DefaultProvider.class.getName();
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Context mContext;
    private LocationChangeListener mListener;

    public DefaultProvider(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new android.location.LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled");
            }

            @Override
            public void onLocationChanged(Location location) {
                if (mListener != null) {
                    mListener.onLocationChanged(location);
                }
            }
        };
    }

    @Override
    public void startPeriodicUpdates() {
        Log.d(TAG, "startPeriodicUpdates");
        String provider = LocationUtils.getBestProvider(mContext);

        if (provider != null) {
            mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL, UPDATE_MIN_DISTANCE, mLocationListener);
        } else {
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.location_service_err_title)
                    .setMessage(
                            mContext.getResources().getText(R.string.app_name) + " "
                                    + mContext.getResources().getText(R.string.location_service_err))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                            try {
                                pendingIntent.send();
                            } catch (CanceledException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }).show();
            return;
        }
    }

    @Override
    public void stopPeriodicUpdates() {
        Log.d(TAG, "stopPeriodicUpdates");
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void setLocationChangeListener(LocationChangeListener listener) {
        mListener = listener;
    }

}
