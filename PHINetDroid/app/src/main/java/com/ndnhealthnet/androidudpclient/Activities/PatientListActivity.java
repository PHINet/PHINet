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
    }

    /**
     * Return all patients from FIB
     *
     * @return ArrayList of all patients
     */
    private ArrayList<FIBEntry> getPatients() {

        // TODO - query server
        return new ArrayList<FIBEntry>();
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