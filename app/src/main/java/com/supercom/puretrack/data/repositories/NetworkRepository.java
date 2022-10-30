package com.supercom.puretrack.data.repositories;


import static com.supercom.puretrack.data.source.local.table.TableScannerType.NORMAL_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MAC_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MANUFACTURER_ID;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.MAX_GPS_POINTS_PER_CHUNK;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.REQUEST_RESULT_ERR;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.REQUEST_RESULT_OK;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.TIME_TO_START_CYCLE;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.screenLockX;
import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.broadcast_receiver.NetworkCycleReceiver;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager.ChargingState;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager.DEVICE_BATTERY_CONS;
import com.supercom.puretrack.data.source.local.local_managers.hardware.CellularInfoManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventTypes;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableDeviceInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.HandleRequestToBeSentToServerData;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager;
import com.supercom.puretrack.data.source.remote.NetworkResponseListener;
import com.supercom.puretrack.data.source.remote.parsers.CreateDeviceResultParser;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.data.source.remote.parsers.GetAuthenticationTokenResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetDateTimeListenerResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetDeviceConfigurationResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetDeviceConfigurationResultParser.DeviceConfigurationType;
import com.supercom.puretrack.data.source.remote.parsers.GetLbsLocationResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetOffenderInformationResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetOffenderRequestsResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetOffenderZonesResultParser;
import com.supercom.puretrack.data.source.remote.parsers.GetScheduleOfZoneListener;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceCommunictionsResultParser;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceDebugInfoResultParser;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceEventsResultParser;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceInfoResultParser;
import com.supercom.puretrack.data.source.remote.parsers.InsertOffenderLocationsResultParser;
import com.supercom.puretrack.data.source.remote.parsers.PostHandleOffenderRequestsResultParser;
import com.supercom.puretrack.data.source.remote.parsers.PostOnDemandPhotoResultParser;
import com.supercom.puretrack.data.source.remote.parsers.PostTerminateTokenResultParser;
import com.supercom.puretrack.data.source.remote.requests.CreateDeviceRequest;
import com.supercom.puretrack.data.source.remote.requests.GetAuthenticationTokenRequest;
import com.supercom.puretrack.data.source.remote.requests.GetDateTimeRequest;
import com.supercom.puretrack.data.source.remote.requests.GetDeviceConfigurationRequest;
import com.supercom.puretrack.data.source.remote.requests.GetLbsLocationRequest;
import com.supercom.puretrack.data.source.remote.requests.GetOffenderInformationRequest;
import com.supercom.puretrack.data.source.remote.requests.GetOffenderRequests;
import com.supercom.puretrack.data.source.remote.requests.GetOffenderScheduleOfZoneRequest;
import com.supercom.puretrack.data.source.remote.requests.GetOffenderZonesRequest;
import com.supercom.puretrack.data.source.remote.requests.InsertDeviceCommunicationsRequest;
import com.supercom.puretrack.data.source.remote.requests.InsertDeviceDebugInfoRequest;
import com.supercom.puretrack.data.source.remote.requests.InsertDeviceEventsRequest;
import com.supercom.puretrack.data.source.remote.requests.InsertDeviceInfoRequest;
import com.supercom.puretrack.data.source.remote.requests.InsertOffenderLocationsRequest;
import com.supercom.puretrack.data.source.remote.requests.PostHandleOffenderRequest;
import com.supercom.puretrack.data.source.remote.requests.PostOnDemandPhotoRequest;
import com.supercom.puretrack.data.source.remote.requests.PostTerminateRequest;
import com.supercom.puretrack.data.source.remote.requests.TestConnectionRequest;
import com.supercom.puretrack.data.source.remote.requests_listeners.TestConnectionListener;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.communication_profile.ProfilingEventsConfig.PmComProfiles;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.CommunicationInterval;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.FlowType;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.OffenderRequestType;
import com.supercom.puretrack.model.database.entities.EntityCallLog;
import com.supercom.puretrack.model.database.entities.EntityDebugInfo;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoCellular;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoDetails;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoStatus;
import com.supercom.puretrack.model.database.entities.EntityEventLog;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.model.database.entities.EntityOffenderPhoto;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.ui_models.FlightModeData;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.ui.dialog.SettingFragmentViewListener;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.encryption.ScramblingTextUtils;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class NetworkRepository implements NetworkResponseListener {
    public static final String TAG = "NetworkRepository";

    //Class Variables - int
    private int DeviceInfoCycles = 10;
    private int DeviceInfoCyclesCount = 0;

    //Class Variables - long
    private long lastCycleStartTime;
    private long lastTimeWhenIdleStateChanged = 0;

    //Class Variables - booleans
    private boolean isIdle;
    private boolean lbsSentInCycle;
    private boolean isInScheduleCycle;
    private boolean shouldRestartAppInTheEndOfTheCycle;
    private boolean isServerApprovedCommIntervalChange = true;

    //Class Variables - String
    private String TOKEN_KEY = "";

    //Class Variables - callbacks
    private ViewUpdateListener updateActivityListener;

    //Class Variables - Objects
    private CommunicationInterval currentCommInterval;
    private final FlightModeData flightModeData = new FlightModeData();
    private ArrayList<HandleRequestToBeSentToServerData> tempFailedHandleOffenderRequestsArray;
    private final NetworkCycleReceiver networkManagerReceiver = new NetworkCycleReceiver();

    public CommunicationInterval getCurrentCommInterval() {
        return currentCommInterval;
    }


    public void setDeviceInfoCycles(int deviceInfoCyclesConfig) {
        DeviceInfoCycles = deviceInfoCyclesConfig;
    }

    public String getServerUrl() {
        // add encrypt decrypt server url
        String serverUrlEnc = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_URL);
        final String AESkeyBytes = "A31380F07B001DFAE38F76EF75B89528";
        String serverUrl = "";
        if (serverUrlEnc != null && !serverUrlEnc.equals("")) {
            try {
                serverUrl = AESUtils.decrypt(AESkeyBytes, serverUrlEnc);
                return serverUrl;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return serverUrlEnc;
    }

    public String getServerPassword() {

        // add encrypt decrypt server password
        String serverPasswordEncAndScrambled = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_PASS);
        String serverPassword = "";
        if (serverPasswordEncAndScrambled != null && !serverPasswordEncAndScrambled.equals("") && !serverPasswordEncAndScrambled.equals("100000")) {
            try {
                serverPassword = AESUtils.decrypt(SERVER_URL_AES_KEY_BYTES, TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_PASS));
                serverPassword = ScramblingTextUtils.unscramble(serverPassword);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
        if (!serverPassword.equals(""))
            return (serverPassword);
        else
            return "100000";
    }


    public void runNetworkRequest(NetworkRequestName requestName) {
        switch (requestName) {

            case RegularFlow:
                break;

            case GetAuthentication:
                httpGetAuthentication();
                break;
            case DeviceInfo:
                sendInsertDeviceInfo();
                break;
            case GpsLocations:
                sendNewGpsPoints();
                break;
            case DeviceEvents:
                sendNewEventArray();
                break;
            case CallLog:
                sendInsertDeviceCommunications();
                break;
            case DebugInfo:
                sendInsertDeviceDebugInfo();
                break;
            case OffenderRequest:
                getOffenderRequest(true);
                break;
            case Handle:
                sendHandleOffenderRequest();
                break;
            case PostTerminate:
                httpTerminateToken();
                break;
        }
    }



    public static String getDeviceSerialNumber() {
        return DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber();
    }

    //Will be called at the end of Session Termination.
    public void initForNewCycle() {
        LoggingUtil.updateNetworkLog("\ninitForNewCicle" + "\n", false);
        lbsSentInCycle = false;
        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.IDLE_STAGE);
        setTOKEN_KEY("");
        NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED = 0;
        SyncRequestsRepository.getInstance().initSyncReqManagerForNewCycle();
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.IS_CYCLE_FINISHED_SUCCESSFULY, 1);
    }

    private void printCurrentCommInterval() {
        boolean hasProfileOpenEvent = TableEventsManager.sharedInstance().hasOpenEventInViolationCategory
                (ViolationCategoryTypes.START_PROFILE);
        if (hasProfileOpenEvent) return;
        String messageToUpload;
        if (currentCommInterval == CommunicationInterval.CommIntervalLow) {
            messageToUpload = "Not in charging, and located in beacon zone or close to home zone, or battery low or critical."
                    + "\nWill use CommIntervalLow every " +
                    TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                            (OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW) + " seconds\n";
        } else {
            messageToUpload = "In charging, or not located in beacon zone and not close to home zone, and battery not low and critical."
                    + "\nWill use CommInterval every " +
                    TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                            (OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL) + " seconds\n";
        }

        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + messageToUpload, false);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public CommunicationInterval calculateComInterval() {
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
        boolean isInBeaconZone = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
        int deviceBatteryStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT);
        boolean isDeviceCharging = (DeviceStateManager.getInstance().getCurChargingState() == ChargingState.ON_CHARGING);

        if ((isInBeaconZone || deviceBatteryStatus == DEVICE_BATTERY_CONS.DEVICE_BATTERY_LOW_STATUS ||
                deviceBatteryStatus == DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS) && isOffenderAllocated && !isDeviceCharging) {
            return CommunicationInterval.CommIntervalLow;
        } else {
            return CommunicationInterval.CommInterval;
        }

    }

    public void switchBetweenCommIntervalModeIfNeeded(CommunicationInterval newCommInterval) {
        if (currentCommInterval == newCommInterval || newCommInterval != CommunicationInterval.CommIntervalLow) return;
        currentCommInterval = CommunicationInterval.CommIntervalLow;
        isServerApprovedCommIntervalChange = true;

        printCurrentCommInterval();

        scheduleNewCycleIfNeeded(true);
    }

    public void scheduleNewCycleIfNeeded(boolean shouldForceScheduleFutureRun) {

        CommunicationInterval newComInterval = calculateComInterval();
        if (currentCommInterval == newComInterval) {

            // if comm interval status was changed or we must schedule new future network run
            if (shouldForceScheduleFutureRun) {
                int currentPmComProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);

                networkManagerReceiver.setAlaramClock(App.getContext(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getCurrentNetCycleInterval(currentPmComProfile) -
                        TIME_TO_START_CYCLE), NetworkCycleReceiver.class, 10, NetworkCycleReceiver.FIVE_SECONDS_TO_START_CYCLE_CONST, null);
            }
        }

        if (newComInterval == CommunicationInterval.CommInterval) {
            currentCommInterval = CommunicationInterval.CommInterval;

            printCurrentCommInterval();

            networkManagerReceiver.setAlaramClock(App.getContext(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(0),
                    NetworkCycleReceiver.class, 10, NetworkCycleReceiver.FIVE_SECONDS_TO_START_CYCLE_CONST, null);

            isServerApprovedCommIntervalChange = true;

        } else if (newComInterval == CommunicationInterval.CommIntervalLow) {
            /*before we switch from CommInterval to currentCommIntervalLow,
            /we first want to be sure PM got the change, therefore we start a new cycle*/
            if (isServerApprovedCommIntervalChange) {
                isServerApprovedCommIntervalChange = false;
                networkManagerReceiver.setAlaramClock(App.getContext(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(0),
                        NetworkCycleReceiver.class, 10, NetworkCycleReceiver.FIVE_SECONDS_TO_START_CYCLE_CONST, null);
                return;
            }
            int currentPmComProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);
            networkManagerReceiver.setAlaramClock(App.getContext(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getCurrentNetCycleInterval(currentPmComProfile) -
                    TIME_TO_START_CYCLE), NetworkCycleReceiver.class, 10, NetworkCycleReceiver.FIVE_SECONDS_TO_START_CYCLE_CONST, null);

        }
    }

    private int getCurrentNetCycleInterval(int currentPmComProfile) {

        int netCycleInterval;
        if (currentPmComProfile != -1) {
            PmComProfiles pmComProfile = TableEventsManager.sharedInstance().profilingEventsConfig.
                    getPmComProfileObjectByProfileId(currentPmComProfile);
            netCycleInterval = pmComProfile.CommInterval;
        } else {
            if (currentCommInterval == CommunicationInterval.CommIntervalLow) {
                netCycleInterval = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().DeviceConfigNetCycleIntervalLow;
            } else {
                netCycleInterval = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().DeviceConfigNetCycleInterval;
            }
        }

        return netCycleInterval;
    }


    public void handleStartCycleReceiver() {
        boolean isManufacturerIdEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MANUFACTURER_ID) > 0;
        boolean isMaScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MAC_SCAN_ENABLED) > 0;
        boolean isNormalScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(NORMAL_SCAN_ENABLED) > 0;
        boolean isDozeModeScanEnabled = isMaScanEnabled || isManufacturerIdEnabled;

        if (!isDozeModeScanEnabled || isNormalScanEnabled) {
            turnOnScreenIfIdle();
        }

        LoggingUtil.updateNetworkLog("\nCalling 'start new cycle' - handleStartCycleReceiver" + "\n", false);
        NetworkRepository.getInstance().startNewCycle();
        NetworkRepository.getInstance().scheduleNewCycleIfNeeded(true);

        TableOffenderDetailsManager.sharedInstance().updateColumnLong
                (OFFENDER_DETAILS_CONS.DETAILS_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI, android.os.SystemClock.elapsedRealtime());
    }

    private void turnOnScreenIfIdle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isIdle()) {
                turnOnScreen();
            }
        }
    }


    public void startNewCycle() {

        Log.i("bug70","startNewCycle US State:" + NetworkRepositoryConstants.getCurrentCommunicationState());
        LoggingUtil.updateNetworkLog("\nstart new cycle, state = " + NetworkRepositoryConstants.getCurrentCommunicationState() + "\n", false);

        if(updateActivityListener == null){
            Log.i("bug70","updateActivityListener is null ");
            return;
        }

        if (NetworkRepositoryConstants.getCurrentCommunicationState() == NetworkStateType.IDLE_STAGE) {
            NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_GET_AUTHENTICATION);
        } else {
            // if state is not start for one minute,  force start cycle
            if (NetworkRepositoryConstants.lastStartCycleDate != null &&
                  new Date().getTime() - NetworkRepositoryConstants.lastStartCycleDate.getTime() > 60000) {
                // force start cycle
                NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_GET_AUTHENTICATION);
            }
        }

        NetworkRepository.getInstance().serverHandler();
    }

    public void testConnection(TestConnectionListener listener) {
        Log.i(TAG, "create a request to TestConnection...");
        new TestConnectionRequest(listener).execute();
    }

    public void registerDeviceRemotely(SettingFragmentViewListener listener) {
        Log.i(TAG, "create a request to register device remotely...");
        new CreateDeviceRequest(new CreateDeviceResultParser(listener)).execute();
    }

    public void continueCycleAfterAuth() {
        lbsSentInCycle = false;
        httpGetDateTime();
    }

    private static final NetworkRepository INSTANCE = new NetworkRepository();

    private final BroadcastReceiver idleModeReceiver = new BroadcastReceiver() {
        @TargetApi(23)
        @Override
        public void onReceive(Context context, Intent intent) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            isIdle = pm.isDeviceIdleMode();
            long intervalInSec = (SystemClock.uptimeMillis() - lastTimeWhenIdleStateChanged) / 1000;
            String messageToUpload = "Current time, when IdleState is changed: " + TimeUtil.getCurrentTimeStr() + ", intervalInSec: " + intervalInSec + ", isIdle: " + isIdle();
            App.writeToNetworkLogsAndDebugInfo(context.getClass().getSimpleName(), messageToUpload, DebugInfoModuleId.Network);
            lastTimeWhenIdleStateChanged = (SystemClock.uptimeMillis());
        }
    };

    public void registerIdleModeReceiver() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                App.getAppContext().registerReceiver(idleModeReceiver, new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterIdleModeReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                App.getAppContext().unregisterReceiver(idleModeReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }


    public static synchronized NetworkRepository getInstance() {
        return INSTANCE;
    }

    public void setActivityListener(ViewUpdateListener updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
        SyncRequestsRepository.getInstance().setViewUpdateListener(updateActivityListener);
    }


    public void serverHandler() {
        switch (NetworkRepositoryConstants.getCurrentCommunicationState()) {
            case NetworkStateType.IDLE_STAGE:
                NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_GET_AUTHENTICATION);
                break;

            // Authentication A
            case NetworkStateType.SEND_GET_AUTHENTICATION:
                NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.WAIT_FOR_AUTHENTICATION);
                lastCycleStartTime = System.currentTimeMillis();
                // MOJ
                MainActivity.commLastCycleTime = System.currentTimeMillis();
                httpGetAuthentication();
                break;
        }
    }

    public void sendHandleOffenderRequest() {
        int lastOffenderReuqestIdTreated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED);
        int lastOffenderRequestStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS);
        handleOffenderRequest(lastOffenderReuqestIdTreated, lastOffenderRequestStatus);
    }

    public void handleOffenderRequestSuccess() {
        setOffenderRequestResultSuccess();
        sendHandleOffenderRequest();
    }

    public void setOffenderRequestResultSuccess() {
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS, REQUEST_RESULT_OK);
    }


    //error
    public void handleOffenderRequestError() {
        setOffenderReqResultError();
        sendHandleOffenderRequest();
    }

    public void setOffenderReqResultError() {
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS, REQUEST_RESULT_ERR);
    }

    //in progress
    public void handleOffenderRequestInProgress() {
        setOffenderReqResultInProgress();
        sendHandleOffenderRequest();
    }

    private void setOffenderReqResultInProgress() {
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS, REQUEST_RESULT_IN_PROGRESS);
    }

    public void handleOffenderRequest(int lastOffenderReuqestIdTreated, int requestStatus) {
        (new PostHandleOffenderRequest(new PostHandleOffenderRequestsResultParser(updateActivityListener, this), lastOffenderReuqestIdTreated, requestStatus)).execute();
    }

    public void httpGetOffenderZones(String currentZoneDataVersion) {
        (new GetOffenderZonesRequest(new GetOffenderZonesResultParser(currentZoneDataVersion), currentZoneDataVersion)).execute();
    }

    public void httpGetScheduleOfZone(int ZoneId, int ScheduleVersion) {
        (new GetOffenderScheduleOfZoneRequest(new GetScheduleOfZoneListener(), ScheduleVersion, ZoneId)).execute();
    }

    public void httpGetDeviceConfiguration(DeviceConfigurationType deviceConfigurationType, int versionNumber) {
        (new GetDeviceConfigurationRequest(new GetDeviceConfigurationResultParser(updateActivityListener), deviceConfigurationType, versionNumber)).execute();//1);
    }


    public void httpTerminateToken() {
        (new PostTerminateRequest(new PostTerminateTokenResultParser(updateActivityListener))).execute();
    }


    public void httpGetAuthentication() {
        new GetAuthenticationTokenRequest(new GetAuthenticationTokenResultParser()).execute();
    }

    public void httpGetDateTime() {
        boolean knoxActivated = KnoxUtil.getInstance().isKnoxActivated();
        boolean automaticTimeEnable = KnoxUtil.getInstance().getKnoxSDKImplementation().isAutomaticTimeEnable();
        if (knoxActivated && !automaticTimeEnable) {
            new GetDateTimeRequest(new GetDateTimeListenerResultParser()).execute();
            Log.i("bug70","GetDateTimeRequest");
            return;
        }
        getOffenderInformation();

    }

    public void getOffenderInformation() {
        Log.i("bug70","getOffenderInformation");
        boolean isPureComAsHomeUnitActivated = DatabaseAccess.getInstance().tableOffenderDetails.getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PURECOM_AS_HOME_UNIT) > 0;
        // TODO - PureCom Zone feature - reset on unallocate
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isPureComAsHomeUnitActivated && isOffenderAllocated) {
            Log.i("bug70","GetOffenderInformationRequest");
            new GetOffenderInformationRequest(new GetOffenderInformationResultParser(updateActivityListener)).execute();
        }  else {
            Log.i("bug70","checkForPostOnDemandPhoto");
            checkForPostOnDemandPhoto(NetworkRequestName.RegularFlow);
        }
    }

    public void sendNewGpsPoints() {
        Log.i("bug70", "sendNewGpsPoints 0");
        if (updateActivityListener == null) return;
        int timeZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE);
        // Check if LBS request is needed
        if ((updateActivityListener.isLbsLocationRequestRequired()) && (!lbsSentInCycle)) {
            Log.i("bug70", "sendNewGpsPoints 1");
            List<CellularInfoManager.LbsInfo> lbsInfoArray = CellularInfoManager.sharedInstance().getLbsInfo();
            if (lbsInfoArray == null) return;
            lbsSentInCycle = true;
            // Execute LBS request
            (new GetLbsLocationRequest(new GetLbsLocationResultParser(updateActivityListener), lbsInfoArray)).execute();
            return;
        }
        List<EntityGpsPoint> recordGpsPointsArray = DatabaseAccess.getInstance().tableGpsPoint.getGpsPointRecordsForUpload();
        if (!recordGpsPointsArray.isEmpty()) {
            Log.i("bug70", "sendNewGpsPoints 2");
            int gpsPointsChunkSize = Math.min(recordGpsPointsArray.size(), MAX_GPS_POINTS_PER_CHUNK);
            (new InsertOffenderLocationsRequest(new InsertOffenderLocationsResultParser(updateActivityListener),
                    recordGpsPointsArray,
                    gpsPointsChunkSize, false, timeZoneId)).execute();
            return;
        }
        EntityGpsPoint recordGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
        if (recordGpsPoint == null) {
            Log.i("bug70", "sendNewGpsPoints 3");
            sendNewEventArray();
            return;
        }

        Log.i("bug70", "sendNewGpsPoints 4");

        List<EntityGpsPoint> tempRecordGpsPointList = new ArrayList<>();
        tempRecordGpsPointList.add(recordGpsPoint);

        int gpsPointsChunkSize = tempRecordGpsPointList.size();
        (new InsertOffenderLocationsRequest(new InsertOffenderLocationsResultParser(updateActivityListener), tempRecordGpsPointList, gpsPointsChunkSize, true, timeZoneId)).execute();
    }

    public void sendNewEventArray() {
        sendNewEventArray(NetworkRequestName.RegularFlow);
    }

    public void sendNewEventArray(NetworkRequestName nextRequestToSend) {
        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_EVENTS_START);
        new InsertDeviceEventsRequest(new InsertDeviceEventsResultParser(updateActivityListener, nextRequestToSend)).execute();
    }

    public void sendInsertDeviceCommunications() {
        List<EntityCallLog> recordCallLogsArray = DatabaseAccess.getInstance().tableCallLog.getCallLogsRecordsForUpload();
        if (recordCallLogsArray.isEmpty()) {
            sendInsertDeviceDebugInfo();
            return;
        }
        InsertDeviceCommunicationsRequest httpManagerInsertDeviceCommunications = new InsertDeviceCommunicationsRequest(new InsertDeviceCommunictionsResultParser(), recordCallLogsArray);
        httpManagerInsertDeviceCommunications.execute();
    }

    public void checkForPostOnDemandPhoto(NetworkRequestName nextRequestToSend) {
        ///if we have photos in DB
        List<EntityOffenderPhoto> offenderPhotos = DatabaseAccess.getInstance().tableOffenderPhoto.getOffenderPhotos();
        if (!offenderPhotos.isEmpty()) {
            Log.i("bug70", "checkForPostOnDemandPhoto 1");
            new PostOnDemandPhotoRequest(new PostOnDemandPhotoResultParser(nextRequestToSend), offenderPhotos).execute();
            return;
        }

        Log.i("bug70", "checkForPostOnDemandPhoto 2");
        NetworkRepository.getInstance().sendNewGpsPoints();
    }

    public void continueToNextRequest(NetworkRequestName nextRequestToSend) {
        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_EVENTS_FINISH);

        LoggingUtil.updateNetworkLog("\nEvents conitnueToNextRequest - nextRequest " + nextRequestToSend + "\n", false);

        if (nextRequestToSend == NetworkRequestName.RegularFlow) {
            NetworkRepository.getInstance().sendInsertDeviceCommunications();
        } else {
            NetworkRepository.getInstance().runNetworkRequest(nextRequestToSend);
        }
    }

    public void sendInsertDeviceDebugInfo() {
        sendInsertDeviceDebugInfo(0, TableDebugInfo.MAX_LOG_PER_REQ, FlowType.RegularFlow);
    }

    public void sendInsertDeviceDebugInfo(int currentSequentSendingDebugInfoCounter, int numberOfRowsToQuery, FlowType specialFlow) {
        List<EntityDebugInfo> recordDebugInfoArray = DatabaseAccess.getInstance().tableDebugInfo.getDeviceInfoRecordsForUpload(numberOfRowsToQuery);
        if (recordDebugInfoArray.isEmpty()) {
            sendInsertDeviceInfo();
            return;
        }
        InsertDeviceDebugInfoRequest insertDeviceDebugInfoRequest = new InsertDeviceDebugInfoRequest(new InsertDeviceDebugInfoResultParser
                (currentSequentSendingDebugInfoCounter, recordDebugInfoArray.size(), specialFlow, updateActivityListener), recordDebugInfoArray);
        insertDeviceDebugInfoRequest.execute();
    }

    private void saveDeviceInfoToDB() {
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
        if (!isOffenderAllocated) return;
        TableDeviceInfoManager.sharedInstance().saveDeviceStatusToDB();
        TableDeviceInfoManager.sharedInstance().saveCellularInfoToDB();
    }


    public void sendInsertDeviceInfo() {

        // send device info every X cycles
        DeviceInfoCyclesCount++;
        if ((DeviceInfoCyclesCount < DeviceInfoCycles) || (DeviceInfoCycles == 0)) {
            // next operation
            getOffenderRequest(true);
            return;
        }
        DeviceInfoCyclesCount = 0;

        EntityDeviceInfoDetails recordDeviceInfoDetails = null;
        if (!TableDeviceInfoManager.sharedInstance().isTableDeviceInfoDetailsEmpty()) {
            recordDeviceInfoDetails = TableDeviceInfoManager.sharedInstance().getDeviceInfoDetailsRecord();
        }

        saveDeviceInfoToDB();

        List<EntityDeviceInfoStatus> recordDeviceInfoStatusArray = TableDeviceInfoManager.sharedInstance().getDeviceInfoStatusRecords();

        List<EntityDeviceInfoCellular> recordDeviceInfoCellularArray = TableDeviceInfoManager.sharedInstance().getDeviceInfoCellularRecords();
        InsertDeviceInfoRequest insertDeviceInfoRequest = new InsertDeviceInfoRequest(new InsertDeviceInfoResultParser(updateActivityListener), recordDeviceInfoDetails, recordDeviceInfoStatusArray, recordDeviceInfoCellularArray);
        insertDeviceInfoRequest.execute();

    }

    public void getOffenderRequest(boolean shouldSendPendingHandleRequests) {

        if (!shouldSendPendingHandleRequests) {

            NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.GET_OFFENDER_REQUEST_SEND);

            new GetOffenderRequests(new GetOffenderRequestsResultParser(updateActivityListener)).execute();
        }

        //if in pending activation we will try to send every cycle activate (7) ok
        int offenderActivateStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS);
        if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_PENDING_ACTIVATION_ENROLLMENT) {
            NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED = OffenderRequestType.ACTIVATE;
            int activationOffenderRequestIdTreated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                    (OFFENDER_STATUS_CONS.OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED);
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED,
                    activationOffenderRequestIdTreated);
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS,
                    REQUEST_RESULT_OK);

            sendHandleOffenderRequest();
            return;
        }

        ArrayList<HandleRequestToBeSentToServerData> failedHandleOffenderRequestsArray = TableOffenderStatusManager.sharedInstance().
                getFailedHandleRequestsList();

        //try to send failed handle requests if exists
        if (!failedHandleOffenderRequestsArray.isEmpty()) {
            this.tempFailedHandleOffenderRequestsArray = failedHandleOffenderRequestsArray;
            fundSendFailedHandleOffenderRequests();
        } else { //send regular offender requests
            NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.GET_OFFENDER_REQUEST_SEND);

            new GetOffenderRequests(new GetOffenderRequestsResultParser(updateActivityListener)).execute();
        }

    }

    private void fundSendFailedHandleOffenderRequests() {
        String messageToUpload = "Send failed handle requests";
        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), messageToUpload, DebugInfoModuleId.Network);

        HandleRequestToBeSentToServerData failedHandleOffenderRequest = tempFailedHandleOffenderRequestsArray.get(0);
        tempFailedHandleOffenderRequestsArray.remove(0);

        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED,
                failedHandleOffenderRequest.requestId);
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS, failedHandleOffenderRequest.status);

        sendHandleOffenderRequest();
    }

    public String getTokenKey() {
        return TOKEN_KEY;
    }

    public void setTOKEN_KEY(String token_key) {
        TOKEN_KEY = token_key;
    }

    public boolean isInScheduleCycle() {
        return isInScheduleCycle;
    }

    public void setIsInScheduleCycle(boolean isScheduleCycle) {
        this.isInScheduleCycle = isScheduleCycle;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean shouldUploadRecordsOrEventsToServerImmediately() {
        List<EntityEventLog> eventsThatDidntFailToSend = DatabaseAccess.getInstance().tableEventLog.getEventsThatDidntFailToSend();
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;


        return (eventsThatDidntFailToSend.size() > InsertDeviceEventsResultParser.MIN_RECORDS_TO_REPEAT_EVENT_LOG_REQUEST) && isOffenderAllocated;
    }

    public boolean shouldRestartAppInTheEndOfTheCycle() {
        return shouldRestartAppInTheEndOfTheCycle;
    }

    public void setShouldRestartAppInTheEndOfTheCycle(boolean shouldRestartAppInTheEndOfTheCycle) {
        this.shouldRestartAppInTheEndOfTheCycle = shouldRestartAppInTheEndOfTheCycle;
    }

    public boolean isIdle() {
        return isIdle;
    }


    public void turnOnScreen() {
        final Context context = App.getAppContext();
        WakeLock screenLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP, "AppName:TAG");

        screenLock.acquire();
        screenLock.release();
        try {
            final int oldTimeOut = Settings.System.getInt(((Activity) context).getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Settings.System.putInt(((Activity) context).getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, oldTimeOut);
                }
            }, 5000);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean turnOnScreenZ() {
        return turnOnScreenZ(App.getAppContext());
    }

    public static boolean turnOnScreenZ(final Context context) {

        try {
            screenLockX = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AppName:TAG");
            screenLockX.acquire();
            screenLockX.release();

            final int oldTimeOut = 600000; // 10 min
            //Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, oldTimeOut);
                }
            }, 600000); // 5000

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean turnOnScreenX() {
        if(!BuildConfig.DEBUG) {
            return turnOnScreenX(App.getAppContext());
        }

        return false;
    }

    public static boolean turnOnScreenX(final Context context) {

        try {
            screenLockX = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP, "AppName:TAG");
            screenLockX.acquire();
            screenLockX.release();

            final int oldTimeOut = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            //Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Settings.System.putInt(((Activity) context).getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, oldTimeOut);
                }
            }, 5000);
            return true;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void onOldHandleRequestResponseSucceeded() {
        if (!tempFailedHandleOffenderRequestsArray.isEmpty()) {
            fundSendFailedHandleOffenderRequests();
        } else {
            getOffenderRequest(false);
        }
    }

    @Override
    public void onHandleRequestResponseFailed() {
        int lastOffenderRequestIdTreated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED);

        int lastOffenderRequestStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS);

        String messageToUpload = "Handle request failed. RequestId " + lastOffenderRequestIdTreated + " Status " + lastOffenderRequestStatus;
        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), messageToUpload, DebugInfoModuleId.Network);

        if (lastOffenderRequestStatus == REQUEST_RESULT_OK && NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED == OffenderRequestType.ACTIVATE) {
            getOffenderRequest(false);
        } else if (lastOffenderRequestStatus == REQUEST_RESULT_OK || lastOffenderRequestStatus == REQUEST_RESULT_ERR) {
            handleRequestResponseFailed(lastOffenderRequestIdTreated, lastOffenderRequestStatus);
        } else {
            try {
                OffenderRequestsRepository.getInstance().handleOffenderRequestArray();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void handleRequestResponseFailed(int lastOffenderRequestIdTreated, int lastOffenderRequestStatus) {
        boolean isHandleRequestExistsInFailedRequests = TableOffenderStatusManager.sharedInstance().isHandleRequestExistsInFailedRequests
                (lastOffenderRequestIdTreated);

        // we want to add new handle request to list that failed
        if (!isHandleRequestExistsInFailedRequests) {
            TableOffenderStatusManager.sharedInstance().updateFailedHandleRequestsList(new HandleRequestToBeSentToServerData
                    (lastOffenderRequestIdTreated, lastOffenderRequestStatus));

            try {
                OffenderRequestsRepository.getInstance().handleOffenderRequestArray();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            if (!tempFailedHandleOffenderRequestsArray.isEmpty()) {
                fundSendFailedHandleOffenderRequests();
            } else {
                getOffenderRequest(false);
            }

        }
    }

    public long getLastCycleStartTime() {
        return lastCycleStartTime;
    }

    public void handleCycleFinished() {
        boolean isCycleFinishedSuccessfuly = (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.IS_CYCLE_FINISHED_SUCCESSFULY)) == 1;
        boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.SYNC) != -1;
        if (isCycleFinishedSuccessfuly && hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(EventTypes.SyncSuccessful, -1, -1);
        }
        // update current time of factory reset
        // when timeout is zero it's first time
        if (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_FACTORY_RESET_ENABLE) != 0)
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LAST_SUCCESSFULY_COM, (int) TimeUtil.getCurrentTime() +
                    TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_FACTORY_RESET_TIMEOUT) * 3600);
    }

    public void handleErrorDuringCycle(String additionalInfo) {

        LoggingUtil.updateNetworkLog("\nhandleErrorDuringCycle: " + additionalInfo + "\n", false);

        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.IS_CYCLE_FINISHED_SUCCESSFULY, 0);
        // check timeput on exp. do a factory reset (will work after device admin enabled)
        if (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_FACTORY_RESET_ENABLE) != 0
                && TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LAST_SUCCESSFULY_COM) != 0
                && TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LAST_SUCCESSFULY_COM) < TimeUtil.getCurrentTime())
            KnoxUtil.getInstance().KnoxFactoryReset(App.getAppContext());

    }

    public FlightModeData getFlightModeData() {
        return flightModeData;
    }

}
