/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tbay.android.FrequentSMS;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu;
import android.view.MenuInflater;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import com.tbay.android.common.logger.Log;
import com.tbay.android.common.logger.LogWrapper;
import com.tbay.android.common.logger.MessageOnlyLogFilter;

import java.util.List;
import java.util.ArrayList;


/**
 * Application implement several functions:
 *  - A 1-click method of sending a user defined TextMessage to one or more numbers.
 *  - The messages and numbers are user editable.
 *  - Automatic transmission of a user definable TextMessage when entering a specific geographic area.
 *
 * This sample uses the logging framework to display log output in the log
 * fragment (LogFragment).
 */
public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    public static final String TAG = "FrequentSMS";

    // Geofencing variables and constants
    // See class AppConstants

    // Reference to the fragment showing events, so we can clear it with a button
    // as necessary.
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> mGeofences;
    PendingIntent mGeofencePendingIntent;
    private AppPreferences mAppPrefs;
    private MyBCreceiver mReceiver;
    private Intent MyIntentService;

    /**
     * Creates Intent if not already existing. Same intent is used for all fences.
     * @return Return the intent to call when position is within geofence
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        // Initialize the logging framework.
        initializeLogging();

        // Check if permissions are set, otherwise exit app.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "Please set ACCESS_FINE_LOCATION permission",
                    Toast.LENGTH_LONG).show();

            finish();
            return;
        }

        // Get selection from shared Preferences. The preference is set when selecting a radiobutton.
        // Then set the text in the editable field to the last selected text (currently a default text)
        mAppPrefs = new AppPreferences(getApplicationContext());

        RadioGroup rg = (RadioGroup) findViewById(R.id.whichSMS);

        RadioButton rb = (RadioButton)rg.getChildAt(mAppPrefs.SelectionId);
        rg.check(rb.getId());
        setText(mAppPrefs.SelectionId);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        }

        // Create a message handler
        // Currently not in use but keep for future projects.
        UIMsgHandler handler = new UIMsgHandler(TAG);


        // Start the intent service
        Intent MyServiceIntent = new Intent(this, GeofenceTransitionsIntentService.class);
        MyServiceIntent.putExtra(GeofenceTransitionsIntentService.PARAM_IN_MSG, "Starting intent service");
        startService(MyServiceIntent);

        // Register context menu for radio button group
        registerForContextMenu(rg);

        mReceiver = new MyBCreceiver();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.smseditmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //int position = info.position;

        switch (item.getItemId()) {
            case R.id.edit:
                //editNote(info.id);
                return true;
            case R.id.show:
                RadioGroup rg = (RadioGroup) findViewById(R.id.whichSMS);
                int rbid = rg.getCheckedRadioButtonId();

                RadioButton rb = (RadioButton) findViewById(rbid);

                int position2 = rg.indexOfChild(rb);

                Toast.makeText(MainActivity.this, "Telefon nummer: " + Integer.toString(position2),
                        Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void sendSMS(View view) {

        EditText Txt = (EditText) findViewById(R.id.SMSText);

        RadioGroup rg = (RadioGroup) findViewById(R.id.whichSMS);
        int rbid = rg.getCheckedRadioButtonId();

        SmsManager smsManager = SmsManager.getDefault();

        switch (rbid) {
            case R.id.Wifi:
                mAppPrefs.Key1_Msg = Txt.getText().toString();
                mAppPrefs.SelectionId = 0;
                smsManager.sendTextMessage(AppConstants.phoneWifi, null, mAppPrefs.Key1_Msg, null, null);
                break;
            case R.id.Aftensmad:
                mAppPrefs.Key2_Msg = Txt.getText().toString();
                mAppPrefs.SelectionId = 1;
                smsManager.sendTextMessage(AppConstants.phoneAnnette, null, mAppPrefs.Key2_Msg, null, null);
                break;
            case R.id.Snart_hjemme:
                mAppPrefs.Key3_Msg = Txt.getText().toString();
                mAppPrefs.SelectionId = 2;
                smsManager.sendTextMessage(AppConstants.phoneAnnette, null, mAppPrefs.Key3_Msg, null, null);
                break;
            case R.id.TestSMS:
                mAppPrefs.Key4_Msg = Txt.getText().toString();
                mAppPrefs.SelectionId = 3;
                smsManager.sendTextMessage(AppConstants.phonePrivate, null, mAppPrefs.Key4_Msg, null, null);
                break;
        }
        mAppPrefs.savePreferences(getApplicationContext());
    }


    /**
     * setText change the text in the editable field according to the id
     * in argument.
     */
    public void setText(int id) {

        EditText Txt = (EditText) findViewById(R.id.SMSText);
        switch (id) {
            case 0:
                Txt.setText(AppPreferences.Key1_Msg);
                break;
            case 1:
                Txt.setText(AppPreferences.Key2_Msg);
                break;
            case 2:
                Txt.setText(AppPreferences.Key3_Msg);
                break;
            case 3:
                Txt.setText(AppPreferences.Key4_Msg);
                break;
        }
    }

    /**
     * SMSSelected change the text in the editable field according to the user
     * selection in the radio buttons.
     */
    public void SMSSelected(View view) {

        RadioGroup rg = (RadioGroup) findViewById(R.id.whichSMS);
        int rbid = rg.getCheckedRadioButtonId();

        EditText Txt = (EditText) findViewById(R.id.SMSText);
        EditText et = (EditText) findViewById(R.id.LogTexts);
        switch (rbid) {
            case R.id.Wifi:
                mAppPrefs.SelectionId = 0;
                Txt.setText(AppPreferences.Key1_Msg);
                et.append(AppPreferences.Key1_Msg+"\n");
                break;
            case R.id.Aftensmad:
                mAppPrefs.SelectionId = 1;
                Txt.setText(AppPreferences.Key2_Msg);
                et.append(AppPreferences.Key2_Msg+"\n");
                break;
            case R.id.Snart_hjemme:
                mAppPrefs.SelectionId = 2;
                Txt.setText(AppPreferences.Key3_Msg);
                et.append(AppPreferences.Key3_Msg+"\n");
                break;
            case R.id.TestSMS:
                mAppPrefs.SelectionId = 3;
                Txt.setText(AppPreferences.Key4_Msg);
                et.append(AppPreferences.Key4_Msg+"\n");
                break;
        }

        // Save time for SMS transmission in preferences

        mAppPrefs.savePreferences(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
/*        switch (item.getItemId()) {
            // When the user clicks TEST, display the connection status.
            case R.id.test_action:
                checkNetworkConnection();
                return true;
            // Clear the log view fragment.
            case R.id.clear_action:
                mLogFragment.getLogView().setText("");
                return true;
            case R.id.stop_action:
                stopSending();
                return true;
            case R.id.start_action:
                startSending();
                return true;
        }
*/
        return false;
    }

    protected void onStart() {
        if (!mGoogleApiClient.isConnected())    // just in case this is an onRestart()
            mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {

        super.onDestroy();

        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    /**
     * Create a chain of targets that will receive log data
     */
    public void initializeLogging() {

        // Using Log, front-end to the logging chain, emulates
        // android.util.log method signatures.

        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        // A filter that strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
    }

    private void handleNewLocation(Location loc) {

        Log.i(TAG, loc.toString());
    }

    /**
     * GoogleApiClient connection callback function for connection established.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        // Create an instance of Geofence.
        if (mGeofences == null) {
            mGeofences = new ArrayList<Geofence>();

            Geofence mFence = new Geofence.Builder().setRequestId(AppConstants.HomeFenceId)
                    .setCircularRegion(AppConstants.HomeLatitude, AppConstants.HomeLongitude, AppConstants.HomeRadius)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(60000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE).build();

            mGeofences.add(mFence);

            mFence = new Geofence.Builder().setRequestId(AppConstants.WorkFenceId)
                    .setCircularRegion(AppConstants.WorkLatitude, AppConstants.WorkLongitude, AppConstants.WorkRadius)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(60000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE).build();

            mGeofences.add(mFence);

            mFence = new Geofence.Builder().setRequestId(AppConstants.LibraryId)
                    .setCircularRegion(AppConstants.BibloLatitude, AppConstants.BibloLongitude, AppConstants.BibloRadius)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(20000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE).build();

            mGeofences.add(mFence);

            GeofencingRequest.Builder mGeoFencingReqBuild = new GeofencingRequest.Builder();
            mGeoFencingReqBuild.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
            mGeoFencingReqBuild.addGeofences(mGeofences);
            GeofencingRequest req = mGeoFencingReqBuild.build();

            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, req, getGeofencePendingIntent()).setResultCallback(this);
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            // Blank for a moment...
        } else {
            //handleNewLocation(location);
        }
    }

    /**
     * GoogleApiClient connection callback function for connection suspension.
     * @param i  cause of the disconnect
     */
    @Override
    public void onConnectionSuspended(int i) {

        String logTxt = "Location services suspended. Please reconnect.";
        logTxt.concat(Integer.toString(i));
        Log.i(TAG, logTxt);
    }

    /**
     * GoogleApiClient OnConnectionFailedListener callback function for connection failed.
     * This function is called if call to GoogleApiClient().connect() fails.
     * @param connectionResult  resultcode for the failed connection attempt
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        String logTxt = "onConnectionFailed: Location services connect failed.";
        logTxt.concat(connectionResult.toString());
        Log.i(TAG, logTxt);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        //egisterReceiver(receiver, new IntentFilter(GeofenceTransitionsIntentService.TAG));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Do NOT disconnect at this point. We want the service to run while not in front.
        if (mGoogleApiClient.isConnected()) {
        //    mGoogleApiClient.disconnect();
        }

        // In future: maybe let receiver registered.
        //unregisterReceiver(receiver);
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = "ged";// GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
        }
    }
}