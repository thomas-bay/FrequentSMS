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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;


import com.tbay.android.common.logger.Log;
import com.tbay.android.common.logger.LogFragment;
import com.tbay.android.common.logger.LogWrapper;
import com.tbay.android.common.logger.MessageOnlyLogFilter;

/**
 * Sample application demonstrating how to test whether a device is connected,
 * and if so, whether the connection happens to be wifi or mobile (it could be
 * something else).
 *
 * This sample uses the logging framework to display log output in the log
 * fragment (LogFragment).
 */
public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "FrequentSMS";
    public static int i = 0;

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    // Reference to the fragment showing events, so we can clear it with a button
    // as necessary.
    private LogFragment mLogFragment;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        // Initialize the logging framework.
        initializeLogging();

        EditText Txt = (EditText) findViewById(R.id.SMSText);
        Txt.setText(R.string.Wifi_txt);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
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

        switch (rbid)
        {
            case R.id.Wifi:
                smsManager.sendTextMessage("72201018", null, Txt.getText().toString(), null, null);
                break;
            case R.id.Aftensmad:
                smsManager.sendTextMessage("30223796", null, Txt.getText().toString(), null, null);
                break;
            case R.id.TestSMS:
                smsManager.sendTextMessage("30221982", null, Txt.getText().toString(), null, null);
                break;
            case R.id.Snart_hjemme:
                smsManager.sendTextMessage("30223796", null, Txt.getText().toString(), null, null);
                break;
        }
    }

    public void SMSSelected(View view) {

        //View rb = findViewById(R.id.radioButton);
        RadioGroup rg = (RadioGroup) findViewById(R.id.whichSMS);
        int rbid = rg.getCheckedRadioButtonId();

        EditText Txt = (EditText) findViewById(R.id.SMSText);
        switch (rbid)
        {
            case R.id.Wifi:
                Txt.setText(R.string.Wifi_txt);
                break;
            case R.id.Aftensmad:
                Txt.setText("Skal jeg købe noget med på vejen hjem?");
                break;
            case R.id.TestSMS:
                Txt.setText(R.string.TestSMS_txt);
                break;
            case R.id.Snart_hjemme:
                Txt.setText(R.string.Hjemme_txt);
        }
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

    /**
     * Stop sending data to the peer.
     */
    private void stopSending() {

        Log.i(TAG, "Sending stopped.");
        //mLogFragment.getLogView().appendToLog("stopped");
    }

    /**
     * Start sending data to the peer.
     */
    private void startSending() {

        String[] args = new String[2];

        String IPStr = "Sending started at: ";

        Log.i(TAG, IPStr);

        new BackgroundActivity().execute(args);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).
     */
    private void checkNetworkConnection() {
      // BEGIN_INCLUDE(connect)
      ConnectivityManager connMgr =
          (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
      if (activeInfo != null && activeInfo.isConnected()) {
          wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
          mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
          if(wifiConnected) {
              Log.i(TAG, getString(R.string.wifi_connection));
          } else if (mobileConnected){
              Log.i(TAG, getString(R.string.mobile_connection));
          }
      } else {
          Log.i(TAG, getString(R.string.no_wifi_or_mobile));
      }
      // END_INCLUDE(connect)

      SmsManager smsManager = SmsManager.getDefault();
      smsManager.sendTextMessage("30221982", null, "sms message", null, null);
    }

    /** Create a chain of targets that will receive log data */
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
        TextView Txt = (TextView) findViewById(R.id.CurrentPosition);

        Txt.setText(loc.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            // Blank for a moment...
        }
        else {
            handleNewLocation(location);
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
 }
