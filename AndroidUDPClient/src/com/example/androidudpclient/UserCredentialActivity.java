package com.example.androidudpclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Provides UI for user to enter/update credentials (i.e., user_id and sensor_id)
 */
public class UserCredentialActivity extends Activity {

    Button backBtn, credentialSaveBtn;
    EditText userIDEdit, sensorIDEdit;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercredential);

        // get current user credentials to populate EditTexts
        String currentSensorID = Utils.getFromPrefs(getApplicationContext(),
                Utils.PREFS_LOGIN_SENSOR_ID_KEY, "");
        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                Utils.PREFS_LOGIN_USER_ID_KEY, "");

        userIDEdit = (EditText) findViewById(R.id.userid_editText);
        userIDEdit.setText(currentUserID);

        sensorIDEdit = (EditText) findViewById(R.id.sensorid_editText);
        sensorIDEdit.setText(currentSensorID);

        backBtn = (Button) findViewById(R.id.credentialBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // get current user credentials and determine whether valid
                String currentSensorID = Utils.getFromPrefs(getApplicationContext(),
                        Utils.PREFS_LOGIN_SENSOR_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        Utils.PREFS_LOGIN_USER_ID_KEY, "");

                // TODO - input validation

                Intent returnIntent = new Intent();

                if (currentSensorID == null || currentUserID == null
                        || currentUserID.equals("") || currentSensorID.equals("")) {
                    setResult(RESULT_CANCELED, returnIntent);
                } else {
                    setResult(RESULT_OK,returnIntent);
                }

                finish();
            }
        });

        // saves user input
        credentialSaveBtn = (Button) findViewById(R.id.credentialSaveBtn);
        credentialSaveBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                Utils.saveToPrefs(getApplicationContext(), Utils.PREFS_LOGIN_USER_ID_KEY,
                        userIDEdit.getText().toString());
                Utils.saveToPrefs(getApplicationContext(), Utils.PREFS_LOGIN_SENSOR_ID_KEY,
                        sensorIDEdit.getText().toString());

                Toast toast = Toast.makeText(getApplicationContext(), "Save successful.", Toast.LENGTH_LONG);
                toast.show();
                // TODO - perform input validation
            }
        });
    }
}
