package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Enables user to connect to a sensor then modify its settings (such as collection interval).
 */
public class SensorSettingsActivity extends Activity {

    Button backBtn, connectBtn, saveBtn, deleteSensorBtn;
    TextView loggedInText, sensorNameText, connectStatusText;
    EditText intervalEdit;
    String sensorName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensorsettings);

        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                StringConst.PREFS_LOGIN_USER_ID_KEY, "");

        // use IP and ID from intent to find patient among all patients
        sensorName = getIntent().getExtras().getString(SensorListActivity.SENSOR_NAME);

        intervalEdit = (EditText) findViewById(R.id.intervalEditText);

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID);

        sensorNameText = (TextView) findViewById(R.id.specificSensorNameTextView);
        sensorNameText.setText(sensorName);

        connectStatusText = (TextView) findViewById(R.id.specificConnectionStatusTextView);
        connectStatusText.setText("DISCONNECTED");
        // TODO - set connection status

        connectBtn = (Button) findViewById(R.id.sensorConnectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO -
            }
        });

        saveBtn = (Button) findViewById(R.id.sensorDataSubmitBtn);
        saveBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - perform input validation

                // TODO - save settings
            }
        });

        deleteSensorBtn = (Button) findViewById(R.id.deleteSensorBtn);
        deleteSensorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO - delete sensor
            }
        });

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                finish();
            }
        });
    }
}
