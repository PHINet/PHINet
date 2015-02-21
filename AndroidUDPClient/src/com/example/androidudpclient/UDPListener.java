package com.example.androidudpclient;

import android.content.Context;

import com.example.androidudpclient.Packet.DataPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

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

                    // TODO - map NAME to IP (avoid this check)
                    int senderPatientIndex = -1;

                    for (int i = 0; i < MainActivity.patients.size(); i++) {

                        if (MainActivity.patients.get(i).getIP().equals(senderIP)) {
                            senderPatientIndex = i;
                        }
                    }

                    if (packetDataArray[0].equals("DATA-TLV")) {
                        handleDataPacket(packetDataArray, senderPatientIndex);
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
        DBData csDATA = MainActivity.datasource.getCSData(packetUserID, packetTimeString);

        if (csDATA != null) {

            // NOTE: params list = Context context, String timestring, String processID, String content
            DataPacket dataPacket = new DataPacket(context, packetTimeString, packetProcessID,
                    Float.toString(csDATA.getDataFloat()));

            new UDPSocket(MainActivity.devicePort, deviceIP)
                    .execute(dataPacket.toString()); // send interest packet
        } else {
            // second, check PIT
            DBData pitDATA = MainActivity.datasource.getPITData(packetUserID,
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

                // TODO - look in FIB for next hop

                // TODO - construct interest packet and send
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
    void handleDataPacket(String[] packetDataArray, int senderPatientIndex)
    {
        String [] nameComponent = null;

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {
                // i+2 corresponds name as per NDN standard
                // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name
                nameComponent = packetDataArray[i+2].split("/"); // split into various components

            } else if (packetDataArray[i].equals("CONTENT-TYPE")) {

                // i+2 corresponds content as per NDN standard
                // i = notifier (CONTENT-TYPE), i+1 = bytes, i+2 = content
                String[] content = packetDataArray[i+2].split(",");

                // store packet content with patient object
                for (int j = 0; j < content.length; j++) {
                    MainActivity.patients.get(senderPatientIndex).addData(Integer.parseInt(content[j]));
                }

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
        String packetFloatContent = nameComponent[5];

        /* TODO - implement

        update CS
        if (data is only for me) {
            process
        } else {
            send data to everyone who wants
        }

         */
    }
}


