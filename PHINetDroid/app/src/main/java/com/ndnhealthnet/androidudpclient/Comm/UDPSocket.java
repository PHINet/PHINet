package com.ndnhealthnet.androidudpclient.Comm;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class handles outbound UDP packets and listens when reply expected.
 */
public class UDPSocket extends AsyncTask<byte[], Void, Void> {

    String destAddr, messageType;
    int destPort;

    public UDPSocket(int port, String addr, String type){
        destPort = port;
        destAddr = addr;
        messageType = type;
    }

    @Override
    protected Void doInBackground(byte[]... message) {

        try {
            final DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(destAddr);
            byte[] packetContent = message[0];

            DatagramPacket sendPacket = new DatagramPacket(packetContent, packetContent.length, IPAddress, destPort);
            clientSocket.send(sendPacket);

            /**
             * method must listen for incoming packet if we've just sent a packet, otherwise
             * we may not be able to detect incoming, requested packets from the server
             *
             * listen in new thread for 1 second
             */
            Timer t = new Timer();
            t.schedule(new TimerTask() {

                @Override
                public void run() {

                    byte[] receiveData = new byte[1024];


                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    try {
                        clientSocket.setSoTimeout(1000); // only listen for 1 second
                        clientSocket.receive(receivePacket);
                        String packetSourceIP = receivePacket.getAddress().getLocalHost().getHostAddress();
                        int packetPort = receivePacket.getPort();

                        UDPListener.handleNDNPacket(receiveData, packetSourceIP, packetPort);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    clientSocket.close();
                    this.cancel();

                }
            }, 1000L);

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return null;
    }
}