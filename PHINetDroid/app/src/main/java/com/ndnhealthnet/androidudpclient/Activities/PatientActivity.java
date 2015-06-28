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
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.PITEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.JNDNUtils;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.util.ArrayList;

/**
 * Activity deals specifically with interacting with patient.
 */
public class PatientActivity extends Activity {

    Button backBtn, requestBtn, viewDataBtn;
    TextView nameText, loggedInText;
    String  patientUserID;

    // --- used by the interval selector ---
    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;
    // --- used by the interval selector ---

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place userID on screen

        // use ID from intent to find patient among all patients
        patientUserID = getIntent().getExtras().getString(PatientListActivity.PATIENT_USER_ID);

        nameText = (TextView) findViewById(R.id.nameText);
        nameText.setText(patientUserID);  // place userID on screen

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
                AlertDialog.Builder initialInterval = generateIntervalSelector(ConstVar.INTERVAL_TITLE_START);
                initialInterval.show();
            }
        });

        backBtn = (Button) findViewById(R.id.patientDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

               finish();
            }
        });
    }

    /**
     * Handles logic associated with requesting patient data
     */
    private void requestHelper() {

        // used to perform a network-capabilities check
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();

        // network connection required
        if (netInfo != null) {

            ArrayList<PITEntry> pitEntries = DBSingleton.getInstance(getApplicationContext())
                    .getDB().getGeneralPITData(patientUserID);

            // TODO - rework this logic

            // place entry into PIT for self; this is because if a request is
            // received for same data we request, we won't send another, identical Interest
            if (pitEntries == null) {

                PITEntry selfPITEntry = new PITEntry(ConstVar.NULL_FIELD, ConstVar.INTEREST_CACHE_DATA,
                        patientUserID, generateTimeString(), MainActivity.deviceIP);

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

            ArrayList<FIBEntry> allFIBEntries = DBSingleton
                    .getInstance(getApplicationContext()).getDB().getAllFIBData();

            if (allFIBEntries != null) {
                int fibRequestsSent = 0;

                for (int i = 0; i < allFIBEntries.size(); i++) {
                    // send request to everyone in FIB; only send to users with actual ip
                    if (Utils.isValidIP(allFIBEntries.get(i).getIpAddr())) {

                        Name packetName = JNDNUtils.createName(patientUserID, ConstVar.NULL_FIELD, generateTimeString(),
                                ConstVar.INTEREST_CACHE_DATA);
                        Interest interest = JNDNUtils.createInterestPacket(packetName);

                        new UDPSocket(ConstVar.PHINET_PORT, allFIBEntries.get(i).getIpAddr(), ConstVar.INTEREST_TYPE)
                                .execute(interest.wireEncode().getImmutableArray()); // send Interest now

                        // store received packet in database for further review
                        Utils.storeInterestPacket(getApplicationContext(), interest);

                        fibRequestsSent++;
                    }
                }

                if (fibRequestsSent == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No neighbors with valid IP found. Enter valid then try again.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        } else {
            // not connected to network
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Error: Network connection required.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * Allows users to select date regarding interval of requested data
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
                    // startYear == 0 means this is the first input (nothing has been set)
                            // store now and request again

                    startYear = year;
                    startMonth = month;
                    startDay = day;

                    // call again to get end interval
                    final DatePicker intervalSelector = new DatePicker(PatientActivity.this);
                    AlertDialog.Builder secondInterval = generateIntervalSelector(ConstVar.INTERVAL_TITLE_END);
                    secondInterval.setView(intervalSelector);

                    secondInterval.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // store user selection
                            endYear = intervalSelector.getYear();
                            endMonth = intervalSelector.getMonth() + 1; // offset required
                            endDay = intervalSelector.getDayOfMonth();

                            if (Utils.isValidInterval(startYear, startMonth, startDay, endYear, endMonth, endDay)) {
                                // now that interval has been entered, request data from patient via NDN/UDP
                                requestHelper();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Invalid interval: start must be before end.", Toast.LENGTH_LONG);
                                toast.show();

                                resetIntervalParams(); // clear so that future updates may occur
                            }
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

            // by default, set HH.mm.ss.SSS from request interval to 00.00.00.000
            timeString += Integer.toString(startYear) + "-" + startMonthString + "-";
            timeString += Integer.toString(startDay) + "T00.00.00.000||" + Integer.toString(endYear) + "-";
            timeString += endMonthString + "-" + Integer.toString(endDay) + "T00.00.00.000";

            return timeString;
        } else {
            throw new NullPointerException("Cannot construct date! Data is bad.");
        }
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