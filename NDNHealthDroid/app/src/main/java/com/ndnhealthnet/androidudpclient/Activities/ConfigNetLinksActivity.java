package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.Comm.UDPSocket;
import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Packet.InterestPacket;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;

import java.util.ArrayList;

/**
 * TODO
 */
public class ConfigNetLinksActivity extends Activity {

    Button backBtn, requestFIBsBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confignetlink);

        /** requests the FIB of each person in your own FIB **/
        requestFIBsBtn = (Button) findViewById(R.id.requestNeighborFIBBtn);
        requestFIBsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int requestsSentCount = 0; // count requests send in order to inform user

                ArrayList<DBData> neighbors = DBSingleton.getInstance(getApplicationContext()).getDB().getAllFIBData();

                // loop over all FIB entries and ask each valid for their FIB
                for (int i = 0; i < neighbors.size(); i++) {

                    // check for valid neighbor ip
                    if (!neighbors.get(i).getIpAddr().equals(StringConst.NULL_IP)) {

                        // check to see if PIT table already has entry for neighbor's FIB
                        ArrayList<DBData> pitsForNeighbor = DBSingleton.getInstance(getApplicationContext()).getDB()
                                .getGeneralPITData(neighbors.get(i).getUserID());

                        boolean pitEntryFound = false;
                        for (int j = 0; j < pitsForNeighbor.size(); j++) {

                            // true if FIB previously requested
                            pitEntryFound |= pitsForNeighbor.get(i).getProcessID().equals(StringConst.INTEREST_FIB);
                        }

                        if (!pitEntryFound) {
                            InterestPacket interestPacket = new InterestPacket(
                                    neighbors.get(i).getUserID(), StringConst.NULL_FIELD,
                                    StringConst.INTEREST_FIB, StringConst.NULL_FIELD, MainActivity.deviceIP);

                            // NOTE: temporary debugging output
                            System.out.println("sent packet: " + interestPacket.toString());

                            // send interest to each neighbor; ask for fib
                            new UDPSocket(MainActivity.devicePort, neighbors.get(i).getIpAddr(), StringConst.INTEREST_TYPE)
                                    .execute(interestPacket.toString()); // send interest packet

                            // put FIB request in PIT
                            DBData selfPITEntry = new DBData();
                            selfPITEntry.setUserID(neighbors.get(i).getUserID());
                            selfPITEntry.setSensorID(StringConst.NULL_FIELD);
                            selfPITEntry.setTimeString(StringConst.CURRENT_TIME);
                            selfPITEntry.setProcessID(StringConst.INTEREST_FIB);

                            // deviceIP, because this device is the requester
                            selfPITEntry.setIpAddr(MainActivity.deviceIP);

                            DBSingleton.getInstance(getApplicationContext()).getDB().addPITData(selfPITEntry);

                            requestsSentCount++;
                        }
                    }
                }

                Toast toast = Toast.makeText(getApplicationContext(),
                        Integer.toString(requestsSentCount) + " requests were successful.", Toast.LENGTH_LONG);
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
