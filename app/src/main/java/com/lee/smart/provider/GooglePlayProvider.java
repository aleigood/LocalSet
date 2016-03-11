package com.lee.smart.provider;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class GooglePlayProvider implements ILocationProvider, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private String TAG = GooglePlayProvider.class.getName();
    private LocationRequest mLocationRequest;
    private LocationListener mGoogleLocationListener;
    private GoogleApiClient mGoogleApiClient;
    private LocationChangeListener mListener;
    private Context mContext;

    public GooglePlayProvider(Context context) {
        mContext = context;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING);

        mGoogleLocationListener = new LocationListener() {
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
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        } else {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void stopPeriodicUpdates() {
        Log.d(TAG, "stopPeriodicUpdates");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mGoogleLocationListener);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void setLocationChangeListener(LocationChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                mGoogleLocationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }
}
