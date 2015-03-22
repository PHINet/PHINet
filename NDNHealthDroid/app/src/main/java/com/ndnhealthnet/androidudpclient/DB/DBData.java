package com.ndnhealthnet.androidudpclient.DB;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Class that handles data that is to be stored in DB and/or processed.
 */
public class DBData {

    private String sensorID;
    private String processID;
    private String timeString;
    private String userID;
    private String ipAddr;
    private String dataFloat;

    public DBData() {}

    /**
     * constructor for either pit/cs
     *
     * @param type used in check to decide if constructor used for PIT or CS data
     * @param sensorID associated with PIT/CS data
     * @param processID associated with PIT/CS data
     * @param timeString associated with PIT/CS data
     * @param userID associated with PIT/CS data
     * @param fifthField associated with PIT/CS data, either IP or DATA_CONTENTS - for PIT,CS respectively
     */
    public DBData(String type, String sensorID, String processID, String timeString,
                  String userID, String fifthField) {

        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

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

    /**
     * unambiguous FIB DBData constructor
     *
     * @param userID associated with FIB data
     * @param timeString associated with FIB data
     * @param ipAddr associated with FIB data
     */
    public DBData(String userID, String timeString, String ipAddr) {

        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.userID = userID;
        this.timeString = timeString;
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

    /**
     * Method sets object's timeString. If CURRENT_TIME chose, invoke Utils method.
     */
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
