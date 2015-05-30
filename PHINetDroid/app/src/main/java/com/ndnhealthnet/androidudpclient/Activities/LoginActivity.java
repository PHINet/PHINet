package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Hashing.BCrypt;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

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
                // TODO - send initial Interest to server and place entry in PIT

                // TODO - server will send back blank DATA (to conform to NDN); remove entry from PIT

                // TODO - server will then send INTEREST for your credentials (then, send)

                // TODO - you then send INTEREST for server's response (server sends data back)

                // TODO - connection over; now notify user and store/encrypt credentials on phone

                String userID = userNameEdit.getText().toString();
                String password = pwEdit.getText().toString();

                if (Utils.validInputUserName(userID) && Utils.validInputPassword(password)) {
                    Utils.saveToPrefs(getApplicationContext(), StringConst.PREFS_LOGIN_USER_ID_KEY, userID);

                    String hashedPW = BCrypt.hashpw(password, BCrypt.gensalt());

                    // store the hashed password
                    Utils.saveToPrefs(getApplicationContext(), StringConst.PREFS_LOGIN_PASSWORD_ID_KEY, hashedPW);

                    Toast toast = Toast.makeText(getApplicationContext(), "Save successful.", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Input invalid", Toast.LENGTH_LONG);
                    toast.show();

                    errorText.setText("Error: input invalid");
                }
            }
        });

        backBtn = (Button) findViewById(R.id.loginBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // get current user credentials and determine whether valid
                String currentSensorID = Utils.getFromPrefs(getApplicationContext(),
                        StringConst.PREFS_LOGIN_PASSWORD_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        StringConst.PREFS_LOGIN_USER_ID_KEY, "");

                Intent returnIntent = new Intent();

                if (Utils.validInputUserName(currentSensorID) && Utils.validInputPassword(currentUserID)) {
                    setResult(RESULT_OK,returnIntent);
                } else {
                    setResult(RESULT_CANCELED, returnIntent);
                }

                finish();
            }
        });
    }
}
