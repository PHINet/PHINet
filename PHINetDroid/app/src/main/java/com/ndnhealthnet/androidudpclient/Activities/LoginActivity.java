package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.ArrayList;

/**
 * Enables user to login to PHINet; request is sent to server for validation, notification sent back.
 */
public class LoginActivity extends Activity {

    Button backBtn, loginBtn;
    EditText userNameEdit, pwEdit;
    TextView errorText;
    ProgressBar progressBar;

    final int SLEEP_TIME = 250; // 250 milliseconds = 1/4 second (chosen somewhat arbitrarily)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEdit = (EditText) findViewById(R.id.usernameEditText);
        pwEdit = (EditText) findViewById(R.id.passwordEditText);
        errorText = (TextView) findViewById(R.id.inputErrorTextView);
        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);

        progressBar.setVisibility(View.GONE); // hide progress bar until login pressed

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

                    final String userID = userNameEdit.getText().toString().trim();
                    final String password = pwEdit.getText().toString().trim();

                    // both inputs are valid, now query server for login
                    if (Utils.isValidUserName(userID) && Utils.isValidPassword(password)) {

                        progressBar.setVisibility(View.VISIBLE); // show progress bar now

                        String currentTime = Utils.getCurrentTime();

                        /**
                         * Here client userID is stored in sensorID position. We needed to send userID 
                         * but had no place to do so and sensorID would have otherwise been null. 
                         */

                        // send Interest to initiate login
                        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                                currentTime, ConstVar.LOGIN_REQUEST);
                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP, ConstVar.INTEREST_TYPE)
                                .execute(interest.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                        // store received packet in database for further review
                        Utils.storeInterestPacket(getApplicationContext(), interest);

                        // invoke method that handles the server's reply
                        initialServerReplyHandler(userID, password, currentTime);

                    }
                    // one input (or both) were invalid, notify user
                    else {

                        errorText.setText("Error: input syntactically incorrect.");
                    }
                }
                // wifi connection invalid; notify user
                else {
                    errorText.setText("Error: WiFi connection required.");
                }
            }
        });

        backBtn = (Button) findViewById(R.id.loginBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // get current user credentials and determine whether valid
                String currentPassword = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

                Intent returnIntent = new Intent();

                if (Utils.isValidUserName(currentUserID)
                        && Utils.isValidPassword(currentPassword)) {
                    setResult(RESULT_OK, returnIntent); // notifies MainActivity of success
                } else {
                    setResult(RESULT_CANCELED, returnIntent); // notifies MainActivity of failure
                }

                finish();
            }
        });
    }

    /**
     * Method creates thread, after initial login request, and checks
     * for a reply from the server before responding appropriately.
     *
     * @param userID entered to login
     * @param password entered to login
     * @param packetTime of initial Interest packet (used to delete from PIT if result not found)
     */
    private void initialServerReplyHandler(final String userID, final String password, final String packetTime) {

        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 4; // check for SLEEP_TIME*4 = 1 second (somewhat arbitrary)
                int loopCount = 0;
                boolean serverResponseFound = false;

                while (loopCount++ < maxLoopCount) {

                    // check to see if server has sent an Interest asking for client's credentials
                    ArrayList<DBData> pitRows = DBSingleton.getInstance(getApplicationContext())
                            .getDB().getGeneralPITData(userID);

                    // valid Interest potentially found
                    if (pitRows != null) {

                        DBData interestFound = null;
                        for (int i = 0; i < pitRows.size(); i++) {

                            // search for Interest with PID CREDENTIAL_REQUEST && request for this client
                            if (pitRows.get(i).getProcessID().equals(ConstVar.LOGIN_CREDENTIAL_DATA)
                                    && pitRows.get(i).getUserID().equals(userID)) {
                                interestFound = pitRows.get(i);
                                break; // valid interest found; break from loop
                            }
                        }

                        // server has replied with Interest requesting credentials
                        if (interestFound != null) {

                            // reply with credentials to satisfy the interest
                            String credentialContent = userID + "," + password;

                            DBData credentialData = new DBData(ConstVar.NULL_FIELD,
                                    ConstVar.REGISTER_CREDENTIAL_DATA, interestFound.getTimeString(),
                                    userID, credentialContent, ConstVar.DEFAULT_FRESHNESS_PERIOD);

                            Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                    interestFound.getTimeString(), ConstVar.LOGIN_CREDENTIAL_DATA);

                            Data data = JNDNUtils.createDataPacket(credentialContent, packetName);

                            new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP, ConstVar.DATA_TYPE)
                                    .execute(data.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                            // delete Interest requesting login credentials from PIT; it has been satisfied
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(interestFound.getUserID(),
                                            interestFound.getTimeString(), interestFound.getIpAddr());

                            // place packet into CACHE
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .addCSData(credentialData);

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                            // store packet in database for further review
                            Utils.storeDataPacket(getApplicationContext(), data);

                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            // wait .5 second for server to process results before checking result
                                            credentialQueryHandler(userID, password);
                                        }
                                    }, 500); // chosen somewhat arbitrarily
                                }
                            });

                            serverResponseFound = true;
                            break; // result was found, break from loop
                        }
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean serverResponseFoundFinal = serverResponseFound;

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!serverResponseFoundFinal) {
                            errorText.setText("Login failed.\nCould not reach server.");
                            progressBar.setVisibility(View.GONE); // hide progress; login failed

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * Method invoked via handler, after login credentials sent, and
     * sends an Interest to the server to inquire whether login was valid.
     *
     * @param userID entered to login
     * @param password entered to login
     */
    private void credentialQueryHandler(final String userID, final String password) {
        String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        // send interest requesting result of login
        Name packetNameInner = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.LOGIN_RESULT);
        Interest interest = JNDNUtils.createInterestPacket(packetNameInner);

        // add entry into PIT
        DBData data = new DBData(userID, ConstVar.LOGIN_RESULT,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(data);

        new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP, ConstVar.INTEREST_TYPE)
                .execute(interest.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

        // store packet in database for further review
        Utils.storeInterestPacket(getApplicationContext(), interest);

        // after sending query result, now handle its response
        credentialQueryResultHandler(userID, password, currentTime);
    }

    /**
     * Method creates thread, after query for result of login sent,
     * and checks the content store for the result before deleting it.
     *
     * @param userID entered to login
     * @param password entered to login
     * @param packetTime of Interest packet sent to query result (used to delete from PIT if result not found)
     */
    private void credentialQueryResultHandler(final String userID, final String password,
                                              final String packetTime) {

        new Thread(new Runnable() {
            public void run() {

                int maxLoopCount = 8; // check for SLEEP_TIME*8 = 2 seconds (somewhat arbitrary)
                int loopCount = 0;
                boolean serverResponseFound = false;
                boolean accountMayExist = false;

                while (loopCount++ < maxLoopCount) {

                    // query the ContentStore for the login result
                    ArrayList<DBData> potentiallyValidRows = DBSingleton.getInstance(getApplicationContext()).getDB()
                            .getGeneralCSData(ConstVar.SERVER_ID);

                    if (potentiallyValidRows != null) {

                        DBData loginResult = null;

                        /**
                         * Here userID is stored in sensorID position. We needed to send userID
                         * but had no place to do so and sensorID would have otherwise been null.
                         */

                        for (int i = 0; i < potentiallyValidRows.size(); i++) {
                            if (potentiallyValidRows.get(i).getProcessID().equals(ConstVar.LOGIN_RESULT)
                                    && potentiallyValidRows.get(i).getSensorID().equals(userID)) {

                                loginResult = potentiallyValidRows.get(i);
                            }
                        }

                        if (loginResult != null && !loginResult.getDataFloat().equals(ConstVar.LOGIN_FAILED)) {

                            // on login, server replies with FIB entries - place into FIB now
                            Utils.insertServerFIBEntries(loginResult.getDataFloat(),
                                    loginResult.getTimeString(), getApplicationContext());

                            // after reading entry, delete it (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deleteCSEntry(loginResult.getUserID(), loginResult.getTimeString());

                            // login was successful; store values now
                            String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);

                            serverResponseFound = true; // response found
                            accountMayExist = false; // result success; account doesn't exist

                            break; // break from loop, response found

                        } else if (loginResult != null && loginResult.getDataFloat().equals(ConstVar.LOGIN_FAILED)) {

                            // after reading entry, delete it (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deleteCSEntry(loginResult.getUserID(), loginResult.getTimeString());

                            serverResponseFound = true; // response found
                            accountMayExist = true; // result failure; account does exist

                            break; // break from loop, response found
                        }
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean accountMayExistFinal = accountMayExist;
                final boolean serverResponseFoundFinal = serverResponseFound;

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.GONE); // hide progress; login process over

                        if (!serverResponseFoundFinal) {

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                            errorText.setText("Login failed.\nCould not reach server.");

                        } else if (serverResponseFoundFinal && accountMayExistFinal) {
                            errorText.setText("Login failed.\nInput combination does not seem to exist.");

                        } else if (serverResponseFoundFinal && !accountMayExistFinal) {
                            // go to main page; signup was successful
                            setResult(RESULT_OK, new Intent());
                            finish();
                        }
                    }
                });
            }
        }).start();
    }
}
