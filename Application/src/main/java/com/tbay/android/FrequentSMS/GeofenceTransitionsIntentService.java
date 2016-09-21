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
    int result = Activity.RESULT_CANCELED;

    protected static int GeofenceEventCount_ENTER = 0;
    protected static int GeofenceEventCount_EXIT = 0;
    protected static int GeofenceEventCount_DWELL = 0;
    private String mGeofenceTransitionDetails;
    private Handler mGeofenceHandler;


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

            result = Activity.RESULT_OK;

            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER: ++GeofenceEventCount_ENTER; break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:  ++GeofenceEventCount_EXIT; break;
                case Geofence.GEOFENCE_TRANSITION_DWELL: ++GeofenceEventCount_DWELL; break;
            }

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            mGeofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            // Send notification and log the transition details.
            sendNotification(mGeofenceTransitionDetails);
            Log.i(TAG, mGeofenceTransitionDetails);

         } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type));
        }

        Intent BcIntent = new Intent(TAG);  // Broadcast intent for signalling the MainActivity
        BcIntent.putExtra(GFS_RESULT, result);
        BcIntent.putExtra(GFS_TRANSITION, geofenceTransition);
        BcIntent.putExtra(GFS_DETAILS, mGeofenceTransitionDetails);

        sendBroadcast(BcIntent);
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

    private void sendNotification(String geofenceTransitionDetails) {
    }

    private class GeofenceErrorMessages {
    }
}
