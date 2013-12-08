package com.example.dustam.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(Constants.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        if (GCMRegistrar.isRegisteredOnServer(context)) {

        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        if(action.equals(Constants.NOTIFY_HOST)) {
            Log.d(TAG, "Received notification for song requests from server");
            Intent broadcast = new Intent(Constants.NOTIFY_HOST);
            context.sendBroadcast(broadcast);
        }
        else if(action.equals(Constants.LISTENER_END_PARTY)) {
            Log.d(TAG, "Received notification that party has ended");
            Intent broadcast = new Intent(Constants.LISTENER_END_PARTY);
            context.sendBroadcast(broadcast);
        }
        else {
            Log.d(TAG, "Unhandled message via GCM");
        }
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.e(TAG, "Received recoverable error: " + errorId);

        return super.onRecoverableError(context, errorId);
    }
}