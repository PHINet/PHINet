package com.example.androidudpclient;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPSocket extends AsyncTask<String, Void, Void> {

    String destAddr;
    int destPort;

    UDPSocket(int port, String addr){
        destPort = port;
        destAddr = addr;
    }

    @Override
    protected Void doInBackground(String... message) {

        // TODO - verify wifi network

        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(destAddr);
            byte[] sendData = new byte[1024];


            sendData = message[0].getBytes();

            System.out.println("DEST ADDR: " + destAddr);
            System.out.println("DEST PORT: " + destPort);
            System.out.println("MESSAGE 0: " + message[0]);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, destPort);
            clientSocket.send(sendPacket);
            System.out.println("SENT");

            clientSocket.close();
        } catch (Exception e) {
            System.out.println("EXCEPTION ENCOUNTERED");
            System.out.println(e.toString());
        } finally {
            System.out.println("FINALLY");
            if (clientSocket != null) {
                clientSocket.close();
            }
        }

        return null;
    }

    protected void onPostExecute(Void result){
        //textResponse.setText(response);
        //super.onPostExecute(result);
    }
}