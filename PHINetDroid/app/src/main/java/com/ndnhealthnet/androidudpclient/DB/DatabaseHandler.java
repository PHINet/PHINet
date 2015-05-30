package com.ndnhealthnet.androidudpclient.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;

import java.util.ArrayList;

/**
 * Class handles logic associated with three DB tables
 * the PIT, CS, FIB (whose contents are specified by NDN)
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // TODO - refactor this class into numerous, specific classes

    private static final String DATABASE_NAME = "NDNHealthNet7";
    private static final int DATABASE_VERSION = 7;

    private static final String KEY_USER_ID = "_userID";
    private static final String KEY_SENSOR_ID = "sensorID";
    private static final String KEY_TIME_STRING = "timeString";
    private static final String KEY_PROCESS_ID = "processID";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String KEY_DATA_CONTENTS = "dataContents";
    private static final String KEY_COLLECTION_INTERVAL = "collectionInterval";
    private static final String KEY_PACKET_NAME = "_packetName";
    private static final String KEY_PACKET_CONTENT = "packetContent";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // keys are USER_ID and TIME_STRING - only one piece of data per user per time instant
        String CREATE_DATABASE_TABLE = "CREATE TABLE " + StringConst.CS_DB + "("
                + KEY_USER_ID + " TEXT ," +  KEY_SENSOR_ID + " TEXT," +
                KEY_TIME_STRING + " TEXT ," + KEY_PROCESS_ID + " TEXT," +KEY_DATA_CONTENTS +
                " TEXT, " + "PRIMARY KEY(" + KEY_USER_ID + ", " + KEY_TIME_STRING + "))";

        db.execSQL(CREATE_DATABASE_TABLE); // create CS

        // keys are USER_ID, TIME_STRING, and IP_ADDRESS
                    // - only one piece of requested data per user per time instant
        CREATE_DATABASE_TABLE = "CREATE TABLE " + StringConst.PIT_DB + "("
                +KEY_USER_ID + " TEXT ," + KEY_SENSOR_ID + " TEXT," +
               KEY_TIME_STRING + " TEXT," +KEY_PROCESS_ID + " TEXT," +KEY_IP_ADDRESS + " TEXT,"
                + "PRIMARY KEY(" + KEY_USER_ID + "," + KEY_TIME_STRING + ", "
                + KEY_IP_ADDRESS+ "))";

        db.execSQL(CREATE_DATABASE_TABLE); // create PIT

        // keys are USER_ID - only location per user
        CREATE_DATABASE_TABLE = "CREATE TABLE " + StringConst.FIB_DB + "("
                +KEY_USER_ID + " TEXT PRIMARY KEY," + KEY_TIME_STRING +
                " TEXT, " +KEY_IP_ADDRESS + " TEXT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create FIB

        // key is sensor_name
        CREATE_DATABASE_TABLE = "CREATE TABLE " + StringConst.SENSOR_DB + "(" +
                KEY_SENSOR_ID + " TEXT PRIMARY KEY," + KEY_COLLECTION_INTERVAL + " BIGINT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create sensor table

        // key is packet name
        CREATE_DATABASE_TABLE = "CREATE TABLE " + StringConst.PACKET_DB + "(" +
                KEY_PACKET_NAME + " TEXT PRIMARY KEY, " + KEY_PACKET_CONTENT + " TEXT)";

        db.execSQL(CREATE_DATABASE_TABLE); // create db to hold packets (only for viewing)
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StringConst.PIT_DB);
        db.execSQL("DROP TABLE IF EXISTS " + StringConst.FIB_DB);
        db.execSQL("DROP TABLE IF EXISTS " + StringConst.CS_DB);
        db.execSQL("DROP TABLE IF EXISTS " + StringConst.SENSOR_DB);
        db.execSQL("DROP TABLE IF EXISTS " + StringConst.PACKET_DB);

        // Create tables again
        onCreate(db);
    }

    /**
     * @param data data object to be entered
     * @param tableName name of table where data should be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    private boolean addData(DBData data, String tableName) {

        if (data == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (tableName.equals(StringConst.PIT_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
        } else if (tableName.equals(StringConst.CS_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_DATA_CONTENTS, data.getDataFloat());
        } else if (tableName.equals(StringConst.FIB_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
            values.put(KEY_TIME_STRING, data.getTimeString());
        } else if (tableName.equals(StringConst.SENSOR_DB)) {

            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_COLLECTION_INTERVAL, data.getSensorCollectionInterval());
        } else if (tableName.equals(StringConst.PACKET_DB)) {

            values.put(KEY_PACKET_NAME, data.getPacketName());
            values.put(KEY_PACKET_CONTENT, data.getPacketContent());
        } else {
           throw new NullPointerException("Cannot add data to DB: param was bad");
        }

        try {

            // Inserting Row
            db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_FAIL);
            db.close(); // Closing database connection

            return true;
        } catch (SQLiteConstraintException e) {

            return false;
        }
    }

    /**
     * Performs insertion into Sensor database
     *
     * @param data of sensor to be inserted
     * @return true if insertion was successful, false otherwise
     */
    public boolean addSensorData(DBData data) {
        return addData(data, StringConst.SENSOR_DB);
    }

    /**
     * Performs insertion into Packet database
     *
     * @param data of Packet to be inserted
     * @return true if insertion was successful, false otherwise
     */
    public boolean addPacketData(DBData data) {
        return addData(data, StringConst.PACKET_DB);
    }

    /**
     * @param data data object to be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    public boolean addPITData(DBData data) {
        return addData(data, StringConst.PIT_DB);
    }

    /**
     * @param data data object to be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    public boolean addCSData(DBData data) {
        return addData(data, StringConst.CS_DB);
    }

    /**
     * @param data data object to be entered
     * @return true if data was successfully entered into DB, false otherwise
     */
    public boolean addFIBData(DBData data) {
        return addData(data, StringConst.FIB_DB);
    }

    /**
     * Data is queried without ipAddr specification; multiple entries may be found.
     *
     * @param userID specifies which PIT entries should be returned
     * @return ArrayList of data for userID param
     */
    public ArrayList<DBData> getGeneralPITData(String userID) {

        if (userID == null) {
            return null; // return empty array list
        }

        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = "_userID=\"" + userID + "\"";

        Cursor cursor = db.query(StringConst.PIT_DB, new String[] {KEY_USER_ID,
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
     * Data is queried with ipAddr specification. At most one entry should be found -
     * because multiple queries for same data should result in updates, not multiple entries;
     * an exception is thrown if multiple entries are detected
     *
     * @param userID specifies which PIT entries should be returned, together with ipAddr
     * @param ipAddr specifies which PIT entries should be returned
     * @return single db entry associated with ip/id combination
     */
    public DBData getSpecificPITData(String userID, String ipAddr) {

        if (userID == null || ipAddr == null) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = "_userID= \"" + userID + "\" AND ipAddress= \"" + ipAddr + "\"";

        Cursor cursor;
        try {
            cursor = db.query(StringConst.PIT_DB, new String[] {KEY_USER_ID,
                            KEY_SENSOR_ID,KEY_TIME_STRING,KEY_PROCESS_ID,KEY_IP_ADDRESS},
                    whereSelection, null, null, null, null);
        } catch (SQLiteException e) {
            cursor = null; // bad query apparently

        }

        if (cursor.getCount() > 1) {
            db.close();
            throw new IllegalStateException("!!Error querying PIT data: redundant entries found.");
        } else {
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
    }

    /**
     * Method queries and returns entire Sensor DB
     *
     * @return an ArrayList of type DBData containing entire Sensor database; null if db empty
     */
    public ArrayList<DBData> getAllSensorData() {

        ArrayList<DBData> allSensorData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + StringConst.SENSOR_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                DBData data = new DBData();

                data.setSensorID(cursor.getString(0));
                data.setSensorCollectionInterval(cursor.getInt(1));
                allSensorData.add(data);
            }
            cursor.close();
        }

        return allSensorData;
    }

    /**
     * Queries and returns a specific Sensor database entry
     *
     * @return sensor entry if found; otherwise, null
     */
    public DBData getSpecificSensorData(String sensorID) {

        DBData sensorData = new DBData();
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = KEY_SENSOR_ID + " = \"" + sensorID + "\"";
        Cursor cursor = db.query(StringConst.SENSOR_DB, new String[]{KEY_SENSOR_ID,
                        KEY_COLLECTION_INTERVAL}, whereSelection, null, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {

            cursor.moveToFirst();

            sensorData.setSensorID(cursor.getString(0));
            sensorData.setSensorCollectionInterval(cursor.getInt(1));
            cursor.close();
        }

        return sensorData;
    }

    /**
     * Method queries and returns entire Packet DB
     *
     * @return an ArrayList of type DBData containing entire Packet database; null if db empty
     */
    public ArrayList<DBData> getAllPacketData() {

        ArrayList<DBData> allPacketData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + StringConst.PACKET_DB, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            // return each row
            while (cursor.moveToNext()) {
                DBData data = new DBData();

                data.setPacketName(cursor.getString(0));
                data.setPacketContent(cursor.getString(1));
                allPacketData.add(data);
            }
            cursor.close();
        }

        return allPacketData;
    }

    /**
     * Method returns specific, single FIB entry.
     *
     * @param userID associated with entry to be returned
     * @return entry if found, otherwise null returned
     */
    public DBData getFIBData(String userID) {

        if (userID == null) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String whereSelection = "_userID=\"" + userID + "\"";
        Cursor cursor = db.query(StringConst.FIB_DB, new String[]{KEY_USER_ID,
                        KEY_TIME_STRING, KEY_IP_ADDRESS},
                whereSelection, null, null, null, null);

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
     *
     * @param userID associated with entries to be returned
     * @return entries if found, otherwise null returned
     */
    public ArrayList<DBData> getGeneralCSData(String userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String whereSelection = "_userID=\"" + userID + "\"";
        Cursor cursor;

        try {
            cursor = db.query(StringConst.CS_DB, new String[] {KEY_USER_ID,
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

    /**
     * Method used to query entire FIB table; useful when multi-casting interests
     *
     * @return entries if any exist, otherwise null returned
     */
    public ArrayList<DBData> getAllFIBData() {
        ArrayList<DBData> allFIBData = new ArrayList<DBData>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + StringConst.FIB_DB, null);

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

    /**
     * Method returns a single, specific CS entry if it exists.
     *
     * @param userID associated with entry to be returned
     * @param timeString associated with entry to be returned
     * @return entry if found, otherwise null returned
     */
    public DBData getSpecificCSData(String userID, String timeString) {
        SQLiteDatabase db = this.getReadableDatabase();

        String whereSelection = "_userID=\"" + userID + "\" AND timeString=\"" + timeString + "\"";

        Cursor cursor = db.query(StringConst.CS_DB, new String[]{KEY_USER_ID,
                        KEY_SENSOR_ID, KEY_TIME_STRING, KEY_PROCESS_ID, KEY_DATA_CONTENTS},
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

    /**
     * Method updates a single, specific entry from specified table.
     *
     * @param data object containing updated row contents
     * @param tableName specified name of table to be updated
     * @return true if entry successfully updated, false otherwise
     */
    private boolean updateData(DBData data, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (tableName.equals(StringConst.PIT_DB)) {

            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());

            // updating row
            db.update(tableName, values, KEY_USER_ID + " = ?",
                    new String[]{data.getUserID()});
        } else if (tableName.equals(StringConst.CS_DB)) {

            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_TIME_STRING, data.getTimeString());
            values.put(KEY_PROCESS_ID, data.getProcessID());
            values.put(KEY_DATA_CONTENTS, data.getDataFloat());

            // updating row
            db.update(tableName, values, KEY_USER_ID + " = ?",
                    new String[]{data.getUserID()});
        } else if (tableName.equals(StringConst.FIB_DB)) {

            values.put(KEY_USER_ID, data.getUserID());
            values.put(KEY_IP_ADDRESS, data.getIpAddr());
            values.put(KEY_TIME_STRING, data.getTimeString());

            // updating row
            db.update(tableName, values, KEY_USER_ID + " = ?",
                    new String[]{data.getUserID()});

        } else if (tableName.equals(StringConst.SENSOR_DB)) {

            values.put(KEY_SENSOR_ID, data.getSensorID());
            values.put(KEY_COLLECTION_INTERVAL, data.getSensorCollectionInterval());

            // updating row
            db.update(tableName, values, KEY_SENSOR_ID + " = ?",
                    new String[]{data.getSensorID()});

        } else {
            throw new NullPointerException("Cannot update data in DB; param was bad");
        }

        return true;
    }

    /**
     * Method updates a single Sensor database row
     *
     * @param data allows for identification of row and update
     * @return true of update was successful, false otherwise
     */
    public boolean updateSensorData(DBData data) {
        if (data == null) {
            return false;
        }

        return updateData(data, StringConst.SENSOR_DB);
    }

    /**
     * Method updates a single, specific FIB entry.
     *
     * @param data object containing updated row contents
     * @return true if entry successfully updated, false otherwise
     */
    public boolean updateFIBData(DBData data) {

        if (data == null) {
            return false;
        }

        return updateData(data, StringConst.FIB_DB);
    }

    /**
     * Method updates a single, specific PIT entry.
     *
     * @param data object containing updated row contents
     * @return true if entry successfully updated, false otherwise
     */
    public boolean updatePITData(DBData data) {

        if (data == null) {
            return false;
        }

        return updateData(data, StringConst.PIT_DB);
    }

    /**
     * Method updates a single, specific CS entry.
     *
     * @param data object containing updated row contents
     * @return true if entry successfully updated, false otherwise
     */
    public boolean updateCSData(DBData data) {

        if (data == null) {
            return false;
        }

        return updateData(data, StringConst.CS_DB);
    }

    /**
     * Method deletes a single, specific PIT entry.
     *
     * @param userID associated with entry to be deleted
     * @param timeString associated with entry to be deleted
     * @param ipAddr associated with entry to be deleted
     * @return true if entry successfully deleted, false otherwise
     */
    public boolean deletePITEntry(String userID, String timeString, String ipAddr) {

        if (userID == null || timeString == null || ipAddr == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = "_userID= \"" + userID + "\"" + "AND timestring=\"" + timeString
                + "\" AND ipAddress= \"" + ipAddr + "\"";

        return db.delete(StringConst.PIT_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes a single, specific FIB entry.
     *
     * @param userID associated with entry to be deleted
     * @return true if entry successfully deleted, false otherwise
     */
    public boolean deleteFIBEntry(String userID) {

        if (userID == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = "_userID= \"" + userID + "\"";

        return db.delete(StringConst.FIB_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes a single Sensor database entry
     *
     * @param sensorID of entry to be deleted
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteSensorEntry(String sensorID) {

        if (sensorID == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = KEY_SENSOR_ID + "= \"" + sensorID + "\"";

        return db.delete(StringConst.SENSOR_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes a single, specific CS entry.
     *
     * @param userID associated with entry to be deleted
     * @param timeString associated with entry to be deleted
     * @return true if entry successfully deleted, false otherwise
     */
    public boolean deleteCSEntry(String userID, String timeString) {

        if (userID == null || timeString == null) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        String whereSelection = "_userID= \"" + userID + "\" AND timestring=\"" + timeString
                + "\"";

        return db.delete(StringConst.CS_DB, whereSelection, null) > 0; // returns true if entry was deleted
    }

    /**
     * Method deletes all entries in PIT.
     */
    public void deleteEntirePIT() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(StringConst.PIT_DB, null, null);
    }

    /**
     * Method deletes all entries in CS.
     */
    public void deleteEntireCS() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(StringConst.CS_DB, null, null);
    }

    /**
     * Method deletes all entries in FIB.
     */
    public void deleteEntireFIB() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(StringConst.FIB_DB, null, null);
    }

    /**
     * Method deletes all entires in the Packet DB.
     */
    public void deleteEntirePacketDB() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(StringConst.PACKET_DB, null, null);
    }
}
