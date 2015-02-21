package com.example.androidudpclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class handles logic associated with three DB tables
 * the PIT, CS, FIB (whose contents are specified by NDN)
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String CS_DB = "ContentStore";
    private static final String PIT_DB = "PendingInterestTable";
    private static final String FIB_DB = "ForwardingInformationBase";

    private static final String DATABASE_NAME = "NDNHealthNet5";
    private static final int DATABASE_VERSION = 5;

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

        String CREATE_DATABASE_TABLE = "CREATE TABLE " + CS_DB + "("
                + KEY_USER_ID + " TEXT ," +  KEY_SENSOR_ID + " TEXT," +
                KEY_TIME_STRING + " TEXT ," + KEY_PROCESS_ID + " TEXT," +KEY_DATA_CONTENTS +
                " REAL, " + "PRIMARY KEY(" + KEY_USER_ID + ", " + KEY_TIME_STRING + "))";

        db.execSQL(CREATE_DATABASE_TABLE); // create CS

        CREATE_DATABASE_TABLE = "CREATE TABLE " + PIT_DB + "("
                +KEY_USER_ID + " TEXT PRIMARY KEY," + KEY_SENSOR_ID + " TEXT," +
               KEY_TIME_STRING + " TEXT," +KEY_PROCESS_ID + " TEXT," +KEY_IP_ADDRESS + " TEXT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create PIT

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
        addData(data, CS_DB);
    }

    public void addFIBData(DBData data) {
        addData(data, FIB_DB);
    }

    private DBData getData(String id, String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        // TODO - rework this feature
        if (tableName.equals(PIT_DB)) {

            cursor = db.query(tableName, new String[] {KEY_USER_ID,
                           KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},KEY_USER_ID + "=?",
                    new String[] { id }, null, null, null, null);
        } else if (tableName.equals(FIB_DB)) {

            cursor = db.query(tableName, new String[] {KEY_USER_ID,
                            KEY_TIME_STRING,KEY_IP_ADDRESS},KEY_USER_ID + "=?",
                    new String[] { id }, null, null, null, null);
        } else if (tableName.equals(CS_DB)) {

            cursor = db.query(tableName, new String[] {KEY_USER_ID,
                           KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_DATA_CONTENTS},KEY_USER_ID + "=?",
                    new String[] { id }, null, null, null, null);
        } else {
            // TODO - throw exception
        }

        DBData data = new DBData();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            // TODO - add to appropriate fields
            if (tableName.equals(CS_DB)) {
                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setTimeString(cursor.getString(2));
                data.setProcessID(cursor.getString(3));
                data.setDataFloat(cursor.getFloat(4));
            } else if (tableName.equals(FIB_DB)) {

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setIpAddr(cursor.getString(2));
            } else if (tableName.equals(PIT_DB)) {

                data.setUserID(cursor.getString(0));
                data.setSensorID(cursor.getString(1));
                data.setTimeString(cursor.getString(2));
                data.setProcessID(cursor.getString(3));
                data.setIpAddr(cursor.getString(4));
            } else {
                // TODO - here
            }


            cursor.close();

        } else {
            data = null; // query found nothing, set return object to null
        }

        db.close();
        return data;
    }

    public DBData getPITData(String id) {
        return getData(id, PIT_DB);
    }

    public DBData getFIBData(String id) {
        return getData(id, FIB_DB);
    }

    public DBData getCSData(String id) {
        return getData(id, CS_DB);
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

    private void deleteData(String id, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName,KEY_USER_ID + " = ?",
                new String[] { id });
        db.close();
    }

    public void deletePITEntry(String id) {
        deleteData(id, PIT_DB);
    }

    public void deleteFIBEntry(String id) {
        deleteData(id, FIB_DB);
    }

    public void deleteCSEntry(String id) {
        deleteData(id, CS_DB);
    }
}
