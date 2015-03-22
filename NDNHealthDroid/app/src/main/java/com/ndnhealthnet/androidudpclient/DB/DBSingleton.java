package com.ndnhealthnet.androidudpclient.DB;

import android.content.Context;

/**
 * Singleton that contains DatabaseHandler object; all manipulations
 * must be done through accessing this singleton.
 */
public class DBSingleton {
    private static DBSingleton instance = null;

    private static DatabaseHandler datasource;

    protected DBSingleton() {
        // Exists only to defeat instantiation.
    }

    public static DBSingleton getInstance(Context context) {
        if(instance == null) {
            instance = new DBSingleton();
            datasource = new DatabaseHandler(context);
        }
        return instance;
    }

    public static DatabaseHandler getDB() {
        return datasource;
    }
}
