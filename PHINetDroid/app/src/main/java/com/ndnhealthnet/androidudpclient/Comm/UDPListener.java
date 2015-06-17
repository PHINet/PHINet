package com.ndnhealthnet.androidudpclient.Comm;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Activities.MainActivity;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

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
            InetSocketAddress address = new InetSocketAddress(MainActivity.deviceIP, ConstVar.PHINET_PORT);

            clientSocket = new DatagramSocket(null);
            clientSocket.bind(address); // give receiver static address

            byte[] receiveData = new byte[1024];

            while (MainActivity.continueReceiverExecution) { // loop for packets while true

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    clientSocket.receive(receivePacket);

                    String hostIP = receivePacket.getAddress().getHostAddress();
                    int hostPort = receivePacket.getPort();

                    handleNDNPacket(receiveData, hostIP, hostPort);

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
     * @param packet received
     * @param hostIP of sender
     * @param hostPort of sender
     */
    static void handleNDNPacket(byte[] packet, String hostIP, int hostPort) {
        ByteBuffer buf = ByteBuffer.wrap(packet);
        Interest interest = JNDNUtils.decodeInterest(buf);
        Data data = JNDNUtils.decodeData(buf);

        System.out.println("PACKET incoming");

        if (data == null && interest != null) {

            // store interest packet in database for further review
            Utils.storeInterestPacket(context, interest);

            handleInterestPacket(interest, hostIP, hostPort);
        } else if (data != null && interest == null) {

            // store data packet in database for further review
            Utils.storeDataPacket(context, data);

            handleDataPacket(data);
        } else {
            // unknown packet type; drop it
        }
    }

    /**
     * handles INTEREST packet as per NDN specification
     * Method parses packet then asks the following questions:
     * 1. Do I have the data?
     * 2. Have I already sent an interest for this data?
     *
     * @param interest sent by entity
     * @param ipAddr of sender
     * @param port of sender
     */
    static void handleInterestPacket(Interest interest, String ipAddr, int port) {

        System.out.println("handling interest packet");

        // store received packet in database for further review
        DBSingleton.getInstance(context).getDB()
                .addPacketData(new DBData(interest.getName().toUri(), Utils.convertInterestToString(interest)));

        //decode the parsing characters "||"
        String [] nameComponent = interest.getName().toUri().replace("%7C%7C", "||").split("/");

        // information extracted from our name format:
        // "/ndn/userID/sensorID/timestring/processID/ip"
        // the indexes used are position + 1 (+1 is due to string properties)
        String userID = nameComponent[2];
        String sensorID = nameComponent[3];
        String timeString = nameComponent[4];
        String processID = nameComponent[5];

        System.out.println("Namecomponent: " + Arrays.toString(nameComponent));

        if (processID.equals(ConstVar.INTEREST_FIB)) {

            handleInterestFIBRequest(userID, sensorID, ipAddr, port);
        } else if (processID.equals(ConstVar.INTEREST_CACHE_DATA)) {

            handleInterestCacheRequest(userID, sensorID, timeString,
                    processID, ipAddr, port);
        } else if (processID.equals(ConstVar.CREDENTIAL_REQUEST)) {

            // store the request in the PIT; it will be checked within Login or Signup Activity shortly
            DBData pitEntry = new DBData(sensorID, processID, timeString, userID, ipAddr);
            DBSingleton.getInstance(context).getDB().addPITData(pitEntry);

        } else if (processID.equals(ConstVar.SYNCH_DATA_REQUEST)) {

            handleInterestSynchRequest(userID, timeString, port);
        } else {
            // unknown process id; drop packet
        }
    }

    /**
     * returns entire FIB to user who requested it
     *
     * @param userID userID of entity that requested FIB contents
     * @param sensorID sensorID of entity that requested FIB contents
     * @param packetIP ip of entity that requested FIB contents
     * @return true if valid input, false otherwise (useful during testing)
     */
    public static boolean handleInterestFIBRequest(String userID, String sensorID, String packetIP, int port)
    {
        if (userID == null || sensorID == null || packetIP == null) {
            return false;
        } else {

            ArrayList<DBData> allFIBData = DBSingleton.getInstance(context).getDB().getAllFIBData();

            if (allFIBData == null || allFIBData.size() == 0) {

                // FIB was empty, only send own device's information
                Name packetName = JNDNUtils.createName(userID, sensorID, ConstVar.CURRENT_TIME, ConstVar.DATA_FIB);
                Data data = JNDNUtils.createDataPacket(MainActivity.deviceIP, packetName);

                // reply to interest with DATA from cache
                new UDPSocket(port, packetIP, ConstVar.DATA_TYPE).execute(data.wireEncode().getImmutableArray());

                // store sent packet in database for further review
                Utils.storeDataPacket(context, data);
            } else {

                String fibContent = "";

                // loop over all valid fib entries and group together
                for (int i = 0; i < allFIBData.size(); i++) {

                    // don't send data to same node that requested; check first
                    if (!allFIBData.get(i).getIpAddr().equals(packetIP)) {

                        // syntax of FIB entry sent in DATA packet: "userID,userIP++"
                        fibContent += allFIBData.get(i).getUserID() + "," + allFIBData.get(i).getIpAddr() + "++";
                    }

                    Name packetName = JNDNUtils.createName(userID, sensorID, ConstVar.CURRENT_TIME, ConstVar.DATA_FIB);
                    Data data = JNDNUtils.createDataPacket(fibContent, packetName);

                    // reply to interest with DATA from cache
                    new UDPSocket(port, packetIP, ConstVar.DATA_TYPE).execute(data.wireEncode().getImmutableArray());

                    // store sent packet in database for further review
                    Utils.storeDataPacket(context, data);
                }
            }

            return true;
        }
    }
    
    /**
     * performs NDN logic on packet that requests data
     *
     * @param userID userID associated with requested data from cache
     * @param sensorID sensorID associated with requested data from cache
     * @param timeString timeString associated with requested data from cache
     * @param processID processID associated with requested data from cache
     * @param packetIP ip of entity that requested data from cache
     */
    static void handleInterestCacheRequest(String userID, String sensorID, String timeString,
                            String processID, String packetIP, int port)
    {
        // first, check CONTENT STORE (cache)
        ArrayList<DBData> csDATA = DBSingleton.getInstance(context).getDB().getGeneralCSData(userID);

        if (csDATA != null) {

            int dataAppendCount = 0;
            String dataPayload = "";

            for (int i = 0; i < csDATA.size(); i++) {

                // only reply to interest with data that matches date-request
              if (Utils.isValidForTimeInterval(timeString, csDATA.get(i).getTimeString())) {

                  // append all data to single string since all going to single source
                    dataPayload += csDATA.get(i).getDataFloat()+ ",";
                    dataAppendCount ++;
                }
            }

            // if valid data was found, now send Data packet
            if (dataAppendCount > 0) {

                // TODO - rework 0th assumption
                // 0th should be equivalent to any; all data originated from name source

                Name packetName = JNDNUtils.createName(csDATA.get(0).getUserID(), csDATA.get(0).getSensorID(),
                        csDATA.get(0).getTimeString(), csDATA.get(0).getProcessID());
                Data data = JNDNUtils.createDataPacket(dataPayload, packetName);

                // reply to interest with DATA from cache
                new UDPSocket(port, packetIP, ConstVar.DATA_TYPE).execute(data.wireEncode().getImmutableArray());

                // add packet content to database for future review
                Utils.storeDataPacket(context, data);
            }
        } else {

            // second, check PIT

            if (DBSingleton.getInstance(context).getDB().getGeneralPITData(userID) == null) {

                // add new request to PIT, then look into FIB before sending request
                DBData newPITEntry = new DBData(sensorID, processID, timeString, userID, packetIP);

                DBSingleton.getInstance(context).getDB().addPITData(newPITEntry);

                ArrayList<DBData> allFIBData = DBSingleton.getInstance(context).getDB().getAllFIBData();

                if (allFIBData == null || allFIBData.size() == 0) {

                    // FIB is empty, user must reconfigure
                    throw new NullPointerException("Cannot send message; FIB is empty.");
                } else {

                    for (int i = 0; i < allFIBData.size(); i++) {

                        // don't send data to same node that requested; check first
                        if (!allFIBData.get(i).getIpAddr().equals(packetIP)
                                && !allFIBData.get(i).getIpAddr().equals(ConstVar.NULL_FIELD)) {

                            Name packetName = JNDNUtils.createName(userID, sensorID,
                                    timeString, processID);
                            Interest interest = JNDNUtils.createInterestPacket(packetName);

                            new UDPSocket(port, allFIBData.get(i).getIpAddr(), ConstVar.INTEREST_TYPE)
                                    .execute(interest.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                            // store sent packet in database for further review
                            Utils.storeInterestPacket(context, interest);
                        }
                    }
                }
            } else {

                // add new request to PIT and wait, request has already been sent
                DBData newPITEntry = new DBData(sensorID, processID, timeString, userID, packetIP);
                DBSingleton.getInstance(context).getDB().addPITData(newPITEntry);
            }
        }
    }

    /**
     * Invoked when server sends a synchronization request.
     *
     * @param userID of client targeted by server for synch
     * @param timeString of data requested
     * @param port of sender
     */
    static void handleInterestSynchRequest(String userID, String timeString, int port) {

        System.out.println("handle interest synch request invoked");

        ArrayList<DBData> candidateData = DBSingleton.getInstance(context).getDB().getGeneralCSData(userID);

        ArrayList<DBData> validData = new ArrayList<>();

        for (int i = 0; i < candidateData.size(); i++) {
            // verify that data came from valid sensor and is within proper interval
            if (!candidateData.get(i).getSensorID().equals(ConstVar.NULL_FIELD)
                   &&  Utils.isValidForTimeInterval(timeString, candidateData.get(i).getTimeString())) {
                validData.add(candidateData.get(i));
            }
        }

        // Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
        String formattedData = Utils.formatSynchData(validData);

        Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                timeString, ConstVar.SYNCH_DATA_REQUEST);
        Data data = JNDNUtils.createDataPacket(formattedData, packetName);

        // reply to interest with DATA from cache
        new UDPSocket(port, ConstVar.SERVER_IP, ConstVar.DATA_TYPE) .execute(data.wireEncode().getImmutableArray());

        // add packet content to database for future review
        Utils.storeDataPacket(context, data);
    }

    /**
     * handles DATA packet as per NDN specification
     * Method parses packet then stores in cache if requested,
     * and sends out to satisfy any potential Interests.
     *
     * @param data newly received
     */
    static void handleDataPacket(Data data) {

        System.out.println("handling data packet");

        // store received packet in database for further review
        DBSingleton.getInstance(context).getDB()
                .addPacketData(new DBData(data.getName().toUri(), Utils.convertDataToString(data)));

        // decode the parsing characters "||"
        String [] nameComponent = data.getName().toUri().replace("%7C%7C", "||").split("/");
        String dataContents = data.getContent().toString();

        // information extracted from our name format:
        // "/ndn/userID/sensorID/timestring/processID/floatContent"
        // the indexes used are position + 1 (+1 is due to string properties)
        String userID = nameComponent[2].trim();
        String sensorID = nameComponent[3].trim();
        String timeString = nameComponent[4].trim();
        String processID = nameComponent[5].trim();

        // first, determine who wants the data
        ArrayList<DBData> allValidPITEntries = DBSingleton.getInstance(context).getDB()
                .getGeneralPITData(userID);

        if (allValidPITEntries == null || allValidPITEntries.size() == 0) {
            // no one requested the data, merely drop it
        } else {

            // determine if data packet's time interval matches any requests
            boolean requestFoundWithinInterval = false;
            for (int i = 0; i < allValidPITEntries.size(); i++) {

                if (Utils.isValidForTimeInterval(allValidPITEntries.get(i).getTimeString(), timeString)) {
                    requestFoundWithinInterval = true;
                    break;
                }

                if ((processID.equals(ConstVar.LOGIN_RESULT)
                        && allValidPITEntries.get(i).getProcessID().equals(ConstVar.LOGIN_RESULT))
                  || (processID.equals(ConstVar.REGISTER_RESULT)
                        && allValidPITEntries.get(i).getProcessID().equals(ConstVar.REGISTER_RESULT))) {

                    /**
                     * login/register packets (currently) are valid irrespective of time, break if match found
                     * server sends an Interest with processID CREDENTIAL_REQUEST and client responds with
                     * Data with processID LOGIN_CREDENTIAL_DATA/REGISTER_CREDENTIAL_DATA
                     */

                    requestFoundWithinInterval = true;
                    break;
                }

                if (Utils.isAnalyticProcessID(processID)
                        && timeString.equals(allValidPITEntries.get(i).getTimeString())) {
                    /**
                     * TODO - doc here ( both time strings are intervals)
                     */

                    requestFoundWithinInterval = true;
                    break;
                }
            }

            if (requestFoundWithinInterval) { // positive request count, process packet now
                if (processID.equals(ConstVar.DATA_FIB)) {

                    handleFIBData(dataContents);

                } else if (processID.equals(ConstVar.DATA_CACHE)) {

                    handleCacheData(userID, sensorID, timeString, processID,
                            dataContents, allValidPITEntries);
                } else if (processID.equals(ConstVar.LOGIN_RESULT)
                        || processID.equals(ConstVar.REGISTER_RESULT)
                        || Utils.isAnalyticProcessID(processID)) {

                    // these ProcessIDs all result in storing data into the ContentStore
                    DBData dataPacket = new DBData(sensorID, processID,
                            timeString, userID, dataContents, ConstVar.DEFAULT_FRESHNESS_PERIOD);

                    DBSingleton.getInstance(context).getDB().addCSData(dataPacket);

                } else {
                    // unknown process id; drop packet
                }
            } else {
                // no PIT requests found; drop packet
            }
        }
    }

    /**
     * Method handles incoming Non-FIB data
     *
     * @param userID userID associated with incoming Data packet
     * @param sensorID sensorID associated with incoming Data packet
     * @param timeString timeString associated with incoming Data packet
     * @param processID processID associated with incoming Data packet
     * @param dataPayload contents of incoming Data packet
     * @param allValidPITEntries ArrayList of all PIT entries requesting this data
     */
    static void handleCacheData(String userID,String  sensorID,String  timeString,
                         String  processID,String  dataPayload,
                         ArrayList<DBData> allValidPITEntries) {

        // data was requested; second, update cache with new packet
        DBData data = new DBData(sensorID, processID, timeString, userID, dataPayload,
                ConstVar.DEFAULT_FRESHNESS_PERIOD);

        // TODO - rework how update/addition takes place (currently, may not store if 3rd party requested)

        // if data exists in cache, just update
        if (DBSingleton.getInstance(context).getDB().getGeneralCSData(userID) != null) {

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


                Name packetName = JNDNUtils.createName(userID, sensorID, timeString,
                        processID);
                Data dataPacket = JNDNUtils.createDataPacket(dataPayload, packetName);

                new UDPSocket(ConstVar.PHINET_PORT, allValidPITEntries.get(i).getIpAddr(), ConstVar.DATA_TYPE)
                        .execute(dataPacket.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                // store sent packet in database for further review
                Utils.storeDataPacket(context, dataPacket);
            }
        }
    }

    /**
     * Method handles incoming FIB data
     *
     * @param dataPayload contents of FIB Data packet (i.e., "userID,userIP" string)
     * @return true if FIB entry was added/updated within database, false otherwise
     */
    public static boolean handleFIBData(String dataPayload) {

        try {
            // data packet contains requested fib data, store in fib now
            String myUserID = Utils.getFromPrefs(context, ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

            // expected format: "userID,userIP"
            String [] packetFIBContent = dataPayload.split(",");

            DBData data = new DBData();
            data.setUserID(packetFIBContent[0].trim());
            data.setIpAddr(packetFIBContent[1].trim());
            data.setTimeString(ConstVar.CURRENT_TIME);

            // perform minimal input validation, and don't add data for self here
            if (!data.getUserID().equals("") && Utils.isValidIP(data.getIpAddr()) && !data.getUserID().equals(myUserID)) {

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