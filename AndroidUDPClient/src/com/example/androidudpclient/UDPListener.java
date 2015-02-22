package com.example.androidudpclient;

import android.content.Context;

import com.example.androidudpclient.Packet.DataPacket;
import com.example.androidudpclient.Packet.InterestPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Class handles incoming UDP packets.
 */
public class UDPListener extends Thread {

    DatagramSocket clientSocket = null;
    String deviceIP;
    Context context;

    public UDPListener(String ip, Context context) {
        this.deviceIP = ip;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            clientSocket = new DatagramSocket(null);
            InetSocketAddress address = new InetSocketAddress(deviceIP, MainActivity.devicePort);

            clientSocket.bind(address); // give receiver static address

            // set timeout so to force thread to check whether its execution is valid
            clientSocket.setSoTimeout(1000);

            byte[] receiveData = new byte[1024];

            while (MainActivity.continueReceiverExecution) { // loop for packets

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    clientSocket.receive(receivePacket);

                    // convert sender IP to string and remove '/' which appears
                    String senderIP = receivePacket.getAddress().toString().replaceAll("/","");
                    String packetData = new String(receivePacket.getData());
                    String [] packetDataArray;

                    // remove "null" unicode characters
                    packetDataArray = packetData.replaceAll("\u0000", "").split(" ");

                    // NOTE; temporary debug print
                    for (int i = 0; i < packetDataArray.length; i++) {
                        System.out.println(packetDataArray[i]);
                    }

                    if (packetDataArray[0].equals("DATA-TLV")) {
                        handleDataPacket(packetDataArray);
                    } else if (packetDataArray[0].equals("INTEREST-TYPE")) {
                        handleInterestPacket(packetDataArray);
                    } else {
                        // throw away, packet is neither INTEREST nor DATA
                    }
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

    /** handles INTEREST packet as per NDN specification
     * Method parses packet then asks the following questions:
     * 1. Do I have the data?
     * 2. Have I already sent an interest for this data?
     */
    void handleInterestPacket(String[] packetDataArray) {

        String [] nameComponent = null;

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {

                // i+2 corresponds name as per NDN standard
                // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name
                nameComponent = packetDataArray[i+2].split("/"); // split into various components

            } else {
                // TODO - inspect other packet elements
            }
        }

        // information extracted from our name format:
        // "/ndn/userID/sensorID/timestring/processID/ip"
        String packetUserID = nameComponent[1];
        String packetSensorID = nameComponent[2];
        String packetTimeString = nameComponent[3];
        String packetProcessID = nameComponent[4];
        String packetIP = nameComponent[5];

        // first, check CONTENT STORE (cache)
        DBData csDATA = MainActivity.datasource.getSpecificCSData(packetUserID, packetTimeString);

        if (csDATA != null) {

            // NOTE: params list = Context context, String timestring, String processID, String content
            DataPacket dataPacket = new DataPacket(context, packetTimeString, packetProcessID,
                    Float.toString(csDATA.getDataFloat()));

            new UDPSocket(MainActivity.devicePort, packetIP)
                    .execute(dataPacket.toString()); // reply to interest with DATA from cache
        } else {
            // second, check PIT
            DBData pitDATA = MainActivity.datasource.getSpecificPITData(packetUserID,
                    packetTimeString, packetIP);

            if (pitDATA == null) {
                // add new request to PIT, then look into FIB before sending request
                DBData newPITEntry = new DBData();
                newPITEntry.setUserID(packetUserID);
                newPITEntry.setSensorID(packetSensorID);
                newPITEntry.setTimeString(packetTimeString);
                newPITEntry.setProcessID(packetProcessID);
                newPITEntry.setIpAddr(packetIP);

                MainActivity.datasource.addPITData(newPITEntry);

                // TODO - access FIB intelligently
                /*
                String nextHopIP;

                // first check for actual source in FIB, then send out broadly
                DBData fibDATA = MainActivity.datasource.getFIBData(packetUserID);
                if (fibDATA == null) {
                    ArrayList<DBData> allFIBData = MainActivity.datasource.getAllFIBData();
                } else {
                    nextHopIP = fibDATA.getIpAddr();
                }*/

                ArrayList<DBData> allFIBData = MainActivity.datasource.getAllFIBData();

                if (allFIBData == null || allFIBData.size() == 0) {
                    // TODO - sophisticate way in which user deals with FIB

                    // FIB is empty, user must reconfigure
                    throw new NullPointerException("Cannot send message; FIB is empty.");
                } else {
                    for (int i = 0; i < allFIBData.size(); i++) {
                        InterestPacket interestPacket = new InterestPacket(context, packetTimeString,
                                packetProcessID, packetIP);

                        new UDPSocket(MainActivity.devicePort, allFIBData.get(i).getIpAddr())
                                .execute(interestPacket.toString()); // send interest packet
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

    /** handles DATA packet as per NDN specification
     * Method parses packet then asks the following questions:
     * 1. Is this data for me?
     */
    void handleDataPacket(String[] packetDataArray)
    {
        String [] nameComponent = null;
        String dataContents = null;

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {
                // i+2 corresponds name as per NDN standard
                // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name
                nameComponent = packetDataArray[i+2].split("/"); // split into various components

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
        String packetUserID = nameComponent[1];
        String packetSensorID = nameComponent[2];
        String packetTimeString = nameComponent[3];
        String packetProcessID = nameComponent[4];

        // TODO - packet structure (floatContent inclusion, specifically)
        String packetFloatContent = dataContents;//nameComponent[5];

        // first, determine who wants the data
        ArrayList<DBData> allValidPITEntries = MainActivity.datasource
                .getGeneralPITData(packetUserID, packetTimeString);

        if (allValidPITEntries == null || allValidPITEntries.size() == 0) {
            // no one requested the data, merely drop it
        } else {
            // data was requested; second, update cache with new packet
            DBData data = new DBData();
            data.setUserID(packetUserID);
            data.setSensorID(packetSensorID);
            data.setTimeString(packetTimeString);
            data.setProcessID(packetProcessID);
            data.setDataFloat(Float.parseFloat(packetFloatContent));

            // if data exists in cache, just update
            if (MainActivity.datasource.getSpecificCSData(packetUserID, packetTimeString) != null) {
                MainActivity.datasource.updateCSData(data);
            } else {
                // data not in cache, add now
                MainActivity.datasource.addCSData(data);
            }

            // now, send packets to each entity that requested the data
            for (int i = 0; i < allValidPITEntries.size(); i++) {
                if (allValidPITEntries.get(i).getIpAddr() == deviceIP) {
                    // this device requested the data, notify
                    // TODO - notify of reception of requested data
                } else {
                    // NOTE: params list = Context context, String timestring, String processID, String content
                    DataPacket dataPacket = new DataPacket(context, packetTimeString, packetProcessID,
                            packetFloatContent);

                    new UDPSocket(MainActivity.devicePort, allValidPITEntries.get(i).getIpAddr())
                            .execute(dataPacket.toString()); // send DATA packet
                }
            }
        }
    }
}