package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
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

        DBData specificSensor = DBSingleton.getInstance(getApplicationContext()).getDB().getSpecificSensorData(sensorName);

        intervalEdit = (EditText) findViewById(R.id.intervalEditText);

        if (specificSensor == null) {
            intervalEdit.setText("24"); // initial, arbitrarily chosen, interval
        } else {
            intervalEdit.setText(Integer.toString(specificSensor.getSensorCollectionInterval()));
        }

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

                Toast toast = Toast.makeText(getApplicationContext(), "Not yet implemented", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        saveBtn = (Button) findViewById(R.id.sensorDataSubmitBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int chosenInterval = Integer.parseInt(intervalEdit.getText().toString());

                if (chosenInterval <= 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid interval", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    DBData sensorQueryResult = DBSingleton.getInstance(getApplicationContext()).getDB().getSpecificSensorData(sensorName);

                    if (sensorQueryResult == null) {
                        // result was null; sensor hasn't been added yet - do so now

                        DBData newEntry = new DBData(sensorName, chosenInterval);

                        DBSingleton.getInstance(getApplicationContext()).getDB().addSensorData(newEntry);
                    } else {
                        // result wasn't null; sensor has already been added - just update it

                        DBData updatedEntry = new DBData(sensorName, chosenInterval);

                        DBSingleton.getInstance(getApplicationContext()).getDB().updateSensorData(updatedEntry);
                    }

                    Toast toast = Toast.makeText(getApplicationContext(), "Save successful", Toast.LENGTH_LONG);
                    toast.show();

                    // TODO - initiate collection interval (create thread to collect, etc)
                }
            }
        });

        deleteSensorBtn = (Button) findViewById(R.id.deleteSensorBtn);
        deleteSensorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DBData sensorQueryResult = DBSingleton.getInstance(getApplicationContext()).getDB().getSpecificSensorData(sensorName);


                if (sensorQueryResult != null) {
                    // sensor exists within db; delete it now

                    DBSingleton.getInstance(getApplicationContext()).getDB().deleteSensorEntry(sensorName);

                }

                Toast toast = Toast.makeText(getApplicationContext(), "Deletion successful", Toast.LENGTH_LONG);
                toast.show();

                finish();
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
