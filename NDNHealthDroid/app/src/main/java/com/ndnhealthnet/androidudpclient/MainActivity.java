package com.ndnhealthnet.androidudpclient;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 *
 */
public class MainActivity extends Activity {

    final int CREDENTIAL_RESULT_CODE = 1;

	Button tempDeletePITBtn, userCredentialBtn, netLinkBtn, selfBeatBtn, getAvgBtn, cliBeatBtn;
	TextView credentialWarningText, doctorText, patientText;
	UDPListener receiverThread;

    // used to specify when listener "receiverThread" should actively listen for packets
    static boolean continueReceiverExecution = true;

    static final int devicePort = 50056; // port used by all NDN-HealthNet applications
    static String deviceIP;

    static DatabaseHandler datasource; // data source that is accessed through application

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the device's ip
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        // create tables
        datasource = new DatabaseHandler(getApplicationContext());

        DBData dbData = new DBData();
        dbData.setIpAddr("52.11.79.46");
        dbData.setUserID("CLOUD-SERVER");

        datasource.addFIBData(dbData); // add cloud-server to FIB

        receiverThread = new UDPListener(getApplicationContext());
        receiverThread.start(); // begin listening for interest packets

        onCreateHelper();
    }

    /**
     * method exists so that layout can easily be reset
     */
    private void onCreateHelper()
    {
        setContentView(R.layout.activity_main);

        String mySensorID = Utils.getFromPrefs(getApplicationContext(),
                StringConst.PREFS_LOGIN_SENSOR_ID_KEY, "");
        String myUserID = Utils.getFromPrefs(getApplicationContext(),
                StringConst.PREFS_LOGIN_USER_ID_KEY, "");

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

        // NOTE: temporary debugging method that allows user to clear the database
        tempDeletePITBtn = (Button) findViewById(R.id.deletePITBtn);
        tempDeletePITBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                datasource.deleteEntirePIT();
                datasource.deleteEntireCS();
                datasource.deleteEntireFIB();
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
            tempDeletePITBtn.setVisibility(View.GONE);

        } else {

            // user has entered credentials, remove warning
            credentialWarningText.setVisibility(View.GONE);
        }
    }

    /**
     * should be invoked automatically after user enters credentials
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CREDENTIAL_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                onCreateHelper(); // reset the layout after user has entered credentials
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        continueReceiverExecution = false;  // notify receiver to terminate
    }
}