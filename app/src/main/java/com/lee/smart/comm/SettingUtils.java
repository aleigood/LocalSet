package com.lee.smart.comm;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.lee.smart.R;
import com.lee.smart.data.SettingEntity;

import java.lang.reflect.Method;
import java.util.List;

public class SettingUtils {
    public static final String PREF_PARAM = "PARAM";
    public static final int BRIGHT_MODE_MANUAL = 0;
    public static final int BRIGHT_MODE_AUTO = 1;
    public static final String BRIGHT_MODE = "screen_brightness_mode";
    public static final int STATE_OTHER = -1;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int RINGER_MODE_NORMAL = 2;
    public static final int RINGER_MODE_SILENT = 0;
    public static final int RINGER_MODE_VIBRATE = 1;
    public static final int SETTING_WIFI = 0;
    public static final int SETTING_DATA = 1;
    public static final int SETTING_BLUETOOTH = 2;
    public static final int SETTING_SYNC = 3;
    public static final int SETTING_VIBRATE = 4;
    public static final int SETTING_BRIGHTNESS = 5;
    public static final int SETTING_VOLUME = 6;
    public static final int SETTING_RINGER_MODE = 7;
    public static final int SETTING_RINGTONE = 8;

    private SettingUtils() {
    }

    public static CharSequence getSettingStrById(Context context, int id) {
        switch (id) {
            case SETTING_WIFI:
                return context.getResources().getText(R.string.wifi);
            case SETTING_DATA:
                return context.getResources().getText(R.string.data_conn);
            case SETTING_BLUETOOTH:
                return context.getResources().getText(R.string.bluetooth);
            case SETTING_SYNC:
                return context.getResources().getText(R.string.sync);
            case SETTING_BRIGHTNESS:
                return context.getResources().getText(R.string.birghtness);
            case SETTING_VIBRATE:
                return context.getResources().getText(R.string.vibrate);
            case SETTING_VOLUME:
                return context.getResources().getText(R.string.volume);
            case SETTING_RINGER_MODE:
                return context.getResources().getText(R.string.ringer_mode);
            case SETTING_RINGTONE:
                return context.getResources().getText(R.string.ringtone);
            default:
                return context.getResources().getText(R.string.unknown);
        }
    }

    public static CharSequence getValueStr(Context context, int id, String value) {
        switch (id) {
            case SETTING_WIFI:
            case SETTING_DATA:
            case SETTING_BLUETOOTH:
            case SETTING_SYNC:
            case SETTING_VIBRATE:
                return getStatusStr(context, Integer.parseInt(value));
            case SETTING_BRIGHTNESS:
                return getBrightnessStr(context, Integer.parseInt(value));
            case SETTING_VOLUME:
                return getVolumeStr(context, Integer.parseInt(value));
            case SETTING_RINGTONE:
                return getRingtoneStr(context, value);
            case SETTING_RINGER_MODE:
                return getRingerModeStr(context, Integer.parseInt(value));
            default:
                return value;
        }
    }

    public static CharSequence getStatusStr(Context context, int stat) {
        switch (stat) {
            case STATE_ENABLED:
                return context.getResources().getText(R.string.state_on);
            case STATE_DISABLED:
                return context.getResources().getText(R.string.state_off);
            default:
                return context.getResources().getText(R.string.state_other);
        }
    }

    public static String getDefaultValue(Context context, int ids) {
        switch (ids) {
            case SETTING_WIFI:
                return String.valueOf(getWifiState(context));
            case SETTING_DATA:
                return String.valueOf(getDataConnState(context));
            case SETTING_BLUETOOTH:
                return String.valueOf(getBluetoothState(context));
            case SETTING_SYNC:
                return String.valueOf(getSyncState());
            case SETTING_VIBRATE:
                return String.valueOf(getVibrateState(context));
            case SETTING_BRIGHTNESS:
                return String.valueOf(getBrightness(context));
            case SETTING_VOLUME:
                return String.valueOf(getVolume(context));
            case SETTING_RINGER_MODE:
                return String.valueOf(getRingerMode(context));
            case SETTING_RINGTONE:
                return String.valueOf(getRingtone(context));
            default:
                return "";
        }
    }

