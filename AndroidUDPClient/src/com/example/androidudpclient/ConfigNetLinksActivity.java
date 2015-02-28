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

        /** requests the FIB of each person in your own FIB **/
        requestFIBsBtn = (Button) findViewById(R.id.requestNeighborFIBBtn);
        requestFIBsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int requestsSent = 0;

                ArrayList<DBData> neighbors = MainActivity.datasource.getAllFIBData();

                for (int i = 0; i < neighbors.size(); i++) {

                    // check for valid neighbor ip
                    if (!neighbors.get(i).getIpAddr().equals(ProcessID.NULL_IP)) {

                        // check to see if PIT table already has entry for neighbor's FIB
                        ArrayList<DBData> pitsForNeighbor = MainActivity.datasource
                                .getGeneralPITData(neighbors.get(i).getUserID(),
                                        neighbors.get(i).getIpAddr());

                        boolean pitEntryFound = false;
                        for (int j = 0; j < pitsForNeighbor.size(); j++) {

                            // true if FIB previously requested
                            pitEntryFound |= pitsForNeighbor.get(i).getProcessID().equals(ProcessID.REQUEST_FIB);
                        }

                        if (!pitEntryFound) {
                            InterestPacket interestPacket = new InterestPacket(
                                    neighbors.get(i).getUserID(), ProcessID.NULL_FIELD,
                                    ProcessID.REQUEST_FIB, ProcessID.NULL_FIELD, MainActivity.deviceIP);

                            // NOTE: temporary debugging output
                            System.out.println("sent packet: " + interestPacket.toString());

                            // send interest to each neighbor; ask for fib
                            new UDPSocket(MainActivity.devicePort, neighbors.get(i).getIpAddr())
                                    .execute(interestPacket.toString()); // send interest packet

                            // put FIB request in PIT
                            DBData selfPITEntry = new DBData();
                            selfPITEntry.setUserID(neighbors.get(i).getUserID());
                            selfPITEntry.setSensorID(ProcessID.NULL_FIELD);
                            selfPITEntry.setTimeString(DBData.CURRENT_TIME);
                            selfPITEntry.setProcessID(ProcessID.REQUEST_FIB);

                            // deviceIP, because this device is the requester
                            selfPITEntry.setIpAddr(MainActivity.deviceIP);

                            MainActivity.datasource.addPITData(selfPITEntry);

                            requestsSent++;
                        }
                    }
                }

                Toast toast = Toast.makeText(getApplicationContext(),
                        Integer.toString(requestsSent) + " requests were successful.", Toast.LENGTH_LONG);
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
