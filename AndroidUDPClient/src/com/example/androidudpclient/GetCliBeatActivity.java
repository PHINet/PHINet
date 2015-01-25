package com.example.androidudpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Handler;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class GetCliBeatActivity extends Activity {

    Button backBtn;
    Button requestPatientDataBtn;
    Button addNewPatientBtn;

    /** used to notify sender of this device's address **/
    int devicePort = 50056; // chosen arbitrarily
    String deviceIP;
    WifiManager wm;
    /** used to notify sender of this device's address **/

    Thread receiverThread;
    boolean continueThreadExecution = true; // initially, thread should execute

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getclibeat);

        receiverThread = initializeReceiver();
        receiverThread.start();

        // TODO - stop thread at appropriate time

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.getCliBeatBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                continueThreadExecution = false;
                finish();
            }
        });

        requestPatientDataBtn = (Button) findViewById(R.id.requestPatientDataBtn);
        requestPatientDataBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - remove hardcoe

                // TODO - define message format; NDN compatible

                new UDPSocket(50055, "10.170.20.31").execute("HELLO FROM ANDROID::" + Integer.toString(devicePort));
            }
        });

        addNewPatientBtn = (Button) findViewById(R.id.addNewPatientBtn);
        addNewPatientBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - implement

                Toast toast = Toast.makeText(getApplicationContext(), "Not yet implemented", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // TODO - perform a check: are networking capabilities enabled?
    }

    @Override
    protected void onStop() {
        super.onStop();
        continueThreadExecution = false; // notify receiver thread
    }

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
                    while (continueThreadExecution) { // loop for packets
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        try {
                            clientSocket.receive(receivePacket);
                            String modifiedSentence = new String(receivePacket.getData());

                            // NOTE: output for debugging only
                            System.out.println("FROM SERVER:" + modifiedSentence);
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

