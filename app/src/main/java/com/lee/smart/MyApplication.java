package com.lee.smart;

import android.app.Application;

import com.lee.smart.data.DatabaseOper;

public class MyApplication extends Application {
    private static MyApplication instance;
    private DatabaseOper dbOper;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dbOper = new DatabaseOper(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbOper.close();
    }

    public DatabaseOper getDataOper() {
        return dbOper;
    }
}
