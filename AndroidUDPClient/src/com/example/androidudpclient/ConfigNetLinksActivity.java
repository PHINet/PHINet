package com.example.androidudpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.androidudpclient.Packet.InterestPacket;

import java.util.ArrayList;

public class ConfigNetLinksActivity extends Activity {

    Button backBtn, requestFIBsBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confignetlink);

        requestFIBsBtn = (Button) findViewById(R.id.requestNeighborFIBBtn);
        requestFIBsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ArrayList<DBData> neighbors = MainActivity.datasource.getAllFIBData();

                for (int i = 0; i < neighbors.size(); i++) {

                    // TODO - use real TIME_STRING and SENSOR_ID

                    InterestPacket interestPacket = new InterestPacket(
                            neighbors.get(i).getUserID(), ".", ProcessID.REQUEST_FIB, ".", MainActivity.deviceIP);

                    new UDPSocket(MainActivity.devicePort, neighbors.get(i).getIpAddr())
                            .execute(interestPacket.toString()); // send interest packet
                }

                /* TODO - place request if PIT
                DBData selfPITEntry = new DBData();
                        selfPITEntry.setUserID(patientUserID);
                        selfPITEntry.setSensorID("abc"); // TODO - rework
                        selfPITEntry.setTimeString("Tuesday"); // TODO - rework
                        selfPITEntry.setProcessID("one"); // TODO - rework

                        // deviceIP, because this device is the requester
                        selfPITEntry.setIpAddr(MainActivity.deviceIP);

                        MainActivity.datasource.addPITData(selfPITEntry);
                 */

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Requests were successful.", Toast.LENGTH_LONG);
                toast.show();
            }
        });


        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.configNetBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
	}
}
