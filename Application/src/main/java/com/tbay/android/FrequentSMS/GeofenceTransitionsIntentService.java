package com.tbay.android.FrequentSMS;

import android.app.IntentService;
import android.content.Intent;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.tbay.android.common.logger.Log;

import java.util.List;

import static com.tbay.android.FrequentSMS.MainActivity.TAG;

/**
 * Created by Thomas on 28-Aug-16.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
           String errorMessage = "Some error";
            Log.e(TAG, errorMessage);
           return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);

            TextView Txt = (TextView) findViewById(R.id.CurrentPosition);

            Txt.setText(geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type));
        }
    }

    private String getGeofenceTransitionDetails(GeofenceTransitionsIntentService geofenceTransitionsIntentService, int geofenceTransition, List triggeringGeofences) {
        return("getGeofenceTransitionDetailsString");
    }

    private void sendNotification(String geofenceTransitionDetails) {
    }

    private class GeofenceErrorMessages {
    }
}
