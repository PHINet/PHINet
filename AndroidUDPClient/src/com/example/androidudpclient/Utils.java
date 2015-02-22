package com.example.androidudpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Class facilitates user credential storage, which is necessary for NDN communication
 *
 * TODO - encrypt
 */
public class Utils {
    public static final String PREFS_LOGIN_USER_ID_KEY = "__USER_ID__" ;
    public static final String PREFS_LOGIN_SENSOR_ID_KEY = "__SENSOR_ID__" ;

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to save supplied value in shared preferences against given key.
     * @param context Context of caller activity
     * @param key Key of value to save against
     * @param value Value to save
     */
    public static void saveToPrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     * @param context Context of caller activity
     * @param key Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static String getFromPrefs(Context context, String key, String defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /** Method takes query results and converts to a format that can be presented via graph **/
    public static ArrayList<Float> convertDBRowTFloats(ArrayList<DBData> myData) {
        // TODO - improve display accuracy (order chonologically, etc)

        ArrayList<Float> myFloatData = new ArrayList<Float>();

        for (int i = 0; i < myData.size(); i++) {
            String [] floatArray = myData.get(i).getDataFloat().trim().split(",");
            for (int j = 0; j < floatArray.length; j++) {
                myFloatData.add(Float.parseFloat(floatArray[j].trim()));
            }
        }

        return myFloatData;
    }
}
