package com.ndnhealthnet.androidudpclient.DB;

import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
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
    private String packetName;
    private String packetContent;
    private int sensorCollectionInterval;
    private int freshnessPeriod;

    public DBData() {}

    /**
     * Constructor for CS
     *
     * @param sensorID associated with CS Data
     * @param processID associated with CS Data
     * @param timeString associated with CS Data
     * @param userID associated with CS Data
     * @param dataPayload associated with CS Data
     * @param freshnessPeriod associated with CS Data
     */
    public DBData(String sensorID, String processID, String timeString,
                  String userID, String dataPayload, int freshnessPeriod) {
        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.dataFloat = dataPayload;
        this.freshnessPeriod = freshnessPeriod;
    }

    /**
     * Constructor for PIT
     *
     * @param sensorID associated with PIT data
     * @param processID associated with PIT data
     * @param timeString associated with PIT data
     * @param userID associated with PIT data
     * @param ipAddr associated with PITS data
     */
    public DBData(String sensorID, String processID, String timeString,
                  String userID, String ipAddr) {

        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.ipAddr = ipAddr;
    }

    /**
     * unambiguous FIB DBData constructor
     *
     * @param userID associated with FIB data
     * @param timeString associated with FIB data
     * @param ipAddr associated with FIB data
     */
    public DBData(String userID, String timeString, String ipAddr) {

        if (timeString.equals(ConstVar.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.userID = userID;
        this.timeString = timeString;
        this.ipAddr = ipAddr;
    }

    /**
     * Constructor for object that holds sensor data
     *
     * @param sensorID of given sensor
     * @param collectionInterval of given sensor
     */
    public DBData(String sensorID, int collectionInterval) {
        this.sensorID = sensorID;
        this.sensorCollectionInterval = collectionInterval;
    }

    /**
     * Constructor for object that holds packet data that
     * is to be stored/manipulated by the database
     *
     * @param packetName of given packet
     * @param packetContent of given packet
     */
    public DBData(String packetName, String packetContent) {
        this.packetName = packetName;
        this.packetContent = packetContent;
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

        if (timeString.equals(ConstVar.CURRENT_TIME)) {
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

    public String getPacketName() {
        return packetName;
    }

    public void setPacketName(String packetName) {
        this.packetName = packetName;
    }

    public int getSensorCollectionInterval() {
        return sensorCollectionInterval;
    }

    public void setSensorCollectionInterval(int sensorCollectionInterval) {
        this.sensorCollectionInterval = sensorCollectionInterval;
    }

    public String getPacketContent() {
        return packetContent;
    }

    public void setPacketContent(String packetContent) {
        this.packetContent = packetContent;
    }

    public int getFreshnessPeriod() {
        return freshnessPeriod;
    }

    public void setFreshnessPeriod(int freshnessPeriod) {
        this.freshnessPeriod = freshnessPeriod;
    }
}
