package com.tbay.android.FrequentSMS;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.tbay.android.common.logger.Log;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Thomas on 28-Aug-16.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    protected static final String TAG = "GeofenceTransitionsIS";
    static final String GFS_RESULT = "result";
    static final String GFS_TRANSITION = "Transition";
    static final String GFS_DETAILS = "Details";
    static final String GFS_TIME = "Time";
    static final int TIME_LAST_WIFI_SMS = 0;

    private SharedPreferences mPrefs;

    int result = Activity.RESULT_CANCELED;

    protected static int GeofenceEventCount_ENTER = 0;
    protected static int GeofenceEventCount_EXIT = 0;
    protected static int GeofenceEventCount_DWELL = 0;

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
           String errorMessage = "Some error";
            Log.e(TAG, errorMessage);
           return;
        }

        // Code for saving persistent data
        SharedPreferences mPref = getSharedPreferences("com.tbay.android.FrequentSMS.PREFS", MODE_PRIVATE);
        long mTimeLastWifiSMS = mPref.getLong("LASTSMS", 0);

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER: ++GeofenceEventCount_ENTER; break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:  ++GeofenceEventCount_EXIT; break;
                case Geofence.GEOFENCE_TRANSITION_DWELL: ++GeofenceEventCount_DWELL; break;
            }

            result = Activity.RESULT_OK;

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String mGeofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            mGeofenceTransitionDetails = mGeofenceTransitionDetails.concat(" Prefs: ");
            mGeofenceTransitionDetails = mGeofenceTransitionDetails.concat(Long.toString(mTimeLastWifiSMS));

            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER: mGeofenceTransitionDetails += " Enter"; break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:  mGeofenceTransitionDetails += " Exit"; break;
                case Geofence.GEOFENCE_TRANSITION_DWELL: mGeofenceTransitionDetails += " Dwell";; break;
            }

            // Log the event.
            Log.i(TAG, mGeofenceTransitionDetails);

            if (AppConstants.SendPositionSMS) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(AppConstants.phonePrivate, AppConstants.phoneWork, mGeofenceTransitionDetails, null, null);
            }

            if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) && mGeofenceTransitionDetails.contains("Work")) {

                SmsManager smsManager = SmsManager.getDefault();

                if ((currentTimeMillis() - mTimeLastWifiSMS) > AppConstants.WorkLatency)
                {
                    smsManager.sendTextMessage(AppConstants.phoneWifi, AppConstants.phoneWork, AppConstants.txtWifi, null, null);

                    // Save time for SMS transmission in preferences
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putLong("LASTSMS", currentTimeMillis());
                    editor.commit();
                }
                else
                {
                    // Temporary debug
                    // smsManager.sendTextMessage(AppConstants.phonePrivate, AppConstants.phoneWork, "Deferred wifi SMS. Too early.", null, null);
                }
            }

            Intent BcIntent = new Intent(TAG);  // Broadcast intent for signalling the MainActivity
            BcIntent.putExtra(GFS_RESULT, result);
            BcIntent.putExtra(GFS_TRANSITION, geofenceTransition);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

            // Add the broadcast intent data
            BcIntent.putExtra(GFS_TIME, currentDateTimeString);
            BcIntent.putExtra(GFS_DETAILS, mGeofenceTransitionDetails);

            // Inform the main activity
            sendBroadcast(BcIntent);

         } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type));
        }
    }

    private String getGeofenceTransitionDetails(GeofenceTransitionsIntentService geofenceTransitionsIntentService, int geofenceTransition, List triggeringGeofences) {

        String transitionString = "";
        String triggerIds = "";
        for (int i=0; i<triggeringGeofences.size(); i++) {
            transitionString += triggeringGeofences.get(i).toString();
            Geofence geofence = (Geofence) triggeringGeofences.get(i);
            triggerIds += geofence.getRequestId();
        }

        return String.format("%s (%d, %d, %d): %s", transitionString, GeofenceEventCount_ENTER, GeofenceEventCount_EXIT, GeofenceEventCount_DWELL, triggerIds);
    }
}
