package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ndnhealthnet.androidudpclient.Comm.BTSensorComm;
import com.ndnhealthnet.androidudpclient.Comm.UDPListener;
import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

/**
 *  The "main loop" of the application; Activity should always be on stack.
 */
public class MainActivity extends Activity {

    final int CREDENTIAL_RESULT_CODE = 1; // used to identify the result of Login/Signup Activities

	Button logoutBtn, myDataBtn, cliBeatBtn, loginBtn,
            signupBtn, sensorBtn, viewPacketsBtn;//, clearDatabaseBtn;
	TextView credentialWarningText, doctorText, patientText, loggedInText;
	UDPListener receiverThread;
    BTSensorComm btSensorComm;
    Thread serverSynchWorker;

    // used to specify when listener "receiverThread" should actively listen for packets
    public static boolean continueReceiverExecution = true;

    public static String deviceIP; // the IP of this particular client

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the device's ip
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        receiverThread = new UDPListener(getApplicationContext());
        receiverThread.start(); // begin listening for interest packets

        serverSynchWorker = createServerSynchRequestWorker();
        serverSynchWorker.start();

        /*
        TODO - initiate bluetooth comm. component
        btSensorComm = new BTSensorComm(getApplicationContext());
        btSensorComm.start(); // initiate bluetooth sensor communication thread
        */

        onCreateHelper();
    }

    /**
     * Method exists so that layout can easily be reset after certain actions (i.e., login/signup)
     */
    private void onCreateHelper()
    {
        setContentView(R.layout.activity_main);

        final  String myUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");
        final String myPassword = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");

        if (!myUserID.equals("")) {
            // place userID on screen if user has logged in
            loggedInText = (TextView) findViewById(R.id.loggedInTextView);
            loggedInText.setText(myUserID);
        }

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
                // clear userID & password on logout
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

        /*// NOTE: temporary debugging method that allows user to clear the database
        clearDatabaseBtn = (Button) findViewById(R.id.deleteDataBtn);
        clearDatabaseBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteEntirePIT();
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteEntireCS();
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteEntirePacketDB();
            }
        });*/

        if (myPassword == null || myUserID == null
                || myPassword.equals("") || myUserID.equals("")) {

            //destroy all buttons until user enters credentials
            cliBeatBtn.setVisibility(View.GONE);
            myDataBtn.setVisibility(View.GONE);
            patientText.setVisibility(View.GONE);
            doctorText.setVisibility(View.GONE);
         //   clearDatabaseBtn.setVisibility(View.GONE);
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
     * Should be invoked automatically after user enters credentials
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

    /**
     * Creates thread that initiates server-synch request every SYNC_INTERVAL_MILLIS period of time.
     */
    private Thread createServerSynchRequestWorker() {
        return new Thread(new Runnable() {
            public void run() {

                // each loop sends server-synch requests then sleeps for SYNC_INTERVAL_MILLIS
                while (true) {

                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    // only attempt synch of wifi is connected
                    if (mWifi.isConnected()) {

                        requestSynch(getApplicationContext());
                    }

                    SystemClock.sleep(ConstVar.SYNCH_INTERVAL_MILLIS); // sleep until next synch
                }
            }
        });
    }

    /**
     * Invoked to request Synch from server.
     */
    static void requestSynch(Context context) {

        // interval syntax: start||end
        String currentTime = Utils.getPreviousSynchTime() + "||" + Utils.getCurrentTime();

        String myUserID = Utils.getFromPrefs(context, ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        /**
         * adding userID as sensorID entry here is a "hack"; the sensorID is null
         * (because no sensorID is used when we send a synch) and we need to include
         * the userID - so we place the userID as a sensorID
         */

        // query server for initialization of synch request
        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, myUserID,
                currentTime, ConstVar.INITIATE_SYNCH_REQUEST);
        Interest interest = JNDNUtils.createInterestPacket(packetName);

        // add entry into PIT (we expect a SYNCH_DATA_REQUEST packet back)
        DBData data = new DBData(myUserID, ConstVar.SYNCH_DATA_REQUEST,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        DBSingleton.getInstance(context).getDB().addPITData(data);

        new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP, ConstVar.INTEREST_TYPE)
                .execute(interest.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

        // store received packet in database for further review
        Utils.storeInterestPacket(context, interest);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        continueReceiverExecution = false;  // notify receiver to terminate
    }
}