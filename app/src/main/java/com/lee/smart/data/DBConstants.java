package com.lee.smart.data;

public class DBConstants {
    public interface ResultCode {
        public static final int OK = 0;
        public static final int FAILED = -1;
    }

    public interface LOCATION {
        String TABLE_NAME = "location";

        String COLUMN_ID = "_id";
        String COLUMN_NAME = "name";
        String COLUMN_LONGITUDE = "longitude";
        String COLUMN_LATITUDE = "latitude";
        String COLUMN_RANGE = "range";
        String COLUMN_STATUS = "status";

        int INDEX_ID = 0;
        int INDEX_NAME = 1;
        int INDEX_LONGITUDE = 2;
        int INDEX_LATITUDE = 3;
        int INDEX_RANGE = 4;
        int INDEX_STATUS = 5;
    }

    public interface SETTINGS {
        String TABLE_NAME = "settings";

        String COLUMN_ID = "_id";
        String COLUMN_LOCATION_ID = "location_id";
        String COLUMN_PARAM = "param";
        String COLUMN_VALUE = "value";

        int INDEX_ID = 0;
        int INDEX_LOCATION_ID = 1;
        int INDEX_PARAM = 2;
        int INDEX_VALUE = 3;
    }
}
