package com.supercom.puretrack.data.service;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

public class ActivityRecognizedService extends IntentService {


    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getMostProbableActivity());
        }
    }

    private void handleDetectedActivities(DetectedActivity activity) {

        Log.i(getClass().getSimpleName(), "New activity recognition: " + getPortableActivityByName(activity.getType()) + " " + activity.getConfidence());

        LoggingUtil.fileLogActivityUpdate("\n***  [" + TimeUtil.getCurrentTimeStr() + "] "
                + "New activity recognition " + getPortableActivityByName(activity.getType()) + " " + activity.getConfidence() + "\n");

        if (activity.getType() != DetectedActivity.UNKNOWN) {
            publishResults(activity.getType(), activity.getConfidence());
        }
    }

    private void publishResults(int status, int confidence) {
        Intent intent = new Intent(LocationManager.LOCATION_HANDLER_ACTION_EXTRA);
        intent.putExtra(LocationManager.IS_ACTIVITY_RECOGNIZE_EXTRA, true);
        intent.putExtra(LocationManager.ACTIVITY_RECOGNIZE_STATUS_EXTRA, status);
        intent.putExtra(LocationManager.ACTIVITY_RECOGNIZE_CONFIDENCE_EXTRA, confidence);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static String getPortableActivityByName(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                return "IN_VEHICLE";
            }
            case DetectedActivity.ON_BICYCLE: {
                return "ON_BICYCLE";
            }
            case DetectedActivity.ON_FOOT: {
                return "ON_FOOT";
            }
            case DetectedActivity.RUNNING: {
                return "RUNNING";
            }
            case DetectedActivity.STILL: {
                return "STILL";
            }
            case DetectedActivity.TILTING: {
                return "TILTING";
            }
            case DetectedActivity.WALKING: {
                return "WALKING";
            }
            case DetectedActivity.UNKNOWN: {
                return "UNKNOWN";
            }
        }

        return "";
    }
}
