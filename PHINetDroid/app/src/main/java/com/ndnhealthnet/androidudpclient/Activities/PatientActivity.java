package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import java.util.ArrayList;

/**
 * Activity deals specifically with interacting with patient.
 *
 * Uses include
 * 1. requesting and viewing patient data
 * 2. editing/saving patient information
 */
public class PatientActivity extends Activity {

    Button backBtn, requestBtn, submitBtn, deleteBtn, viewDataBtn;
    EditText ipEditText;
    TextView nameText, loggedInText;
    String patientIP, patientUserID;

    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;

    // title of dialog that allows user to select interval
    private final String INTERVAL_TITLE_1 = "Choose the start interval.";
    private final String INTERVAL_TITLE_2 = "Choose the end interval.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID);

        // use IP and ID from intent to find patient among all patients
        patientIP = getIntent().getExtras().getString(PatientListActivity.PATIENT_IP);
        patientUserID = getIntent().getExtras().getString(PatientListActivity.PATIENT_USER_ID);

        nameText = (TextView) findViewById(R.id.nameText);
        nameText.setText(patientUserID);

        ipEditText = (EditText) findViewById(R.id.ip_editText);
        ipEditText.setText(patientIP);

        viewDataBtn = (Button) findViewById(R.id.viewDataBtn);
        viewDataBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(PatientActivity.this, ViewDataActivity.class);

