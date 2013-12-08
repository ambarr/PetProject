package com.example.dustam.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GCMBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            Log.d(TAG, "Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            Log.d(TAG, "Deleted messages on server: " +
                    intent.getExtras().toString());
        } else {
            String action = intent.getExtras().getString("action");
            if (Constants.NOTIFY_HOST.equals(action)) {
                Log.d(TAG, "Received notification for song requests from server");
                Intent broadcast = new Intent(Constants.NOTIFY_HOST);
                context.sendBroadcast(broadcast);
            } else if (Constants.LISTENER_END_PARTY.equals(action)) {
                Log.d(TAG, "Received notification that party has ended");
                Intent broadcast = new Intent(Constants.LISTENER_END_PARTY);
                context.sendBroadcast(broadcast);
            } else {
                Log.d(TAG, "Unhandled action via GCM: " + action);
            }
        }

        setResultCode(Activity.RESULT_OK);
    }
}
