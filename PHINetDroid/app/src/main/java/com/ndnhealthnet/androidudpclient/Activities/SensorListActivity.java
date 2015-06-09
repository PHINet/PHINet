package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.ArrayList;

/**
 * Enables user to add sensors then modify things such as collection intervals.
 */
public class SensorListActivity extends ListActivity {

    Button backBtn, addSensorBtn;
    TextView loggedInText;

    final static String SENSOR_NAME = "SENSOR_NAME";

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null); // force activity to reload
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensorlist);

        ArrayList<DBData> sensorList = DBSingleton.getInstance(getApplicationContext()).getDB().getAllSensorData();

        if (sensorList == null) {
            // this should not be called; the sensor should always be present
            sensorList = new ArrayList<>();
        }

        final SensorAdapter adapter = new SensorAdapter(this, sensorList);
        setListAdapter(adapter);

        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place username on screen

        addSensorBtn = (Button) findViewById(R.id.addNewSensorBtn);
        addSensorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final EditText sensorInput = new EditText(SensorListActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(SensorListActivity.this);
                builder.setTitle("Input Sensor Name");
                builder.setView(sensorInput);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!sensorInput.getText().toString().equals("") && sensorInput.getText() != null) {
                            // TODO - perform input validation (e.g., syntax correct and unique)

                            Intent intent = new Intent(SensorListActivity.this, SensorSettingsActivity.class);

                            // TODO - perform a better removal of spaces (such as notify user)
                            String sensorName = sensorInput.getText().toString().replace(" ", "");

                            // through intent, pass sensor information to activity
                            intent.putExtra(SENSOR_NAME, sensorName);
                            startActivity(intent);

                        } else {
                            // notify user of bad input

                            Toast toast = Toast.makeText(SensorListActivity.this,
                                    "Invalid sensor name.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Used by sensor list view.
     */
    private class SensorAdapter extends ArrayAdapter<DBData> {

        Activity activity = null;
        ArrayList<DBData> listData;

        public SensorAdapter(ListActivity li, ArrayList<DBData> allSensors)
        {
            super(li, 0, allSensors);
            listData = allSensors;
            activity = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_sensor, null);
            }

            final String sensorName = listData.get(position).getSensorID();

            // creates individual button in ListView for each patient
            Button sensorButton = (Button)convertView.findViewById(R.id.listSensorButton);
            sensorButton.setText(sensorName);
            sensorButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(SensorListActivity.this);
                    builder.setTitle("Go to the sensor's page?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (sensorName.equals(ConstVar.HEARTBEAT_SENSOR)) {

                                startActivity(new Intent(SensorListActivity.this, RecordHeartbeatActivity.class));
                            } else {
                                Intent intent = new Intent(SensorListActivity.this, SensorSettingsActivity.class);

                                // through intent, pass sensor information to activity
                                intent.putExtra(SENSOR_NAME, sensorName);
                                startActivity(intent);
                            }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });
            return convertView;
        }
    }
}
