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
 * Enables user to join to PHINet; request is sent to server for validation, notification sent back.
 */
public class SignupActivity extends Activity {

    Button backBtn, signupBtn;
    EditText userNameEdit, pwEdit, verifyPWEdit, emailEdit;
    TextView errorText;
    ProgressBar progressBar;

    final int SLEEP_TIME = 250; // 250 milliseconds = 1/4 second (chosen somewhat arbitrarily)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userNameEdit = (EditText) findViewById(R.id.usernameEditText);
        pwEdit = (EditText) findViewById(R.id.passwordEditText);
        verifyPWEdit = (EditText) findViewById(R.id.verifyPasswordEditText);
        emailEdit = (EditText) findViewById(R.id.email_editText);
        errorText = (TextView) findViewById(R.id.inputErrorTextView);
        progressBar = (ProgressBar) findViewById(R.id.registerProgressBar);

        progressBar.setVisibility(View.GONE); // hide progress bar until signup pressed

        backBtn = (Button) findViewById(R.id.signupBackBtn);
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

        signupBtn = (Button) findViewById(R.id.signupSubmitBtn);
        signupBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                errorText.setText(""); // remove any error text; user is attempting login again

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                // a wifi connection is required to signup
                if (mWifi.isConnected()) {

                    /**
                     * Due to the nature of NDN, the client must first send signup Interest to
                     * server (because the server is the only one who can validate such requests). The
                     * server will then reply with a blank Data and, shortly, an Interest requesting
                     * signup credentials, and the client will then reply with a Data packet containing
                     * them. The client then sends an Interest to the server querying for the result,
                     * to which the server replies with a Data packet. If the results are positive,
                     * the client has signed up; otherwise, signup failed.
                     */

                    final String userID = userNameEdit.getText().toString().trim();
                    final String password = pwEdit.getText().toString().trim();
                    final String email = emailEdit.getText().toString().equals("")
                            ? ConstVar.NULL_FIELD : emailEdit.getText().toString().trim();
                    final boolean pwMatch = password.equals(verifyPWEdit.getText().toString());

                    // both inputs are valid, now query server for signup
                    if (pwMatch && Utils.isValidUserName(userID) && Utils.isValidPassword(password)
                            && (email.equals(ConstVar.NULL_FIELD) || Utils.isValidEmail(email))) {

                        progressBar.setVisibility(View.VISIBLE); // show progress bar now

                        String currentTime = Utils.getCurrentTime();

                        /**
                         * Here client userID is stored in sensorID position. We needed to send userID 
                         * but had no place to do so and sensorID would have otherwise been null. 
                         */

                        // send Interest to initiate signup
                        Name packetName = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                                currentTime, ConstVar.REGISTER_REQUEST);

                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP, ConstVar.INTEREST_TYPE)
                                .execute(interest.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                        // store received packet in database for further review
                        Utils.storeInterestPacket(getApplicationContext(), interest);

                        // invoke method that handles the server's reply
                        initialServerReplyHandler(userID, password, email, currentTime);
                    }
                    // passwords don't match; only notify is PW was valid in first place
                    else if (Utils.isValidPassword(password) && !pwMatch) {
                        errorText.setText("Error: passwords don't match.");
                    }
                    // one input (or both) were invalid, notify user
                    else {
                        errorText.setText("Error: input syntactically incorrect.");
                    }
                }
                // wifi connection invalid; notify user
                else {
                    errorText.setText("Error: WiFi connection is required.");
                }
            }
        });
    }

    /**
     * Method creates thread, after initial signup request, and checks
     * for a reply from the server before responding appropriately.
     *
     * @param userID entered to signup
     * @param password entered to signup
     * @param email entered to signup
     * @param packetTime of initial Interest packet (used to delete from PIT if result not found)
     */
    private void initialServerReplyHandler(final String userID, final String password,
                                           final String email, final String packetTime) {

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

                            // search for Interest with PID CREDENTIAL_REQUEST && request for this user
                            if (pitRows.get(i).getProcessID().equals(ConstVar.REGISTER_CREDENTIAL_DATA)
                                    && pitRows.get(i).getUserID().equals(userID)) {
                                interestFound = pitRows.get(i);
                                break; // valid interest found; break from loop
                            }
                        }

                        // server has replied with Interest requesting credentials
                        if (interestFound != null) {

                            // reply with credentials to satisfy the interest
                            String credentialContent = userID + "," + password + "," + email;

                            DBData credentialData = new DBData(ConstVar.NULL_FIELD,
                                    ConstVar.REGISTER_CREDENTIAL_DATA, interestFound.getTimeString(),
                                    userID, credentialContent, ConstVar.DEFAULT_FRESHNESS_PERIOD);

                            Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                    interestFound.getTimeString(), ConstVar.REGISTER_CREDENTIAL_DATA);

                            Data data = JNDNUtils.createDataPacket(credentialContent, packetName);

                            new UDPSocket(ConstVar.PHINET_PORT, ConstVar.SERVER_IP, ConstVar.DATA_TYPE)
                                    .execute(data.wireEncode().getImmutableArray()); // reply to interest with DATA from cache

                            // delete Interest requesting signup credentials from PIT; it has been satisfied
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(interestFound.getUserID(),
                                            interestFound.getTimeString(), interestFound.getIpAddr());

                            // place packet into CACHE
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .addCSData(credentialData);

                            // delete initial Interest placed in PIT to initiate signup
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);

                            // store packet in database for further review
                            Utils.storeDataPacket(getApplicationContext(), data);

                            SignupActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            // wait 1 second for server to process results before checking result
                                            credentialQueryHandler(userID, password);
                                        }
                                    }, 1000); // chosen somewhat arbitrarily
                                }
                            });

                            serverResponseFound = true;
                            break; // result was found, break from loop
                        }
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean serverResponseFoundFinal = serverResponseFound;

                SignupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!serverResponseFoundFinal) {
                            errorText.setText("Signup failed.\nCould not reach server.");
                            progressBar.setVisibility(View.GONE); // hide progress; signup failed

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
     * Method invoked via handler, after register credentials sent, and
     * sends an Interest to the server to inquire whether register was valid.
     *
     * @param userID entered to signup
     * @param password entered to signup
     */
    private void credentialQueryHandler(final String userID, final String password) {

        String currentTime = Utils.getCurrentTime();

        /**
         * Here userID is stored in sensorID position. We needed to send userID
         * but had no place to do so and sensorID would have otherwise been null.
         */

        // send interest requesting result of signup
        Name packetNameInner = JNDNUtils.createName(ConstVar.SERVER_ID, userID,
                currentTime, ConstVar.REGISTER_RESULT);
        Interest interest = JNDNUtils.createInterestPacket(packetNameInner);

        // add entry into PIT
        DBData data = new DBData(userID, ConstVar.REGISTER_RESULT,
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
     * Method creates thread, after query for result of register send,
     * and checks the content store for the result before deleting it.
     *
     * @param userID entered to signup
     * @param password entered to signup
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

                    // query the ContentStore for the signup result

                    ArrayList<DBData> potentiallyValidRows = DBSingleton.getInstance(getApplicationContext()).getDB()
                            .getGeneralCSData(ConstVar.SERVER_ID);

                    if (potentiallyValidRows != null) {

                        DBData signupResult = null;

                        /**
                         * Here userID is stored in sensorID position. We needed to send userID
                         * but had no place to do so and sensorID would have otherwise been null.
                         */

                        for (int i = 0; i < potentiallyValidRows.size(); i++) {
                            if (potentiallyValidRows.get(i).getProcessID().equals(ConstVar.REGISTER_RESULT)
                                    && potentiallyValidRows.get(i).getSensorID().equals(userID)) {

                                signupResult = potentiallyValidRows.get(i);
                            }
                        }

                        // signup response yields success
                        if (signupResult != null && !signupResult.getDataFloat().equals(ConstVar.REGISTER_FAILED)) {

                            // on signup, server replies with FIB entries - place into FIB now
                            Utils.insertServerFIBEntries(signupResult.getDataFloat(),
                                    signupResult.getTimeString(), getApplicationContext());

                            // after reading entry, delete it (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deleteCSEntry(signupResult.getUserID(), signupResult.getTimeString());

                            // signup was successful; store values now
                            String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);
                            Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);

                            serverResponseFound = true; // response found
                            accountMayExist = false; // result success; account doesn't exist

                            break; // break from loop, response found

                        } else if (signupResult != null && signupResult.getDataFloat().equals(ConstVar.REGISTER_FAILED)) {

                            // after reading entry, delete it (it's been satisfied)
                            DBSingleton.getInstance(getApplicationContext())
                                    .getDB().deleteCSEntry(signupResult.getUserID(), signupResult.getTimeString());

                            serverResponseFound = true; // response found
                            accountMayExist = true; // result failure; account does exist

                            break; // break from loop, response found
                        }
                    }

                    SystemClock.sleep(SLEEP_TIME); // sleep until next check for reply
                }

                final boolean accountMayExistFinal = accountMayExist;
                final boolean serverResponseFoundFinal = serverResponseFound;

                SignupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.GONE); // hide progress; signup process over

                        if (!serverResponseFoundFinal) {

                            // delete initial Interest placed in PIT to initiate login
                            DBSingleton.getInstance(getApplicationContext()).getDB()
                                    .deletePITEntry(ConstVar.SERVER_ID, packetTime, ConstVar.SERVER_IP);


                            errorText.setText("Signup failed.\nCould not reach server.");

                        } else if (serverResponseFoundFinal && accountMayExistFinal) {

                            errorText.setText("Signup failed.\nAccount may already exist.");

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

