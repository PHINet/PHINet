package com.ndnhealthnet.androidudpclient;

/**
 *
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
     * @param type
     * @param sensorID
     * @param processID
     * @param timeString
     * @param userID
     * @param fifthField
     */
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

    /**
     * unambiguous FIB DBData constructor
     *
     * @param userID
     * @param timestring
     * @param ipAddr
     */
    public DBData(String userID, String timestring, String ipAddr) {
        this.userID = userID;
        this.timeString = timestring;
        this.ipAddr = ipAddr;
    }

    /**
     *
     *
     * @return
     */
    public String getSensorID() {
        return sensorID;
    }

    /**
     *
     * @param sensorID
     */
    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    /**
     *
     * @return
     */
    public String getProcessID() {
        return processID;
    }

    /**
     *
     * @param processID
     */
    public void setProcessID(String processID) {
        this.processID = processID;
    }

    /**
     *
     * @return
     */
    public String getTimeString() {
        return timeString;
    }

    /**
     *
     * @param timeString
     */
    public void setTimeString(String timeString) {

        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.timeString = timeString;
    }

    /**
     *
     * @return
     */
    public String getUserID() {
        return userID;
    }

    /**
     *
     * @param userID
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     *
     * @return
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     *
     * @param ipAddr
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     *
     * @return
     */
    public String getDataFloat() {
        return dataFloat;
    }

    /**
     *
     * @param dataFloat
     */
    public void setDataFloat(String dataFloat) {
        this.dataFloat = dataFloat;
    }
}
