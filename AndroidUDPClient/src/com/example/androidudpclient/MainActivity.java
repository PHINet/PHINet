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
import android.widget.TextView;

import java.net.InetAddress;

public class MainActivity extends Activity {

    final int CREDENTIAL_RESULT_CODE = 1;

	Button userCredentialBtn, netLinkBtn, selfBeatBtn, getAvgBtn, cliBeatBtn;
	TextView credentialWarningText, doctorText, patientText;
	Thread receiverThread;
    static boolean continueReceiverExecution = true;

    /** used to notify sender of this device's address **/
    static final int devicePort = 50056; // chosen arbitrarily
    static String deviceIP;
    WifiManager wm;
    /** used to notify sender of this device's address **/

    static DatabaseHandler datasource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get ip of phone
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        // create tables
        datasource = new DatabaseHandler(getApplicationContext());
        receiverThread = initializeReceiver();
        receiverThread.start(); // begin listening for interest packets

        onCreateHelper();
    }

    /** method exists so that layout can easily be reset **/
    private void onCreateHelper()
    {
        setContentView(R.layout.activity_main);

        String mySensorID = Utils.getFromPrefs(getApplicationContext(),
                Utils.PREFS_LOGIN_SENSOR_ID_KEY, "");
        String myUserID = Utils.getFromPrefs(getApplicationContext(),
                Utils.PREFS_LOGIN_USER_ID_KEY, "");

        credentialWarningText = (TextView) findViewById(R.id.credentialWarning_textView);
        doctorText = (TextView) findViewById(R.id.doctor_textView);
        patientText = (TextView) findViewById(R.id.patient_textView);

        userCredentialBtn = (Button) findViewById(R.id.userCredentialBtn);
        userCredentialBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                startActivityForResult(new Intent(MainActivity.this, UserCredentialActivity.class),
                        CREDENTIAL_RESULT_CODE);
            }
        });

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
                startActivity(new Intent(MainActivity.this, ViewMyDataActivity.class));
            }
        });

        cliBeatBtn = (Button) findViewById(R.id.cliBeatBtn);
        cliBeatBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GetCliBeatActivity.class));
            }
        });

        if (mySensorID == null || myUserID == null
                || mySensorID.equals("") || myUserID.equals("")) {

            //destroy all buttons until user enters credentials
            cliBeatBtn.setVisibility(View.GONE);
            getAvgBtn.setVisibility(View.GONE);
            netLinkBtn.setVisibility(View.GONE);
            selfBeatBtn.setVisibility(View.GONE);
            patientText.setVisibility(View.GONE);
            doctorText.setVisibility(View.GONE);

        } else {
            // user has entered credentials, remove warning
            credentialWarningText.setVisibility(View.GONE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CREDENTIAL_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                onCreateHelper(); // reset the layout after user has entered credentials
            }
        }
    }

    static boolean validIP(String ip) {
        boolean validIP;
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

    /** create and return receiver thread **/
    Thread initializeReceiver() {
        // get the device's ip
        wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        ;

        // create thread to receive all incoming packets expected after request to patient
        return new UDPListener(deviceIP, getApplicationContext());
    }
}