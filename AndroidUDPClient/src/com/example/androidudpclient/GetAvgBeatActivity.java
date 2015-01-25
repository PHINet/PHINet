package com.example.androidudpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GetAvgBeatActivity extends Activity {

    Button backBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getavgbeat);

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.getAvgBeatBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
	}
}
