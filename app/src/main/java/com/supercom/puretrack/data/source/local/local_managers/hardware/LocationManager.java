package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.BatteryManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;
import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.broadcast_receiver.LocationPointReceiver;
import com.supercom.puretrack.data.repositories.KnoxProfileConfig;
import com.supercom.puretrack.data.service.ActivityRecognizedService;
import com.supercom.puretrack.data.service.LocationService;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.business_logic_models.network.communication_profile.ProfilingEventsConfig.PmComProfiles;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.LoggingUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@SuppressLint("NewApi")
public class LocationManager implements LocationListener,
        GpsStatus.Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String AVERAGE_GPS_PROVIDER = "AverageGpsProvider";
    private static final String AVERAGE_NETWORK_PROVIDER = "AverageNetworkProvider";
    private static boolean isDeviceInPurecomZoneState = false;
    public static final String LOCATION_HANDLER_ACTION_EXTRA = "locatioHandlerActionExtra";
    public static final String CHECK_BEST_LOCATION_EXTRA = "locatioHandlerCheckBestLocationExtraExtra";
    public static final String IS_ACTIVITY_RECOGNIZE_EXTRA = "isActivityRecognizeExtra";
    public static final String ACTIVITY_RECOGNIZE_STATUS_EXTRA = "activityRecognizeStatusExtra";
    public static final String ACTIVITY_RECOGNIZE_CONFIDENCE_EXTRA = "activityRecognizeConfidenceExtra";
    public static final String LOCATION_SERVICE_STARTED_STATUS_EXTRA = "isServiceStartedExtra";
    //public static boolean didGetGpsSignals = false;


    private static boolean isDeviceInAccelerometerMotion = true;

    private final String TAG = "LocationHandler";

    private static final long MIN_DISTANCE = TimeUnit.SECONDS.toMillis(0);

    private final Context context;

    private android.location.LocationManager locationManager;
    private int localBadGpsAccuracyCounter;
    public static int currentLocationTimeInterval = 60;
    private int localBadAttemptsToUploadPointCounter;
    private long currentLocationValidity;
    private String messageToDebugInfo;
    private final LocationHandlerListener locationHandlerListener;
    private final LocationPointReceiver locationPointReceiver = new LocationPointReceiver();

    private int satellitesNumber;


    private int localWeightedAverageCounter;
    private GoogleApiClient apiClient;
    private static int currentActivityRecognizeStatus = DetectedActivity.STILL;
    private static int activityRecognizeConfidence = 0;
    public static final float IN_VEHICLE_SPEED = (float) 6.0;
    public static final float ON_BICYCLE_SPEED = (float) 4.0;
    public static final float RUNNING_SPEED = (float) 2.0;
    public static final float WALKING_SPEED = (float) 1.0;
    public static int locationServiceInterval = 60;
    private int restartLocationUpdatesCounter = 0;

    public static int currentActivityRecognizeStatusX = DetectedActivity.STILL;

    private boolean lbsRequestRequired;

    public static boolean isInAverageMode = false;

    private enum LocationSmoothingType {
        None,
        SimpleMovingAverage,
        AdvancedAlgorithm
    }


    public static ArrayList<Location> locationsArray = new ArrayList<>();

    public boolean IsLbsRequestRequired() {
        return lbsRequestRequired;
    }

    public static LocationManager getInstance(){
        return _Instance;
    }
    private static LocationManager _Instance;

    public LocationManager(Context context, LocationHandlerListener locationHandlerListener) {
        this.context = context;
        this.locationHandlerListener = locationHandlerListener;
        isDeviceInPurecomZoneState = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFFENDER_IN_PURECOM_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_PURECOM_ZONE;
        init();
        _Instance=this;
    }

    public interface LocationHandlerListener {
        void onNewPointAddedToDB();
    }

    private void init() {
        lbsRequestRequired = false;
        locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        LocalBroadcastManager.getInstance(context).registerReceiver(locationHandlerLocalReceiver,
                new IntentFilter(LOCATION_HANDLER_ACTION_EXTRA));


        apiClient = new GoogleApiClient.Builder(context).addApi(ActivityRecognition.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        apiClient.connect();


        startLocationUpdate(true);
    }

    public static int getCurrentLocationTimeInterval() {
        return currentLocationTimeInterval;
    }

    public void startLocationUpdate(boolean startup) {
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (!isOffenderAllocated) return;
        printStartLocationLogs();
        currentLocationTimeInterval = (int) getCurrentGPSTimeInterval();
        // foreground location service interval
        locationServiceInterval = getLocationServiceInterval();
        if (!startup) {
            // LocationServiceJava - location updates (gps only)
            // not on started, maybe service is not running
            if (LocationService.getInstance() != null) {
                LocationService.getInstance().requestGpsLocationUpdates(locationServiceInterval);
            }
        }

        // LocationHandler - location updates (network only)
        requestLocationUpdates(currentLocationTimeInterval);
        locationPointReceiver.setAlaramClock(App.getContext(),
                getNextIntervalToInsertNewPointToDB(true), LocationPointReceiver.class, 16,
                LocationPointReceiver.START_LOCATION_CONST, null);


    }

    public void handlePureComZone(boolean atHome) {
        isDeviceInPurecomZoneState = atHome;
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates(long currentGPSTimeInterval) {
        stopLocationUpdate();

        App.writeToZoneLogsAndDebugInfo(TAG, "requestLocationUpdates, interval - " + currentGPSTimeInterval, DebugInfoModuleId.Zones);


        String locationType = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.LOCATION_TYPES);

        switch (locationType) {
            case "All":
                locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, TimeUnit.SECONDS.toMillis(currentGPSTimeInterval), MIN_DISTANCE, this);
                locationManager.addGpsStatusListener(this);
                break;
            case "GPS":
                break;
            case "Network":
                locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, TimeUnit.SECONDS.toMillis(currentGPSTimeInterval), MIN_DISTANCE, this);
                break;
        }
    }

    private void printStartLocationLogs() {
        boolean hasStartProfileEvent = TableEventsManager.sharedInstance().hasOpenEventInViolationCategory
                (ViolationCategoryTypes.START_PROFILE);
        if (!hasStartProfileEvent) {
            String messageToUpload = "";
            if (isLocatedInBeaconZoneAndHasNoOpenBeaconEvent()) {
                messageToUpload = "Located in beacon zone and has no open beacon event. will get location updates every " +
                        TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                                (OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL) + " seconds\n";
            } else {
                messageToUpload = "Not located in beacon zone or located in beacon zone and has open beacon event. "
                        + "will get location updates every " + TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                        (OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_INTERVAL) + " seconds\n";
            }

            Log.i(TAG, messageToUpload);
            LoggingUtil.fileLogZonesUpdate("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "] "
                    + messageToUpload);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
    }

    private final BroadcastReceiver locationHandlerLocalReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("bug7833","locationHandlerLocalReceiver");
