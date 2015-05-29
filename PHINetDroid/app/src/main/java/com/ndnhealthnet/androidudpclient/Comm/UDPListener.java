package com.ndnhealthnet.androidudpclient.Comm;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Activities.MainActivity;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Packet.DataPacket;
import com.ndnhealthnet.androidudpclient.Packet.InterestPacket;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

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

                    String packetSourceIP = receivePacket.getAddress().getHostAddress();
                    int packetSourcePort = receivePacket.getPort();

                    // process incoming packet
                    handleIncomingNDNPacket(new String(receivePacket.getData()), packetSourceIP, packetSourcePort);

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
     *
     * @param packetData entire packet to have its contents assessed
     */
    static void handleIncomingNDNPacket(String packetData, String ipAddr, int port) {

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

            handleInterestPacket(packetDataArray, ipAddr, port);
        } else {
            // throw away, packet is neither INTEREST nor DATA
        }
    }

    /**
     * handles INTEREST packet as per NDN specification
     * Method parses packet then asks the following questions:
     * 1. Do I have the data?
     * 2. Have I already sent an interest for this data?
     *
     * @param packetDataArray incoming packet after having been "split" by space and placed array
     */
    static void handleInterestPacket(String[] packetDataArray, String ipAddr, int port) {
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

        // add packet content to database for future review
        DBData data = new DBData(Arrays.toString(nameComponent), Arrays.toString(packetDataArray));
        DBSingleton.getInstance(context).getDB().addPacketData(data);
                
        if (packetProcessID.equals(StringConst.INTEREST_FIB)) {

            handleInterestFIBRequest(packetUserID, packetSensorID, ipAddr, port);
        } else if (packetProcessID.equals(StringConst.INTEREST_CACHE_DATA)) {
            System.out.println("interest for cache");
            handleInterestCacheRequest(packetUserID, packetSensorID, packetTimeString,
                    packetProcessID, ipAddr, port);
        } else {
            System.out.println("dropped interest");
            // unknown process id; drop packet
        }
    }

    /**
     * returns entire FIB to user who requested it
     *
     * @param packetUserID userID of entity that requested FIB contents
     * @param packetSensorID sensorID of entity that requested FIB contents
     * @param packetIP ip of entity that requested FIB contents
     * @return true if valid input, false otherwise (useful during testing)
     */
    public static boolean handleInterestFIBRequest(String packetUserID, String packetSensorID, String packetIP, int port)
    {
        if (packetUserID == null || packetSensorID == null || packetIP == null) {
            return false;
        } else {

            ArrayList<DBData> allFIBData = DBSingleton.getInstance(context).getDB().getAllFIBData();

            String mySensorID = Utils.getFromPrefs(context, StringConst.PREFS_LOGIN_SENSOR_ID_KEY, "");
            String myUserID = Utils.getFromPrefs(context, StringConst.PREFS_LOGIN_USER_ID_KEY, "");

            if (allFIBData == null || allFIBData.size() == 0) {

                // FIB was empty, only send own device's information
                DataPacket dataPacket = new DataPacket(packetUserID, packetSensorID,
                        StringConst.CURRENT_TIME, StringConst.DATA_FIB, MainActivity.deviceIP);
                
                new UDPSocket(port, packetIP, StringConst.DATA_TYPE)
                        .execute(dataPacket.toString()); // reply to interest with DATA from cache

                // add packet content to database for future review
                DBSingleton.getInstance(context).getDB().addPacketData(new DBData(dataPacket.getName(), dataPacket.toString()));
            } else {

                String fibContent = "";

                // loop over all valid fib entries and group together
                for (int i = 0; i < allFIBData.size(); i++) {

                    // don't send data to same node that requested; check first
                    if (!allFIBData.get(i).getIpAddr().equals(packetIP)) {

                        // syntax of FIB entry sent in DATA packet: "userID,userIP++"
                        fibContent += allFIBData.get(i).getUserID() + "," + allFIBData.get(i).getIpAddr() + "++";
                    }

                    DataPacket dataPacket = new DataPacket(myUserID, mySensorID,
                            StringConst.CURRENT_TIME,  StringConst.DATA_FIB, fibContent);

                    new UDPSocket(port, packetIP, StringConst.DATA_TYPE)
                            .execute(dataPacket.toString()); // send interest packet

                    // add packet content to database for future review
                    DBSingleton.getInstance(context).getDB().addPacketData(new DBData(dataPacket.getName(), dataPacket.toString()));
                }
            }

            return true;
        }
    }
    
    /**
     * performs NDN logic on packet that requests data
     *
     * @param packetUserID userID associated with requested data from cache
     * @param packetSensorID sensorID associated with requested data from cache
     * @param packetTimeString timeString associated with requested data from cache
     * @param packetProcessID processID associated with requested data from cache
     * @param packetIP ip of entity that requested data from cache
     */
    static void handleInterestCacheRequest(String packetUserID, String packetSensorID, String packetTimeString,
                            String packetProcessID, String packetIP, int port)
    {
        // first, check CONTENT STORE (cache)
        ArrayList<DBData> csDATA = DBSingleton.getInstance(context).getDB().getGeneralCSData(packetUserID);

        if (csDATA != null) {

            int dataAppendCount = 0;
            String dataPayload = "";

            for (int i = 0; i < csDATA.size(); i++) {

                // only reply to interest with data that matches date-request
              if (Utils.isValidForTimeInterval(packetTimeString, csDATA.get(i).getTimeString())) {

                  // append all data to single string since all going to single source
                    dataPayload += csDATA.get(i).getDataFloat()+ ",";
                    dataAppendCount ++;
                }
            }

            // if valid data was found, now send Data packet
            if (dataAppendCount > 0) {

                // TODO - rework 0th assumption

                // 0th should be equivalent to any; all data originated from name source
                DataPacket dataPacket = new DataPacket(csDATA.get(0).getUserID(), csDATA.get(0).getSensorID(),
                        csDATA.get(0).getTimeString(), csDATA.get(0).getProcessID(), dataPayload);

                new UDPSocket(port, packetIP, StringConst.DATA_TYPE)
                        .execute(dataPacket.toString()); // reply to interest with DATA from cache

                // add packet content to database for future review
                DBSingleton.getInstance(context).getDB().addPacketData(new DBData(dataPacket.getName(), dataPacket.toString()));
            }
        } else {
            // second, check PIT

            if (DBSingleton.getInstance(context).getDB().getGeneralPITData(packetUserID) == null) {

                // add new request to PIT, then look into FIB before sending request
                DBData newPITEntry = new DBData();
                newPITEntry.setUserID(packetUserID);
                newPITEntry.setSensorID(packetSensorID);
                newPITEntry.setTimeString(packetTimeString);
                newPITEntry.setProcessID(packetProcessID);
                newPITEntry.setIpAddr(packetIP);

                DBSingleton.getInstance(context).getDB().addPITData(newPITEntry);

                ArrayList<DBData> allFIBData = DBSingleton.getInstance(context).getDB().getAllFIBData();

                if (allFIBData == null || allFIBData.size() == 0) {

                    // FIB is empty, user must reconfigure
                    throw new NullPointerException("Cannot send message; FIB is empty.");
                } else {

                    for (int i = 0; i < allFIBData.size(); i++) {

                        // don't send data to same node that requested; check first
                        if (!allFIBData.get(i).getIpAddr().equals(packetIP)
                                && !allFIBData.get(i).getIpAddr().equals("null")) {

                            InterestPacket interestPacket = new InterestPacket(packetUserID, packetSensorID,
                                    packetTimeString,  packetProcessID);

                            new UDPSocket(port, allFIBData.get(i).getIpAddr(), StringConst.INTEREST_TYPE)
                                    .execute(interestPacket.toString()); // send interest packet

                            // add packet content to database for future review
                            DBSingleton.getInstance(context).getDB()
                                    .addPacketData(new DBData(interestPacket.getName(), interestPacket.toString()));
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

                DBSingleton.getInstance(context).getDB().addPITData(newPITEntry);
            }
        }
    }

    /**
     * handles DATA packet as per NDN specification
     * Method parses packet then stores in cache if requested,
     * and sends out to satisfy any potential Interests.
     *
     * @param packetDataArray incoming packet after having been "split" by space and placed array
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
                System.out.println("name component: " + packetDataArray[i+2]);

                nameComponent = packetDataArray[i+2].trim().split("/"); // split into various components

            } else if (packetDataArray[i].equals("CONTENT-TYPE")) {

                // i+2 corresponds content as per NDN standard
                // i = notifier (CONTENT-TYPE), i+1 = bytes, i+2 = content
                dataContents = packetDataArray[i+2];
            } else {
                // TODO - inspect other packet elements
            }
        }

        // add packet content to database for future review
        DBSingleton.getInstance(context).getDB()
                .addPacketData(new DBData(Arrays.toString(nameComponent), Arrays.toString(packetDataArray)));

        // information extracted from our name format:
        // "/ndn/userID/sensorID/timestring/processID/floatContent"
        // the indexes used are position + 1 (+1 is due to string properties)
        String packetUserID = nameComponent[2].trim();
        String packetSensorID = nameComponent[3].trim();
        String packetTimeString = nameComponent[4].trim();
        String packetProcessID = nameComponent[5].trim();

        // first, determine who wants the data
        ArrayList<DBData> allValidPITEntries = DBSingleton.getInstance(context).getDB()
                .getGeneralPITData(packetUserID);

        if (allValidPITEntries == null || allValidPITEntries.size() == 0) {

            // no one requested the data, merely drop it
        } else {

            // determine if data packet's time interval matches any requests
            int requestCount = 0;
            for (int i = 0; i < allValidPITEntries.size(); i++) {

                if (Utils.isValidForTimeInterval(allValidPITEntries.get(i).getTimeString(), packetTimeString)) {
                    requestCount++;
                }
            }

            if (requestCount > 0) { // positive request count, process packet now
                if (packetProcessID.equals(StringConst.DATA_FIB)) {

                    handleFIBData(dataContents);

                } else if (packetProcessID.equals(StringConst.DATA_CACHE)) {

                    handleCacheData(packetUserID, packetSensorID, packetTimeString, packetProcessID,
                            dataContents, allValidPITEntries);
                } else {
                    // unknown process id; drop packet
                }
            }
        }
    }

    /**
     * Method handles incoming Non-FIB data
     *
     * @param packetUserID userID associated with incoming Data packet
     * @param packetSensorID sensorID associated with incoming Data packet
     * @param packetTimeString timeString associated with incoming Data packet
     * @param packetProcessID processID associated with incoming Data packet
     * @param packetFloatContent contents of incoming Data packet
     * @param allValidPITEntries ArrayList of all PIT entries requesting this data
     */
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

        System.out.println("handle cache data");

        // TODO - rework how update/addition takes place (currently, may not store if 3rd party requested)

        // if data exists in cache, just update
        if (DBSingleton.getInstance(context).getDB().getGeneralCSData(packetUserID) != null) {

            DBSingleton.getInstance(context).getDB().updateCSData(data);
        } else {

            // data not in cache, add now
            DBSingleton.getInstance(context).getDB().addCSData(data);
        }

        // now, send packets to each entity that requested the data
        for (int i = 0; i < allValidPITEntries.size(); i++) {

            // data satisfies PIT entry; delete the entry
            DBSingleton.getInstance(context).getDB().deletePITEntry(allValidPITEntries.get(i).getUserID(),
                    allValidPITEntries.get(i).getTimeString(), allValidPITEntries.get(i).getIpAddr());

            // another device requested the data, send reply as Datapacket
            if (!allValidPITEntries.get(i).getIpAddr().equals(MainActivity.deviceIP))  {

                // NOTE: params list = Context context, String timestring, String processID, String content
                DataPacket dataPacket = new DataPacket(packetUserID,
                        packetSensorID, packetTimeString, packetProcessID, packetFloatContent);

                new UDPSocket(MainActivity.devicePort, allValidPITEntries.get(i).getIpAddr(), StringConst.DATA_TYPE)
                        .execute(dataPacket.toString()); // send DATA packet

                // add packet content to database for future review
                DBSingleton.getInstance(context).getDB().addPacketData(new DBData(dataPacket.getName(), dataPacket.toString()));
            }
        }
    }

    /**
     * Method handles incoming FIB data
     *
     * @param packetFloatContent contents of FIB Data packet (i.e., "userID,userIP" string)
     * @return true if FIB entry was added/updated within database, false otherwise
     */
    public static boolean handleFIBData(String packetFloatContent) {

        try {
            DBData data = new DBData();

            // data packet contains requested fib data, store in fib now
            String myUserID = Utils.getFromPrefs(context, StringConst.PREFS_LOGIN_USER_ID_KEY, "");

            // expected format: "userID,userIP"
            String [] packetFIBContent = packetFloatContent.split(",");

            data.setUserID(packetFIBContent[0].trim());
            data.setIpAddr(packetFIBContent[1].trim());
            data.setTimeString(StringConst.CURRENT_TIME);

            // perform minimal input validation, and don't add data for self here
            if (data.getUserID() != "" && Utils.validIP(data.getIpAddr()) && !data.getUserID().equals(myUserID)) {

                DBData fibCheckObject = DBSingleton.getInstance(context).getDB().getFIBData(data.getUserID());

                if (fibCheckObject == null) {
                    DBSingleton.getInstance(context).getDB().addFIBData(data);
                } else {
                    DBSingleton.getInstance(context).getDB().updateFIBData(data);
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // FIB wasn't touched; return false
        }
    }
}