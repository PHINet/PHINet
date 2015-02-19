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

    private static final String DATABASE_NAME = "sensordata.db";
    private static final int DATABASE_VERSION = 1;

    private static final String USER_ID = "userID";
    private static final String SENSOR_ID = "sensorID";
    private static final String TIME_STRING = "timestring";
    private static final String PROCESS_ID = "processID";
    private static final String IP_ADDRESS = "ipAddress";
    private static final String DATA_CONTENTS = "dataContents";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DATABASE_TABLE = "CREATE TABLE " + CS_DB + "("
                + USER_ID + " INTEGER PRIMARY KEY," +  SENSOR_ID + " INTEGER," +
                TIME_STRING + " TEXT," + PROCESS_ID + " TEXT," + DATA_CONTENTS + "REAL)";

        db.execSQL(CREATE_DATABASE_TABLE); // create CS

        CREATE_DATABASE_TABLE = "CREATE TABLE " + PIT_DB + "("
                + USER_ID + " INTEGER PRIMARY KEY," +  SENSOR_ID + " INTEGER," +
                TIME_STRING + " TEXT," + PROCESS_ID + " TEXT," + IP_ADDRESS + "TEXT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create PIT

        CREATE_DATABASE_TABLE = "CREATE TABLE " + FIB_DB + "("
                + USER_ID + " INTEGER PRIMARY KEY," +  TIME_STRING +
                "TEXT, " + IP_ADDRESS + "TEXT)";

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

            values.put(USER_ID, data.getUserID());
            values.put(SENSOR_ID, data.getSensorID());
            values.put(TIME_STRING, data.getTimeString());
            values.put(PROCESS_ID, data.getProcessID());
            values.put(IP_ADDRESS, data.getIpAddr());
        } else if (tableName.equals(CS_DB)) {

            values.put(USER_ID, data.getUserID());
            values.put(SENSOR_ID, data.getSensorID());
            values.put(TIME_STRING, data.getTimeString());
            values.put(PROCESS_ID, data.getProcessID());
            values.put(DATA_CONTENTS, data.getDataFloat());
        } else if (tableName.equals(FIB_DB)) {

            values.put(USER_ID, data.getApplicationName());
            values.put(IP_ADDRESS, data.getIpAddr());
            values.put(TIME_STRING, data.getTimeString());
        } else {
            // TODO - throw exception
        }

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

    private DBData getData(int id, String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        // TODO - rework this feature
        if (tableName.equals(PIT_DB)) {

            cursor = db.query(tableName, new String[] { USER_ID,
                            USER_ID, SENSOR_ID, TIME_STRING}, USER_ID + "=?",
                    new String[] { String.valueOf(id) }, null, null, null, null);
        } else if (tableName.equals(FIB_DB)) {

            cursor = db.query(tableName, new String[] { USER_ID,
                            USER_ID, SENSOR_ID, TIME_STRING}, USER_ID + "=?",
                    new String[] { String.valueOf(id) }, null, null, null, null);
        } else if (tableName.equals(CS_DB)) {

            cursor = db.query(tableName, new String[] { USER_ID,
                            USER_ID, SENSOR_ID, TIME_STRING}, USER_ID + "=?",
                    new String[] { String.valueOf(id) }, null, null, null, null);
        } else {
            // TODO - throw exception
        }

        DBData data = new DBData();

        // ensure query was successful
        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            // TODO - add to appropriate fields
            data.setUserID(cursor.getString(0));
            data.setApplicationName(cursor.getString(1));
            data.setSensorID(cursor.getString(2));
            data.setTimeString(cursor.getString(3));
            cursor.close();

        } else {
            data = null; // query found nothing, set return object to null
        }

        db.close();
        return data;
    }

    public void getPITData(int id) {
        getData(id, PIT_DB);
    }

    public void getFIBData(int id) {
        getData(id, FIB_DB);
    }

    public void getCSData(int id) {
        getData(id, CS_DB);
    }

    /*
    public ArrayList<Data> getData() {
        try {
            databaseList.clear();

            // Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_DATABASE;

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Data database = new Data();
                    // TODO - add to appropriate fields
                    database.setUserId(cursor.getString(0));
                    database.setDomainName(cursor.getString(1));
                    database.setSensorId(cursor.getString(2));
                    database.setTimestring(cursor.getString(3));
                    // Adding data to list
                    databaseList.add(database);
                } while (cursor.moveToNext());
            }

            // return data list
            cursor.close();
            db.close();
            return databaseList;
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("all_database", "" + e);
        }

        return databaseList;
    }*/

    private int updateData(DBData data, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (tableName.equals(PIT_DB)) {

            values.put(SENSOR_ID, data.getSensorID());
            values.put(TIME_STRING, data.getTimeString());
            values.put(PROCESS_ID, data.getProcessID());
            values.put(IP_ADDRESS, data.getIpAddr());
        } else if (tableName.equals(CS_DB)) {

            values.put(SENSOR_ID, data.getSensorID());
            values.put(TIME_STRING, data.getTimeString());
            values.put(PROCESS_ID, data.getProcessID());
            values.put(DATA_CONTENTS, data.getDataFloat());
        } else if (tableName.equals(FIB_DB)) {

            values.put(USER_ID, data.getApplicationName());
            values.put(IP_ADDRESS, data.getIpAddr());
            values.put(TIME_STRING, data.getTimeString());
        } else {
            // TODO - throw exception
        }

        // updating row
        return db.update(tableName, values, USER_ID + " = ?",
                new String[] { data.getUserID() });
    }

    public void updateFIBData(DBData data) {
        updateData(data, FIB_DB);
    }

    public void updatePITData(DBData data) {
        updateData(data, PIT_DB);
    }

    public void updateCSData(DBData data) {
        updateData(data, CS_DB);
    }

    private void deleteData(int id, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, USER_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public void deletePITEntry(int id) {
        deleteData(id, PIT_DB);
    }

    public void deleteFIBEntry(int id) {
        deleteData(id, FIB_DB);
    }

    public void deleteCSEntry(int id) {
        deleteData(id, CS_DB);
    }
}
