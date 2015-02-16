package com.example.androidudpclient;

/*
 * This class is the Data Access Object
 */
public class DBDataSource {

	  // Database fields
	/*  private SQLiteDatabase database;
	  private DatabaseHandler dbHelper;
	  private String[] pitColumns =DatabaseHandler.PIT_COLUMN_ID_ARRAY;
	  private String[] localstoreColumns = DatabaseHandler.LOCAL_STORAGE_ID_ARRAY;*/

	  /*
	   * Create Instance of SQLiteHelper
	   */
/*	  public DBDataSource(Context context) {
	    dbHelper = new DatabaseHandler(context);
	  }

	  /*
	   * Open Database
	   */
	/*  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  /*
	   * Close Database
	   */
/*	  public void close() {
	    dbHelper.close();
	  }

	  /*
	   * Called when Incoming PIT is not in PIT AND not in datastore
	   */
	/*  public Data insertInterest(List<String> data) {
	    ContentValues values = new ContentValues();
	    
	    StringBuilder s = new StringBuilder();
	    
	    int i = 0;
	    while (i < (data.size()-1)){
	    	values.put(pitColumns[i], data.get(i));
	    	
	    	s.append(pitColumns[i] + " = " + data.get(i));
	    	if(i < (data.size()-2)){
	    		s.append(" AND ");
	    	}

            // TODO - i isn't updated? what is point of method?

	    }
	    
	    long insertId = database.insert(DatabaseHandler.PENDING_INTERESTS, null,
	        values);
	    Cursor cursor = database.query(DatabaseHandler.PENDING_INTERESTS, pitColumns,
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
	/*  public void deleteInterest(Data interest) {
		    
		  
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
	    
	    database.delete(DatabaseHandler.PENDING_INTERESTS,
	    		pitColumns[0] + " = " + domainname + " AND "
	    		+ pitColumns[1] + " = " + userid + " AND "
	    		+ pitColumns[2] + " = " + sensorid + " AND "
	    		+ pitColumns[3] + " = " + timestring + " AND "
	    		+ pitColumns[4] + " = " + processid + "AND"
	    		+ pitColumns[5] + " = " + IPAddr,
	    		null);
	  }
	  
	  /*
	   * Method for Retrieving List of Interests
	   */
	/*  public List<Data> getInterests() {
	    List<Data> datalist = new ArrayList<Data>();

	    Cursor cursor = database.query(DatabaseHandler.PENDING_INTERESTS,
	        pitColumns, null, null, null, null, null);

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
	/*  public List<Data> getData() {
	    List<Data> datalist = new ArrayList<Data>();

	    Cursor cursor = database.query(DatabaseHandler.LOCAL_STORAGE,
	        localstoreColumns, null, null, null, null, null);

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
	 /* public int interestExists(Data interest){
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
	/*  public int dataExists(){
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
	/*  private Data cursorToInterest(Cursor cursor) {
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
	/*  private Data cursorToData(Cursor cursor) {
	    Data data = new Data();
	    data.setDomainName(cursor.getString(0));
	    data.setUserId(cursor.getString(1));
	    data.setSensorId(cursor.getString(2));
	    data.setTimestring(cursor.getString(3));
	    data.setProcessId(cursor.getString(4));
	    data.setData(cursor.getFloat(5));
	    return data;
	  }*/
}