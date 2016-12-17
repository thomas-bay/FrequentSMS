package com.tbay.android.FrequentSMS;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.tbay.android.common.logger.Log;

import static android.app.Activity.RESULT_OK;
import static android.widget.Toast.*;

/**
 * Created by Thomas on 04-Dec-16.
 */

public class MyBCreceiver extends BroadcastReceiver {

    public static final String TAG = "FrequentSMS:BC";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "BCIntent received: " + intent.getAction());

        if (intent.getAction().contains("MAIN_ACTIVITY_INFO")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String time = bundle.getString(GeofenceTransitionsIntentService.GFS_TIME);
                String string = bundle.getString(GeofenceTransitionsIntentService.GFS_DETAILS);
                int resultCode = bundle.getInt(GeofenceTransitionsIntentService.GFS_RESULT);
                if (resultCode == RESULT_OK) {

                    Log.i(TAG, string);
                    Toast.makeText(context, time + " " + string, LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Broadcast receiver result not OK",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
