package com.example.androidudpclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySqliteHelper extends SQLiteOpenHelper {

  public static final String PENDING_INTERESTS = "pit";
  public static final String LOCAL_STORAGE = "localstore";
  public static final String[] PIT_COLUMN_ID_ARRAY = {"domainname", "userid", "sensorid", "timestring", "processeid", "incomingIP"};
  public static final String[] LOCAL_STORAGE_ID_ARRAY = {"domainname", "userid", "sensorid", "timestring", "processeid", "data"};
  public static final String COLUMN_COMMENT = "comment";

  private static final String DATABASE_NAME = "sensordata.db";
  private static final int DATABASE_VERSION = 1;
  
  // Database creation sql statements
  
//  Interest Table
//  Domainname 			text:  �ndn�
//  userid PK/CK: 		text firstname+lastname+dob
//  sensorID PK/CK 		text: �heartbeat�
//  timestring PK/CK 	text: time OR string:time_start+�-�+time_end
//  processed PK/CK 	text:	  �average� OR string: �instance�

//CREATE TABLE something (column1, column2, column3, PRIMARY KEY (column1, column2));  
  
  private static final String PIT_CREATE = "create table "
      + PENDING_INTERESTS + "(" 
      + PIT_COLUMN_ID_ARRAY[0] + " text not null, "
      + PIT_COLUMN_ID_ARRAY[1] + " text not null, " 
      + PIT_COLUMN_ID_ARRAY[2] + " text not null, " 
      + PIT_COLUMN_ID_ARRAY[3] + " text not null, " 
      + PIT_COLUMN_ID_ARRAY[4] + " text not null, "
      + PIT_COLUMN_ID_ARRAY[5] + " text not null, "
      + "PRIMARY KEY (PIT_COLUMN_ID_ARRAY [0], "
      				+ "PIT_COLUMN_ID_ARRAY [1], "
      				+ "PIT_COLUMN_ID_ARRAY [2], "
      				+ "PIT_COLUMN_ID_ARRAY [3], "
      				+ "PIT_COLUMN_ID_ARRAY [4], "
      				+ "PIT_COLUMN_ID_ARRAY [5]));";
  
//  Data Table
//  Domainname 			text:  �ndn�
//  userid PK/CK: 		text firstname+lastname+dob
//  sensorID PK/CK 		text: �heartbeat�
//  timestring PK/CK 	text: time OR string:time_start+�-�+time_end
//  processed PK/CK 	text:	  �average� OR string: �instance�
//  data real: 			real number value for heartbeat or average  
  private static final String LOCALSTORE_CREATE = "create table "
		  + LOCAL_STORAGE + "(" 
	      + LOCAL_STORAGE_ID_ARRAY[0] + " text not null, "
	      + LOCAL_STORAGE_ID_ARRAY[1] + " text not null, " 
	      + LOCAL_STORAGE_ID_ARRAY[2] + " text not null, " 
	      + LOCAL_STORAGE_ID_ARRAY[3] + " text not null, " 
	      + LOCAL_STORAGE_ID_ARRAY[4] + " text not null, "
	      + LOCAL_STORAGE_ID_ARRAY[5] + " real not null, " 
	      + "PRIMARY KEY (LOCAL_STORAGE_ID_ARRAY [0], "
	      				+ "LOCAL_STORAGE_ID_ARRAY [1], "
	      				+ "LOCAL_STORAGE_ID_ARRAY [2], "
	      				+ "LOCAL_STORAGE_ID_ARRAY [3], "
	      				+ "LOCAL_STORAGE_ID_ARRAY [4]));";

  
  /*Create Instance of MysqlHelper (implementation of interface required 
   * 			to create/edit/access sqlite3 db on android platform
  */
  public MySqliteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /*
   * Create Sql Database
   * (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
   */
  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(PIT_CREATE);
    database.execSQL(LOCALSTORE_CREATE);
  }

  /*
   * Update Sql Database
   * (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySqliteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + PENDING_INTERESTS);
    db.execSQL("DROP TABLE IF EXISTS " + LOCAL_STORAGE);
    onCreate(db);
  }

} 