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
 * Activity displays list of current doctors and allows addition of more doctors.
 */
public class DoctorListActivity extends ListActivity {

    Button backBtn;
    Button addNewDoctorBtn;
    TextView emptyListTextView, loggedInText;

    @Override
    protected void onResume() {
        super.onResume();
        this.onCreate(null); // force activity to reload (to display new patients)
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctorlist);

        final String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place username on screen

        ArrayList<FIBEntry> patientList = getDoctors();

        final DoctorAdapter adapter = new DoctorAdapter(this, patientList);
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

        addNewDoctorBtn = (Button) findViewById(R.id.addNewDoctorBtn);
        addNewDoctorBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                final EditText patientInput = new EditText(DoctorListActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(DoctorListActivity.this);
                builder.setTitle("Input Name");
                builder.setView(patientInput);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean isValidInput = false;
                        boolean ipEntered = false;
                        boolean doctorAlreadyExists = false;
                        try { // try/catch attempts input validation

                            String userID; // stores userID entered to check if user already exists


                            // TODO - handle this event

                        } catch (Exception e) {

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
     * Return all doctors from FIB
     *
     * @return ArrayList of all doctors
     */
    private ArrayList<FIBEntry> getDoctors() {

        // TODO - query server for list of doctors

        return new ArrayList<FIBEntry>();
    }

    /**
     * Used by doctor list view.
     */
    private class DoctorAdapter extends ArrayAdapter<FIBEntry> {

        Activity activity = null;
        ArrayList<FIBEntry> listData;

        public DoctorAdapter(ListActivity li, ArrayList<FIBEntry> allFIBData)
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
                        .inflate(R.layout.list_item_doctor, null);
            }

            final FIBEntry dbData = listData.get(position);

            // creates individual button in ListView for each patient
            TextView doctorText = (TextView) convertView.findViewById(R.id.listDoctorText);
            doctorText.setText(dbData.getUserID());

            return convertView;
        }
    }
}