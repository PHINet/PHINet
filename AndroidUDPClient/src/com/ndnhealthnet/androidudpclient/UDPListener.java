package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Packet.DataPacket;
import com.ndnhealthnet.androidudpclient.Packet.InterestPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class handles incoming UDP packets.
 */
public class UDPListener extends Thread {

    static  DatagramSocket clientSocket = null;
    static Context context;

    public UDPListener(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {
            InetSocketAddress address = new InetSocketAddress(MainActivity.deviceIP, MainActivity.devicePort);

            clientSocket = new DatagramSocket(null);
            clientSocket.bind(address); // give receiver static address

            // set timeout so to force thread to check whether its execution is valid
            clientSocket.setSoTimeout(1000);

            byte[] receiveData = new byte[1024];

            while (MainActivity.continueReceiverExecution) { // loop for packets

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    clientSocket.receive(receivePacket);

                    // process incoming packet
                    handleIncomingNDNPacket(new String(receivePacket.getData()));

                } catch (SocketTimeoutException e) {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    /**
     * Helper method that handles all incoming packets;
     * may be invoked from elsewhere in code (namely, UDPSocket)
     * **/
    static void handleIncomingNDNPacket(String packetData) {

        // NOTE: temporary debugging print
        System.out.println("incoming packet: " + packetData);

        String [] packetDataArray;

        // remove "null" unicode characters
        packetDataArray = packetData.replaceAll("\u0000", "").split(" ");

        // NOTE; temporary debug print
        for (int i = 0; i < packetDataArray.length; i++) {
            System.out.println(packetDataArray[i]);
        }

        if (packetDataArray[0].equals(StringConst.DATA_TYPE)) {

            handleDataPacket(packetDataArray);
        } else if (packetDataArray[0].equals(StringConst.INTEREST_TYPE)) {

            handleInterestPacket(packetDataArray);
        } else {
            // throw away, packet is neither INTEREST nor DATA
        }
    }

    /** handles INTEREST packet as per NDN specification
     * Method parses packet then asks the following questions:
     * 1. Do I have the data?
     * 2. Have I already sent an interest for this data?
     */
    static void handleInterestPacket(String[] packetDataArray) {
        String [] nameComponent = null;

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {

                // NOTE: temporary debugging print
                System.out.println("INTEREST NAME: " +  packetDataArray[i+2]);

                // i+2 corresponds name as per NDN standard
                // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name
                nameComponent = packetDataArray[i+2].split("/"); // split into various components

            } else {
                // TODO - inspect other packet elements
            }
        }

        // information extracted from our name format:
        // "/ndn/userID/sensorID/timestring/processID/ip"
        // the indexes used are position + 1 (+1 is due to string properties)
        String packetUserID = nameComponent[2];
        String packetSensorID = nameComponent[3];
        String packetTimeString = nameComponent[4];
        String packetProcessID = nameComponent[5];
        String packetIP = nameComponent[6];

        if (packetProcessID.equals(StringConst.INTEREST_FIB)) {

            handleInterestFIBRequest(packetUserID, packetSensorID, packetIP);
        } else if (packetProcessID.equals(StringConst.INTEREST_CACHE_DATA)) {

            handleInterestCacheRequest(packetUserID, packetSensorID, packetTimeString,
                    packetProcessID, packetIP);
        } else {
            // unknown process id; drop packet
        }
    }

    /** returns entire FIB to user who requested it **/
    static void handleInterestFIBRequest(String packetUserID, String packetSensorID, String packetIP)
    {
        ArrayList<DBData> allFIBData = MainActivity.datasource.getAllFIBData();

        String mySensorID = Utils.getFromPrefs(context, Utils.PREFS_LOGIN_SENSOR_ID_KEY, "");
        String myUserID = Utils.getFromPrefs(context, Utils.PREFS_LOGIN_USER_ID_KEY, "");

        if (allFIBData == null || allFIBData.size() == 0) {

            DataPacket dataPacket = new DataPacket(packetUserID, packetSensorID,
                    StringConst.CURRENT_TIME, StringConst.DATA_FIB, MainActivity.deviceIP);

            new UDPSocket(MainActivity.devicePort, packetIP, StringConst.DATA_TYPE)
                    .execute(dataPacket.toString()); // reply to interest with DATA from cache*/
        } else {

            for (int i = 0; i < allFIBData.size(); i++) {

                // don't send data to same node that requested; check first
                if (!allFIBData.get(i).getIpAddr().equals(packetIP)) {

                    // content returned in format: "userID,userIP"
                    String fibContent = allFIBData.get(i).getUserID() + "," + allFIBData.get(i).getIpAddr();

                    DataPacket dataPacket = new DataPacket(myUserID, mySensorID,
                            StringConst.CURRENT_TIME,  StringConst.DATA_FIB, fibContent);

                    new UDPSocket(MainActivity.devicePort, packetIP, StringConst.DATA_TYPE)
                            .execute(dataPacket.toString()); // send interest packet
                }
            }
        }
    }

    /** performs NDN logic on packet that requests data **/
    static void handleInterestCacheRequest(String packetUserID, String packetSensorID, String packetTimeString,
                            String packetProcessID, String packetIP)
    {
        // first, check CONTENT STORE (cache)
        ArrayList<DBData> csDATA = MainActivity.datasource.getGeneralCSData(packetUserID);

        if (csDATA != null) {

            for (int i = 0; i < csDATA.size(); i++) {

                // only reply to interest with data that matches date-request
                if (isValidForTimeInterval(packetTimeString, csDATA.get(i).getTimeString())) {

                    DataPacket dataPacket = new DataPacket(csDATA.get(i).getUserID(), csDATA.get(i).getSensorID(),
                            csDATA.get(i).getTimeString(), csDATA.get(i).getProcessID(), csDATA.get(i).getDataFloat());

                    new UDPSocket(MainActivity.devicePort, packetIP, StringConst.DATA_TYPE)
                            .execute(dataPacket.toString()); // reply to interest with DATA from cache
                }
            }

        } else {
            // second, check PIT

            if (MainActivity.datasource.getGeneralPITData(packetUserID) == null) {

                // add new request to PIT, then look into FIB before sending request
                DBData newPITEntry = new DBData();
                newPITEntry.setUserID(packetUserID);
                newPITEntry.setSensorID(packetSensorID);
                newPITEntry.setTimeString(packetTimeString);
                newPITEntry.setProcessID(packetProcessID);
                newPITEntry.setIpAddr(packetIP);

                MainActivity.datasource.addPITData(newPITEntry);

                ArrayList<DBData> allFIBData = MainActivity.datasource.getAllFIBData();

                if (allFIBData == null || allFIBData.size() == 0) {

                    // FIB is empty, user must reconfigure
                    throw new NullPointerException("Cannot send message; FIB is empty.");
                } else {

                    for (int i = 0; i < allFIBData.size(); i++) {

                        // don't send data to same node that requested; check first
                        if (!allFIBData.get(i).getIpAddr().equals(packetIP)
                                && !allFIBData.get(i).getIpAddr().equals("null")) {

                            InterestPacket interestPacket = new InterestPacket(packetUserID, packetSensorID,
                                    packetTimeString,  packetProcessID, packetIP);

                            new UDPSocket(MainActivity.devicePort, allFIBData.get(i).getIpAddr(), StringConst.INTEREST_TYPE)
                                    .execute(interestPacket.toString()); // send interest packet
                        }
                    }
                }
            } else {

                // add new request to PIT and wait, request has already been sent
                DBData newPITEntry = new DBData();
                newPITEntry.setUserID(packetUserID);
                newPITEntry.setSensorID(packetSensorID);
                newPITEntry.setTimeString(packetTimeString);
                newPITEntry.setProcessID(packetProcessID);
                newPITEntry.setIpAddr(packetIP);

                MainActivity.datasource.addPITData(newPITEntry);
            }
        }
    }

    /** Method returns true if the data interval is within request interval **/
    static  boolean isValidForTimeInterval(String requestInterval, String dataInterval) {

        String [] requestIntervals = requestInterval.split("\\|\\|"); // split interval into start/end

        // TIME_STRING FORMAT: "yyyy-MM-dd||yyyy-MM-dd"; the former is start, latter is end

        boolean beforeStartDate = false;
        boolean afterEndDate = false;

        Date startDate, endDate, dataDate;

        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            startDate = df.parse(requestIntervals[0]);
            endDate = df.parse(requestIntervals[1]);
            dataDate = df.parse(dataInterval);

            beforeStartDate = dataDate.before(startDate);
            afterEndDate = dataDate.after(endDate);

        } catch (ParseException e) {

            return false; // some problem occurred, default return is false
        }

        // if dataInterval is not before start and not after end, then its with interval
        return (!beforeStartDate && !afterEndDate) || requestInterval.equals(dataInterval);
    }

    /** handles DATA packet as per NDN specification
     * Method parses packet then stores in cache if requested,
     * and sends out to satisfy any potential Interests.
     */
    static void handleDataPacket(String[] packetDataArray)
    {

        String [] nameComponent = null;
        String dataContents = null;

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {
                // i+2 corresponds name as per NDN standard
                // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name

                // NOTE: debugging print only
                System.out.println("name component: " + nameComponent);

                nameComponent = packetDataArray[i+2].trim().split("/"); // split into various components

            } else if (packetDataArray[i].equals("CONTENT-TYPE")) {

                // i+2 corresponds content as per NDN standard
                // i = notifier (CONTENT-TYPE), i+1 = bytes, i+2 = content
                dataContents = packetDataArray[i+2];
            } else {
                // TODO - inspect other packet elements
            }
        }

        // information extracted from our name format:
        // "/ndn/userID/sensorID/timestring/processID/floatContent"
        // the indexes used are position + 1 (+1 is due to string properties)
        String packetUserID = nameComponent[2].trim();
        String packetSensorID = nameComponent[3].trim();
        String packetTimeString = nameComponent[4].trim();
        String packetProcessID = nameComponent[5].trim();
        String packetFloatContent = dataContents.trim();

        // first, determine who wants the data
        ArrayList<DBData> allValidPITEntries = MainActivity.datasource
                .getGeneralPITData(packetUserID);

        if (allValidPITEntries == null || allValidPITEntries.size() == 0) {
            // no one requested the data, merely drop it
        } else {

            // determine if data packet's time interval matches any requests
            int requestCount = 0;
            for (int i = 0; i < allValidPITEntries.size(); i++) {

                if (isValidForTimeInterval(allValidPITEntries.get(i).getTimeString(), packetTimeString)) {
                    requestCount++;
                }
            }

            if (requestCount > 0) { // positive request count, process packet now
                if (packetProcessID.equals(StringConst.DATA_FIB)) {

                    handleFIBData(packetFloatContent);

                } else if (packetProcessID.equals(StringConst.DATA_CACHE)) {

                    handleCacheData(packetUserID, packetSensorID, packetTimeString, packetProcessID,
                            packetFloatContent, allValidPITEntries);
                } else {
                    // unknown process id; drop packet
                }
            }
        }
    }

