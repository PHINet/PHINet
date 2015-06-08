package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ndnhealthnet.androidudpclient.Comm.UDPListener;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 *  The "main loop" of the application; Activity should always be on stack.
 */
public class MainActivity extends Activity {

    final int CREDENTIAL_RESULT_CODE = 1;

	Button clearDatabaseBtn, logoutBtn, myDataBtn, cliBeatBtn,
            loginBtn, signupBtn, sensorBtn, viewPacketsBtn;
	TextView credentialWarningText, doctorText, patientText, loggedInText;
	UDPListener receiverThread;

    // used to specify when listener "receiverThread" should actively listen for packets
    public static boolean continueReceiverExecution = true;

    public static String deviceIP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the device's ip
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

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

        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        if (currentUserID != "" || currentUserID != null) {
            loggedInText = (TextView) findViewById(R.id.loggedInTextView);
            loggedInText.setText(currentUserID);
        }

        String myPasswordID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");
        String myUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        credentialWarningText = (TextView) findViewById(R.id.credentialWarningTextView);
        doctorText = (TextView) findViewById(R.id.doctorTextView);
        patientText = (TextView) findViewById(R.id.patientTextView);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), CREDENTIAL_RESULT_CODE);
            }
        });

        signupBtn = (Button) findViewById(R.id.signupBtn);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SignupActivity.class), CREDENTIAL_RESULT_CODE);
            }
        });

        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");

                onCreateHelper();
            }
        });

        viewPacketsBtn = (Button) findViewById(R.id.packetsBtn);
        viewPacketsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PacketListActivity.class));
            }
        });

        sensorBtn = (Button) findViewById(R.id.sensorSettingsBtn);
        sensorBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SensorListActivity.class));
            }
        });

        myDataBtn = (Button) findViewById(R.id.myDataBtn);
        myDataBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ViewDataActivity.class);

                String myUserID = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                // to view client's data, pass their user id
                intent.putExtra(ConstVar.ENTITY_NAME, myUserID);
                startActivity(intent);
            }
        });

        cliBeatBtn = (Button) findViewById(R.id.cliBeatBtn);
        cliBeatBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PatientListActivity.class));
            }
        });

        // NOTE: temporary debugging method that allows user to clear the database
        clearDatabaseBtn = (Button) findViewById(R.id.deleteDataBtn);
        clearDatabaseBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteEntirePIT();
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteEntireCS();
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteEntirePacketDB();
            }
        });

        if (myPasswordID == null || myUserID == null
                || myPasswordID.equals("") || myUserID.equals("")) {

            //destroy all buttons until user enters credentials
            cliBeatBtn.setVisibility(View.GONE);
            myDataBtn.setVisibility(View.GONE);
            patientText.setVisibility(View.GONE);
            doctorText.setVisibility(View.GONE);
            clearDatabaseBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.GONE);
            sensorBtn.setVisibility(View.GONE);
            viewPacketsBtn.setVisibility(View.GONE);

        } else {

            // user has entered credentials, remove warning and buttons
            credentialWarningText.setVisibility(View.GONE);
            loginBtn.setVisibility(View.GONE);
            signupBtn.setVisibility(View.GONE);
        }
    }

    /**
     * should be invoked automatically after user enters credentials
     *
     * @param requestCode code of activity that has returned a result
     * @param resultCode status of activity return
     * @param data intent associated with returned activity
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