package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
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

                if (!pwEdit.getText().equals(verifyPWEdit.getText())) {

                    errorText.setText("Error: passwords don't match");

                    pwEdit.setText("");
                    verifyPWEdit.setText("");

                } else {

                    String hashedPW = BCrypt.hashpw(pwEdit.getText().toString(), BCrypt.gensalt());

                    // send this Hashed PW

                    // TODO - send initial Interest to server and place entry in PIT

                    // TODO - server will send back blank DATA (to conform to NDN); remove entry from PIT

                    // TODO - server will then send INTEREST for your credentials (then, send)

                    // TODO - you then send INTEREST for server's response (server sends data back)

                    // TODO - connection over; now notify user and store/encrypt credentials on phone
                }
            }
        });
    }
}

