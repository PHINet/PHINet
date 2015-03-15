package com.ndnhealthnet.androidudpclient;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Packet.InterestPacket;
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

    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;

    // title of dialog that allows user to select interval
    private final String INTERVAL_TITLE_1 = "Choose the start interval.";
    private final String INTERVAL_TITLE_2 = "Choose the end interval.";

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

                AlertDialog.Builder initialInterval = generateIntervalSelector(INTERVAL_TITLE_1);
                initialInterval.show();
            }
        });

        /** saves updated user information **/
        submitBtn = (Button) findViewById(R.id.patientDataSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // check before save AND notify user if invalid ip
                if (!MainActivity.validIP(ipEditText.getText().toString())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(PatientDataActivity.this);

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

                // TODO - rework with actual Patient/Doctor relationship

                // delete from FIB
                MainActivity.datasource.deleteFIBEntry(patientUserID);

                finish();
            }
        });
    }

    /** handles logic associated with requesting patient data **/
    private void requestHelper() {

        // performs a network-capabilities AND IP check before attempting to send
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isValidIP = MainActivity.validIP(patientIP);

        if (mWifi.isConnected()) {

            ArrayList<DBData> pitEntries = MainActivity.datasource.getGeneralPITData(patientUserID);

            // place entry into PIT for self; this is because if a request is
            // received for same data, we won't send two identical PITs
            if (pitEntries == null) {

                DBData selfPITEntry = new DBData();
                selfPITEntry.setUserID(patientUserID);

                // sensor id is currently irrelevant
                // TODO - rework sensorID with server
                selfPITEntry.setSensorID(StringConst.NULL_FIELD);

                selfPITEntry.setTimeString(generateTimeString());
                selfPITEntry.setProcessID(StringConst.INTEREST_CACHE_DATA);

                // deviceIP, because this device is the requester
                selfPITEntry.setIpAddr(MainActivity.deviceIP);

                MainActivity.datasource.addPITData(selfPITEntry);

            } else {
                // user has already requested data, update PIT entries

                // TODO - rework the way PIT entries are updated
                //  TODO - (the request interval should not be changed but rather sent time)

                /*for (int i = 0; i < pitEntries.size(); i++) {

                    pitEntries.get(i).setTimeString(StringConst.CURRENT_TIME);
                    MainActivity.datasource.updatePITData(pitEntries.get(i));
                }*/
            }

            ArrayList<DBData> allFIBEntries = MainActivity.datasource.getAllFIBData();

            int fibRequestsSent = 0;

            for (int i = 0; i < allFIBEntries.size(); i++) {
                // send request to everyone in FIB; only send to users with actual ip
                if (MainActivity.validIP(allFIBEntries.get(i).getIpAddr())) {

                    InterestPacket interestPacket = new InterestPacket(
                            patientUserID, StringConst.NULL_FIELD, generateTimeString(),
                            StringConst.INTEREST_CACHE_DATA, MainActivity.deviceIP);

                    new UDPSocket(MainActivity.devicePort, allFIBEntries.get(i).getIpAddr(), StringConst.INTEREST_TYPE)
                            .execute(interestPacket.toString()); // send interest packet

                    fibRequestsSent ++;
                }
            }

            if (fibRequestsSent == 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No neighbors with valid IP found. Enter valid then try again.", Toast.LENGTH_LONG);
                toast.show();
            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // reload activity after 3 seconds, so to check if client data arrived
                        finish();
                        startActivity(getIntent());
                    }
                }, 3000);
            }

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

    /** allows users to select date regarding interval of requested data **/
    AlertDialog.Builder generateIntervalSelector(String title) {
        final DatePicker intervalSelector = new DatePicker(PatientDataActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(PatientDataActivity.this);
        builder.setTitle(title);
        builder.setView(intervalSelector);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // get user input
                int day = intervalSelector.getDayOfMonth();
                int month = intervalSelector.getMonth() + 1; // offset required
                int year = intervalSelector.getYear();

                if (startYear == 0) {
                    // this is the first input, store now and request again

                    startYear = year;
                    startMonth = month;
                    startDay = day;

                    // call again to get end interval
                    AlertDialog.Builder secondInterval = generateIntervalSelector(INTERVAL_TITLE_2);
                    secondInterval.show();
                } else {

                    // start input already set, now store end input
                    endYear = year;
                    endMonth = month;
                    endDay = day;

                    // now that interval has been entered, request data from patient via NDN/UDP
                    requestHelper();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder;
    }

    /** method generates UTC-compliant time string from user input **/
    private String generateTimeString()
    {
        // minimal input validation; if years are valid, assume all date data is valid
        if (startYear != 0 && endYear != 0) {
            String timeString = "";

            // TIME_STRING FORMAT: "yyyy-MM-dd||yyyy-MM-dd"; the former is start, latter is end

            timeString += Integer.toString(startYear) + "-" + Integer.toString(startMonth) + "-";
            timeString += Integer.toString(startDay) + "||" + Integer.toString(endYear) + "-";
            timeString += Integer.toString(endMonth) + "-" + Integer.toString(endDay);

            return timeString;
        } else {
            throw new NullPointerException("Cannot construct date! Data is bad.");
        }
    }

    /**
     * Called when user wishes to save patient data. Updates FIB entry.
     */
    void updatePatientData() {

        // updates patient data
        DBData updatedFIBEntry = new DBData();
        updatedFIBEntry.setTimeString(StringConst.CURRENT_TIME);
        updatedFIBEntry.setIpAddr(ipEditText.getText().toString());
        updatedFIBEntry.setUserID(patientUserID);

        MainActivity.datasource.updateFIBData(updatedFIBEntry);

        Toast toast = Toast.makeText(this, "Save successful.", Toast.LENGTH_LONG);
        toast.show();
    }
}

