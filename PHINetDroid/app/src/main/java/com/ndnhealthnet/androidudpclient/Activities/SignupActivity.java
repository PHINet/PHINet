package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import java.util.ArrayList;

/**
 * Enables user to join to PHINet; request is sent to server for validation, notification sent back.
 */
public class SignupActivity extends Activity {

    Button backBtn, signupBtn;
    EditText usernameEdit, pwEdit, verifyPWEdit, emailEdit;
    TextView errorText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEdit = (EditText) findViewById(R.id.usernameEditText);
        pwEdit = (EditText) findViewById(R.id.passwordEditText);
        verifyPWEdit = (EditText) findViewById(R.id.verifyPasswordEditText);
        emailEdit = (EditText) findViewById(R.id.email_editText);
        errorText = (TextView) findViewById(R.id.inputErrorTextView);

        backBtn = (Button) findViewById(R.id.signupBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        signupBtn = (Button) findViewById(R.id.signupSubmitBtn);
        signupBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                // a wifi connection is required to signup
                if (mWifi.isConnected()) {

                    /**
                     * Due to the nature of NDN, the client must first send signup Interest to
                     * server (because the server is the only one who can process such requests). The
                     * server will then reply with a blank Data and, shortly, an Interest requesting
                     * user credentials, and the client will then reply with a Data packet containing
                     * them. The client then sends an Interest to the server querying for the result,
                     * to which the server replies with a Data packet. If the results are positive,
                     * the client has registered; otherwise, register failed.
                     */

                    String userID = usernameEdit.getText().toString();
                    String password = pwEdit.getText().toString();

                    if (!pwEdit.getText().equals(verifyPWEdit.getText())) {

                        errorText.setText("Error: passwords don't match");

                        pwEdit.setText("");
                        verifyPWEdit.setText("");

                    } else if (Utils.validInputUserName(userID) && Utils.validInputPassword(password)) {

                        String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());

                        // send this Hashed PW

                        Name packetName = JNDNUtils.createName(userID, ConstVar.NULL_FIELD,
                                Utils.getCurrentTime(), ConstVar.REGISTER_REQUEST);
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
                        Handler h = new Handler();
                        int delay = 10000; //milliseconds

                        h.postDelayed(new Runnable() {
                            public void run() {

                                ArrayList<DBData> pitRows = DBSingleton.getInstance(getApplicationContext())
                                        .getDB().getGeneralPITData(ConstVar.SERVER_ID);

                                if (pitRows != null) {
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
                                        // TODO - 1. reply with credentials; 2. send interest to server
                                        // TODO - 3. wait for server correspondence and react accordingly

                                    } else {
                                        // TODO - handle this case
                                        System.out.println("ERROR: the server has not replied");

                                    }

                                } else {
                                    // TODO - notify user
                                }

                            }
                        }, delay);

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
    }
}

