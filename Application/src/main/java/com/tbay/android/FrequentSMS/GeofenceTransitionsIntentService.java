package com.tbay.android.FrequentSMS;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
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

    protected static final String TAG = "FrequentSMS:GeoIS";
    static final String GFS_RESULT = "result";
    static final String GFS_TRANSITION = "Transition";
    static final String GFS_DETAILS = "Details";
    static final String GFS_TIME = "Time";
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";

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
        long currentMillis = currentTimeMillis();

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
                try {
                    smsManager.sendTextMessage(AppConstants.phonePrivate, AppConstants.phoneWork, mGeofenceTransitionDetails, null, null);
                }
                catch (Exception e)  {
                    Log.i(TAG, e.getMessage());
                }
            }

            String msg = "Time: " + Long.toString(currentMillis) + " Last: " + Long.toString(mTimeLastWifiSMS) + " Diff: " + Long.toString(currentMillis-mTimeLastWifiSMS);
            Log.i(TAG, msg);

            if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) && (mGeofenceTransitionDetails.contains("Work")) || mGeofenceTransitionDetails.contains("Biblo")){

                SmsManager smsManager2 = SmsManager.getDefault();

                if ((currentMillis - mTimeLastWifiSMS) > AppConstants.WorkLatency)
                {

                    Log.i(TAG, AppConstants.phoneWifi);
                    Log.i(TAG, AppConstants.phoneWork);
                    Log.i(TAG, AppPreferences.Key1_Msg);

                    try
                    {
                        smsManager2.sendTextMessage(AppConstants.phoneWifi, AppConstants.phoneWork, AppPreferences.Key1_Msg, null, null);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, e.getMessage());
                    }

                    // Save time for SMS transmission in preferences
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putLong("LASTSMS", currentMillis);
                    editor.commit();
                }
                else
                {

                    // Temporary debug
                    try {
                        smsManager2.sendTextMessage(AppConstants.phonePrivate, AppConstants.phoneWork, "Deferred wifi SMS. Too early.", null, null);
                    }
                    catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                    }
                }
            }

            Intent BcIntent = new Intent("com.tbay.android.FrequentSMS.MAIN_ACTIVITY_INFO");  // Broadcast intent for signalling the MainActivity

            //BcIntent.setAction("com.android.tbay.MAIN_ACTIVITY_INFO");

            BcIntent.putExtra(GFS_RESULT, result);
            BcIntent.putExtra(GFS_TRANSITION, geofenceTransition);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

            // Add the broadcast intent data
            BcIntent.putExtra(GFS_TIME, currentDateTimeString);
            BcIntent.putExtra(GFS_DETAILS, mGeofenceTransitionDetails);

            // Inform the main activity
            sendBroadcast(BcIntent);

            Log.i(TAG, "Test message");
        } else {
            // Log the error.
            Log.w(TAG, getString(R.string.geofence_transition_invalid_type) + ":" + Integer.toString(geofenceTransition));
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
