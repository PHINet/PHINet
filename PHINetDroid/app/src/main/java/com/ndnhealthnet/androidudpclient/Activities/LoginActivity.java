package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Comm.UDPListener;
import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Hashing.BCrypt;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import java.util.ArrayList;

/**
 * Enables user to login to PHINet; request is sent to server for validation, notification sent back.
 */
public class LoginActivity extends Activity {

    Button backBtn, loginBtn;
    EditText userNameEdit, pwEdit;
    TextView errorText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEdit = (EditText) findViewById(R.id.usernameEditText);
        pwEdit = (EditText) findViewById(R.id.passwordEditText);
        errorText = (TextView) findViewById(R.id.inputErrorTextView);

        loginBtn = (Button) findViewById(R.id.loginSubmitBtn);
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                errorText.setText(""); // remove any error text; user is attempting login again

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                // a wifi connection is required to login
                if (mWifi.isConnected()) {

                    /**
                     * Due to the nature of NDN, the client must first send login Interest to
                     * server (because the server is the only one who can validate such requests). The
                     * server will then reply with a blank Data and, shortly, an Interest requesting
                     * login credentials, and the client will then reply with a Data packet containing
                     * them. The client then sends an Interest to the server querying for the result,
                     * to which the server replies with a Data packet. If the results are positive,
                     * the client has logged in; otherwise, login failed.
                     */

                    final String userID = userNameEdit.getText().toString();
                    final String password = pwEdit.getText().toString();

                    // both inputs are valid, now query server for login
                    if (Utils.validInputUserName(userID) && Utils.validInputPassword(password)) {

                        Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                Utils.getCurrentTime(), ConstVar.LOGIN_REQUEST);
                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        Blob blob = interest.wireEncode();

                        System.out.println("sending the initial login interest");

                        // TODO - update with server's real IP
                        new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.INTEREST_TYPE)
                                .execute(blob.getImmutableArray()); // reply to interest with DATA from cache

                        // store received packet in database for further review
                        DBSingleton.getInstance(getApplicationContext()).getDB()
                                .addPacketData(new DBData(interest.getName().toUri(), Utils.convertInterestToString(interest)));

                        // after sent, wait 1 second (chosen arbitrarily) for server reply
                        Handler hOuter = new Handler();
                        int delay = 1000; //milliseconds

                        System.out.println("timer staring now");
                        hOuter.postDelayed(new Runnable() {
                            public void run() {


                                // TODO- rename
                                handleOuterLoginHandler(userID, password);

                            }
                        }, delay);


