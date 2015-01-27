package com.example.androidudpclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;

public class GetCliBeatActivity extends ListActivity {

    Button backBtn;
    Button editPatientDataBtn;
    Button addNewPatientBtn;
    private String[] patientInputString;
    ArrayList<Patient> patients;

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getclibeat);

        patients = new ArrayList<Patient>(); // TODO - store this data elsewhere

        // NOTE: two fake patients to test functionality
        patients.add(new Patient("10.170.20.10","Test Patient 1"));
        patients.add(new Patient("10.170.21.9", "Test Patient 2"));

        PatientAdapter adapter = new PatientAdapter(patients, this);
        setListAdapter(adapter);

        /** Returns to MainActivity **/
        backBtn = (Button) findViewById(R.id.getCliBeatBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });

        editPatientDataBtn = (Button) findViewById(R.id.editPatientDataBtn);
        editPatientDataBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // TODO - implement; allow for editing of patient data
            }
        });

        final Context c = this; // TODO - can this work around be avoided?

        addNewPatientBtn = (Button) findViewById(R.id.addNewPatientBtn);
        addNewPatientBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                final EditText patientInput = new EditText(c);
                final AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle("Input Format: 'IP,Name'");
                builder.setView(patientInput);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            patientInputString = patientInput.getText().toString().split(",");
                        } catch (Exception e) {
                            // TODO - handle
                        }

                        boolean validIP = false;
                        try {
                            // tests validity of IP input
                            patientInputString[0] = patientInputString[0].trim();
                            patientInputString[1] = patientInputString[1].trim();

                            InetAddress.getByName(patientInputString[0]);
                            validIP = true;
                        } catch (Exception e) {
                            validIP = false;
                        }

                        // NOTE: name-length contraints were chosen somewhat arbitrarily
                        if (validIP && patientInputString[1].length() >= 3
                                && patientInputString[1].length() <= 10) {
                            patients.add(new Patient(patientInputString[0], patientInputString[1]));
                        } else {
                            Toast toast = Toast.makeText(c,
                                    "Invalid IP or name length (3-10 characters).", Toast.LENGTH_LONG);
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

    private class PatientAdapter extends ArrayAdapter<Patient> {

        Activity activity = null;

        public PatientAdapter(ArrayList<Patient> patients, ListActivity li)
        {
            super(li, 0, patients);
            activity = (Activity)li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_patient, null);
            }

            final Patient p = patients.get(position);
            final Context c = this.activity;

            Button patientButton = (Button)convertView.findViewById(R.id.list_patientButton);
            patientButton.setText("IP: "  + p.getIP() + "\nName: "+ p.getName());
            patientButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // dialog allows doctor to contact patient and retrieve data

                    final AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    final EditText editText = new EditText(c);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                    builder.setTitle("Request patient data? Nearest selected interval will be returned.");
                    builder.setView(editText);

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // TODO - perform a check: are networking capabilities enabled?
                            // TODO - define message format; NDN compatible
                            // TODO - define/improve request interval

                            new UDPSocket(MainActivity.devicePort, p.getIP()).execute("INTEREST::" + Integer.toString(MainActivity.devicePort));

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