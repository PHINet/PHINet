package com.ndnhealthnet.androidudpclient.Activities;

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

import com.ndnhealthnet.androidudpclient.DB.DBDataTypes.FIBEntry;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Activity displays list of current patients and allows
 * 1. add new patient
 * 2. select patient and move to activity where modification/data-requests are possible
 */
public class PatientListActivity extends ListActivity {

    Button backBtn;
    Button addNewPatientBtn;
    private String[] patientInputString;
    TextView emptyListTextView, loggedInText;

    // used to identify intent-data
    final static String PATIENT_IP = "PATIENT_IP";
    final static String PATIENT_USER_ID = "PATIENT_USER_ID";

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null); // force activity to reload (to display new patients)
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientlist);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place username on screen

        ArrayList<FIBEntry> patientList = getPatients();

        final PatientAdapter adapter = new PatientAdapter(this, patientList);
        setListAdapter(adapter);

        emptyListTextView = (TextView) findViewById(R.id.emptyListTextView);

        if (adapter.getCount() > 0) {
            // hide "empty patient list" text when patients actually do exist
            emptyListTextView.setVisibility(View.GONE);
        }

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });

        addNewPatientBtn = (Button) findViewById(R.id.addNewPatientBtn);
        addNewPatientBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                final EditText patientInput = new EditText(PatientListActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(PatientListActivity.this);
                builder.setTitle("Input Format (IP is optional): 'IP,Name'");
                builder.setView(patientInput);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean isValidInput = false;
                        boolean ipEntered = false;
                        boolean patientAlreadyExists = false;
                        try { // try/catch attempts input validation

                            String userID; // stores userID entered to check if user already exists

                            if (!patientInput.getText().toString().contains(",")) {
                                // this means there was no comma; user likely didn't enter IP
                                ipEntered = false;

                                isValidInput = Utils.isValidUserName(patientInput.getText().toString());
                                userID = patientInput.getText().toString();
                            } else {
                                ipEntered = true;

                                // if syntactically correct, input is separated by comma
                                patientInputString = patientInput.getText().toString().split(",");

                                patientInputString[0] = patientInputString[0].trim(); // ip input
                                patientInputString[1] = patientInputString[1].trim(); // name input

                                userID = patientInputString[1];

                                // perform input validation
                                isValidInput = Utils.isValidIP(patientInputString[0]);
                                isValidInput &= Utils.isValidUserName(patientInputString[1]);
                            }

                            FIBEntry queryResult = DBSingleton.getInstance(getApplicationContext()).getDB().getFIBData(userID);
                            patientAlreadyExists = queryResult != null; // check that user doesn't already exist

                        } catch (Exception e) {
                            // input didn't pass checks, mark input as invalid and notify user
                            isValidInput = false;
                        }

                        if (isValidInput && !patientAlreadyExists) { // add user to fib

                            FIBEntry data = new FIBEntry();
                            data.setTimeString(ConstVar.CURRENT_TIME);
                            data.setIsMyPatient(true);

                            if (!ipEntered) {

                                data.setIpAddr(ConstVar.NULL_IP);
                                data.setUserID(patientInput.getText().toString());
                            } else {

                                data.setIpAddr(patientInputString[0]);
                                data.setUserID(patientInputString[1]);
                            }

                            // add patient to FIB now
                            DBSingleton.getInstance(getApplicationContext()).getDB().addFIBData(data);

                            adapter.add(data);
                            adapter.notifyDataSetChanged();
                            emptyListTextView.setVisibility(View.GONE); // hide "no patients" text

                        } else if (patientAlreadyExists) {
                            Toast toast = Toast.makeText(PatientListActivity.this,
                                    "Patient already exists.", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(PatientListActivity.this,
                                    "Invalid IP, or invalid name length.", Toast.LENGTH_LONG);
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
     * Return all patients from FIB
     *
     * @return ArrayList of all patients
     */
    private ArrayList<FIBEntry> getPatients() {

        ArrayList<FIBEntry> fibList = DBSingleton.getInstance(getApplicationContext()).getDB().getAllFIBData();

        if (fibList == null) {
            // array list is null; pass empty data structure rather than null
            return new ArrayList<>();
        } else {

            // entries found; now remove all who aren't the client's patients

            Iterator<FIBEntry> i = fibList.iterator();

            while (i.hasNext()) {
                FIBEntry fibEntry = i.next();

                if (!fibEntry.isMyPatient()) {
                    i.remove(); // remove all non-patients before returning
                }
            }

            return fibList;
        }
    }

    /**
     * Used by patient list view.
     */
    private class PatientAdapter extends ArrayAdapter<FIBEntry> {

        Activity activity = null;
        ArrayList<FIBEntry> listData;

        public PatientAdapter(ListActivity li, ArrayList<FIBEntry> allFIBData)
        {
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

            final FIBEntry dbData = listData.get(position);

            // creates individual button in ListView for each patient
            Button patientButton = (Button)convertView.findViewById(R.id.listPatientButton);
            patientButton.setText("IP: "  + dbData.getIpAddr() + "\nName: "+ dbData.getUserID());
            patientButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(PatientListActivity.this);
                    builder.setTitle("Go to patient page?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(PatientListActivity.this, PatientActivity.class);

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