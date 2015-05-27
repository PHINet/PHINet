package com.ndnhealthnet.androidudpclient.DB;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;

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

            // the heartbeat sensor should always be in the DB; any device will possess it
            datasource.addSensorData(new DBData(StringConst.HEARTBEAT_SENSOR, 1));
        }
        return instance;
    }

    public static DatabaseHandler getDB() {
        return datasource;
    }

    /**
     * TODO - document
     *
     * @param packetName
     * @param packetContent
     */
    public static void addToPacketDB(String packetName, String packetContent) {
        datasource.addPacketData(new DBData(packetName, packetContent));
    }
}