/*            boolean locationActivityRequiredToBeMonitored = shouldCheckForLocationReceptionsForOpenEvent || shouldCheckForLocationReceptionsForCloseEvent;
            if (locationActivityRequiredToBeMonitored && intent != null) didGetGpsSignals = true;*/
            if (intent.getExtras().getString(LocationManager.CHECK_BEST_LOCATION_EXTRA) != null) {
                handleLocationPointReceiver();
            } else if (intent.getExtras().getBoolean(LocationManager.LOCATION_SERVICE_STARTED_STATUS_EXTRA)) {
                App.writeToZoneLogsAndDebugInfo(TAG, "LocationHandler - LOCATION_SERVICE_STARTED_STATUS_EXTRA - service started", DebugInfoModuleId.Zones);
                startLocationUpdate(false);
            } else if (intent.getExtras().getBoolean(LocationManager.IS_ACTIVITY_RECOGNIZE_EXTRA)) {
                int activityRecognizeStatus = intent.getExtras().getInt(LocationManager.ACTIVITY_RECOGNIZE_STATUS_EXTRA);
                activityRecognizeConfidence = intent.getExtras().getInt(LocationManager.ACTIVITY_RECOGNIZE_CONFIDENCE_EXTRA);

                LoggingUtil.fileLogActivityUpdate("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "] "
                        + "LocationHandler - activity update: " + activityRecognizeStatus + " " + activityRecognizeConfidence);

                if (activityRecognizeStatus == currentActivityRecognizeStatus) return;
                currentActivityRecognizeStatus = activityRecognizeStatus;

                String messageToUpload = "Activity Recognize changed. Status: " + ActivityRecognizedService.getPortableActivityByName(currentActivityRecognizeStatus) +
                        " Confidence: " + activityRecognizeConfidence;
                App.writeToZoneLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Zones);
            }
        }
    };

    private void handleLocationPointReceiver() {

        locationPointReceiver.setAlaramClock(App.getContext(), getNextIntervalToInsertNewPointToDB(false),
                LocationPointReceiver.class, 16, LocationPointReceiver.START_LOCATION_CONST, null);


        chooseBestLastPointInTimeIntervalAndInsertToDB(getCurrentGPSTimeInterval());
    }


    private long getNextIntervalToInsertNewPointToDB(boolean isFirstTime) {
        Calendar calendar = Calendar.getInstance();
        if (isFirstTime) {

            //f.e currentInterval - 30, current time: 11:20:20, will set to 11:21:00
            if (60 - calendar.get(Calendar.SECOND) >= (int) getCurrentGPSTimeInterval()) {
                calendar.add(Calendar.SECOND, 60 - calendar.get(Calendar.SECOND));
            }

            //f.e currentInterval - 30, current time: 11:20:42, will set to 11:21:30
            else {
                calendar.add(Calendar.SECOND, 60 - calendar.get(Calendar.SECOND) + (int) getCurrentGPSTimeInterval());
            }

        } else {
            calendar.add(Calendar.SECOND, (int) getCurrentGPSTimeInterval());
        }
        calendar.set(Calendar.MILLISECOND, 200);

        return calendar.getTimeInMillis();
    }


    private long getCurrentGPSTimeInterval() {
        int currentPmComProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);
        long minGPSTimeInterval;
        if (currentPmComProfile != -1) {
            PmComProfiles pmComProfile = TableEventsManager.sharedInstance().profilingEventsConfig.
                    getPmComProfileObjectByProfileId(currentPmComProfile);
            minGPSTimeInterval = pmComProfile.LocationInterval;
        } else {
            if (!isDeviceInAccelerometerMotion) {
                minGPSTimeInterval = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_INT_NO_MOTION);
            } else {
                boolean isLocatedInBeaconZoneAndHasNoOpenBeaconEvent = isLocatedInBeaconZoneAndHasNoOpenBeaconEvent();
                if (isLocatedInBeaconZoneAndHasNoOpenBeaconEvent) {
                    minGPSTimeInterval = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL);
                } else {
                    minGPSTimeInterval = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_INTERVAL);
                }
            }
        }

        return minGPSTimeInterval;
    }

    private boolean isLocatedInBeaconZoneAndHasNoOpenBeaconEvent() {
        boolean isInBeaconZone = TableOffenderStatusManager.sharedInstance()
                .getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
        boolean hasAnyOpenBeaconEventOpen = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_HAS_OPEN_EVENT) == 1;
        return isInBeaconZone && !hasAnyOpenBeaconEventOpen;
    }

    public void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationHandlerLocalReceiver);
        apiClient.disconnect();
    }

    public void stopLocationUpdate() {

        App.writeToZoneLogsAndDebugInfo(TAG, "stopLocationUpdate", DebugInfoModuleId.Zones);


        locationPointReceiver.CancelAlarm(context, LocationPointReceiver.class, 16, null);

        locationManager.removeUpdates(this);

        localBadGpsAccuracyCounter = 0;
        localBadAttemptsToUploadPointCounter = 0;

        int locationSmoothing = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.LOCATION_SMOOTHING);
        if ((apiClient.isConnected() && locationSmoothing == LocationSmoothingType.None.ordinal())) {

            App.writeToZoneLogsAndDebugInfo(TAG, "Activity recognition - Connection Suspended, Changed to normal location algorithm", DebugInfoModuleId.Zones);

            resetWeightAverageField();
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i("bug783","onLocationChanged Location "+location.getProvider()+":"+location.getLatitude()+","+location.getLongitude());

        Log.i("NETWORK_PROVIDER","LocationHandler - New Loc - " + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy());
        if (location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) return;

        currentActivityRecognizeStatusX = getMotionType(location.getSpeed());

        App.writeToZoneLogsAndDebugInfo(TAG, "LocationHandler - New Loc - " + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy(),
                DebugInfoModuleId.Zones);

        locationToTCopy=location;
        locationsArray.add(location);
    }

    Location locationToTCopy;
    public boolean addTestLocation() {
        if(locationToTCopy==null){
            return false;
        }
        double lat = 32.085879;//moshe home
        double lon = 34.8763807;
        Location l=new Location(locationToTCopy);
        l.setLatitude(lat);
        l.setLongitude(lon);
        l.setProvider(android.location.LocationManager.GPS_PROVIDER);
        locationsArray.add(l);
        insertNewLocationToDB(l);

        return true;
    }

    public void addTestLocations() {
         if (locationToTCopy == null) {
            Log.i("addTestLocations", "locationToTCopy== null");
            return;
        }

        double lat = 32.085879;//moshe home
        double lon = 34.8763807;

        for (int i = 0; i < 1000; i++) {
            lat += 0.001;
            lon += 0.001;

            Location l=new Location(locationToTCopy);
            l.setLatitude(lat);
            l.setLongitude(lon);
            l.setProvider(android.location.LocationManager.GPS_PROVIDER);
            locationsArray.add(l);
            insertNewLocationToDB(l);
        }
    }


    public boolean checkLbsLocationRequired() {
        boolean lbsLocationRequired = false;
        int lbsStartThreshold, lbsInterval;

        // Check if LBS is enabled
        if (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.LBS_ENABLE) == 0) {
            lbsRequestRequired = false;
            return false;
        }
        // get last non-LBS & LBS location times
        long lastLocationTime = TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(OFFENDER_STATUS_CONS.OFF_LAST_LOCATION_UTC_TIME);
        long lastLbsLocationTime = TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(OFFENDER_STATUS_CONS.OFF_LAST_LBS_LOCATION_UTC_TIME);
        long currTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        // fix times
        if (lastLbsLocationTime > currTime) {
            lastLbsLocationTime = 0;
        }
        if (lastLocationTime > currTime) {
            lastLocationTime = 0;
        }
        // get relevant configs
        if ((DatabaseAccess.getInstance().tableOpenEventsLog.getOffenderStatus() == TableEventConfig.ViolationSeverityTypes.NORMAL)) {
            // get non-violation configs
            lbsStartThreshold = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.START_LBS_THRESHOLD_NORMAL);
            lbsInterval = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.LBS_INTERVAL_NORMAL);
        } else {
            // get violation configs
            lbsStartThreshold = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.START_LBS_THRESHOLD_IN_VIOLATION);
            lbsInterval = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.LBS_INTERVAL_IN_VIOLATION);
        }

        // check if LBS need to be started, according to last GPS/network time
        if ((currTime - lastLocationTime) >= lbsStartThreshold) {
            // check if LBS first time OR interval has elapsed
            if ((lastLbsLocationTime == 0) || ((currTime - lastLbsLocationTime) >= lbsInterval)) {
                // mark LBS required for communication manager
                lbsRequestRequired = true;
                if (lastLbsLocationTime == 0) {
                    boolean hasOpenLbsStarted = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                            (ViolationCategoryTypes.LBS_REQUESTED) != -1;
                    // generate event if this is the start of LBS location
                    if (!hasOpenLbsStarted) {
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.lbsLocationRequested, -1, -1);
                    }
                }
                if(KnoxProfileConfig.getInstance().lbsLocationRequired()){
                    lbsLocationRequired = true;
                }
            }
        }
        return lbsLocationRequired;
    }


    public void chooseBestLastPointInTimeIntervalAndInsertToDB(long minGPSTimeInterval) {
        messageToDebugInfo = "";
        @SuppressLint("MissingPermission") Location lastGpsKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        @SuppressLint("MissingPermission") Location lastNetworkKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);

        App.writeToZoneLogsAndDebugInfo(TAG,
                "lastGpsKnownLocation - " + (lastGpsKnownLocation == null)
                + "lastNetworkKnownLocation - " + (lastNetworkKnownLocation == null)
                + "locationsArray - " + locationsArray.size(), DebugInfoModuleId.Zones);

        Location serviceLocation = getLocationServiceLocation();

        boolean hasGpsPointInLastMinTimeInterval = false;
        if (lastGpsKnownLocation != null && (System.currentTimeMillis() - lastGpsKnownLocation.getTime() <= TimeUnit.SECONDS.toMillis(minGPSTimeInterval))) {
            hasGpsPointInLastMinTimeInterval = true;
            String messageToUpload = "lastGpsKnownLocation: Lat = " + lastGpsKnownLocation.getLatitude() +
                    " Long = " + lastGpsKnownLocation.getLongitude() +
                    " Accuracy = " + lastGpsKnownLocation.getAccuracy() +
                    " provider " + lastGpsKnownLocation.getProvider() +
                    " Time " + TimeUtil.formatFromMiliSecondToString(lastGpsKnownLocation.getTime(), TimeUtil.SIMPLE);
            App.writeToZoneLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Zones);
        }

        boolean hasNetworkPointInLastMinTimeInterval = false;
        if (lastNetworkKnownLocation != null && (System.currentTimeMillis() - lastNetworkKnownLocation.getTime() <= TimeUnit.SECONDS.toMillis(minGPSTimeInterval))) {
            hasNetworkPointInLastMinTimeInterval = true;

            String messageToUpload = "lastNetworkKnownLocation: Lat = " + lastNetworkKnownLocation.getLatitude() +
                    " Long = " + lastNetworkKnownLocation.getLongitude() +
                    " Accuracy = " + lastNetworkKnownLocation.getAccuracy() +
                    " provider " + lastNetworkKnownLocation.getProvider() +
                    " Time " + TimeUtil.formatFromMiliSecondToString(lastNetworkKnownLocation.getTime(), TimeUtil.SIMPLE);
            App.writeToZoneLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Zones);
        }

        //No GPS or Network or Service available
        if (!hasGpsPointInLastMinTimeInterval && !hasNetworkPointInLastMinTimeInterval && serviceLocation == null) {

            // restart location updates request
            restartLocationUpdatesCounter++;
            if (restartLocationUpdatesCounter > 5) {
                restartLocationUpdatesCounter = 0;
                startLocationUpdate(false);
            }

            long badGpsAccuracyCounterFromServer = TableOffenderDetailsManager.sharedInstance().
                    getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER);

            if (!checkLbsLocationRequired()) {
                if (localBadGpsAccuracyCounter >= badGpsAccuracyCounterFromServer) {

                    messageToDebugInfo = "NO gps or network point in the last " + minGPSTimeInterval + " seconds";

                    handleLocationPoint(null);
                } else {

                    localBadGpsAccuracyCounter++;
                    localBadAttemptsToUploadPointCounter++;

                    messageToDebugInfo = "NO gps or network point in the last " + minGPSTimeInterval + " seconds" + ", "
                            + "localBadGpsAccuracyCounter " + localBadGpsAccuracyCounter +
                            ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                            ", Point didn't Upload.";
                    handleLogs();

                    EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
                    if (offenderLastGpsPoint != null) {
                        TableZonesManager.sharedInstance().checkZoneIntersection(offenderLastGpsPoint);
                    }
                }
            }

        }

        // service location available
        else if (serviceLocation != null) {
            App.writeToZoneLogsAndDebugInfo(TAG,
                    "handleLocationPoint: serviceLocation", DebugInfoModuleId.Zones);

            handleLocationPoint(serviceLocation);
        }


        //Only GPS available
        else if (!hasNetworkPointInLastMinTimeInterval) {

            messageToDebugInfo = "\nOnly gps point in the last " + minGPSTimeInterval + " seconds"
                    + ", Accuracy " + lastGpsKnownLocation.getAccuracy()
                    + ", SatellitesNumber: " + satellitesNumber + "Latitude - " + lastGpsKnownLocation.getLatitude() +
                    "Longitude - " + lastGpsKnownLocation.getLongitude();

            long minSatellitesNumber = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_SATELLITE_NUM);

            if (satellitesNumber >= minSatellitesNumber) {
                handleLocationPoint(lastGpsKnownLocation);
            } else {

                localBadGpsAccuracyCounter++;
                localBadAttemptsToUploadPointCounter++;

                messageToDebugInfo += ", localBadGpsAccuracyCounter " + localBadGpsAccuracyCounter +
                        ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                        ", Point didn't Upload.";
                handleLogs();

            }
        }

        //Only Network available
        else if (!hasGpsPointInLastMinTimeInterval) {
            messageToDebugInfo = "\nOnly network point in the last " + minGPSTimeInterval + " seconds"
                    + ", Accuracy " + lastNetworkKnownLocation.getAccuracy() + "Latitude - " + lastNetworkKnownLocation.getLatitude() +
                    "Longitude - " + lastNetworkKnownLocation.getLongitude();

            handleLocationPoint(lastNetworkKnownLocation);
        }

        //GPS and network available
        else {

            long minSatellitesNumber = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_SATELLITE_NUM);

            //if GPS lower than network and more than MIN_SATELLITES_NUMBER
            if (lastGpsKnownLocation.getAccuracy() < lastNetworkKnownLocation.getAccuracy() &&
                    (satellitesNumber >= minSatellitesNumber)) {

                messageToDebugInfo = "Current gps point " + lastGpsKnownLocation.getAccuracy()
                        + " better than last network point " + lastNetworkKnownLocation.getAccuracy()
                        + ", SatellitesNumber: " + satellitesNumber;

                handleLocationPoint(lastGpsKnownLocation);
            }

            //if network lower than GPS or GPS has less than MIN_SATELLITES_NUMBER
            else {
                messageToDebugInfo = "Current network point " + lastNetworkKnownLocation.getAccuracy()
                        + " better than last gps point " + lastGpsKnownLocation.getAccuracy() +
                        ". Number of gps satellites is " + satellitesNumber;

                handleLocationPoint(lastNetworkKnownLocation);
            }
        }
        sendGpsSignalEventIfNeeded();
        satellitesNumber = 0;
    }

    private void sendGpsSignalEventIfNeeded() {
        long badGpsAccuracyCounterFromServer = TableOffenderDetailsManager.sharedInstance().
                getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER);

        boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.GPS_SIGNAL) != -1;

        if (localBadAttemptsToUploadPointCounter == 0 && hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.offenderLocationRestored, -1, -1);
        } else if (localBadAttemptsToUploadPointCounter > badGpsAccuracyCounterFromServer && !hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.offenderLocationUnavailable, -1, -1);
        }
    }

    private void handleLocationPoint(Location currentLocation) {
        long goodPointThreshold = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_GOOD_POINT_THRESHOLD);
        long badPointThreshold = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BAD_POINT_THRESHOLD);
        long badGpsAccuracyCounterFromServer = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER);
        Log.i("bug783","handleLocationPoint "+currentLocation.getProvider()+":"+currentLocation.getLatitude()+","+currentLocation.getLongitude());

        EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();

        if (localBadGpsAccuracyCounter < badGpsAccuracyCounterFromServer) {

            //first time after activation or no pointed inserted
            Log.i("bug783","first time after "+currentLocation.getProvider()+":"+currentLocation.getLatitude()+","+currentLocation.getLongitude());
            if (offenderLastGpsPoint == null) {

                messageToDebugInfo += ", First time after activation";

                //first we want to upload to PM, even if its higher than good point threshold
                if (currentLocation != null && currentLocation.getAccuracy() < badPointThreshold) {
                    messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                            ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                            ", Current accuracy lower than " + badPointThreshold +
                            ", Good speed";
                    Log.i("bug783","handlePointToUpload "+currentLocation.getProvider()+":"+currentLocation.getLatitude()+","+currentLocation.getLongitude());
                    handlePointToUpload(currentLocation);
                } else {
                    Log.i("bug783","localBadGpsAccuracy "+currentLocation.getProvider()+":"+currentLocation.getLatitude()+","+currentLocation.getLongitude());
                    localBadGpsAccuracyCounter++;
                    localBadAttemptsToUploadPointCounter++;

                    messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                            ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                            ", Current accuracy higher than " + badPointThreshold + ", Point didn't Upload.";
                    handleLogs();
                }
            } else if (currentLocation.getAccuracy() < goodPointThreshold) {
                if (isPassedSpeedTest(currentLocation)) {
                    messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                            ", Current accuracy lower than " + goodPointThreshold + ", Good speed";
                    handlePointToUpload(currentLocation);
                } else {
                    localBadGpsAccuracyCounter++;
                    localBadAttemptsToUploadPointCounter++;
                    messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                            ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                            ", Current accuracy lower than " + goodPointThreshold + ", Bad speed, Point didn't upload.";
                    handleLogs();
                }
            } else {
                Log.i("bug783","smallestAccuracy "+currentLocation.getProvider()+":"+currentLocation.getLatitude()+","+currentLocation.getLongitude());

                EntityGpsPoint smallestAccuracyPointAndAboveGoodThreshold = TableOffenderStatusManager.sharedInstance().
                        getSmallestAccuracyPointAndAboveGoodThreshold();

				/* if no smallestAccuracyPointAndAboveGoodThreshold exists in DB or accuracy of current location less than the
				   field from local DB */
                if (smallestAccuracyPointAndAboveGoodThreshold == null ||
                        currentLocation.getAccuracy() < smallestAccuracyPointAndAboveGoodThreshold.accuracy) {
                    if (isPassedSpeedTest(currentLocation)) {
                        insertNewSmallestAccuracyPointAndAboveGoodThresholdToDB(currentLocation);
                    }
                }

                localBadGpsAccuracyCounter++;
                localBadAttemptsToUploadPointCounter++;

                messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                        ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                        ", Current accuracy higher than " + goodPointThreshold + ", Point didn't Upload.";
                handleLogs();
            }
        } else {

            EntityGpsPoint smallestAccuracyPointAndAboveGoodThreshold = TableOffenderStatusManager.sharedInstance().
                    getSmallestAccuracyPointAndAboveGoodThreshold();

            String smallestAccuracyFromDBMessage =
                    (smallestAccuracyPointAndAboveGoodThreshold == null ? "- " : String.valueOf(smallestAccuracyPointAndAboveGoodThreshold.accuracy));

            //choose between current location or location from local DB(depends on smallest accuracy), and set to locationWithSmallestAccuracy
            Location locationWithSmallestAccuracy = null;
            if (currentLocation == null || (smallestAccuracyPointAndAboveGoodThreshold != null &&
                    smallestAccuracyPointAndAboveGoodThreshold.accuracy < currentLocation.getAccuracy())) {
                locationWithSmallestAccuracy = convertRecordGpsPointToLocationObject(smallestAccuracyPointAndAboveGoodThreshold);
            } else if (isPassedSpeedTest(currentLocation)) {

                locationWithSmallestAccuracy = currentLocation;
                insertNewSmallestAccuracyPointAndAboveGoodThresholdToDB(locationWithSmallestAccuracy);

                smallestAccuracyFromDBMessage = String.valueOf(locationWithSmallestAccuracy.getAccuracy());
            }

            localBadGpsAccuracyCounter++;

            //if location with smallest accuracy exists, and less than badPointThreshold

            if (locationWithSmallestAccuracy != null && locationWithSmallestAccuracy.getAccuracy() < badPointThreshold) {
                messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                        ", Accuracy lower than " + badPointThreshold + ", Smallest accuracy from local DB " + smallestAccuracyFromDBMessage;

                handlePointToUpload(locationWithSmallestAccuracy);
            } else {

                localBadAttemptsToUploadPointCounter++;

                messageToDebugInfo += ", localBadGpsAccuracyCounter: " + localBadGpsAccuracyCounter +
                        ", localBadAttemptsToUploadPointCounter: " + localBadAttemptsToUploadPointCounter +
                        ", Accuracy higher than " + badPointThreshold
                        + ", Smallest accuracy from local DB " + smallestAccuracyFromDBMessage +
                        ", Point didn't Upload.";
                handleLogs();
            }
        }
    }


    private void insertNewSmallestAccuracyPointAndAboveGoodThresholdToDB(Location currentLocation) {
        BatteryManager batteryManager = (BatteryManager) App.getAppContext().getSystemService(Context.BATTERY_SERVICE);
        int isDeviceChargingAsInt = 0;
        if (batteryManager.isCharging()) {
            isDeviceChargingAsInt = 1;
        }
        EntityGpsPoint tempRecordGpsPoint = new EntityGpsPoint(
                DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId,
                currentLocation.getTime(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                currentLocation.getAltitude(),
                currentLocation.getAccuracy(),
                getLocationSatellitesNumber(currentLocation),
                getProviderType(currentLocation.getProvider()),
                EntityGpsPoint.INITIAL_SYNC_RETRY_COUNT,
                DeviceStateManager.getInstance().getIsMobileDataAvaliable(),
                currentLocation.getSpeed(),
                currentLocation.getBearing(),
                isMockLocation(currentLocation),
                currentActivityRecognizeStatusX,
                isDeviceChargingAsInt,
                AccelerometerManager.getInstance().getIsDeviceLayingFlatAsInt(),
                AccelerometerManager.getInstance().getLatestAccelerometerValues());

        Gson gson = new Gson();
        String smallestAccuracyPointAndAboveGoodThresholdJsonStr = gson.toJson(tempRecordGpsPoint);
        TableOffenderStatusManager.sharedInstance().updateColumnString(OFFENDER_STATUS_CONS.OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD, smallestAccuracyPointAndAboveGoodThresholdJsonStr);
    }

    public static int isMockLocation(Location location) {
        return location.isFromMockProvider() ? 1 : 0;
    }

    public static double Mock_location_lat;
    public static double Mock_location_lon;

    public static int getMotionType() {
        return currentActivityRecognizeStatusX;
    }

    public static int getDeviceActivity() {
        return currentActivityRecognizeStatus;
    }

    public static int getDeviceActivityConfidence() {
        return activityRecognizeConfidence;
    }


    private Location convertRecordGpsPointToLocationObject(EntityGpsPoint gpsPoint) {
        if (gpsPoint != null) {
            String provider = getProviderString(gpsPoint.providerType);
            Location location = new Location(provider);
            location.setAccuracy(gpsPoint.accuracy);
            location.setAltitude(gpsPoint.altitude);
            location.setLatitude(gpsPoint.latitude);
            location.setLongitude(gpsPoint.longitude);
            location.setSpeed((float) gpsPoint.speed);
            location.setTime(gpsPoint.time);
            satellitesNumber = gpsPoint.satellitesNumber;

            return location;
        }
        return null;
    }

    private boolean isPassedSpeedTest(Location location) {
        Log.i("bug783","");
        Log.i("bug783","-----------------");
        Log.i("bug783","isPassedSpeedTest");
        Log.i("bug783","Location "+location.getProvider()+":"+location.getLatitude()+","+location.getLongitude());

        boolean isPassedSpeedTest;
        long allowedSpeed = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_ALLOWED_SPEED);
        long locationValidity = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_LOCATION_VALIDITY);
        float speedBetweenCurrentAndLastLocation = getSpeedBetweenCurrentAndLastLocation(location, allowedSpeed);

        Log.i("bug783","speedBetweenCurrentAndLastLocation:"+speedBetweenCurrentAndLastLocation);
        Log.i("bug783","allowedSpeed:"+allowedSpeed);
        Log.i("bug783","currentLocationValidity:"+currentLocationValidity);
        Log.i("bug783","locationValidity:"+locationValidity);

        if (speedBetweenCurrentAndLastLocation > allowedSpeed && speedBetweenCurrentAndLastLocation != -1) {
            if (currentLocationValidity < locationValidity) {
                Log.i("bug783","currentLocationValidity < locationValidity");
                messageToDebugInfo += ", CurrentLocationValidity: " + currentLocationValidity +
                        ", LocationValidityFromServer: " + locationValidity /* + ", Bad point not uploaded" */;
                handleLogs();
                isPassedSpeedTest = false;
            } else {
                messageToDebugInfo += "\nCurrentLocationValidity: " + currentLocationValidity +
                        ", LocationValidityFromServer: " + locationValidity /* + ", Bad point uploaded as good point !" */;
                currentLocationValidity = 0;
                Log.i("bug783","currentLocationValidity set to "+currentLocationValidity);
                isPassedSpeedTest = true;
            }

            currentLocationValidity++;
            Log.i("bug783","currentLocationValidity set to "+currentLocationValidity);
        } else {
            Log.i("bug783","simple option");
            currentLocationValidity = 0;
            isPassedSpeedTest = true;
        }

        Log.i("bug783","return "+isPassedSpeedTest);
        Log.i("bug783","-----------------");
        return isPassedSpeedTest;
    }

    /**
     * Calculate the speed between current location to last location
     *
     * @param location current location
     * @return speed between locations, and -1 if no last location exists in DB
     */
    private float getSpeedBetweenCurrentAndLastLocation(Location location, long allowedSpeed) {
        Location lastLocationThatUploaded = getLastLocationThatUploaded();
        float speed = -1;
        if (lastLocationThatUploaded != null) {
            float distanceInKilometer = lastLocationThatUploaded.distanceTo(location) / 1000;
            float timeBetweenPoints = TimeUnit.MILLISECONDS.toSeconds(location.getTime() - lastLocationThatUploaded.getTime())
                    / (float) 60 / (float) 60;
            speed = distanceInKilometer / timeBetweenPoints;

            messageToDebugInfo += ", Speed between points: " + speed + ", Allowed speed: " + allowedSpeed + ", distanceInKilometer: " +
                    distanceInKilometer + ", TimeBetweenPoints: " + timeBetweenPoints + "\nCurrentTime: " + TimeUtil.getCurrentTimeStr();
        }
        return speed;
    }

    private Location getLastLocationThatUploaded() {
        EntityGpsPoint lastPointThatUploaded = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
        Location lastLocation = null;
        if (lastPointThatUploaded != null) {
            lastLocation = new Location("");
            lastLocation.setLatitude(lastPointThatUploaded.latitude);
            lastLocation.setLongitude(lastPointThatUploaded.longitude);
            lastLocation.setTime(lastPointThatUploaded.time);
        }
        return lastLocation;
    }


    public void handleNewLbsLocation(EntityGpsPoint lbsRec) {
        // update off id
        lbsRec.offenderId = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId;
        // LBS updated by communication, request not required anymore
        lbsRequestRequired = false;
        // insert ot database
        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_GPS_POINTS, lbsRec);
        // update last LBS location time (for LBS use)
        TableOffenderStatusManager.sharedInstance().updateColumnLong(OFFENDER_STATUS_CONS.OFF_LAST_LBS_LOCATION_UTC_TIME, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        // schedule / zone logic
        TableZonesManager.sharedInstance().checkZoneIntersection(lbsRec);
    }


    private void handlePointToUpload(Location location) {
        Log.i("bug783","handlePointToUpload "+location.getProvider()+":"+location.getLatitude()+","+location.getLongitude());

        //if inside suspend schedule, don't save locations
        if (MainActivity.isOffenderInSuspendSchedule || isDeviceInPurecomZoneState) return;

        //insert original location
        EntityGpsPoint recordGpsPoint = insertNewLocationToDB(location);

        Gson gson = new Gson();
        String recordGpsPointJsonStr = gson.toJson(recordGpsPoint);
        TableOffenderStatusManager.sharedInstance().updateColumnString(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_LAST_GPS_POINT, recordGpsPointJsonStr);

        messageToDebugInfo += ", localWeightedAverage: " + localWeightedAverageCounter + ", Point uploaded !";
        handleLogs();

        TableOffenderStatusManager.sharedInstance().updateColumnString(OFFENDER_STATUS_CONS.OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD, "");
        long goodPointThreshold = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_GOOD_POINT_THRESHOLD);
        if (location.getAccuracy() < goodPointThreshold) {
            localBadGpsAccuracyCounter = 0;
        }
        TableZonesManager.sharedInstance().checkZoneIntersection(recordGpsPoint);

        if (locationHandlerListener != null) {
            locationHandlerListener.onNewPointAddedToDB();
        }

        localBadAttemptsToUploadPointCounter = 0;
        sendGpsFraudLocationIfNeeded(recordGpsPoint);
    }

    private void sendGpsFraudLocationIfNeeded(EntityGpsPoint lastGpsRecByRowId) {
        boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.GPS_FRAUD_LOCATION) != -1;
        if (lastGpsRecByRowId.isMockLocation == 0 && hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.gpsFraudLocationClosed, -1, -1);
        } else if (lastGpsRecByRowId.isMockLocation == 1 && !hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.gpsFraudLocation, -1, -1);
        }
    }

    private Location getLocationServiceLocation() {
        Log.i("bug7833","getLocationServiceLocation" );
        Location location = null;
        int serviceCalculationType = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.LOCATION_SERVICE_CALC_TYPE);
        int serviceAverageTime = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.LOCATION_AVERAGE_TIME_FRAME);
        long goodPointThreshold = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_GOOD_POINT_THRESHOLD);

        int serviceInterval = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.LOCATION_SERVICE_INTERVAL);
        if (serviceInterval <= 0) {
            return null;
        }

        App.writeToZoneLogsAndDebugInfo(TAG,
                "get Location Service Location:  calc: " + serviceCalculationType + " size: " + locationsArray.size(), DebugInfoModuleId.Zones);

        if (locationsArray.size() == 0) {
            return null;
        }

        if (serviceCalculationType == 1) {
            //**********************************************
            // Type 1. get best location from service locations list
            //**********************************************
            float bestAccuracy = 10000;
            boolean bestLocFound = false;
            for (Location mojLoc : locationsArray) {
                if ((mojLoc.getAccuracy() < goodPointThreshold) && (mojLoc.getAccuracy() < bestAccuracy)) {
                    bestAccuracy = mojLoc.getAccuracy();
                    location = mojLoc;
                    bestLocFound = true;
                }
            }
            if (bestLocFound) {
                App.writeToZoneLogsAndDebugInfo(TAG,
                        "Best service locations: " + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy(), DebugInfoModuleId.Zones);
            }
        } else if (serviceCalculationType == 2) {
            //**********************************************
            // Type 2. get location average from service locations list
            //**********************************************
            ArrayList<Location> avarageLocationArray = new ArrayList<Location>();
            Location avrLocation = null;
            int avgSearchCycles = 0;
            long toTime = System.currentTimeMillis();
            double averageLong = 0, averageLat = 0, averageAccuracy = 0;
            boolean averageDataFound = false;
            while ((!averageDataFound) &&
                    (avgSearchCycles < 3) &&
                    (System.currentTimeMillis() - toTime) < TimeUnit.SECONDS.toMillis(getCurrentGPSTimeInterval())) {
                for (Location mojLoc : locationsArray) {
                    Log.i("bug7833","check location "+mojLoc.getProvider()+":"+mojLoc.getLatitude()+","+mojLoc.getLongitude());
                    // last minute
                    if ((toTime - mojLoc.getTime()) <= (serviceAverageTime * 1000)) {
                        if ((int) mojLoc.getAccuracy() < goodPointThreshold) {
                            avarageLocationArray.add(mojLoc);
                            Log.i("bug7833","add to avarageLocationArray "+mojLoc.getProvider()+":"+mojLoc.getLatitude()+","+mojLoc.getLongitude());

                            averageDataFound = true;
                        }
                    }
                }
                App.writeToZoneLogsAndDebugInfo(TAG,
                        "Average service locations: cycle: " + avgSearchCycles + ", found: " + averageDataFound +
                                ", avg time" + toTime / 1000 + ", avg size: " + avarageLocationArray.size(), DebugInfoModuleId.Zones);
                // subtract 10 seconds from time, in order to find older locations for average
                toTime -= 20000;
                avgSearchCycles++;
            }

            if (avarageLocationArray.size() > 0) {
                for (Location loc : avarageLocationArray) {
                    if (avrLocation == null) {
                        avrLocation = loc;
                    }
                    averageLong += loc.getLongitude();
                    averageLat += loc.getLatitude();
                    averageAccuracy += loc.getAccuracy();

                    Log.i("bug7833","add to final location "+loc.getProvider()+":"+loc.getLatitude()+","+loc.getLongitude());

                }
                averageLong /= avarageLocationArray.size();
                averageLat /= avarageLocationArray.size();
                averageAccuracy /= avarageLocationArray.size();

                App.writeToZoneLogsAndDebugInfo(TAG,
                        "Average service locations: " + averageLat + "," + averageLong + "," + averageAccuracy, DebugInfoModuleId.Zones);

                try {
                    avrLocation.setLongitude(averageLong);
                    avrLocation.setLatitude(averageLat);
                    avrLocation.setAccuracy((float) averageAccuracy);
                } catch (Exception exp) {
                    System.out.println(exp.toString());
                    App.writeToZoneLogsAndDebugInfo(TAG,
                            "Failed to set Average service locations", DebugInfoModuleId.Zones);
                }
                location = avrLocation;
            }
        }

        // clear location average array (serviceX)
        locationsArray.clear();
        locationsArray = new ArrayList<>();
        return location;
    }


    @SuppressLint("NewApi")
    private EntityGpsPoint insertNewLocationToDB(Location location) {
        BatteryManager batteryManager = (BatteryManager) App.getAppContext().getSystemService(Context.BATTERY_SERVICE);
        int isDeviceChargingAsInt = 0;
        if (batteryManager.isCharging()) {
            isDeviceChargingAsInt = 1;
        }
        EntityGpsPoint recordGpsPoint = new EntityGpsPoint(
                DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId,
                location.getTime(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getAccuracy(),
                getLocationSatellitesNumber(location),
                getProviderType(location.getProvider()),
                EntityGpsPoint.INITIAL_SYNC_RETRY_COUNT,
                DeviceStateManager.getInstance().getIsMobileDataAvaliable(),
                location.getSpeed(),
                location.getBearing(),
                isMockLocation(location),
                currentActivityRecognizeStatusX,
                isDeviceChargingAsInt,
                AccelerometerManager.getInstance().getIsDeviceLayingFlatAsInt(),
                AccelerometerManager.getInstance().getLatestAccelerometerValues());
        MainActivity.playLocationDBTone = true;
        recordGpsPoint.id= (int)DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_GPS_POINTS, recordGpsPoint);

        // handle "stopped LBS" event
        boolean hasOpenLbsStarted = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.LBS_REQUESTED) != -1;
        if (hasOpenLbsStarted) {
            // close "LBS started" event
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.lbsLocationStopRequesting, -1, -1);
        }
        lbsRequestRequired = false;
        // update last non-LBS location time (for LBS use)
        TableOffenderStatusManager.sharedInstance().updateColumnLong(OFFENDER_STATUS_CONS.OFF_LAST_LOCATION_UTC_TIME, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        // Reset last LBS location time (for LBS use)
        TableOffenderStatusManager.sharedInstance().updateColumnLong(OFFENDER_STATUS_CONS.OFF_LAST_LBS_LOCATION_UTC_TIME, 0);

        return recordGpsPoint;
    }

    private void resetWeightAverageField() {
        localWeightedAverageCounter = 0;
    }

    private int getLocationSatellitesNumber(Location location) {
        int satellitesInFix = 0;
        for (GpsSatellite satellite : locationManager.getGpsStatus(null).getSatellites()) {
            if (satellite.usedInFix()) {
                satellitesInFix++;
            }
        }
        if (location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
            return satellitesInFix;
        } else {
            return 0;
        }
    }

    private void handleLogs() {
        Log.i(TAG, messageToDebugInfo);
        LoggingUtil.fileLogZonesUpdate("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] "
                + "\n" + messageToDebugInfo);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToDebugInfo,
                DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public static int getProviderType(String provider) {
        int providerType = 0;
        switch (provider) {
            case android.location.LocationManager.GPS_PROVIDER:
                providerType = 1;
                break;
            case android.location.LocationManager.NETWORK_PROVIDER:
                providerType = 2;
                if (isInAverageMode) {
                    providerType = 1;
                }
                break;
            case AVERAGE_GPS_PROVIDER:
                providerType = 4;
                if (isInAverageMode) {
                    providerType = 2;
                }
                break;
            case AVERAGE_NETWORK_PROVIDER:
                providerType = 5;
                if (isInAverageMode) {
                    providerType = 2;
                }
                break;
        }
        return providerType;
    }

    private String getProviderString(int type) {
        String provider = "";
        if (type == 1) {
            provider = android.location.LocationManager.GPS_PROVIDER;
        } else if (type == 2) {
            provider = android.location.LocationManager.NETWORK_PROVIDER;
        } else if (type == 4) {
            provider = AVERAGE_GPS_PROVIDER;
        } else if (type == 5) {
            provider = AVERAGE_NETWORK_PROVIDER;
        }
        return provider;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public void onGpsStatusChanged(int event) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(context, ActivityRecognizedService.class);
        PendingIntent activityRecognizePendingIntent = PendingIntent.getService(context, 20, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, 8000, activityRecognizePendingIntent);

        LoggingUtil.fileLogActivityUpdate("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "] "
                + "LocationHandler - onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        App.writeToZoneLogsAndDebugInfo(TAG, "Activity recognition - Connection Suspended, Changed to normal location algorithm", DebugInfoModuleId.Zones);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        App.writeToZoneLogsAndDebugInfo(TAG, "Activity recognition - Connection failed", DebugInfoModuleId.Zones);
    }


    public static int getMotionType(float speed) {
        if (speed > IN_VEHICLE_SPEED) {
            return DetectedActivity.IN_VEHICLE;
        } else if ((speed > ON_BICYCLE_SPEED) && (speed <= IN_VEHICLE_SPEED)) {
            return DetectedActivity.ON_BICYCLE;
        } else if ((speed > RUNNING_SPEED) && (speed <= ON_BICYCLE_SPEED)) {
            return DetectedActivity.RUNNING;
        } else if ((speed > WALKING_SPEED) && (speed <= RUNNING_SPEED)) {
            return DetectedActivity.WALKING;
        } else {
            return DetectedActivity.STILL;
        }
    }

    public static int getLocationServiceInterval() {
        int Multiplier = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.LOCATION_SERVICE_INTERVAL);
        int userInterval = getCurrentLocationTimeInterval();
        return userInterval / Multiplier;
    }

    public void setMotionState(boolean isMotion, boolean stateChanged) {
        isDeviceInAccelerometerMotion = isMotion;
        if (isMotion) {
            // remove "location to duplicate" - which is used only in Static state
        }
        if (stateChanged) {
            String msgDebugInfo = "Motion state: " + ((isMotion) ? "motion" : "static");
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), msgDebugInfo,
                    DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
    }

    public static boolean getMotionState() {
        return isDeviceInAccelerometerMotion;
    }

    public static boolean isDeviceInPureComZoneState() {
        return isDeviceInPurecomZoneState;
    }


}
