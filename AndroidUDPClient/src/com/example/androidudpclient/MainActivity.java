/*****************************************************************************
*  Copyright (c) 2004-2008, 2013 Digi International Inc., All Rights Reserved
*
*  This software contains proprietary and confidential information of Digi
*  International Inc.  By accepting transfer of this copy, Recipient agrees
*  to retain this software in confidence, to prevent disclosure to others,
*  and to make no use of this software other than that for which it was
*  delivered.  This is an unpublished copyrighted work of Digi International
*  Inc.  Except as permitted by federal law, 17 USC 117, copying is strictly
*  prohibited.
*
*  Restricted Rights Legend
*
*  Use, duplication, or disclosure by the Government is subject to
*  restrictions set forth in sub-paragraph (c)(1)(ii) of The Rights in
*  Technical Data and Computer Software clause at DFARS 252.227-7031 or
*  subparagraphs (c)(1) and (2) of the Commercial Computer Software -
*  Restricted Rights at 48 CFR 52.227-19, as applicable.
*
*  Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
*
*****************************************************************************/
package com.example.androidudpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    /*
    private static final String host = null;
	private int port = 1635;
	private String IPAddr = "10.0.2.15";
	String message = null;
	TextView txt5,txt1;
	//Buffers for UDP Sockets
	byte[] send_data = new byte[1024];
	byte[] receiveData = new byte[1024];
	*/
	// /** Called when the activity is first created. */

	String modifiedSentence;

	Button netLinkBtn;
	Button selfBeatBtn;
	Button getAvgBtn;
	Button cliBeatBtn;
	
	EditText edittext;

	private DBDataSource datasource;

	/*
	 * Run Upon Initial Installation of Applciation
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
         * Creates Tables
         */
         // TODO - problem cause crash
         //      datasource = new DBDataSource(this);
         //        datasource.open();
        
        /*
         * TODO UDP Sockets Creation
         */

        selfBeatBtn = (Button) findViewById(R.id.selfBeatBtn);
        selfBeatBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
        	startActivity(new Intent(MainActivity.this, RetrieveHeartbeatActivity.class));
        	}
        });
        
        netLinkBtn = (Button) findViewById(R.id.netLinkBtn);
        netLinkBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
        	    startActivity(new Intent(MainActivity.this, ConfigNetLinksActivity.class));
        	}
        });
        
        getAvgBtn = (Button) findViewById(R.id.getAvgBtn);
        getAvgBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GetAvgBeatActivity.class));
        	}
        });
        
        cliBeatBtn = (Button) findViewById(R.id.cliBeatBtn);
        cliBeatBtn.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v) {
        		startActivity(new Intent(MainActivity.this, GetCliBeatActivity.class));
        	}
        });
    }

   /* public void setText(){
    	Button myButton = (Button)findViewById(R.id.selfBeatBtn);
    	//edittext = (EditText)findViewById(R.id.search);
    	myButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				message = edittext.getText().toString();
				UDPSocket mySocket = new UDPSocket(port, IPAddr);
				Toast.makeText(getApplicationContext(), "Running 'client'",Toast.LENGTH_LONG).show();
				//txt1.setText(modifiedSentence);
			}
    	});
    }*/
}