                // to view client's data, pass their user id
                intent.putExtra(ConstVar.ENTITY_NAME, patientUserID);
                startActivity(intent);
            }
        });

        requestBtn = (Button) findViewById(R.id.patientDataRequestBtn);
        requestBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                resetIntervalParams(); // clear so that new/first request may be made
                AlertDialog.Builder initialInterval = generateIntervalSelector(INTERVAL_TITLE_1);
                initialInterval.show();
            }
        });

        /** saves updated user information **/
        submitBtn = (Button) findViewById(R.id.patientDataSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                // check before save AND notify user if invalid ip
                if (!Utils.validIP(ipEditText.getText().toString())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(PatientActivity.this);

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
                DBSingleton.getInstance(getApplicationContext()).getDB().deleteFIBEntry(patientUserID);

                finish();
            }
        });
    }

    /**
     * handles logic associated with requesting patient data
     */
    private void requestHelper() {

        // performs a network-capabilities AND IP check before attempting to send
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isValidIP = Utils.validIP(patientIP);

        if (mWifi.isConnected()) {

            ArrayList<DBData> pitEntries = DBSingleton.getInstance(getApplicationContext()).getDB().getGeneralPITData(patientUserID);

            // place entry into PIT for self; this is because if a request is
            // received for same data, we won't send two identical PITs
            if (pitEntries == null) {

                DBData selfPITEntry = new DBData();
                selfPITEntry.setUserID(patientUserID);

                // sensor id is currently irrelevant
                // TODO - rework sensorID with server
                selfPITEntry.setSensorID(ConstVar.NULL_FIELD);

                selfPITEntry.setTimeString(generateTimeString());
                selfPITEntry.setProcessID(ConstVar.INTEREST_CACHE_DATA);

                // deviceIP, because this device is the requester
                selfPITEntry.setIpAddr(MainActivity.deviceIP);

                DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(selfPITEntry);

            } else {
                // user has already requested data, update PIT entries

                // TODO - rework the way PIT entries are updated
                //  TODO - (the request interval should not be changed but rather sent time)

                /*for (int i = 0; i < pitEntries.size(); i++) {

                    pitEntries.get(i).setTimeString(ConstVar.CURRENT_TIME);
                    DBSingleton.getInstance(getApplicationContext()).getDB().updatePITData(pitEntries.get(i));
                }*/
            }

            ArrayList<DBData> allFIBEntries = DBSingleton.getInstance(getApplicationContext()).getDB().getAllFIBData();

            int fibRequestsSent = 0;

            System.out.println("prior to interest loop");
            for (int i = 0; i < allFIBEntries.size(); i++) {
                // send request to everyone in FIB; only send to users with actual ip
                if (Utils.validIP(allFIBEntries.get(i).getIpAddr())) {

                    Name packetName = JNDNUtils.createName(patientUserID, ConstVar.NULL_FIELD, generateTimeString(),
                            ConstVar.INTEREST_CACHE_DATA);
                    Interest interest = JNDNUtils.createInterestPacket(packetName);

                    Blob blob = interest.wireEncode();
                    new UDPSocket(ConstVar.PHINET_PORT, allFIBEntries.get(i).getIpAddr(), ConstVar.INTEREST_TYPE)
                            .execute(blob.getImmutableArray()); // reply to interest with DATA from cache

                    // store received packet in database for further review
                    DBSingleton.getInstance(getApplicationContext()).getDB()
                            .addPacketData(new DBData(interest.getName().toUri(), Utils.convertInterestToString(interest)));

                    fibRequestsSent++;
                }
            }

            if (fibRequestsSent == 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No neighbors with valid IP found. Enter valid then try again.", Toast.LENGTH_LONG);
                toast.show();
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

    /**
     * TODO - create a class for this dialog
     *
     * allows users to select date regarding interval of requested data
     *
     * @param title used to set title of dialog
     * @return returns the dialog so that it can be initiated elsewhere
     */
    AlertDialog.Builder generateIntervalSelector(String title) {
        final DatePicker intervalSelector = new DatePicker(PatientActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(PatientActivity.this);
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
                    final DatePicker intervalSelector = new DatePicker(PatientActivity.this);
                    AlertDialog.Builder secondInterval = generateIntervalSelector(INTERVAL_TITLE_2);
                    secondInterval.setView(intervalSelector);

                    // TODO - rework this sloppy nesting

                    secondInterval.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // get user input
                            int day = intervalSelector.getDayOfMonth();
                            int month = intervalSelector.getMonth() + 1; // offset required
                            int year = intervalSelector.getYear();

                            // start input already set, now store end input
                            endYear = year;
                            endMonth = month;
                            endDay = day;

                            // now that interval has been entered, request data from patient via NDN/UDP
                            requestHelper();
                        }
                    }); secondInterval.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    secondInterval.show();
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

    /**
     * @return a UTC-compliant time string from user input
     */
    private String generateTimeString()
    {
        // minimal input validation; if years are valid, assume all date data is valid
        if (startYear != 0 && endYear != 0) {
            String timeString = "";

            // append a "0" if month is single digit - so to conform to formatting
            String startMonthString = "", endMonthString = "";
            if (startMonth < 10) {
                startMonthString += "0" + Integer.toString(startMonth);
            }

            if (endMonth < 10) {
                endMonthString += "0" + Integer.toString(endMonth);
            }

            // TIME_STRING FORMAT: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS";
                    // where the former is start, latter is end

            // by default, set HH.mm.ss.SSS from request interval to 00.00.00.00000
            timeString += Integer.toString(startYear) + "-" + startMonthString + "-";
            timeString += Integer.toString(startDay) + "T00.00.00.000||" + Integer.toString(endYear) + "-";
            timeString += endMonthString + "-" + Integer.toString(endDay) + "T00.00.00.000";

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
        updatedFIBEntry.setTimeString(ConstVar.CURRENT_TIME);
        updatedFIBEntry.setIpAddr(ipEditText.getText().toString());
        updatedFIBEntry.setUserID(patientUserID);

        DBSingleton.getInstance(getApplicationContext()).getDB().updateFIBData(updatedFIBEntry);

        Toast toast = Toast.makeText(this, "Save successful.", Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Resets the interval params so that future requests may start fresh.
     */
    public void resetIntervalParams() {
        startDay = 0;
        endDay = 0;
        startMonth = 0;
        endMonth = 0;
        startYear = 0;
        endYear = 0;
    }
}