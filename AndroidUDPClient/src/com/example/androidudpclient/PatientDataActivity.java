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
    EditText nameEditText, ipEditText;
    TextView dataStatusText;
    GraphView graph;

    String patientIP;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientdata);

        // use IP from intent to find patient among all patients (TODO - later use CACHE)
        patientIP = getIntent().getExtras().getString(GetCliBeatActivity.PATIENT_ID_STRING);

        Patient pa = null;  // identify chosen patient
        final ArrayList<Patient> patients = MainActivity.patients;
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getIP().equals(patientIP)) {
                pa = patients.get(i);
                break;
            }
        }

        // TODO - can this work around be avoided?
        final Patient patient = pa; // set patient final so that can be used in anonymous-classes

        if (pa == null) {
            // TODO - handle this problem; exit activity and notify user
        }

        // textview used to notify user whether data for patient exists
        dataStatusText = (TextView) findViewById(R.id.currentDataStatus_textView);

        // display graph is data is present
        graph = (GraphView) findViewById(R.id.graph);
        if (patient.getData().size() > 0) {
            dataStatusText.setText("Some data present");//

            // TODO - improve presentation
            DataPoint[] dataPoints = new DataPoint[patient.getData().size()];
            for (int i = 0; i < patient.getData().size(); i++) {
                dataPoints[i] = new DataPoint(i, patient.getData().get(i));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
            graph.addSeries(series);

        } else {
            dataStatusText.setText("No Data present");
            graph.setVisibility(View.INVISIBLE); // no data, make graph go away
        }

        nameEditText = (EditText) findViewById(R.id.name_editText);
        nameEditText.setText(patient.getName());

        ipEditText = (EditText) findViewById(R.id.ip_editText);
        ipEditText.setText(patient.getIP());

        final Context c = this; // TODO - can this work around be avoided?

        /** Returns to GetCliBeat **/
        requestBtn = (Button) findViewById(R.id.patientDataRequestBtn);
        requestBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - define/improve request interval

                // performs a network-capabilities AND IP check before attempting to send
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isValidIP = MainActivity.validIP(patient.getIP());

                if (mWifi.isConnected() && isValidIP) {

                    // TODO - rework packet
                    InterestPacket interestPacket = new InterestPacket();

                    new UDPSocket(MainActivity.devicePort, patient.getIP())
                            .execute(interestPacket.toString()); // send interest packet

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

                    Toast toast = Toast.makeText(c,
                            "The IP address is invalid. Enter valid then save.", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    // not connected to wifi

                    Toast toast = Toast.makeText(c,
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
                if (!MainActivity.validIP(patientIP)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    builder.setTitle("Invalid IP entered. Submit anyways?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updatePatientData(patientIP);
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
                    updatePatientData(patientIP);
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

                ArrayList<Patient> patients = MainActivity.patients;

                for (int i = 0; i < patients.size(); i++) {
                    if (patients.get(i).getIP().equals(patientIP)) {
                        patients.remove(patients.get(i));
                        break;
                    }
                }
                finish();
            }
        });
    }

    /**
     * Called when user wishes to save patient data.
     */
    void updatePatientData(String patientIP) {

        // updates patient data
        for (int i = 0; i < MainActivity.patients.size(); i++) {
            if (MainActivity.patients.get(i).getIP().equals(patientIP)) {
                MainActivity.patients.get(i).setIP(ipEditText.getText().toString());
                MainActivity.patients.get(i).setName(nameEditText.getText().toString());
                break;
            }
        }

        Toast toast = Toast.makeText(this, "Save successful.", Toast.LENGTH_LONG);
        toast.show();
    }
}

