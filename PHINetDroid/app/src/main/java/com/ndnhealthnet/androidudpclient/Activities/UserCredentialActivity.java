package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Provides UI for user to enter/update credentials (i.e., user_id and sensor_id)
 */
public class UserCredentialActivity extends Activity {

    Button backBtn, credentialSaveBtn;
    EditText userIDEdit, sensorIDEdit;

    final private int SENSOR_ID_LENGTH = 4; // valid sensor id is defined as 4 digit integer

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usercredential);

        // get current user credentials to populate EditTexts
        String currentSensorID = Utils.getFromPrefs(getApplicationContext(),
                StringConst.PREFS_LOGIN_SENSOR_ID_KEY, "");
        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                StringConst.PREFS_LOGIN_USER_ID_KEY, "");

        userIDEdit = (EditText) findViewById(R.id.userid_editText);
        userIDEdit.setText(currentUserID);

        sensorIDEdit = (EditText) findViewById(R.id.sensorid_editText);
        sensorIDEdit.setText(currentSensorID);

        backBtn = (Button) findViewById(R.id.credentialBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // get current user credentials and determine whether valid
                String currentSensorID = Utils.getFromPrefs(getApplicationContext(),
                        StringConst.PREFS_LOGIN_SENSOR_ID_KEY, "");
                String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                        StringConst.PREFS_LOGIN_USER_ID_KEY, "");

               Intent returnIntent = new Intent();

                if (validInputSensorID(currentSensorID) && validInputUserID(currentUserID)) {
                    setResult(RESULT_OK,returnIntent);
                } else {
                    setResult(RESULT_CANCELED, returnIntent);
                }

                finish();
            }
        });

        // saves user input
        credentialSaveBtn = (Button) findViewById(R.id.credentialSaveBtn);
        credentialSaveBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                String userID = userIDEdit.getText().toString();
                String sensorID = sensorIDEdit.getText().toString();

                if (validInputUserID(userID) && validInputSensorID(sensorID)) {
                    Utils.saveToPrefs(getApplicationContext(), StringConst.PREFS_LOGIN_USER_ID_KEY, userID);
                    Utils.saveToPrefs(getApplicationContext(), StringConst.PREFS_LOGIN_SENSOR_ID_KEY, sensorID);

                    Toast toast = Toast.makeText(getApplicationContext(), "Save successful.", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Input invalid", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    /**
     * attempts to determine whether userID input is valid
     *
     * @param userID input to have validity assessed
     * @return boolean regarding validity of input
     */
    private boolean validInputUserID(String userID) {
        // TODO - perform sophisticated input validation

        return userID.length() > 5 && userID.length() < 15;
    }

    /**
     * attempts to determine whether sensorID input is valid
     * valid input is a four digit integer string
     *
     * @param sensorID input to have validity assessed
     * @return boolean regarding validity of input
     */
    private boolean validInputSensorID(String sensorID) {

        boolean allDigits = sensorID.length() == SENSOR_ID_LENGTH;

        for (int i = 0; i < sensorID.length(); i++) {
            allDigits &= Character.isDigit(sensorID.charAt(i));
        }

        return allDigits;
    }
}
