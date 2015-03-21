package com.ndnhealthnet.androidudpclient;

public class DBData {

    private String sensorID;
    private String processID;
    private String timeString;
    private String userID;
    private String ipAddr;
    private String dataFloat;

    public DBData() {}

    // constructor for either pit/cs
    public DBData(String type, String sensorID, String processID, String timeString,
                  String userID, String fifthField) {
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;

        // assigns fifth field based upon type of db data
        if (type.equals(StringConst.PIT_DB)) {
            this.ipAddr = fifthField; // PIT, by nature, gets IP
        } else if (type.equals(StringConst.CS_DB)) {
            this.dataFloat = fifthField; // ContentStore, by nature, gets data float
        } else {
            throw new NullPointerException("Error creating DBData object: unknown type.");
        }
    }

    // unambiguous FIB DBData constructor
    public DBData(String userID, String timestring, String ipAddr) {
        this.userID = userID;
        this.timeString = timestring;
        this.ipAddr = ipAddr;
    }

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
