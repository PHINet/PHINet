package com.example.androidudpclient;


//Domainname 			text:  “ndn”
//userid PK/CK: 		text firstname+lastname+dob
//sensorID PK/CK 		text: “heartbeat”
//timestring PK/CK 		text: time OR string:time_start+”-“+time_end
//processed PK/CK 		text:	  “average” OR string: “instance”
//data real: 			real number value for heartbeat or average

public class Data {
	  private String domainname;
	  private String userid;
	  private String sensorid;
	  private String timestring;
	  private String processid;
	  private String IPAddr;
	  private float datafloat;

	  /*
	   * Domain Name Get/Set
	   */
	  public String getDomainName() {
	    return domainname;
	  }

	  public void setDomainName(String domain) {
	    this.domainname = domain;
	  }
	  
	  /*
	   * UserId Get/Set
	   */
	  public String getUserId() {
		return userid;
	  }

	  public void setUserId(String user) {
		  this.userid = user;
	  }
	  
	  /*
	   * Sensor ID Get/Set
	   */
	  public String getSensorId() {
		  return sensorid;
	  }

	  public void setSensorId(String sensor) {
		  this.sensorid = sensor;
	  }
	  
	  /*
	   * TimeString Get/Set
	   */
	  public String getTimestring() {
		  return timestring;
	  }

	  public void setTimestring(String time) {
		  this.timestring = time;
	  }
	  
	  /*
	   * ProcessId Get/Set
	   */
	  public String getProcessId() {
		  return processid;
	  }

	  public void setProcessId(String process) {
		  this.processid = process;
	  }
	  
	  /*
	   * IP Addr Get/Set
	   */
	  public String getIPAddr() {
		  return IPAddr;
	  }

	  public void setIPAddr(String IP) {
		  this.IPAddr= IP;
	  }

	  /*
	   * Data Get/Set
	   */
	  public float getData() {
	    return datafloat;
	  }

	  public void setData(Float data) {
	    this.datafloat = data;
	  }

	  // Will be used by the ArrayAdapter in the ListView
//	  @Override
//	  public String toString() {
//	  //  return data;
//	  }
	} 
