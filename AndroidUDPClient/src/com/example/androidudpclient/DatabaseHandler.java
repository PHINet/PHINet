package com.example.androidudpclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Class handles logic associated with three DB tables
 * the PIT, CS, FIB (whose contents are specified by NDN)
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String CS_DB = "ContentStore";
    private static final String PIT_DB = "PendingInterestTable";
    private static final String FIB_DB = "ForwardingInformationBase";

    private static final String DATABASE_NAME = "NDNHealthNet6";
    private static final int DATABASE_VERSION = 6;

    private static final String KEY_USER_ID = "_userID";
    private static final String KEY_SENSOR_ID = "sensorID";
    private static final String KEY_TIME_STRING = "timestring";
    private static final String KEY_PROCESS_ID = "processID";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String KEY_DATA_CONTENTS = "dataContents";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // keys are USER_ID and TIME_STRING - only one piece of data per user per time instant
        String CREATE_DATABASE_TABLE = "CREATE TABLE " + CS_DB + "("
                + KEY_USER_ID + " TEXT ," +  KEY_SENSOR_ID + " TEXT," +
                KEY_TIME_STRING + " TEXT ," + KEY_PROCESS_ID + " TEXT," +KEY_DATA_CONTENTS +
                " TEXT, " + "PRIMARY KEY(" + KEY_USER_ID + ", " + KEY_TIME_STRING + "))";

        db.execSQL(CREATE_DATABASE_TABLE); // create CS

        // keys are USER_ID, TIME_STRING, and IP_ADDRESS
                    // - only one piece of requested data per user per time instant
        CREATE_DATABASE_TABLE = "CREATE TABLE " + PIT_DB + "("
                +KEY_USER_ID + " TEXT ," + KEY_SENSOR_ID + " TEXT," +
               KEY_TIME_STRING + " TEXT," +KEY_PROCESS_ID + " TEXT," +KEY_IP_ADDRESS + " TEXT,"
                + "PRIMARY KEY(" + KEY_USER_ID + "," + KEY_TIME_STRING + ", "
                + KEY_IP_ADDRESS+ "))";

        db.execSQL(CREATE_DATABASE_TABLE); // create PIT

        // keys are USER_ID - only location per user
        CREATE_DATABASE_TABLE = "CREATE TABLE " + FIB_DB + "("
                +KEY_USER_ID + " TEXT PRIMARY KEY," + KEY_TIME_STRING +
                " TEXT, " +KEY_IP_ADDRESS + " TEXT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create FIB
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PIT_DB);
        db.execSQL("DROP TABLE IF EXISTS " + FIB_DB);
        db.execSQL("DROP TABLE IF EXISTS " + CS_DB);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    private void addData(DBData data, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (tableName.equals(PIT_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
        } else if (tableName.equals(CS_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_DATA_CONTENTS, data.getDataFloat());
        } else if (tableName.equals(FIB_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
            values.put(KEY_TIME_STRING, data.getTimeString());
        } else {
            // TODO - throw exception
        }

        // TODO - check to see if doesn't exist first

        // Inserting Row
        db.insert(tableName, null, values);
        db.close(); // Closing database connection
    }

    public void addPITData(DBData data) {
        addData(data, PIT_DB);
    }

    public void addCSData(DBData data) {

        System.out.println("ADD DATA CALLED");

        /* TODO - rework (currently only "updating" string rather than storing multiple entries
                 with multiple time strings */
        ArrayList<DBData> csDATA = getGeneralCSData(data.getUserID());


        if (csDATA != null) {

            System.out.println("ONLY UPDATING");
            // append data to current entry
            // TODO - again, rework this

            // NOTE: there should only be one in array list, we'll rely on this
                    // horrible assumption for now
            csDATA.get(0).setDataFloat(csDATA.get(0).getDataFloat() + "," + data.getDataFloat());
            updateCSData(csDATA.get(0));
        } else {
            System.out.println("ACTUALLY ADDING");
            addData(data, CS_DB);
        }
    }

    public void addFIBData(DBData data) {
        addData(data, FIB_DB);
    }

    /**
     * Data is queried without ipAddr specification; multiple entries may be found.
     * **/
    public ArrayList<DBData> getGeneralPITData(String userID, String timeString) {
        SQLiteDatabase db = this.getReadableDatabase();

        // TODO - rework with real timestring

        String whereSelection = "_userID=\"" + userID + "\"";// AND timestring=\"" + timeString + "\"";

        Cursor cursor = db.query(PIT_DB, new String[] {KEY_USER_ID,
                        KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},
                whereSelection, null, null, null, null);

        ArrayList<DBData> allValidPITEntries = new ArrayList<DBData>();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                DBData data = new DBData();

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setTimeString(cursor.getString(2));
                data.setProcessID(cursor.getString(3));
                data.setIpAddr(cursor.getString(4));
                allValidPITEntries.add(data);
            }

            cursor.close();

        } else {
            allValidPITEntries = null; // query found nothing, set return object to null
        }

        db.close();
        return allValidPITEntries;
    }

    /**
     * Data is queried with ipAddr specification; at most one entry is found.
     * **/
    public DBData getSpecificPITData(String userID, String timeString, String ipAddr) {
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = "_userID= \"" + userID + "\" AND timestring=\"" + timeString
                + "\" AND ipAddress= \"" + ipAddr + "\"";

        Cursor cursor;
        try {
            cursor = db.query(PIT_DB, new String[] {KEY_USER_ID,
                            KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},
                    whereSelection, null, null, null, null);
        } catch (SQLiteException e) {
            cursor = null; // bad query apparently

            System.out.println("SQL EXCEPTION: " + e.toString());
        }

        DBData data = new DBData();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            data.setUserID(cursor.getString(0));
            data.setSensorID(cursor.getString(1));
            data.setTimeString(cursor.getString(2));
            data.setProcessID(cursor.getString(3));
            data.setIpAddr(cursor.getString(4));
            cursor.close();
        } else {
            data = null; // query found nothing, set return object to null
        }

        db.close();
        return data;
    }

    public DBData getFIBData(String userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(FIB_DB, new String[] {KEY_USER_ID,
                        KEY_TIME_STRING,KEY_IP_ADDRESS},KEY_USER_ID + "=?",
                new String[] { userID }, null, null, null, null);

        DBData data = new DBData();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            data.setUserID(cursor.getString(0));
            data.setSensorID(cursor.getString(1));
            data.setIpAddr(cursor.getString(2));

            cursor.close();
        } else {
            data = null; // query found nothing, set return object to null
        }

        db.close();
        return data;
    }

    /**
     * Data is queried without timestring specification; multiple entries may be found.
     * **/
    public ArrayList<DBData> getGeneralCSData(String userID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = "_userID=\"" + userID + "\"";

        Cursor cursor;

        try {
            cursor = db.query(CS_DB, new String[] {KEY_USER_ID,
                            KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_DATA_CONTENTS},
                    whereSelection, null, null, null, null);
        } catch (SQLiteException e) {
            cursor = null; // presumably bad request
        }

        ArrayList<DBData> allValidCSEntries = new ArrayList<DBData>();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                DBData data = new DBData();

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setTimeString(cursor.getString(2));
                data.setProcessID(cursor.getString(3));
                data.setDataFloat(cursor.getString(4));
                allValidCSEntries.add(data);
            }

            cursor.close();
        } else {
            allValidCSEntries = null; // query found nothing, set return object to null
        }

        db.close();
        return allValidCSEntries;
    }

    /** Method used to query entire FIB table; useful when multi-casting interests **/
    public ArrayList<DBData> getAllFIBData() {
        ArrayList<DBData> allFIBData = new ArrayList<DBData>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + FIB_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                DBData data = new DBData();

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setIpAddr(cursor.getString(2));
                allFIBData.add(data);
            }
            cursor.close();
        }

        return allFIBData;
    }

    public DBData getSpecificCSData(String userID, String timeString) {
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = "_userID=\"" + userID + "\" AND timestring=\"" + timeString + "\"";

        Cursor cursor = db.query(CS_DB, new String[] {KEY_USER_ID,
                        KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_DATA_CONTENTS},
                whereSelection, null, null, null, null);

        DBData data = new DBData();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            data.setUserID(cursor.getString(0));
            data.setSensorID(cursor.getString(1));
            data.setTimeString(cursor.getString(2));
            data.setProcessID(cursor.getString(3));
            data.setDataFloat(cursor.getString(4));

            cursor.close();
        } else {
            data = null; // query found nothing, set return object to null
        }

        db.close();
        return data;
    }

    private int updateData(DBData data, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (tableName.equals(PIT_DB)) {

            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
        } else if (tableName.equals(CS_DB)) {

            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_DATA_CONTENTS, data.getDataFloat());
        } else if (tableName.equals(FIB_DB)) {

            values.put(KEY_USER_ID, data.getApplicationName());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
            values.put(KEY_TIME_STRING, data.getTimeString());
        } else {
            // TODO - throw exception
        }

        // updating row
        return db.update(tableName, values,KEY_USER_ID + " = ?",
                new String[] { data.getUserID() });
    }

    public int updateFIBData(DBData data) {
        return updateData(data, FIB_DB);
    }

    public int updatePITData(DBData data) {
        return updateData(data, PIT_DB);
    }

    public int updateCSData(DBData data) {
        return updateData(data, CS_DB);
    }

    public boolean deletePITEntry(String userID, String timeString, String ipAddr) {
        SQLiteDatabase db = this.getWritableDatabase();

        // TODO - use PARAM TIME_STRING
        String whereSelection = "_userID= \"" + userID + "\"" // AND timestring=\"" + timeString
                + " AND ipAddress= \"" + ipAddr + "\"";

        return db.delete(PIT_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    public boolean deleteFIBEntry(String userID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = "_userID= \"" + userID + "\"";

        return db.delete(FIB_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    public boolean deleteCSEntry(String userID, String timeString) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = "_userID= \"" + userID + "\" AND timestring=\"" + timeString
                + "\"";

        return db.delete(CS_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }
}
