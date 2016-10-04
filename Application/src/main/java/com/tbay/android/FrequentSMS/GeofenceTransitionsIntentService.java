package com.tbay.android.FrequentSMS;

import android.app.Activity;
import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.tbay.android.common.logger.Log;


import java.text.DateFormat;
import java.util.Date;
import java.util.List;

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

    int result = Activity.RESULT_CANCELED;

    protected static int GeofenceEventCount_ENTER = 0;
    protected static int GeofenceEventCount_EXIT = 0;
    protected static int GeofenceEventCount_DWELL = 0;
    private String mGeofenceTransitionDetails;

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

        Context c = getApplicationContext();

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
            mGeofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER: mGeofenceTransitionDetails += " Enter"; break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:  mGeofenceTransitionDetails += " Exit"; break;
                case Geofence.GEOFENCE_TRANSITION_DWELL: mGeofenceTransitionDetails += " Dwell";; break;
            }

            // Log the event.
            Log.i(TAG, mGeofenceTransitionDetails);

            Intent BcIntent = new Intent(TAG);  // Broadcast intent for signalling the MainActivity
            BcIntent.putExtra(GFS_RESULT, result);
            BcIntent.putExtra(GFS_TRANSITION, geofenceTransition);
            //BcIntent.putExtra(GFS_POSITIONID, );

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
