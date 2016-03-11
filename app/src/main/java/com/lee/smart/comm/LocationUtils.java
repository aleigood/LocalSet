package com.lee.smart.comm;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Locale;

public final class LocationUtils {
    private final static double EARTH_RADIUS = 6378137.0;

    public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public static boolean isPlayServicesAvailable(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        }

        return false;
    }

    public static String getBestProvider(Context context) {
        Criteria criteria = new Criteria();
        // 设置精度
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        // 设置是否需要提供海拔信息
        criteria.setAltitudeRequired(false);
        // 是否需要方向信息
        criteria.setBearingRequired(false);
        // 设置找到的 Provider 是否允许产生费用
        criteria.setCostAllowed(false);
        // 设置耗电
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        // 这里可能返回 null, 地理位置信息服务未开启
        return provider;
    }

    public static String getMapUrl(Double lat, Double lon, int width, int height) {
        final String coordPair = lat + "," + lon;
        return "http://maps.googleapis.com/maps/api/staticmap?" + "&language=" + Locale.getDefault().getLanguage()
                + "&zoom=16" + "&size=" + width + "x" + height + "&maptype=roadmap&sensor=true" + "&center="
                + coordPair + "&markers=color:red|" + coordPair;
    }
}
