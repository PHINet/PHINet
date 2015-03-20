package com.ndnhealthnet.androidudpclient;

public class DBData {

    private String sensorID;
    private String processID;
    private String timeString;
    private String userID;
    private String ipAddr;
    private String dataFloat;

    public DBData() {}

   /* // TODO - later use and specify constructors // Data Cache Constructor
    public DBData(String applicationName, String sensorID, String processID, String timeString,
                  String userID, String datafloat) {
        this.applicationName = applicationName;
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.dataFloat = datafloat;
    }

    // PIT Entry Constructor
    public DBData(String applicationName, String sensorID, String processID, String timeString,
                  String userID, String ipAddr) {
        this.applicationName = applicationName;
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.ipAddr = ipAddr;
    }*/

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {

        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.timeString = timeString;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getDataFloat() {
        return dataFloat;
    }

    public void setDataFloat(String dataFloat) {
        this.dataFloat = dataFloat;
    }
}
