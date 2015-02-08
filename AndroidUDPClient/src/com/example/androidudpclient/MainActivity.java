/*****************************************************************************
*  Copyright (c) 2004-2008, 2013 Digi International Inc., All Rights Reserved
*
*  This software contains proprietary and confidential information of Digi
*  International Inc.  By accepting transfer of this copy, Recipient agrees
*  to retain this software in confidence, to prevent disclosure to others,
*  and to make no use of this software other than that for which it was
*  delivered.  This is an unpublished copyrighted work of Digi International
*  Inc.  Except as permitted by federal law, 17 USC 117, copying is strictly
*  prohibited.
*
*  Restricted Rights Legend
*
*  Use, duplication, or disclosure by the Government is subject to
*  restrictions set forth in sub-paragraph (c)(1)(ii) of The Rights in
*  Technical Data and Computer Software clause at DFARS 252.227-7031 or
*  subparagraphs (c)(1) and (2) of the Commercial Computer Software -
*  Restricted Rights at 48 CFR 52.227-19, as applicable.
*
*  Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
*
*****************************************************************************/
package com.example.androidudpclient;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class MainActivity extends Activity {

	Button netLinkBtn;
	Button selfBeatBtn;
	Button getAvgBtn;
	Button cliBeatBtn;
	
	Thread receiverThread;
    boolean continueReceiverExecution = true;

    /** used to notify sender of this device's address **/
    static final int devicePort = 50056; // chosen arbitrarily
    String deviceIP;
    WifiManager wm;
    /** used to notify sender of this device's address **/

    static ArrayList<Patient> patients; // NOTE: this is only temporary, data will be stored in cache eventually

    private DBDataSource datasource;

	/*
	 * Run Upon Initial Installation of Applciation
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
         * Creates Tables
         */
        // TODO - resolve error
        //datasource = new DBDataSource(this);
        //datasource.open();
        
        receiverThread = initializeReceiver();
        receiverThread.start(); // begin listening for interest packets

        selfBeatBtn = (Button) findViewById(R.id.selfBeatBtn);
        selfBeatBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
            	startActivity(new Intent(MainActivity.this, RecordHeartbeatActivity.class));
        	}
        });
        
        netLinkBtn = (Button) findViewById(R.id.netLinkBtn);
        netLinkBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
        	    startActivity(new Intent(MainActivity.this, ConfigNetLinksActivity.class));
        	}
        });
        
        getAvgBtn = (Button) findViewById(R.id.getAvgBtn);
        getAvgBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GetAvgBeatActivity.class));
        	}
        });
        
        cliBeatBtn = (Button) findViewById(R.id.cliBeatBtn);
        cliBeatBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
        		startActivity(new Intent(MainActivity.this, GetCliBeatActivity.class));
        	}
        });
    }

    static boolean validIP(String ip) {
        boolean validIP = false;
        try {
            // tests validity of IP input

            InetAddress.getByName(ip);
            validIP = true;
        } catch (Exception e) {
            validIP = false;
        }

        return validIP;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        continueReceiverExecution = false;  // notify receiver to terminate
    }

    // TODO - break up thread into methods (perhaps a class)

    /** create and return receiver thread **/
    Thread initializeReceiver()
    {
        // get the device's ip
        wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());;

        // create thread to receive all incoming packets expected after request to patient
        Thread thread = new Thread(new Runnable(){
            DatagramSocket clientSocket = null;
            @Override
            public void run() {
                try {
                    clientSocket = new DatagramSocket(null);
                    InetSocketAddress address = new InetSocketAddress(deviceIP, devicePort);

                    clientSocket.bind(address); // give receiver static address

                    // set timeout so to force thread to check whether its execution is valid
                    clientSocket.setSoTimeout(1000);

                    byte[] receiveData = new byte[1024];
                    while (continueReceiverExecution) { // loop for packets
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

                            for (int i = 0; i < patients.size(); i++) {
                                if (patients.get(i).getIP().equals(senderIP)) {
                                    senderPatientIndex = i;
                                }
                            }

                            if (packetDataArray[0].equals("DATA-TLV")) {


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
                                            patients.get(senderPatientIndex).addData(Integer.parseInt(content[j]));
                                        }

                                    } else {
                                        // TODO - inspect other packet elements
                                    }
                                }

                            } else if (packetDataArray[0].equals("INTEREST-TYPE")) {

                                // TODO - interest directed to me? If not, send according to
                                //          FIB and place in PIT (if not currently there)

                                for (int i = 0; i < packetDataArray.length; i++) {
                                    if (packetDataArray[i].equals("NAME-COMPONENT-TYPE")) {
                                        // TODO - store


                                    } else {
                                        // TODO - inspect other packet elements

                                    }
                                }

                                // TODO - reply to interest

                            } else {
                                // throw away, packet is neither INTEREST nor DATA
                            }

                            // TODO - think from perspective of either doctor or patient when
                            //          accepting data

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
        });
        return thread;
    }
}