    public static void setCurSettings(Context context, List<SettingEntity> settings) {
        for (SettingEntity entity : settings) {
            switch (entity.getParam()) {
                case SETTING_WIFI:
                    setWifiState(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_RINGER_MODE:
                    setRingerMode(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_DATA:
                    setDataConnState(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_BLUETOOTH:
                    setBluetoothState(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_SYNC:
                    setSyncState(Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_VIBRATE:
                    setVibrateState(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_BRIGHTNESS:
                    setBrightness(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_VOLUME:
                    setVolume(context, Integer.parseInt(entity.getValue()));
                    break;
                case SETTING_RINGTONE:
                    setRingtone(context, entity.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    public static void restoreSettings(Context context, List<SettingEntity> list) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        for (SettingEntity settingEntity : list) {
            switch (settingEntity.getParam()) {
                case SETTING_WIFI:
                    if (pref.contains(PREF_PARAM + SETTING_WIFI)) {
                        setWifiState(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_WIFI, "")));
                    }
                    break;
                case SETTING_RINGER_MODE:
                    if (pref.contains(PREF_PARAM + SETTING_RINGER_MODE)) {
                        setRingerMode(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_RINGER_MODE, "")));
                    }
                    break;
                case SETTING_DATA:
                    if (pref.contains(PREF_PARAM + SETTING_DATA)) {
                        setDataConnState(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_DATA, "")));
                    }
                    break;
                case SETTING_BLUETOOTH:
                    if (pref.contains(PREF_PARAM + SETTING_BLUETOOTH)) {
                        setBluetoothState(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_BLUETOOTH, "")));
                    }
                    break;
                case SETTING_SYNC:
                    if (pref.contains(PREF_PARAM + SETTING_SYNC)) {
                        setSyncState(Integer.parseInt(pref.getString(PREF_PARAM + SETTING_SYNC, "")));
                    }
                    break;
                case SETTING_BRIGHTNESS:
                    if (pref.contains(PREF_PARAM + SETTING_BRIGHTNESS)) {
                        setBrightness(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_BRIGHTNESS, "")));
                    }
                    break;
                case SETTING_VIBRATE:
                    if (pref.contains(PREF_PARAM + SETTING_VIBRATE)) {
                        setVibrateState(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_VIBRATE, "")));
                    }
                    break;
                case SETTING_VOLUME:
                    if (pref.contains(PREF_PARAM + SETTING_VOLUME)) {
                        setVolume(context, Integer.parseInt(pref.getString(PREF_PARAM + SETTING_VOLUME, "")));
                    }
                    break;
                case SETTING_RINGTONE:
                    if (pref.contains(PREF_PARAM + SETTING_RINGTONE)) {
                        setRingtone(context, pref.getString(PREF_PARAM + SETTING_RINGTONE, ""));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void saveCurSettings(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_PARAM + SETTING_WIFI, String.valueOf(getWifiState(context)))
                .putString(PREF_PARAM + SETTING_DATA, String.valueOf(getDataConnState(context)))
                .putString(PREF_PARAM + SETTING_BLUETOOTH, String.valueOf(getBluetoothState(context)))
                .putString(PREF_PARAM + SETTING_SYNC, String.valueOf(getSyncState()))
                .putString(PREF_PARAM + SETTING_BRIGHTNESS, String.valueOf(getBrightness(context)))
                .putString(PREF_PARAM + SETTING_VIBRATE, String.valueOf(getVibrateState(context)))
                .putString(PREF_PARAM + SETTING_VOLUME, String.valueOf(getVolume(context)))
                .putString(PREF_PARAM + SETTING_RINGTONE, getRingtone(context))
                .putString(PREF_PARAM + SETTING_RINGER_MODE, String.valueOf(getRingerMode(context))).commit();
    }

    public static CharSequence getRingtoneStr(Context context, String ringtone) {
        if (ringtone != null && !ringtone.equals("")) {
            final Ringtone r = RingtoneManager.getRingtone(context, Uri.parse(ringtone));

            if (r != null) {
                return r.getTitle(context);
            }
        } else {
            Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);

            if (defaultUri != null) {
                Ringtone ringstone = RingtoneManager.getRingtone(context, defaultUri);

                if (ringstone != null) {
                    return ringstone.getTitle(context);
                }
            }
        }
        return "";
    }

    public static String getRingtone(Context context) {
        Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);

        if (defaultUri != null) {
            return defaultUri.toString();
        }