                   /*   final String userID = userNameEdit.getText().toString();
                         String password = pwEdit.getText().toString();
                        Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);

                        String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());

                        // store the hashed password
                        Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);

                        Toast toast = Toast.makeText(getApplicationContext(), "Save successful.", Toast.LENGTH_LONG);
                        toast.show();*/
                    }
                    // one input (or both) were invalid, notify user
                    else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Input invalid", Toast.LENGTH_LONG);
                        toast.show();

                        errorText.setText("Error: input invalid");
                    }
                }
                // wifi connection invalid; notify user
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "A WiFi connection is required.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        backBtn = (Button) findViewById(R.id.loginBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // get current user credentials and determine whether valid
                String currentPassword = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                Intent returnIntent = new Intent();

                if (Utils.validInputUserName(currentUserID) && Utils.validInputPassword(currentPassword)) {

                    setResult(RESULT_OK,returnIntent);
                } else {

                    setResult(RESULT_CANCELED, returnIntent);
                }

                finish();
            }
        });
    }

    /**
     *
     * TODO - doc
     *
     * @param userID
     * @param password
     */
    private void handleOuterLoginHandler(final String userID, final String password) {
        System.out.println("executing inner code now");
        ArrayList<DBData> pitRows = DBSingleton.getInstance(getApplicationContext())
                // TODO - doc "userID" choice here
                .getDB().getGeneralPITData(userID);

        if (pitRows != null) {

            System.out.println("PIT QUERY GOOD");

            DBData interestFound = null;
            for (int i = 0; i < pitRows.size(); i++) {
                if (pitRows.get(i).getProcessID().equals(ConstVar.CREDENTIAL_REQUEST)) {
                    interestFound = pitRows.get(i);
                    break;
                }
            }

            // server has replied with Interest requesting credentials
            if (interestFound != null) {
                System.out.println("the server has replied with an interest");


                Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                        Utils.getCurrentTime(), ConstVar.LOGIN_CREDENTIAL_DATA);

                // reply with credentials to satisfy the interest
                String credentialContent = userID + "," + password;


                System.out.println("sending credentials: " + credentialContent);

                Data data = JNDNUtils.createDataPacket(credentialContent, packetName);

                Blob blob = data.wireEncode();

                System.out.println("sending the credential data");

                // TODO - update with server's real IP
                new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.DATA_TYPE)
                        .execute(blob.getImmutableArray()); // reply to interest with DATA from cache

                // delete Interest requesting login credentials from PIT; it has been satisfied
                DBSingleton.getInstance(getApplicationContext()).getDB()
                        .deletePITEntry(interestFound.getUserID(),
                                interestFound.getTimeString(), interestFound.getIpAddr());


                Handler hInner = new Handler();
                int delay = 4000; //milliseconds

                hInner.postDelayed(new Runnable() {
                    public void run() {
                        handleInnerLoginHandler(userID, password);

                    }
                }, delay);


            }
            // the server has not replied with an Interest; error
            else {
                // TODO - handle this case
                System.out.println("ERROR: the server has not replied");

            }

        } else {

            System.out.println("PIT QUERY BAD");
            // TODO - notify user
        }
    }

    /**
     * TODO - doc
     *
     * @param userID
     * @param password
     */
    private void handleInnerLoginHandler(final String userID, final String password) {
        System.out.println("sending inner now");
        // TODO - 1. reply with credentials; 2. send interest to server
        // TODO - 3. wait for server correspondence and react accordingly

        String currentTime = Utils.getCurrentTime();

        // send interest requesting result of login
        Name packetNameInner = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                currentTime, ConstVar.INTEREST_LOGIN_RESULT);
        Interest interestInner = JNDNUtils.createInterestPacket(packetNameInner);

        // add entry into PIT
        DBData data = new DBData(ConstVar.PIT_DB, ConstVar.NULL_FIELD, ConstVar.DATA_LOGIN_RESULT,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        // TODO - note DATA_LOGIN_RESULT (we should not send an INTEREST PID and expect a DATA PID); fix

        boolean pitAddition = DBSingleton.getInstance(getApplicationContext()).getDB()
                .addPITData(data);

        System.out.println("NAME OF PIT ADDITION: " + data.getUserID());

        Blob blobInner = interestInner.wireEncode();

        System.out.println("sending the initial login interest");

        // TODO - update with server's real IP
        new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.INTEREST_TYPE)
                .execute(blobInner.getImmutableArray()); // reply to interest with DATA from cache

        // store received packet in database for further review
        DBSingleton.getInstance(getApplicationContext()).getDB()
                .addPacketData(new DBData(interestInner.getName().toUri(), Utils.convertInterestToString(interestInner)));

        Handler hInnerInner = new Handler();
        int delay = 4000; //milliseconds

        hInnerInner.postDelayed(new Runnable() {
            public void run() {

                handleInnerInnerLoginHandler(userID, password);


            }
        }, delay);
    }

    /**
     *
     * TODO - doc
     *
     * @param userID
     * @param password
     */
    private void handleInnerInnerLoginHandler(String userID, String password) {
        // query the ContentStore for the login result

        ArrayList<DBData> potentiallyValidRows = DBSingleton.getInstance(getApplicationContext()).getDB()
                .getGeneralCSData(ConstVar.SERVER_ID);

        if (potentiallyValidRows == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "THE LOGIN ACTUALLY FAILED; initial query returned nothing", Toast.LENGTH_LONG);
            toast.show();

        } else {
            DBData loginResult = null;

            for (int i = 0; i < potentiallyValidRows.size(); i++) {
                if (potentiallyValidRows.get(i).getProcessID().equals(ConstVar.DATA_LOGIN_RESULT)) {

                    loginResult = potentiallyValidRows.get(i);

                    // TODO - provide more validation (this may not be the login result for THIS client)

                }
            }

            if (loginResult == null || loginResult.getProcessID().equals(ConstVar.LOGIN_FAILED)) {
                // failure
                Toast toast = Toast.makeText(getApplicationContext(), "THE LOGIN ACTUALLY FAILED", Toast.LENGTH_LONG);
                toast.show();

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "THE LOGIN ACTUALLY SUCCEEDED", Toast.LENGTH_LONG);
                toast.show();

               /*
               Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);

                String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());

                // store the hashed password
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);*/

            }

            // TODO - after reading entry, delete it so that others can't get it
            // TODO - check for result in Data
        }
    }
}
