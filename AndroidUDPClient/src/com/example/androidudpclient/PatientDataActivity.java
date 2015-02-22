package com.example.androidudpclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidudpclient.Packet.InterestPacket;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * Activity deals specifically with interacting with patient.
 *
 * Uses include
 * 1. requesting and viewing patient data
 * 2. editing/saving patient information
 */
public class PatientDataActivity extends Activity {

    Button backBtn, requestBtn, submitBtn, deleteBtn;
    EditText ipEditText;
    TextView dataStatusText, nameText;
    GraphView graph;
    String patientIP, patientUserID;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientdata);

        // use IP and ID from intent to find patient among all patients
        patientIP = getIntent().getExtras().getString(GetCliBeatActivity.PATIENT_IP);
        patientUserID = getIntent().getExtras().getString(GetCliBeatActivity.PATIENT_USER_ID);

        ArrayList<DBData> patientCacheData = MainActivity.datasource.getGeneralCSData(patientUserID);

        // textview used to notify user whether data for patient exists
        dataStatusText = (TextView) findViewById(R.id.currentDataStatus_textView);

        // display graph is data is present
        graph = (GraphView) findViewById(R.id.graph);
        if (patientCacheData != null && patientCacheData.size() > 0) {
            dataStatusText.setText("Some data present");//

            // TODO - improve presentation
            ArrayList<Float> patientFloatData = Utils.convertDBRowTFloats(patientCacheData);

            DataPoint[] dataPoints = new DataPoint[patientFloatData.size()];
            for (int i = 0; i < patientFloatData.size(); i++) {
                dataPoints[i] = new DataPoint(i, patientFloatData.get(i));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
            graph.addSeries(series);

        } else {
            dataStatusText.setText("No Data present");
            graph.setVisibility(View.INVISIBLE); // no data, make graph go away
        }

        nameText = (TextView) findViewById(R.id.name_Text);
        nameText.setText(patientUserID);

        ipEditText = (EditText) findViewById(R.id.ip_editText);
        ipEditText.setText(patientIP);

        /** Returns to GetCliBeat **/
        requestBtn = (Button) findViewById(R.id.patientDataRequestBtn);
        requestBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - define/improve request interval

                System.out.println("REQUEST button clicked");

                // performs a network-capabilities AND IP check before attempting to send
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isValidIP = MainActivity.validIP(patientIP);

                if (mWifi.isConnected() && isValidIP) {
                    System.out.println("SENDING PACKET");
                    System.out.println("PATIENT IP: " + patientIP);

                    // TODO - pass real TIMESTRING, PROCESS_ID, and IP_ADDR

                    // place entry into PIT for self; this is because if a request is
                    // received for same data, we won't send two identical PITs
                    if (MainActivity.datasource.getSpecificPITData(patientUserID, "Tuesday", patientIP) == null) {

                        DBData selfPITEntry = new DBData();
                        selfPITEntry.setUserID(patientUserID);
                        selfPITEntry.setSensorID("abc"); // TODO - rework
                        selfPITEntry.setTimeString("Tuesday"); // TODO - rework
                        selfPITEntry.setProcessID("one"); // TODO - rework
                        selfPITEntry.setIpAddr(patientIP);

                        MainActivity.datasource.addPITData(selfPITEntry);

                        InterestPacket interestPacket = new InterestPacket(getApplicationContext(),
                                ".", ".", MainActivity.deviceIP);

                        new UDPSocket(MainActivity.devicePort, patientIP)
                                .execute(interestPacket.toString()); // send interest packet
                    } else {
                        // user has already requested data, notify

                        Toast toast = Toast.makeText(getApplicationContext(),
                                "You've already requested data. Wait a moment.", Toast.LENGTH_LONG);
                        toast.show();
                    }

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // reload activity after 2 seconds, so to check if client data arrived
                            // TODO - rework so that multiple instances of this activity aren't on stack
                            finish();
                            startActivity(getIntent());
                        }
                    }, 2000);
                } else if (!isValidIP) {
                    // invalid ip

                    Toast toast = Toast.makeText(getApplicationContext(),
                            "The IP address is invalid. Enter valid then save.", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    // not connected to wifi

                    Toast toast = Toast.makeText(getApplicationContext(),
                            "You must first connect to WiFi.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        /** saves updated user inroamation **/
        submitBtn = (Button) findViewById(R.id.patientDataSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - rework once cache is functional

                // check before save AND notify user if invalid ip
                if (!MainActivity.validIP(ipEditText.getText().toString())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setTitle("Invalid IP entered. Submit anyways?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updatePatientData();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });

                    builder.show();
                } else {
                    updatePatientData();
                }
            }
        });

        /** Returns to GetCliBeat **/
        backBtn = (Button) findViewById(R.id.patientDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });

        /** Deletes patient from Doctor's patient list **/
        deleteBtn = (Button) findViewById(R.id.deletePatientBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // TODO - rework with cache

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Feature isn't currently functional.", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * Called when user wishes to save patient data. Updates FIB entry.
     */
    void updatePatientData() {

        // updates patient data
        DBData updatedFIBEntry = new DBData();
        updatedFIBEntry.setTimeString(DBData.CURRENT_TIME);
        updatedFIBEntry.setIpAddr(ipEditText.getText().toString());
        updatedFIBEntry.setUserID(patientUserID);

        /* TODO - allow name modification
        if (!patientUserID.equals(nameEditText.getText().toString())) {
            // patient updated id, delete FIB entry
            // rework other tables as well
        }*/

        MainActivity.datasource.updateFIBData(updatedFIBEntry);

        Toast toast = Toast.makeText(this, "Save successful.", Toast.LENGTH_LONG);
        toast.show();
    }
}

