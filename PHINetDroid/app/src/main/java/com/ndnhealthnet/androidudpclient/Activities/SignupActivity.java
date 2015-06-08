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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
 * Enables user to join to PHINet; request is sent to server for validation, notification sent back.
 */
public class SignupActivity extends Activity {

    Button backBtn, signupBtn;
    EditText userNameEdit, pwEdit, verifyPWEdit, emailEdit;
    TextView errorText;
    ProgressBar progressBar;

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

                if (Utils.validInputUserName(currentUserID)
                        && Utils.validInputPassword(currentPassword)) {
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
                     * Due to the nature of NDN, the client must first send register Interest to
                     * server (because the server is the only one who can validate such requests). The
                     * server will then reply with a blank Data and, shortly, an Interest requesting
                     * register credentials, and the client will then reply with a Data packet containing
                     * them. The client then sends an Interest to the server querying for the result,
                     * to which the server replies with a Data packet. If the results are positive,
                     * the client has signed up; otherwise, signup failed.
                     */

                    final String userID = userNameEdit.getText().toString();
                    final String password = BCrypt.hashpw(pwEdit.getText().toString(), BCrypt.gensalt());
                    final String email = emailEdit.getText().toString().equals("") ? "null" : emailEdit.getText().toString();

                    // both inputs are valid, now query server for signup
                    if (Utils.validInputUserName(userID) && Utils.validInputPassword(password)
                            && Utils.validEmail(email)) {

                        progressBar.setVisibility(View.VISIBLE); // show progress bar now

                        Toast toast = Toast.makeText(getApplicationContext(), "This takes about 10 seconds", Toast.LENGTH_LONG);
                        toast.show();

                        // send Interest to initiate signup
                        Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                Utils.getCurrentTime(), ConstVar.REGISTER_REQUEST);
                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        Blob blob = interest.wireEncode();

                        // TODO - update with server's real IP
                        new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.INTEREST_TYPE)
                                .execute(blob.getImmutableArray()); // reply to interest with DATA from cache

                        // store received packet in database for further review
                        Utils.storeInterestPacket(getApplicationContext(), interest);

                        // after sent, wait 1 second (chosen arbitrarily) for server reply
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                initialServerReplyHandler(userID, password, email);
                            }
                        }, 1000);

                    }
                    // one input (or both) were invalid, notify user
                    else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Input invalid", Toast.LENGTH_LONG);
                        toast.show();

                        errorText.setText("Error: input syntactically incorrect.");
                    }
                }
                // wifi connection invalid; notify user
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "A WiFi connection is required.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    /**
     * Method invoked via handler, after initial signup request, and checks
     * for a reply from the server before responding appropriately.
     *
     * @param userID entered to signup
     * @param password entered to signup
     */
    private void initialServerReplyHandler(final String userID, final String password, final String email) {
        // check to see if server has sent an Interest asking for client's credentials
        ArrayList<DBData> pitRows = DBSingleton.getInstance(getApplicationContext())
                .getDB().getGeneralPITData(userID);

        // valid Interest potentially found
        if (pitRows != null) {

            DBData interestFound = null;
            for (int i = 0; i < pitRows.size(); i++) {
                if (pitRows.get(i).getProcessID().equals(ConstVar.CREDENTIAL_REQUEST)) {
                    interestFound = pitRows.get(i);
                    break; // valid interest found; break from loop
                }
            }

            // server has replied with Interest requesting credentials
            if (interestFound != null) {
                Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                        Utils.getCurrentTime(), ConstVar.REGISTER_CREDENTIAL_DATA);

                // reply with credentials to satisfy the interest
                String credentialContent = userID + "," + password + "," + email;

                Data data = JNDNUtils.createDataPacket(credentialContent, packetName);
                Blob blob = data.wireEncode();

                // TODO - update with server's real IP
                new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.DATA_TYPE)
                        .execute(blob.getImmutableArray()); // reply to interest with DATA from cache

                // delete Interest requesting signup credentials from PIT; it has been satisfied
                DBSingleton.getInstance(getApplicationContext()).getDB()
                        .deletePITEntry(interestFound.getUserID(),
                                interestFound.getTimeString(), interestFound.getIpAddr());

                // store packet in database for further review
                Utils.storeDataPacket(getApplicationContext(), data);

                // wait 4 seconds (arbitrary) after sending credentials before querying for result
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        credentialQueryHandler(userID, password);
                    }
                }, 4000);
            }
            // the server has not replied with an Interest; error
            else {
                errorText.setText("Signup failed.\nCould not reach server.");
                progressBar.setVisibility(View.GONE); // hide progress; signup failed
            }

        }
        // the server has not replied with an Interest; error
        else {
            errorText.setText("Signup failed.\nCould not reach server.");
            progressBar.setVisibility(View.GONE); // hide progress; signup failed
        }
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

        // send interest requesting result of signup
        Name packetNameInner = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                currentTime, ConstVar.INTEREST_REGISTER_RESULT);
        Interest interest = JNDNUtils.createInterestPacket(packetNameInner);

        // add entry into PIT
        DBData data = new DBData(ConstVar.PIT_DB, ConstVar.NULL_FIELD, ConstVar.DATA_REGISTER_RESULT,
                currentTime, ConstVar.SERVER_ID, ConstVar.SERVER_IP);

        // TODO - note DATA_REGISTER_RESULT (we should not send an INTEREST PID and expect a DATA PID); fix

        DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(data);

        Blob blobInner = interest.wireEncode();

        // TODO - update with server's real IP
        new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.INTEREST_TYPE)
                .execute(blobInner.getImmutableArray()); // reply to interest with DATA from cache

        // store packet in database for further review
        Utils.storeInterestPacket(getApplicationContext(), interest);

        // wait 4 seconds (arbitrary) after sending credentials before checking result
        new Handler().postDelayed(new Runnable() {
            public void run() {
                credentialQueryResultHandler(userID, password);
            }
        }, 4000);
    }

    /**
     * Method invoked via handler, after query for result of register send,
     * and checks the content store for the result before deleting it.
     *
     * @param userID entered to signup
     * @param password entered to signup
     */
    private void credentialQueryResultHandler(String userID, String password) {
        // query the ContentStore for the signup result

        progressBar.setVisibility(View.GONE); // results have come back; remove progress bar

        ArrayList<DBData> potentiallyValidRows = DBSingleton.getInstance(getApplicationContext()).getDB()
                .getGeneralCSData(ConstVar.SERVER_ID);

        if (potentiallyValidRows == null) {
            errorText.setText("Signup failed.\nCould not reach server.");
            progressBar.setVisibility(View.GONE); // hide progress; signup failed

        } else {
            DBData signupResult = null;

            for (int i = 0; i < potentiallyValidRows.size(); i++) {
                if (potentiallyValidRows.get(i).getProcessID().equals(ConstVar.DATA_REGISTER_RESULT)) {

                    signupResult = potentiallyValidRows.get(i);

                    // TODO - provide more validation (this may not be the signup result for THIS client)
                }
            }

            if (signupResult == null || signupResult.getProcessID().equals(ConstVar.REGISTER_FAILED)) {
                // failure
                errorText.setText("Register failed.\nAccount may already exist.");
                progressBar.setVisibility(View.GONE); // hide progress; signup failed

            } else {
                // after reading entry, delete it so that others can't get it
                DBSingleton.getInstance(getApplicationContext())
                        .getDB().deleteCSEntry(signupResult.getUserID(), signupResult.getTimeString());

                // signup was successful; store values now
                String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, userID);
                Utils.saveToPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);

                // go to main page; signup was successful
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        }
    }
}