    /** Method handles incoming Non-FIB data**/
    static void handleCacheData(String packetUserID,String  packetSensorID,String  packetTimeString,
                         String  packetProcessID,String  packetFloatContent,
                         ArrayList<DBData> allValidPITEntries) {

        // data was requested; second, update cache with new packet
        DBData data = new DBData();

        data.setUserID(packetUserID);
        data.setSensorID(packetSensorID);
        data.setTimeString(packetTimeString);
        data.setProcessID(packetProcessID);
        data.setDataFloat(packetFloatContent);

        // if data exists in cache, just update
        if (MainActivity.datasource.getGeneralCSData(packetUserID) != null) {
            MainActivity.datasource.updateCSData(data);
        } else {
            // data not in cache, add now
            MainActivity.datasource.addCSData(data);
        }

        // now, send packets to each entity that requested the data
        for (int i = 0; i < allValidPITEntries.size(); i++) {

            // data satisfies PIT entry; delete the entry
            MainActivity.datasource.deletePITEntry(allValidPITEntries.get(i).getUserID(),
                    allValidPITEntries.get(i).getTimeString(), allValidPITEntries.get(i).getIpAddr());

            // another device requested the data, send reply as Datapacket
            if (!allValidPITEntries.get(i).getIpAddr().equals(MainActivity.deviceIP))  {

                // NOTE: params list = Context context, String timestring, String processID, String content
                DataPacket dataPacket = new DataPacket(packetUserID,
                        packetSensorID, packetTimeString, packetProcessID, packetFloatContent);

                new UDPSocket(MainActivity.devicePort, allValidPITEntries.get(i).getIpAddr(), StringConst.DATA_TYPE)
                        .execute(dataPacket.toString()); // send DATA packet
            }
        }
    }

    /** Method handles incoming FIB data**/
    static void handleFIBData(String packetFloatContent) {

        DBData data = new DBData();

        // data packet contains requested fib data, store in fib now
        String myUserID = Utils.getFromPrefs(context, Utils.PREFS_LOGIN_USER_ID_KEY, "");

        // expected format: "userID,userIP"
        String [] packetFIBContent = packetFloatContent.split(","); // TODO - don't rely on this assumption

        data.setUserID(packetFIBContent[0].trim());
        data.setIpAddr(packetFIBContent[1].trim());
        data.setTimeString(StringConst.CURRENT_TIME);

        // don't add data for self here
        if (!data.getUserID().equals(myUserID)) {

            DBData fibCheckObject = MainActivity.datasource.getFIBData(data.getUserID());

            if (fibCheckObject == null) {
                MainActivity.datasource.addFIBData(data);
            } else {
                MainActivity.datasource.updateFIBData(data);
            }
        }
    }
}