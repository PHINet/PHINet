package com.example.androidudpclient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Data {

    private String applicationName;
    private String sensorID;
    private String processID;
    private String timeString;
    private String userID;
    private String ipAddr;
    private float datafloat;

    static final String CURRENT_TIME = "CURRENT_TIME"; // const notifies current time should be given

    public Data() {}

    // Data Cache Constructor
    public Data(String applicationName, String sensorID, String processID, String timeString,
                String userID, float datafloat) {
        this.applicationName = applicationName;
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.datafloat = datafloat;
    }

    // PIT Entry Constructor
    public Data(String applicationName, String sensorID, String processID, String timeString,
                String userID, String ipAddr) {
        this.applicationName = applicationName;
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.ipAddr = ipAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

        if (timeString.equals(CURRENT_TIME)) {
            SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");
            formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            timeString =  formatUTC.format(new Date()).toString();
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

    public float getDatafloat() {
        return datafloat;
    }

    public void setDatafloat(float datafloat) {
        this.datafloat = datafloat;
    }
}
