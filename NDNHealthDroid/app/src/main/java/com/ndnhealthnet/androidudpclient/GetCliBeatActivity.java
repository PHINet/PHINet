package com.ndnhealthnet.androidudpclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.ArrayList;

/**
 * Activity displays list of current patients and allows the following
 * 1. add new patient
 * 2. select patient and move to activity where modification/data-requests are possible
 */
public class GetCliBeatActivity extends ListActivity {

    Button backBtn;
    Button addNewPatientBtn;
    private String[] patientInputString;
    TextView emptyListTextView;

    // used to identify intent-data
    final static String PATIENT_IP = "PATIENT_IP";
    final static String PATIENT_USER_ID = "PATIENT_USER_ID";

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null); // force activity to reload
    }

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getclibeat);

        ArrayList<DBData> patientList = MainActivity.datasource.getAllFIBData();
        if (patientList == null) {
            // array list is null; pass empty data structure rather than null
            patientList = new ArrayList<DBData>();
        }

        final PatientAdapter adapter = new PatientAdapter(this, patientList);
        setListAdapter(adapter);

        emptyListTextView = (TextView) findViewById(R.id.emptyListTextView);

        if (adapter.getCount() > 0) {
            // hide "empty patient list" text when patients actually do exist
            emptyListTextView.setVisibility(View.GONE);
        }

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.getCliBeatBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });

        addNewPatientBtn = (Button) findViewById(R.id.addNewPatientBtn);
        addNewPatientBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                final EditText patientInput = new EditText(GetCliBeatActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(GetCliBeatActivity.this);
                builder.setTitle("Input Format (IP is optional): 'IP,Name'");
                builder.setView(patientInput);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean isValidInput = true; // we'll later check if false
                        boolean ipEntered = false;
                        try { // try/catch attempts input validation

                            if (!patientInput.getText().toString().contains(",")) {
                                // this means there was no comma; user likely didn't enter IP
                                ipEntered = false;

                                isValidInput &= patientInput.getText().toString().length() <= 15; // max name requirement
                            } else {
                                ipEntered = true;

                                // if syntactically correct, input is separated by comma
                                patientInputString = patientInput.getText().toString().split(",");

                                // tests validity of input
                                patientInputString[0] = patientInputString[0].trim(); // name input
                                patientInputString[1] = patientInputString[1].trim(); // ip input

                                isValidInput = Utils.validIP(patientInputString[0]);

                                // NOTE: name-length constraints were chosen somewhat arbitrarily
                                isValidInput &= patientInputString[1].length() >= 3; // min. name requirement
                                isValidInput &= patientInputString[1].length() <= 15; // max name requirement
                            }

                        } catch (Exception e) {
                            // input didn't pass checks, mark input as invalid and notify user
                            isValidInput = false;
                        }

                        if (isValidInput) { // add user to fib

                            DBData data = new DBData();
                            if (!ipEntered) {

                                data.setUserID(patientInput.getText().toString());
                                data.setTimeString(StringConst.CURRENT_TIME);
                                data.setIpAddr(StringConst.NULL_IP);

                                MainActivity.datasource.addFIBData(data);
                            } else {
                                data.setIpAddr(patientInputString[0]);
                                data.setUserID(patientInputString[1]);
                                data.setTimeString(StringConst.CURRENT_TIME);

                                if (MainActivity.datasource.getFIBData(patientInputString[0]) == null) {
                                    // user entered valid patient, now add to fib

                                    MainActivity.datasource.addFIBData(data);

                                    // hide "empty patient list" text when patient added
                                    emptyListTextView.setVisibility(View.GONE);
                                } else {
                                    // user entered previous patient, just update

                                    MainActivity.datasource.updateFIBData(data);
                                }
                            }

                            adapter.add(data);
                            adapter.notifyDataSetChanged();
                            emptyListTextView.setVisibility(View.GONE); // hide "no patients" text
                        } else {
                            Toast toast = Toast.makeText(GetCliBeatActivity.this,
                                    "Invalid IP or name length (3-15 characters).", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    /**
     * Used by patient list view.
     */
    private class PatientAdapter extends ArrayAdapter<DBData> {

        Activity activity = null;
        ArrayList<DBData> listData;

        public PatientAdapter(ListActivity li, ArrayList<DBData> allFIBData)
        {
            // TODO - rework (be selective with FIB data that is displayed)

            super(li, 0, allFIBData);
            listData = allFIBData;
            activity = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_patient, null);
            }

            final DBData dbData = listData.get(position);

            // creates individual button in ListView for each patient
            Button patientButton = (Button)convertView.findViewById(R.id.list_patientButton);
            patientButton.setText("IP: "  + dbData.getIpAddr() + "\nName: "+ dbData.getUserID());
            patientButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(GetCliBeatActivity.this);
                    builder.setTitle("Go to patient page?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(GetCliBeatActivity.this, PatientDataActivity.class);

                            // through intent, pass patient information to activity
                            intent.putExtra(PATIENT_IP, dbData.getIpAddr());
                            intent.putExtra(PATIENT_USER_ID, dbData.getUserID());
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });
            return convertView;
        }
    }
}