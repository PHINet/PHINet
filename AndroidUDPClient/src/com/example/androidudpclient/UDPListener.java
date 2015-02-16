package com.example.androidudpclient;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class UDPListener extends Thread {


    DatagramSocket clientSocket = null;
    String deviceIP;

    public UDPListener(String ip) {
        this.deviceIP = ip;
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
                    // TODO - think from perspective of either doctor or patient when
                    //          accepting data``
                    // TODO - validate data from sender
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

    void handleInterestPacket(String[] packetDataArray) {
        // TODO - interest directed to me? If not, send according to
        //          FIB and place in PIT (if not currently there)

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {
                // TODO - store

            } else {
                // TODO - inspect other packet elements
            }
        }

        // reply to interest
        // NOTE: currently assumes interest requests all user data on phone
        //      later, rework so that specifics can be requested

        // TODO - rework with cache

        // TODO - later add actual name rather than ""


        //DataPacket dataPacket = new DataPacket("", MainActivity.myData.getDataAsString());


       // new UDPSocket(MainActivity.devicePort, deviceIP)
        //        .execute(dataPacket.toString()); // send interest packet
    }

    void handleDataPacket(String[] packetDataArray, int senderPatientIndex)
    {
        // TODO - LOOK in PIT - should forward or do I want?

        for (int i = 0; i < packetDataArray.length; i++) {
            if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {
                // TODO - store

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
    }
}