        return "";
    }

    public static void setRingtone(Context context, String ringtone) {
        Uri cusRingtone = Uri.parse(ringtone);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, cusRingtone);
    }

    public static int getWifiState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();

        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            return STATE_DISABLED;
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            return STATE_ENABLED;
        } else {
            return STATE_OTHER;
        }
    }

    public static void setWifiState(Context context, int state) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (state == STATE_ENABLED) {
            wifiManager.setWifiEnabled(true);
        } else if (state == STATE_DISABLED) {
            wifiManager.setWifiEnabled(false);
        }
    }

    public static CharSequence getRingerModeStr(Context context, int stat) {
        switch (stat) {
            case RINGER_MODE_NORMAL:
                return context.getResources().getText(R.string.ringer_mode_normal);
            case RINGER_MODE_SILENT:
                return context.getResources().getText(R.string.ringer_mode_silent);
            case RINGER_MODE_VIBRATE:
                return context.getResources().getText(R.string.ringer_mode_vibrate);
            default:
                return context.getResources().getText(R.string.unknown);
        }
    }

    public static int getRingerMode(Context context) {
        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        return audioManager.getRingerMode();
    }

    public static void setRingerMode(Context context, int mode) {
        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        audioManager.setRingerMode(mode);
    }

    public static int getSyncState() {
        try {
            return ((Boolean) (ContentResolver.class.getMethod("getMasterSyncAutomatically", new Class[]{}).invoke(
                    null, new Object[]{}))).booleanValue() ? STATE_ENABLED : STATE_DISABLED;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return STATE_DISABLED;
    }

    public static void setSyncState(int state) {
        try {
            ContentResolver.class.getMethod("setMasterSyncAutomatically", new Class[]{boolean.class}).invoke(null,
                    new Object[]{state == STATE_ENABLED ? true : false});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDataConnState(Context context, int state) {
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");

        try {
            Method setMethod = ConnectivityManager.class.getMethod("setMobileDataEnabled",
                    new Class[]{boolean.class});
            setMethod.invoke(sConnectivityManager, new Object[]{state == STATE_ENABLED ? true : false});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getDataConnState(Context context) {
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        try {
            Method getMethod = ConnectivityManager.class.getMethod("getMobileDataEnabled");
            return (Boolean) getMethod.invoke(sConnectivityManager, new Object[]{}) ? STATE_ENABLED : STATE_DISABLED;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return STATE_DISABLED;
    }

    public static void setBrightness(Context context, int val) {
        if (val == -1) {
            Settings.System.putInt(context.getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_AUTO);
        } else {
            Settings.System.putInt(context.getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_MANUAL);
            android.provider.Settings.System.putInt(context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, val);
        }
    }

    public static int getBrightness(Context context) {
        int brightness = Settings.System.getInt(context.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, 255);
        int mode = Settings.System.getInt(context.getContentResolver(), BRIGHT_MODE, 1);

        if (mode == BRIGHT_MODE_AUTO) {
            return -1;
        } else {
            return brightness;
        }
    }

    public static CharSequence getBrightnessStr(Context context, int val) {
        return val == -1 ? context.getString(R.string.brightness_auto) : (int) ((float) val / 255f * 100f) + "%";
    }

    public static void setVolume(Context context, int val) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, val, 0);
    }

    public static int getVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_RING);
    }

    public static int getMaxVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
    }

    public static String getVolumeStr(Context context, int val) {
        return (int) ((float) val / (float) getMaxVolume(context) * 100f) + "%";
    }

    public static void setBluetoothState(Context context, int state) {
        try {
            Class<?> localClass = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothAdapter");
            Method localMethod = localClass.getMethod("getDefaultAdapter", new Class[0]);
            Object[] arrayOfObject = new Object[0];
            Object device1 = localMethod.invoke(null, arrayOfObject);
            Method enableMethod = localClass.getMethod("enable", new Class[0]);
            enableMethod.setAccessible(true);
            Method disableMethod = localClass.getMethod("disable", new Class[0]);
            disableMethod.setAccessible(true);

            if (state == STATE_ENABLED) {
                enableMethod.invoke(device1, new Object[]{});
            } else {
                disableMethod.invoke(device1, new Object[]{});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getBluetoothState(Context context) {
        try {
            Class<?> localClass = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothAdapter");
            Method localMethod = localClass.getMethod("getDefaultAdapter", new Class[0]);
            Object[] arrayOfObject = new Object[0];
            Object device1 = localMethod.invoke(null, arrayOfObject);
            Method method = localClass.getMethod("getState", new Class[0]);
            method.setAccessible(true);

            int state = ((Integer) method.invoke(device1, new Object[]{})).intValue();
            return (state == BluetoothAdapter.STATE_ON) ? STATE_ENABLED : STATE_DISABLED;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return STATE_DISABLED;
    }

    public static void setVibrateState(Context context, int state) {
        Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", state == STATE_ENABLED ? 1 : 0);
    }

    public static int getVibrateState(Context context) {
        try {
            int state = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing");
            return state == 1 ? STATE_ENABLED : STATE_DISABLED;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }

        return STATE_DISABLED;
    }
}
