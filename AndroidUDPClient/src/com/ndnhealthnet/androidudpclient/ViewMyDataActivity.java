package com.ndnhealthnet.androidudpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * Activity deals specifically with interacting with user's own data.
 *
 * Uses include
 * 1. viewing patient data
 */
public class ViewMyDataActivity extends Activity {

    Button backBtn;
    TextView dataStatusText;
    GraphView graph;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewmydata);

        // TextView used to notify user whether data for patient exists
        dataStatusText = (TextView) findViewById(R.id.currentDataStatus_textView);

        // display graph is data is present
        graph = (GraphView) findViewById(R.id.graph);

        // get user id for querying db
        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                Utils.PREFS_LOGIN_USER_ID_KEY, "");

        ArrayList<DBData> myData = MainActivity.datasource.getGeneralCSData(currentUserID);
        ArrayList<Float> myFloatData;

        if (myData == null) {
            myFloatData = new ArrayList<Float>(); // no user data found in cache
        } else {
            myFloatData = Utils.convertDBRowTFloats(myData);  // use data from cache
        }

        if (myData != null && myData.size() > 0) {
            dataStatusText.setText("Some data present");

            // TODO - improve presentation
            DataPoint[] dataPoints = new DataPoint[myFloatData.size()];
            for (int i = 0; i < myFloatData.size(); i++) {
                dataPoints[i] = new DataPoint(i, myFloatData.get(i));
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
            graph.addSeries(series);

        } else {
            dataStatusText.setText("No Data present");
            graph.setVisibility(View.INVISIBLE); // no data, make graph go away
        }

        /** Returns to GetCliBeat **/
        backBtn = (Button) findViewById(R.id.userDataBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}


