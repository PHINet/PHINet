package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
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
import java.util.Arrays;
import java.util.Calendar;

/**
 * Activity deals specifically with interacting with user's own data.
 *
 * Uses include
 * 1. viewing patient data
 */
public class ViewDataActivity extends Activity {

    Button backBtn, intervalSelectionBtn, analyticsBtn;
    TextView dataStatusText, loggedInText, entityNameText, analyticsResultText;
    Spinner sensorSelectionSpinner;
    GraphView graph;
    String entityName, currentSensorSelected, mostRecentlySelectedTask;
    ProgressBar analyticsWait;

    // --- used by the interval selector ---
    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;
    // --- used by the interval selector ---

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdata);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        // get name of entity to determine whose data to display
        entityName = getIntent().getExtras().getString(ConstVar.ENTITY_NAME);

        graph = (GraphView) findViewById(R.id.graph); // reset graph when updating

        analyticsResultText = (TextView) findViewById(R.id.analyticsResultTextView);

        analyticsWait = (ProgressBar) findViewById(R.id.analyticsProgressBar);
        analyticsWait.setVisibility(View.GONE); // node analytics requested yet; hide it

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID);  // display username on screen

        entityNameText = (TextView) findViewById(R.id.entityNameView);
        entityNameText.setText(entityName + "'s data");

        // TextView used to notify user whether data for patient exists
        dataStatusText = (TextView) findViewById(R.id.currentDataStatusTextView);

        ArrayList<DBData> myData = DBSingleton.getInstance(getApplicationContext()).getDB().getGeneralCSData(entityName);
        ArrayList<String> sensors = new ArrayList<>();

        if (myData != null) {
            // get all sensors in order to allow user to choose
            for (int i = 0; i < myData.size(); i++) {
                if (!sensors.contains(myData.get(i).getSensorID())) {
                    sensors.add(myData.get(i).getSensorID()); // new sensor detected; store now
                }
            }
        } else {
            sensors.add("No data available");
        }

        sensorSelectionSpinner = (Spinner) findViewById(R.id.sensorSelectorSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sensors);
        sensorSelectionSpinner.setAdapter(adapter);

        currentSensorSelected = sensorSelectionSpinner.getSelectedItem().toString();

        sensorSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // reload if different sensor chosen
                if (!currentSensorSelected.equals(adapter.getItem(position).toString())) {

                    // update the currentSensorSelected then reload the graph
                    currentSensorSelected = adapter.getItem(position).toString();
                    updateGraph();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        updateGraph(); // provides the initial rendering of the graph

        intervalSelectionBtn = (Button) findViewById(R.id.intervalSelectionBtn);
        intervalSelectionBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                AlertDialog.Builder initialInterval = generateIntervalSelector(ConstVar.INTERVAL_TITLE_START);
                initialInterval.show();
            }
        });

        analyticsBtn = (Button) findViewById(R.id.analyticsBtn);
        analyticsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder analyticDialog = generateAnalyticsSelector();
                analyticDialog.show();
            }
        });

        backBtn = (Button) findViewById(R.id.userDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Allows users to select analytics function to be perform on current data set
     *
     * @return returns the dialog so that it can be initiated elsewhere
     */
    AlertDialog.Builder generateAnalyticsSelector() {
        final Spinner analyticSelector = new Spinner(ViewDataActivity.this);

        final ArrayList<String> analyticTasks = new ArrayList<>();
        analyticTasks.add("Mean");
        analyticTasks.add("Mode");
        analyticTasks.add("Median");
        // TODO - create more analytic tasks

        final ArrayAdapter<String> adapter = new ArrayAdapter(ViewDataActivity.this,
                android.R.layout.simple_spinner_item, analyticTasks);
        analyticSelector.setAdapter(adapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewDataActivity.this);
        builder.setTitle("Choose analytic task");
        builder.setView(analyticSelector);

        analyticSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // update chosen task; if "Select" is chosen in dialog, task will be invoked
                mostRecentlySelectedTask = analyticTasks.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String myUserID = Utils.getFromPrefs(getApplicationContext(), ConstVar.PREFS_LOGIN_USER_ID_KEY, "");
                final String currentTime = Utils.createAnalyticTimeInterval(dataStatusText.getText().toString());
                final String processID = selectAnalyticProcessID(mostRecentlySelectedTask);

                // query server for analytic task
                Name packetName = JNDNUtils.createName(myUserID, currentSensorSelected,
                        currentTime, processID);
                Interest interest = JNDNUtils.createInterestPacket(packetName);

                Blob blob = interest.wireEncode();

                // add entry into PIT
                DBData data = new DBData(ConstVar.PIT_DB, currentSensorSelected, processID,
                        currentTime, myUserID, ConstVar.SERVER_IP);

                DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(data);

                // TODO - include real server IP
                new UDPSocket(ConstVar.PHINET_PORT, "10.0.0.3", ConstVar.INTEREST_TYPE)
                        .execute(blob.getImmutableArray()); // reply to interest with DATA from cache

                // store received packet in database for further review
                Utils.storeInterestPacket(getApplicationContext(), interest);

                analyticsWait.setVisibility(View.VISIBLE); // request sent; display progress bar
                analyticsBtn.setVisibility(View.GONE); // prevent user from resending request

                // wait 15 seconds (arbitrary) after sending request before checking for result
                new Handler().postDelayed(new Runnable() {
                    public void run() {

                        analyticsBtn.setVisibility(View.VISIBLE); // place button back on view
                        analyticsWait.setVisibility(View.GONE); // hide; result checked now

                        ArrayList<DBData> candidateData = DBSingleton
                                .getInstance(getApplicationContext()).getDB().getGeneralCSData(myUserID);

                        DBData analyticsResult = null;

                        for (int i = 0; i < candidateData.size(); i++) {

                            if (candidateData.get(i).getProcessID().equals(processID)
                                    && candidateData.get(i).getTimeString().equals(currentTime)) {

                                analyticsResult = candidateData.get(i);
                                break; // result found; break from
                            }
                        }

                        if (analyticsResult != null) {
                            analyticsResultText.setText(mostRecentlySelectedTask + " is " + analyticsResult.getDataFloat());
                        } else {
                            // nothing found; display error
                            analyticsResultText.setText("Error processing request.");
                        }
                    }
                }, 15000);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetIntervalParams(); // clear so that future updates may occur
                dialog.cancel();
            }
        });

        return builder;
    }

    /**
     * Allows users to select date regarding interval of requested data
     *
     * @param title used to set title of dialog
     * @return returns the dialog so that it can be initiated elsewhere
     */
    AlertDialog.Builder generateIntervalSelector(String title) {
        final DatePicker intervalSelector = new DatePicker(ViewDataActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewDataActivity.this);
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
                    final DatePicker intervalSelector = new DatePicker(ViewDataActivity.this);
                    AlertDialog.Builder secondInterval = generateIntervalSelector(ConstVar.INTERVAL_TITLE_END);
                    secondInterval.setView(intervalSelector);

                    // TODO - rework this sloppy nesting

                    secondInterval.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // start input already set, now store end input
                            endYear = intervalSelector.getDayOfMonth();
                            endMonth = intervalSelector.getMonth() + 1; // offset required
                            endDay = intervalSelector.getYear();

                            // now that interval has been entered, update the graph
                            updateGraph();

                        }
                    });
                    secondInterval.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetIntervalParams(); // clear so that future updates may occur
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
                resetIntervalParams(); // clear so that future updates may occur
                dialog.cancel();
            }
        });

        return builder;
    }

    /**
     * Method takes an analyticTask and maps it to the appropriate process id.
     *
     * TODO - expand available analytic tasks
     *
     * @param analyticTask used to determine process id
     * @return process id mapped to analyticTask
     */
    public String selectAnalyticProcessID(String analyticTask) {
        if (analyticTask.equals("Mode")) {
            return ConstVar.MODE_ANALYTIC;
        } else if (analyticTask.equals("Median")) {
            return ConstVar.MEDIAN_ANALYTIC;
        } else {
            return ConstVar.MEAN_ANALYTIC; // mean is only task that remains
        }
    }

    /**
     * Should be invoked when graphing params have been
     * updated (i.e., new sensor or interval selection).
     *
     * Method redraws the graph based upon the updated criterion.
     */
    public void updateGraph() {

        if (graph != null) {
            graph.removeAllSeries(); // graph exists, wipe all its data before adding new data
        }

        // holds data before conversion
        ArrayList<DBData> myData = DBSingleton.getInstance(getApplicationContext()).getDB().getGeneralCSData(entityName);

        // holds data after conversion
        ArrayList<Float> myFloatData;

        // TODO - more robust verification

        // attempts to determine whether an interval has been selected
        if (startYear == 0 || endYear == 0) {
            // startYear and endYear are invalid (interval hasn't been selected), provide default

            Calendar now = Calendar.getInstance();

            startYear = now.get(Calendar.YEAR) - 1;
            endYear = now.get(Calendar.YEAR) + 1;

            startMonth = now.get(Calendar.MONTH);
            endMonth = startMonth;

            startDay = now.get(Calendar.DAY_OF_MONTH);
            endDay = startDay;
        }

        // db query unsuccessful: no user data found in cache
        if (myData == null) {

            myFloatData = new ArrayList<>(); // create empty ArrayList; no data exists for it

        }
        // db query was successful; populate ArrayList that will be used to generate the graph
        else {

            // date syntax: yyyy-MM-ddTHH.mm.ss.SSS
            String startDate = Integer.toString(startYear) + "-" + Integer.toString(startMonth);
                startDate += "-" + Integer.toString(startDay) + "T00.00.00.000"; // append zeros at end

            String endDate = Integer.toString(endYear) + "-" + Integer.toString(endMonth);
              endDate += "-" + Integer.toString(endDay) + "T00.00.00.000"; // append zeros at end

            // convert valid data to a format that can be displayed
            myFloatData = Utils.convertDBRowTFloats(myData, currentSensorSelected, startDate, endDate);
        }

        // generate interval text to display to user
        String intervalText = Integer.toString(startMonth) + "/" + Integer.toString(startDay) + "/"
                + Integer.toString(startYear) + " - " + Integer.toString(endMonth) + "/"
                + Integer.toString(endDay) + "/" + Integer.toString(endYear);

        // database query returned valid data, display it now
        if (myFloatData.size() > 0) {

            graph.setTitle("Sensor Values / Chronological Data Points");
            dataStatusText.setText(intervalText); // notify user of chosen interval

            // TODO - improve presentation
            DataPoint[] dataPoints = new DataPoint[myFloatData.size()];
            for (int i = 0; i < myFloatData.size(); i++) {
                dataPoints[i] = new DataPoint(i, myFloatData.get(i));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            graph.addSeries(series); // update graph with new data
        }
        // database query returned nothing , notify the user
        else {
            graph.setTitle(""); // no data; remove title
            dataStatusText.setText("Nothing during " + intervalText);
        }

        resetIntervalParams(); // after using the parameters, clear so that future updates may occur
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