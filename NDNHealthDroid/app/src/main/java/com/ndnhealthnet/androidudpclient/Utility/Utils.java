package com.ndnhealthnet.androidudpclient.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ndnhealthnet.androidudpclient.DB.DBData;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class facilitates user credential storage, which is necessary for NDN communication -
 * as well as numerous other helpful, miscellaneous features.
 *
 * TODO - encrypt
 */
public class Utils {

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to save supplied value in shared preferences against given key.
     * @param context Context of caller activity
     * @param key Key of value to save against
     * @param value Value to save
     */
    public static boolean saveToPrefs(Context context, String key, String value) {

        if (context == null || key == null || value == null
                || (!key.equals(StringConst.PREFS_LOGIN_SENSOR_ID_KEY) // key must equal either one
                && !key.equals(StringConst.PREFS_LOGIN_USER_ID_KEY))) { // otherwise, it's invalid) {
            return false;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();

        return true;
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

        if (context == null || key == null || defaultValue == null
                || (!key.equals(StringConst.PREFS_LOGIN_SENSOR_ID_KEY) // key must equal either one
                && !key.equals(StringConst.PREFS_LOGIN_USER_ID_KEY))) { // otherwise, it's invalid
            return null;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Method takes query results and converts to a format that can be presented via graph
     *
     * @param myData array list of database data
     * @return data from input in graphable format
     */
    public static ArrayList<Float> convertDBRowTFloats(ArrayList<DBData> myData) {
        // TODO - improve display accuracy (order chronologically, etc)

        ArrayList<Float> myFloatData = new ArrayList<Float>();

        for (int i = 0; i < myData.size(); i++) {
            String [] floatArray = myData.get(i).getDataFloat().trim().split(",");
            for (int j = 0; j < floatArray.length; j++) {
                myFloatData.add(Float.parseFloat(floatArray[j].trim()));
            }
        }

        return myFloatData;
    }

    /**
     * @return UTC-compliant current time
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        // replace space with T; change makes parsing easier
        String currentTimeString = formatUTC.format(new Date()).replace(" ", "T");

        return currentTimeString;
    }

    /**
     * tests validity of IP input
     *
     * @param ip input to be validated
     * @return validity status of input IP
     */
    public static boolean validIP(String ip) {

        if (ip == null) {
            return false;
        } else {
            boolean validIP;

            try {
                InetAddress.getByName(ip);
                validIP = true;
            } catch (Exception e) {
                validIP = false;
            }

            return validIP;
        }
    }
}
