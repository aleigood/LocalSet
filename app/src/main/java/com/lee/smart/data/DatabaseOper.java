package com.lee.smart.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DatabaseOper {
    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;

    public DatabaseOper(Context context) {
        SQLiteOpenHelper openHelper = new DatabaseHelper(context);
        mWritableDatabase = openHelper.getWritableDatabase();
        mReadableDatabase = openHelper.getReadableDatabase();
    }

    public void beginTransaction() {
        mWritableDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mWritableDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        mWritableDatabase.endTransaction();
    }

    public void close() {
        mWritableDatabase.close();
        mReadableDatabase.close();
    }

    public long insertLocation(LocationEntity entity) {
        beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBConstants.LOCATION.COLUMN_NAME, entity.getName());
        values.put(DBConstants.LOCATION.COLUMN_LATITUDE, entity.getLatitude());
        values.put(DBConstants.LOCATION.COLUMN_LONGITUDE, entity.getLongitude());
        values.put(DBConstants.LOCATION.COLUMN_RANGE, entity.getRange());
        values.put(DBConstants.LOCATION.COLUMN_STATUS, entity.getStatus());
        long rowId = mWritableDatabase.insert(DBConstants.LOCATION.TABLE_NAME, null, values);

        if (entity.getSettings() != null) {
            for (SettingEntity setting : entity.getSettings()) {
                ContentValues settingValues = new ContentValues();
                settingValues.put(DBConstants.SETTINGS.COLUMN_LOCATION_ID, rowId);
                settingValues.put(DBConstants.SETTINGS.COLUMN_PARAM, setting.getParam());
                settingValues.put(DBConstants.SETTINGS.COLUMN_VALUE, setting.getValue());
                mWritableDatabase.insert(DBConstants.SETTINGS.TABLE_NAME, null, settingValues);
            }
        }
        setTransactionSuccessful();
        endTransaction();
        return rowId;
    }

    public void deleteLocation(int id) {
        beginTransaction();
        mWritableDatabase.execSQL("DELETE FROM " + DBConstants.LOCATION.TABLE_NAME + " WHERE "
                + DBConstants.LOCATION.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        deleteSettingsByLocation(id);
        setTransactionSuccessful();
        endTransaction();
    }

    public LocationEntity getLocation(long id) {
        List<LocationEntity> entitys = getLocation(DBConstants.LOCATION.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});

        if (entitys == null || entitys.size() == 0) {
            return null;
        }
        return entitys.get(0);
    }

    public List<LocationEntity> getAllLocation() {
        return getLocation(null, null);
    }

    public List<LocationEntity> getActivedLocation() {
        return getLocation(DBConstants.LOCATION.COLUMN_STATUS + "=?",
                new String[]{String.valueOf(LocationEntity.STATE_ACTIVE)});
    }

    public List<LocationEntity> getAllEnableLocation() {
        return getLocation(DBConstants.LOCATION.COLUMN_STATUS + "!=?",
                new String[]{String.valueOf(LocationEntity.STATE_DISABLE)});
    }

    private List<LocationEntity> getLocation(String selection, String[] selectionArgs) {
        Cursor locationCursor = mReadableDatabase.query(DBConstants.LOCATION.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        if (locationCursor == null) {
            return null;
        }

        List<LocationEntity> list = new LinkedList<LocationEntity>();

        for (locationCursor.moveToFirst(); !locationCursor.isAfterLast(); locationCursor.moveToNext()) {
            LocationEntity en = new LocationEntity();
            en.setId(locationCursor.getInt(DBConstants.LOCATION.INDEX_ID));
            en.setName(locationCursor.getString(DBConstants.LOCATION.INDEX_NAME));
            en.setLatitude(locationCursor.getDouble(DBConstants.LOCATION.INDEX_LATITUDE));
            en.setLongitude(locationCursor.getDouble(DBConstants.LOCATION.INDEX_LONGITUDE));
            en.setRange(locationCursor.getInt(DBConstants.LOCATION.INDEX_RANGE));
            en.setStatus(locationCursor.getInt(DBConstants.LOCATION.INDEX_STATUS));

            Cursor settingCursor = mReadableDatabase.query(DBConstants.SETTINGS.TABLE_NAME, null,
                    DBConstants.SETTINGS.COLUMN_LOCATION_ID + "=?",
                    new String[]{"" + locationCursor.getInt(DBConstants.LOCATION.INDEX_ID)}, null, null,
                    DBConstants.SETTINGS.COLUMN_PARAM);

            List<SettingEntity> settings = new ArrayList<SettingEntity>();

            if (settingCursor != null && settingCursor.getCount() != 0) {
                for (settingCursor.moveToFirst(); !settingCursor.isAfterLast(); settingCursor.moveToNext()) {
                    SettingEntity setting = new SettingEntity();
                    setting.setId(settingCursor.getInt(DBConstants.SETTINGS.INDEX_ID));
                    setting.setLocationId(settingCursor.getInt(DBConstants.SETTINGS.INDEX_LOCATION_ID));
                    setting.setParam(settingCursor.getInt(DBConstants.SETTINGS.INDEX_PARAM));
                    setting.setValue(settingCursor.getString(DBConstants.SETTINGS.INDEX_VALUE));
                    settings.add(setting);
                }
            }

            en.setSettings(settings);
            list.add(en);
            settingCursor.close();
        }

        locationCursor.close();
        return list;
    }

    public void updateLocation(LocationEntity entity) {
        beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBConstants.LOCATION.COLUMN_NAME, entity.getName());
        values.put(DBConstants.LOCATION.COLUMN_LATITUDE, entity.getLatitude());
        values.put(DBConstants.LOCATION.COLUMN_LONGITUDE, entity.getLongitude());
        values.put(DBConstants.LOCATION.COLUMN_RANGE, entity.getRange());
        values.put(DBConstants.LOCATION.COLUMN_STATUS, entity.getStatus());
        mWritableDatabase.update(DBConstants.LOCATION.TABLE_NAME, values, DBConstants.LOCATION.COLUMN_ID + "=?",
                new String[]{String.valueOf(entity.getId())});

        for (SettingEntity setting : entity.getSettings()) {
            updateSetting(setting);
        }
        setTransactionSuccessful();
        endTransaction();
    }

    public void updateSetting(SettingEntity entity) {
        ContentValues settingValues = new ContentValues();
        settingValues.put(DBConstants.SETTINGS.COLUMN_LOCATION_ID, entity.getLocationId());
        settingValues.put(DBConstants.SETTINGS.COLUMN_PARAM, entity.getParam());
        settingValues.put(DBConstants.SETTINGS.COLUMN_VALUE, entity.getValue());
        mWritableDatabase.update(DBConstants.SETTINGS.TABLE_NAME, settingValues, DBConstants.SETTINGS.COLUMN_ID + "=?",
                new String[]{String.valueOf(entity.getId())});
    }

    public void insertSetting(SettingEntity entity) {
        ContentValues settingValues = new ContentValues();
        settingValues.put(DBConstants.SETTINGS.COLUMN_LOCATION_ID, entity.getLocationId());
        settingValues.put(DBConstants.SETTINGS.COLUMN_PARAM, entity.getParam());
        settingValues.put(DBConstants.SETTINGS.COLUMN_VALUE, entity.getValue());
        mWritableDatabase.insert(DBConstants.SETTINGS.TABLE_NAME, null, settingValues);
    }

    public void deleteSettingsByLocation(int locationId) {
        mWritableDatabase.execSQL("DELETE FROM " + DBConstants.SETTINGS.TABLE_NAME + " WHERE "
                + DBConstants.SETTINGS.COLUMN_LOCATION_ID + "=?", new String[]{String.valueOf(locationId)});
    }

    public void deleteSetting(int settingId) {
        mWritableDatabase.execSQL("DELETE FROM " + DBConstants.SETTINGS.TABLE_NAME + " WHERE "
                + DBConstants.SETTINGS.COLUMN_ID + "=?", new String[]{String.valueOf(settingId)});
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "data.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DBConstants.LOCATION.TABLE_NAME + " (" + DBConstants.LOCATION.COLUMN_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBConstants.LOCATION.COLUMN_NAME + " TEXT,"
                    + DBConstants.LOCATION.COLUMN_LONGITUDE + " DOUBLE," + DBConstants.LOCATION.COLUMN_LATITUDE
                    + " DOUBLE, " + DBConstants.LOCATION.COLUMN_RANGE + " INTEGER,"
                    + DBConstants.LOCATION.COLUMN_STATUS + " INTEGER)");
            db.execSQL("CREATE TABLE " + DBConstants.SETTINGS.TABLE_NAME + " (" + DBConstants.SETTINGS.COLUMN_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBConstants.SETTINGS.COLUMN_LOCATION_ID + " INTEGER,"
                    + DBConstants.SETTINGS.COLUMN_PARAM + " INTEGER," + DBConstants.SETTINGS.COLUMN_VALUE + " TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        }
    }
}
