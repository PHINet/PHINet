package com.example.androidudpclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {


    private static final String TABLE_DATABASE = "database";

   /* public static final String PENDING_INTERESTS = "pit";
    public static final String LOCAL_STORAGE = "localstore";
    public static final String[] PIT_COLUMN_ID_ARRAY = {"domainname", "userid", "sensorid", "timestring", "processeid", "incomingIP"};
    public static final String[] LOCAL_STORAGE_ID_ARRAY = {"domainname", "userid", "sensorid", "timestring", "processeid", "data"};
    public static final String COLUMN_COMMENT = "comment";*/

    private static final String DATABASE_NAME = "sensordata.db";
    private static final int DATABASE_VERSION = 1;

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SENSOR = "phone_number";
    private static final String KEY_TIME = "email";
   // private final ArrayList<Data> databaseList = new ArrayList<Data>();

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DATABASE_TABLE = "CREATE TABLE " + TABLE_DATABASE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_SENSOR + " TEXT," + KEY_TIME + " TEXT" + ")";
        db.execSQL(CREATE_DATABASE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATABASE);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public void addData(Data data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, data.getApplicationName());
        values.put(KEY_SENSOR, data.getSensorID());
        values.put(KEY_TIME, data.getTimeString());
        values.put(KEY_ID, data.getUserID());

        // Inserting Row
        db.insert(TABLE_DATABASE, null, values);
        db.close(); // Closing database connection
    }

    Data getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DATABASE, new String[] { KEY_ID,
                        KEY_NAME, KEY_SENSOR, KEY_TIME}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        Data data = new Data();

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

    public int updateData(Data data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, data.getApplicationName());
        values.put(KEY_SENSOR, data.getSensorID());
        values.put(KEY_TIME, data.getTimeString());

        // updating row
        return db.update(TABLE_DATABASE, values, KEY_ID + " = ?",
                new String[] { data.getUserID() });
    }

    public void deleteData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATABASE, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    /*public int getTotalDatabase() {
        String countQuery = "SELECT  * FROM " + TABLE_DATABASE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }*/
}
