package com.example.androidudpclient;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
//import android.support.v4.widget.SearchViewCompatIcs.MySearchView;


/*
 * This class is the Data Access Object
 */
public class DBDataSource {

	  // Database fields
	  private SQLiteDatabase database;
	  private MySqliteHelper dbHelper;
	  private String[] pit_Columns = MySqliteHelper.PIT_COLUMN_ID_ARRAY;
	  private String[] localstore_Columns = MySqliteHelper.LOCAL_STORAGE_ID_ARRAY;

	  /*
	   * Create Instance of SQLiteHelper
	   */
	  public DBDataSource(Context context) {
	    dbHelper = new MySqliteHelper(context);
	  }

	  /*
	   * Open Database
	   */
	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  /*
	   * Close Database
	   */
	  public void close() {
	    dbHelper.close();
	  }

	  /*
	   * Called when Incoming PIT is not in PIT AND not in datastore
	   */
	  public Data insertInterest(List<String> data) {
	    ContentValues values = new ContentValues();
	    
	    StringBuilder s = new StringBuilder();
	    
	    int i = 0;
	    while (i < (data.size()-1)){
	    	values.put(pit_Columns[i], data.get(i));
	    	
	    	s.append(pit_Columns[i] + " = " + data.get(i));
	    	if(i < (data.size()-2)){
	    		s.append(" AND ");
	    	}
	    }
	    
	    long insertId = database.insert(MySqliteHelper.PENDING_INTERESTS, null,
	        values);
	    Cursor cursor = database.query(MySqliteHelper.PENDING_INTERESTS, pit_Columns,
	    		s.toString(), null, null, null, null);
	    cursor.moveToFirst();
	    Data newData = cursorToData(cursor);
	    cursor.close();
	    return newData;
	  }

	  /*
	   * 
	   * Delete Method for PIT
	   */
	//Domainname 			text:  “ndn”
	//userid PK/CK: 		text firstname+lastname+dob
	//sensorID PK/CK 		text: “heartbeat”
	//timestring PK/CK 		text: time OR string:time_start+”-“+time_end
	//processed PK/CK 		text:	  “average” OR string: “instance”
	//data real: 			real number value for heartbeat or average

	  public void deleteInterest(Data interest) {
		    
		  
	    String domainname = interest.getDomainName();
	    String userid = interest.getUserId();
	    String sensorid = interest.getSensorId();
	    String timestring = interest.getTimestring();
	    String processid = interest.getProcessId();
	    String IPAddr = interest.getIPAddr();
	    
	    System.out.println("Data deleted with \n Username: " + userid
	    		+ "\n SensorID: " + sensorid
	    		+ "\n Timestring: " + timestring
	    		+ "\n ProcessID: " + processid);
	    
	    database.delete(MySqliteHelper.PENDING_INTERESTS,
	    		pit_Columns[0] + " = " + domainname + " AND "
	    		+ pit_Columns[1] + " = " + userid + " AND "
	    		+ pit_Columns[2] + " = " + sensorid + " AND "
	    		+ pit_Columns[3] + " = " + timestring + " AND "
	    		+ pit_Columns[4] + " = " + processid + "AND"
	    		+ pit_Columns[5] + " = " + IPAddr,
	    		null);
	  }

	  
	  /*
	   * Method for Retrieving List of Interests
	   */
	  public List<Data> getInterests() {
	    List<Data> datalist = new ArrayList<Data>();

	    Cursor cursor = database.query(MySqliteHelper.PENDING_INTERESTS,
	        pit_Columns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Data data = cursorToInterest(cursor);
	      datalist.add(data);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return datalist;
	  }

	  /*
	   * Method for Retrieving List of Data
	   */
	  public List<Data> getData() {
	    List<Data> datalist = new ArrayList<Data>();

	    Cursor cursor = database.query(MySqliteHelper.LOCAL_STORAGE,
	        localstore_Columns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Data data = cursorToData(cursor);
	      datalist.add(data);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return datalist;
	  }
	  
	  /*
	   * Check if Incoming Interest Exists in PIT
	   */
	  public int interestExists(Data interest){
		  int Exists = 0;
		  List<Data> pit = getInterests();
		  for(int i = 0; i<pit.size(); i++){
			  if (pit.get(i).getDomainName() == interest.getDomainName()){
				  if (pit.get(i).getUserId() == interest.getUserId()){
					  if(pit.get(i).getSensorId() == interest.getSensorId()){
						  if(pit.get(i).getTimestring() == interest.getTimestring()){
							  if(pit.get(i).getProcessId() == interest.getProcessId()){
								  Exists = 1;
							  }
						  }
					  }
				  }
			  }
		  }
		  return Exists;
	  }
	  	
	  
	  /*
	   * Check If Entry in PIT Exists in Local Data Storage
	   */
	  public int dataExists(){ 
		  int Exists = 0;
		  List<Data> interests = getInterests();
		  List<Data> data = getData();
		  for(int i = 0; i<interests.size(); i++){
			  for(int j = 0; j<data.size(); j++){
				  Data tempInterest = interests.get(i);
				  Data tempData = data.get(j);
				  
				  if (tempInterest.getDomainName() == tempData.getDomainName()){
					  if (tempInterest.getUserId() == tempData.getUserId()){
						  if(tempInterest.getSensorId() == tempData.getSensorId()){
							  if(tempInterest.getTimestring() == tempData.getTimestring()){
								  if(tempInterest.getProcessId() == tempData.getProcessId()){
									  Exists = 1;
								  }
							  }
						  }
					  }
				  }
			  }
		  }
		  return Exists;
	  }
	  
	  /*
	   * Method for converting cursor contents to data
	   */
	  private Data cursorToInterest(Cursor cursor) {
	    Data data = new Data();
	    data.setDomainName(cursor.getString(0));
	    data.setUserId(cursor.getString(1));
	    data.setSensorId(cursor.getString(2));
	    data.setTimestring(cursor.getString(3));
	    data.setProcessId(cursor.getString(4));
	    data.setIPAddr(cursor.getString(5));
	    return data;
	  }	  
	  
	  /*
	   * Method for converting cursor contents to data
	   */
	  private Data cursorToData(Cursor cursor) {
	    Data data = new Data();
	    data.setDomainName(cursor.getString(0));
	    data.setUserId(cursor.getString(1));
	    data.setSensorId(cursor.getString(2));
	    data.setTimestring(cursor.getString(3));
	    data.setProcessId(cursor.getString(4));
	    data.setData(cursor.getFloat(5));
	    return data;
	  }
	} 