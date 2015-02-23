package com.example.androidudpclient;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Class handles outbound UDP packets.
 */
class UDPSocket extends AsyncTask<String, Void, Void> {

    String destAddr;
    int destPort;

    UDPSocket(int port, String addr){
        destPort = port;
        destAddr = addr;
    }

    @Override
    protected Void doInBackground(String... message) {

        DatagramSocket clientSocket = null;

        System.out.println("OUTGOING PACKET");

        try {
            clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(destAddr);
            byte[] sendData = message[0].getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, destPort);
            clientSocket.send(sendPacket);

            clientSocket.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }

        return null;
    }
}