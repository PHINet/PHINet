package com.example.androidudpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
 * Linked to GetOwnHeartbeatButton
 */
public class RetrieveHeartbeatActivity extends Activity {

    Button backBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrievebeat);

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.retrieveBeatBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });

	    // TODO Add Heartbeat Sensor Code
	    // TODO Add Method to Send to Server
	}
}
