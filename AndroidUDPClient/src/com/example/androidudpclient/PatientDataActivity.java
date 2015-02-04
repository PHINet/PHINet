package com.example.androidudpclient;

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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class PatientDataActivity extends Activity {

    Button backBtn, requestBtn, submitBtn;
    EditText nameEditText, ipEditText;
    TextView dataStatusText;
    GraphView graph;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientdata);

        final String paramIP = getIntent().getExtras().getString(GetCliBeatActivity.PATIENT_ID_STRING);

        Patient pa = null;  // identify chosen patient
        final ArrayList<Patient> patients = MainActivity.patients;
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getIP().equals(paramIP)) {
                pa = patients.get(i);
                break;
            }
        }

        // TODO - can this work around be avoided?
        final Patient patient = pa; // set patient final so that can be used in anonymous-classes

        if (pa == null) {
            // TODO - handle this problem; exit activity and notify user
        }

        dataStatusText = (TextView) findViewById(R.id.currentDataStatus_textView);

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

            graph.setVisibility(View.INVISIBLE);
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

                // TODO - define message format; NDN compatible
                // TODO - define/improve request interval

                // performs a network-capabilities check before attempting to send
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {
                    new UDPSocket(MainActivity.devicePort, patient.getIP())
                            .execute("INTEREST::" + Integer.toString(MainActivity.devicePort));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {

                            // reload activity after 2 seconds, so to check if client data arrived

                            // TODO - rework so that multiple instances of this activity aren't on stack
                            finish();
                            startActivity(getIntent());
                        }
                    }, 2000);
                } else {
                    Toast toast = Toast.makeText(c,
                            "You must first connect to WiFi.", Toast.LENGTH_LONG);
                    toast.show();

                }



            }
        });

        /** Returns to GetCliBeat **/
        submitBtn = (Button) findViewById(R.id.patientDataSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // TODO - rework once cache is functional

                // updates patient data
                for (int i = 0; i < MainActivity.patients.size(); i++) {
                    if (MainActivity.patients.get(i).getIP().equals(paramIP)) {
                        MainActivity.patients.get(i).setIP(ipEditText.getText().toString());
                        MainActivity.patients.get(i).setName(nameEditText.getText().toString());
                        break;
                    }
                }

                finish();
            }
        });

        /** Returns to GetCliBeat **/
        backBtn = (Button) findViewById(R.id.patientDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
    }
}

