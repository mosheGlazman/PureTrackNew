//package com.supercom.puretrack.data.source.remote.network_managers;
//
//import static com.supercom.puretrack.ui.activity.MainActivity.isOffenderInSuspendSchedule;
//
//import android.content.Intent;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import com.supercom.puretrack.data.R;
//import com.supercom.puretrack.data.source.local.local_managers.hardware.AccelerometerManager;
//import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
//import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
//import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
//import com.supercom.puretrack.data.source.local.table.TableEventConfig;
//import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
//import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
//import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
//import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
//import com.supercom.puretrack.data.source.local.table_managers.TableScheduleOfZones;
//import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
//import com.supercom.puretrack.data.source.remote.DownloadTaskMain;
//import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
//import com.supercom.puretrack.database.DatabaseAccess;
//import com.supercom.puretrack.model.business_logic_models.enums.ServerMessageType;
//import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
//import com.supercom.puretrack.ui.activity.LauncherActivity;
//import com.supercom.puretrack.ui.activity.MainActivity;
//import com.supercom.puretrack.ui.adapter.SchedulePagerAdapter;
//import com.supercom.puretrack.ui.dialog.MessageDialog;
//import com.supercom.puretrack.ui.enrolment.EnrolmentActivity;
//import com.supercom.puretrack.ui.schedule.ScheduleTab;
//import com.supercom.puretrack.util.application.App;
//import com.supercom.puretrack.util.date.TimeUtil;
//import com.supercom.puretrack.util.general.KnoxUtil;
//import com.supercom.puretrack.util.general.LoggingUtil;
//import com.supercom.puretrack.util.general.NumberComputationUtil;
//
//import java.io.File;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
////TODO - Alon - This is in-work progress to be ViewModel class to separate the logic from the MainActivity UI as much as possible.
//public class NetworkViewModel implements ViewUpdateListener, DownloadTaskMain.DownloadTaskListener {
//
//    //Class Variables - tag
//    private final String TAG = "NetworkRequestManager";
//
//    //Class Variables - listener
//    private NetworkRequestManagerListener listener;
//
//    //Class variables - booleans
//    private boolean isFingerprintEnabled;
//    private boolean isFingerprintAvailable;
//
//
//
//    public interface NetworkRequestManagerListener {
//        void updateUI();
//
//        void handlePureComZone(boolean atHome);
//
//        void handleLocationUpdate(boolean start);
//
//        void handleTagIdChanged(boolean changed);
//
//        void startTagActivities();
//
//        void appLanguageChange();
//
//        void updateMojScreenTurnOn(long value);
//
//        void checkForEmergencyButtonEnabled();
//
//        void checkForFileSettingsChanges();
//
//        void handleBluetoothModelEnabled();
//
//        void handleMagneticListener(boolean enable);
//
//        void definedAlertDialogNotifications();
//
//        void handleMessageDialogSwipe();
//
//        void addToAlertDialog(MessageDialog messageDialog);
//
//        void scheduleMessageFutureRun();
//
//        void setActivityToForegroundIfNeeded();
//
//        void handlePhotoOnDemandDialog(int requestId);
//
//        void handleBiometricDialogToGetCreated();
//    }
//
//    public DeviceConfigurationViewModel(NetworkRequestManagerListener listener, boolean isFingerprintEnabled, boolean isFingerprintAvailable) {
//        this.listener = listener;
//        this.isFingerprintEnabled = isFingerprintEnabled;
//        this.isFingerprintAvailable = isFingerprintAvailable;
//    }
//
//    @Override
//    public void onGpsPointsUploadedFinishedToParse() {
//
//        listener.updateUI();
////        updateHomeScreenUI();
//    }
//
//    @Override
//    public void onEventResposeOkFromServer() {
//        NetworkCycleManager.getInstance().switchBetweenCommIntervalModeIfNeeded(NetworkCycleManager.getInstance().calculateComInterval());
//    }
//
//    @Override
//    public void onGetDeviceConfigurationResultParserFinishedToParse(boolean isTagIdChanged, boolean isBeaconIdChanged, boolean isCommIntervalChanged,
//                                                                    String lastTagRfId, boolean isLocationSettingsChanged, boolean isVoipSettingsChanged, boolean isCurrentlyDBPureTrackCaseTamperEnabledChanged,
//                                                                    boolean isLockScreenChanged, boolean isAppLanguageChanged) {
//        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
//        boolean isPureComZoneEnabled = DatabaseAccess.getInstance().tableOffenderDetails.getIntValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PURECOM_AS_HOME_UNIT) > 0;
//        if (!isPureComZoneEnabled) {
//            listener.handlePureComZone(false);
//            listener.startTagActivities();
////            locationManager.handlePureComZone(false);
////            startTagActivitiesIfNeeded();
//        }
//
//        if (isOffenderAllocated) {
//            Intent intent = new Intent(LauncherActivity.LAUNCHER_ACTIVITY_MESSAGE_RECEIVER);
//            intent.putExtra(LauncherActivity.LAUNCHER_ACTIVITY_EXTRA, LauncherActivity.MESSAGE_EXTRA);
////            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
//            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
//
//            String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
//            listener.handleTagIdChanged(isTagIdChanged && tagRFIDFromServer.equals(BluetoothManager.NO_TAG));
////            treatBleDeviceStatusChanged(isTagIdChanged);
//            listener.handleLocationUpdate(isLocationSettingsChanged);
////            locationManager.startLocationUpdate(false);
////            treatLocationSettingsChanged(isLocationSettingsChanged);
//            treatCommIntervalChanged(isCommIntervalChanged);
//        }
//
//        //update white list config file - used by external dialer application
//        LoggingUtil.createWhiteListConfigFile();
//
////        long mojScreenTurnOn = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_TURN_ON_SCREEN_MOTION);
//        long mojScreenTurnOn = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_TURN_ON_SCREEN_MOTION);
//        listener.updateMojScreenTurnOn(mojScreenTurnOn);
//
//
//        treatCurrentlyDBPureTrackCaseTamperEnabledChanged(isCurrentlyDBPureTrackCaseTamperEnabledChanged);
//
//        treatLockScreenChanged(isLockScreenChanged);
//
//        listener.checkForEmergencyButtonEnabled();
////        addEmergencyButtonIfNeeded();
//
//        listener.checkForFileSettingsChanges();
////        changeWriteToLogsSettingsIfNeeded();
//
//        listener.updateUI();
////        updateHomeScreenUI();
//
//        if (isAppLanguageChanged) {
//            listener.appLanguageChange();
////            treatAppLanguageChanged();
//        }
//
//
//        // start accelerometer if required
//        AccelerometerManager.getInstance().updateAccelerometerConfig(TableOffenderDetailsManager.sharedInstance().getAccelerometerSettings());
//
//
//        boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
//        if (isKnoxLicenceActivated) {
//            boolean inKioskMode = KnoxUtil.getInstance().getKnoxSDKImplementation().isInKioskMode();
//            if (inKioskMode) {
//                KnoxUtil.getInstance().enterOffenderMode(true);
//            } else {
//                KnoxUtil.getInstance().enterOfficerMode(true);
//            }
//        }
//    }
//
//    private void treatLockScreenChanged(boolean isLockScreenChanged) {
//        if (isLockScreenChanged) {
//            if (!bluetoothInBackgroundModel.getEnabled()) {
//                listener.handleBluetoothModelEnabled();
////                handleLockScreen();
//            }
//        }
//    }
//
//    private void treatCurrentlyDBPureTrackCaseTamperEnabledChanged(boolean isCurrentlyDBPureTrackCaseTamperEnabledChanged) {
//        if (isCurrentlyDBPureTrackCaseTamperEnabledChanged) {
//            int isCurrentlyDBPureTrackCaseTamperEnabled = (int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED);
//
//            //                registerMagneticListenerIfSupported();
//            //                unregisterMagneticListener();
//            listener.handleMagneticListener(isCurrentlyDBPureTrackCaseTamperEnabled == 1);
//        }
//    }
//
//    private void treatCommIntervalChanged(boolean isCommIntervalChanged) {
//        if (isCommIntervalChanged) {
//            NetworkCycleManager.getInstance().scheduleNewCycleIfNeeded(true);
//        }
//    }
//
////    private void treatBleDeviceStatusChanged(boolean isTagIdChanged) {
////
////        if (isTagIdChanged) {
////
////            //if PM changed tag type from regular tag to virtual tag an no beacon exists in DB
////            String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
////            if (tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
////                // stop mechanism to turn on screen / BLE restart
////                lastTagReceivedTime = 0;
////                lastBleTagRx = 0;
////                stopBleActivities(false);
////            } else {  //if PM changed tag type from virtual tag to regular tag or from regular tag to regular tag
////                // start mechanism to turn on screen / BLE restart
////                lastTagReceivedTime = System.currentTimeMillis();
////                lastBleTagRx = System.currentTimeMillis();
////                startTagActivitiesIfNeeded();
////            }
////        }
////    }
//
//    @Override
//    public void onMessageReceivedFromServer(ServerMessageType serverMessageType, int requestId) {
//        NetworkCycleManager.getInstance().setOffenderReqResultSuccess();
//        NetworkCycleManager.getInstance().sendHandleOffenderRequest();
//
//        String recordTextMessage = "";
//        if (DatabaseAccess.getInstance().tableMessages.GetLastMsg() != null && serverMessageType == ServerMessageType.MESSAGE) {
//            recordTextMessage = DatabaseAccess.getInstance().tableMessages.GetLastMsg().Text;
//        }
//        listener.definedAlertDialogNotifications();
////        defineNotification(alertDialogArray.size());
//        MessageDialog messageDialog;
//        MessageDialog.MessageDialogListener messageDialogListener = new MessageDialog.MessageDialogListener() {
//            @Override
//            public void onMessageDialogSwipeComplete(ServerMessageType serverMessageType, int requestId) {
//                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventMessageAck, -1, -1);
//                if (serverMessageType == ServerMessageType.MESSAGE) {
////                    handleMessageDialogSwipe();
//                    listener.handleMessageDialogSwipe();
//                    return;
//                }
////                handlePhotoOnDemandDialog(requestId);
//                listener.handlePhotoOnDemandDialog(requestId);
//            }
//        };
//        if (serverMessageType == ServerMessageType.MESSAGE) {
//            messageDialog = new MessageDialog(App.getContext(), messageDialogListener, recordTextMessage, serverMessageType, requestId);
//        } else {
//            messageDialog = new MessageDialog(App.getContext(), messageDialogListener, null, serverMessageType, requestId);
//        }
////        alertDialogArray.add(messageDialog);
//        listener.addToAlertDialog(messageDialog);
//        messageDialog.show();
//
//
//        long messageTimeOut = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_MESSAGE_TIMEOUT);
//        if (serverMessageType == ServerMessageType.MESSAGE)
////            messageReceiveFutureTaskManager.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(messageTimeOut));
//            listener.scheduleMessageFutureRun();
////        setActivityToForegroundIfNeeded();
//        listener.setActivityToForegroundIfNeeded();
//    }
//
//    @Override
//    public void onBiometricReceivedFromServer() {
//        if (isFingerprintEnabled && isFingerprintAvailable) {
////            handleBiometricDialogToGetCreated();
//            listener.handleBiometricDialogToGetCreated();
//        } else {
//            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.noFingerprintRegistered, -1, -1);
//        }
//
//        NetworkCycleManager.getInstance().setOffenderReqResultSuccess();
//        NetworkCycleManager.getInstance().sendHandleOffenderRequest();
//    }
//
//    @Override
//    public void onTerminateReceivedFromServer(int requestId) {
//        String serialNumber = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber();
//        int databaseSize = DatabaseAccess.getInstance().getDatabaseSize();
//        int lastCreatedEventType = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
//                (TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_LAST_CREATED_EVENT_TYPE);
//
//        String messageToUpload = "Received Unallocate request from PM on: " + TimeUtil.getCurrentTimeStr() + ", Request id: " +
//                requestId + ", Serial number: " + serialNumber + ", Size of db: " + databaseSize + " bytes " +
//                ", Last created event type: " + lastCreatedEventType;
//
//        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + messageToUpload, false);
//        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
//                EntityDebugInfo.DebugInfoModuleId.Network.ordinal(), TableDebugInfo.DebugInfoPriority.NORMAL_PRIORITY);
//
//        onUnallocateRecordUploaded(NetworkCycleManager.Flow_Type.Unallocated);
//    }
//
//    @Override
//    public void onUnallocateRecordUploaded(NetworkCycleManager.Flow_Type flowType) {
//
//        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
//                == TableOffenderStatusManager.OFFENDER_ACTIVATE_STATUS.OFF_ALLOCATED;
//
//        if (!isOffenderAllocated && App.isActivityOnForegroundTop(EnrolmentActivity.class.getName())) {
//            finishActivity(ENROLMENT_EXTRA_CODE);
//        }
//
//        NetworkCycleManager.isGpsProximityViolationOpened = false;
//        NetworkCycleManager.isGpsProximityWarningOpened = false;
//
//        boolean isDeviceResetSuccesfull = resetDBToInitialDeviceState();
//
//        if (isDeviceResetSuccesfull) {
//            NetworkCycleManager.getInstance().sendHandleOffenderRequest();
//
//            String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
//            if (isOffenderAllocated && !tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
//                unallocateDialogManager.showNoticeScreen();
//            }
//        } else {
//            NetworkCycleManager.getInstance().setOffenderReqResultError();
//            NetworkCycleManager.getInstance().sendHandleOffenderRequest();
//        }
//
//    }
//
//    @Override
//    public void onActivateReceivedFromServer() {
//
//        if (TableOffenderStatusManager.sharedInstance().updateColumnInt(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS,
//                TableOffenderStatusManager.OFFENDER_ACTIVATE_STATUS.OFF_PENDING_ENROLMENT) >= 1) {
//
//            if (!DatabaseAccess.getInstance().tableEventLog.isEventExistsInDB(TableEventConfig.EventTypes.pendingEnrolment)) {
//                TableOffenderDetailsManager.sharedInstance().updateColumnString(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS, "");
//                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.pendingEnrolment, -1, -1);
//                NetworkCycleManager.getInstance().sendNewEventArray(NetworkCycleManager.NetworkRequestName.PostTerminate);
//
//                listener.updateUI();
////                updateHomeScreenUI();
//            } else {
//                NetworkCycleManager.getInstance().httpTerminateToken();
//            }
//
//        } else {
//            App.writeToNetworkLogsAndDebugInfo(TAG, "MainActivity - Can't update OFF_IS_OFFENDER_PENDING_ENROLMENT", EntityDebugInfo.DebugInfoModuleId.DB);
//            NetworkCycleManager.getInstance().httpTerminateToken();
//        }
//
//    }
//
//    @Override
//    public void downloadApkFromServer(String downloadURL, String apkTargetFileName, String versionFromServer, DownloadTaskMain.Download_Task_Type downloadTaskType) {
//        new DownloadTaskMain(MainActivity.this, apkTargetFileName, this, versionFromServer, downloadTaskType).downloadFromURL(downloadURL);
//        App.writeToNetworkLogsAndDebugInfo(TAG, "SW_UPGRADE: Started downloading the new version of [" + apkTargetFileName + "] .", EntityDebugInfo.DebugInfoModuleId.Network);
//    }
//
//    @Override
//    public void installApk(String apkTargetFileName) {
//        File apkTargetFile = new File(DownloadTaskMain.APK_LOCATION + apkTargetFileName);
//        NetworkCycleManager.getInstance().httpTerminateToken();
//        installDownloadedApk(apkTargetFile);
//    }
//
//    @Override
//    public void onManualHandleReceivedFromServer(int openEventId) {
//
//        EntityOpenEventLog openEventByEventId = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenEventByEventId(openEventId);
//        if (openEventByEventId != null) {
//            boolean isCategoryHasCloseEvent = DatabaseAccess.getInstance().tableEventConfig.isCategoryHasCloseEvent
//                    (openEventByEventId.OpenEventViolationCategory);
//
//            StringBuilder log = new StringBuilder();
//				/* if has close event, we will update on open event table that this event was handled manually, and then we will not use it while calculate
//				offender status, else we will delete this event from open event table */
//            if (isCategoryHasCloseEvent) {
//                DatabaseAccess.getInstance().tableOpenEventsLog.updateIsHandleColumnByOpenEventId(openEventId);
//                log.append("manual handle, updated event ").append(openEventId).append(" type ").append(openEventByEventId.OpenEventType).append(" to handle status, since he has close event");
//            } else {
//                DatabaseAccess.getInstance().tableOpenEventsLog.deleteOpenEventFromDB(openEventId);
//                log.append("manual handle, deleted open event ").append(openEventId).append(" type ").append(openEventByEventId.OpenEventType);
//            }
//
//            TableEventsManager.sharedInstance().updateOffenderStatus();
//            App.writeToNetworkLogsAndDebugInfo(TAG, log.toString(), EntityDebugInfo.DebugInfoModuleId.Network);
//        } else {
//            App.writeToNetworkLogsAndDebugInfo(TAG, "Failed to do manual handle, since openEventId " + openEventId + " not exists in local DB",
//                    EntityDebugInfo.DebugInfoModuleId.Network);
//        }
//
//        if (shouldBackToNormalProfile()) {
//            handlePMComProfileEnded("Manual handle received from server. No more open events with last profile id and above min duration time");
//        }
//
//        NetworkCycleManager.getInstance().sendHandleOffenderRequest();
//    }
//
//    @Override
//    public void onHandleResponseSucceeded(int requestType) {
//        if (NetworkCycleManager.OFFENDER_REQUEST_TYPE_TREATED == OffenderRequestsManager.OffenderRequestType.ACTIVATE) {
//            TableOffenderStatusManager.sharedInstance().updateColumnInt(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS,
//                    TableOffenderStatusManager.OFFENDER_ACTIVATE_STATUS.OFF_ALLOCATED);
//
//            locationManager.startLocationUpdate(false);
//
//            registerMagneticListenerIfSupported();
//            registerTemperatureListenerIfSupported();
//            startTagActivitiesIfNeeded();
//
//            TableZonesManager.sharedInstance().checkBeaconZoneStatus();
//
//            biometricScheduleManager.handleBiometricScheudleIfExists();
//
//            updateAppointmentsInScheduleScreen();
//
//            updateUIReceiver.registerForNextClosestTimeAppointment();
//
//            listener.updateUI();
////            updateHomeScreenUI();
//
//            if (!DatabaseAccess.getInstance().tableEventLog.isEventExistsInDB(TableEventConfig.EventTypes.eventMonitoringStarted)) {
//                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventMonitoringStarted, -1, -1,
//                        NumberComputationUtil.createRandomPassword().toString());
//            }
//        }
//    }
//
//    @Override
//    public void onBeaconZoneAddedToDB() {
//        if (bluetoothManager == null) {
//            bluetoothManager = new BluetoothManager(MainActivity.this, true);
//        }
//        bluetoothManager.initBeaconIndexesToDefaultValues();
//
//        restartBleScan();
//    }
//
//    @Override
//    public void onBeaconZoneDeletedFromDB() {
//        String additionalInfo = "Beacon was deleted from server";
//        TableZonesManager.sharedInstance().handleOutsideBeaconZone(true, additionalInfo);
//        String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
//        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
//                .getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
//        if (tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
//            stopBleScan();
//        } else if (!isBeaconExistsInDBZone) {
//            restartBleScan();
//        }
//
//    }
//
//    @Override
//    public void onZonesRequestFinishedToParse() {
//        updateUIReceiver.registerForNextClosestTimeAppointment();
//
//        EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
//        if (offenderLastGpsPoint != null) {
//            TableZonesManager.sharedInstance().checkZoneIntersection(offenderLastGpsPoint);
//        }
//
//        TableZonesManager.sharedInstance().checkBeaconZoneStatus();
//
//        if (!isOffenderInSuspendSchedule) {
//            listener.updateUI();
////            updateHomeScreenUI();
//        }
//
//        biometricScheduleManager.handleBiometricScheudleIfExists();
//
//        updateAppointmentsInScheduleScreen();
//    }
//
//    private void updateAppointmentsInScheduleScreen() {
//        if (schedulePagerAdapter != null) {
//            ScheduleTab fragment = (ScheduleTab) schedulePagerAdapter.getItemAt(mPager.getCurrentItem());
//            if (fragment != null) { // could be null if not instantiated yet
//
//                if (fragment.getView() != null) {
//
//                    // no need to call if fragment's onDestroyView()
//                    //has since been called.
//                    List<EntityScheduleOfZones> recordsScheduleOfZonesList = null;
//                    switch (mPager.getCurrentItem()) {
//                        case SchedulePagerAdapter.PAGE_0:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_MINUS_48_HOURS, SchedulePagerAdapter.OFFSET_MINUS_24_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_1:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_MINUS_24_HOURS, SchedulePagerAdapter.OFFSET_0_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_2:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_0_HOURS, SchedulePagerAdapter.OFFSET_24_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_3:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_24_HOURS, SchedulePagerAdapter.OFFSET_48_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_4:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_48_HOURS, SchedulePagerAdapter.OFFSET_72_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_5:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_72_HOURS, SchedulePagerAdapter.OFFSET_96_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_6:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_96_HOURS, SchedulePagerAdapter.OFFSET_120_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_7:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_120_HOURS, SchedulePagerAdapter.OFFSET_144_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_8:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_144_HOURS, SchedulePagerAdapter.OFFSET_168_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_9:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_168_HOURS, SchedulePagerAdapter.OFFSET_192_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_10:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_192_HOURS, SchedulePagerAdapter.OFFSET_216_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_11:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_216_HOURS, SchedulePagerAdapter.OFFSET_240_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_12:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_240_HOURS, SchedulePagerAdapter.OFFSET_264_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_13:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_264_HOURS, SchedulePagerAdapter.OFFSET_288_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_14:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_288_HOURS, SchedulePagerAdapter.OFFSET_312_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_15:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_312_HOURS, SchedulePagerAdapter.OFFSET_336_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_16:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_336_HOURS, SchedulePagerAdapter.OFFSET_360_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_17:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_360_HOURS, SchedulePagerAdapter.OFFSET_384_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_18:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_384_HOURS, SchedulePagerAdapter.OFFSET_408_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_19:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_408_HOURS, SchedulePagerAdapter.OFFSET_432_HOURS);
//                            break;
//                        case SchedulePagerAdapter.PAGE_20:
//                            recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(SchedulePagerAdapter.OFFSET_456_HOURS, SchedulePagerAdapter.OFFSET_480_HOURS);
//                            break;
//                    }
//                    fragment.updateAppointmentScheduleList(recordsScheduleOfZonesList);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onSucceededToDownloadFileFromServer(File apkTargetFile, String apkTargetShortFileName, String versionFromServer, DownloadTaskMain.Download_Task_Type downloadTaskType) {
//        String messageToUpload = "SW_UPGRADE: Success downloading new version of [ " + apkTargetShortFileName + " " + versionFromServer + " ] .";
//        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, EntityDebugInfo.DebugInfoModuleId.Network);
//
//        if (downloadTaskType == DownloadTaskMain.Download_Task_Type.PT_Version_Upgrade) {
//            NetworkCycleManager.getInstance().httpTerminateToken();
//            TableOffenderStatusManager.sharedInstance().updateColumnString(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_DEVICE_DOWNLOADED_VERSION, versionFromServer);
//            installDownloadedApk(apkTargetFile);
//        } else if (downloadTaskType == DownloadTaskMain.Download_Task_Type.All_Apk_Upgrade) {
//            NetworkCycleManager.getInstance().httpTerminateToken();
//            openApkInstallDialog(apkTargetFile);
//        } else if (downloadTaskType == DownloadTaskMain.Download_Task_Type.Google_Play_Version) {
//            openApkInstallDialog(apkTargetFile);
//        }
//    }
//
//    @Override
//    public void onFailedToDownloadFileFromServer(String result, String apkTargetShortFileName, String versionFromServer, DownloadTaskMain.Download_Task_Type downloadTaskType) {
//        String messageToUpload = "SW_UPGRADE: Failed downloading new version of [ " + apkTargetShortFileName + " " + versionFromServer + " ] . Error : " + result;
//        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, EntityDebugInfo.DebugInfoModuleId.errors);
//
//        if (downloadTaskType == DownloadTaskMain.Download_Task_Type.PT_Version_Upgrade || downloadTaskType == DownloadTaskMain.Download_Task_Type.All_Apk_Upgrade) {
//            NetworkCycleManager.getInstance().handleOffenderRequestError();
//            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.softwareUpgradeFailed, -1, -1);
//        }
//    }
//
//    private void installDownloadedApk(File apkTargetFile) {
//
//        isUpgradeToNewVersionScreenShouldOpen = false;
//        isCameFromRegularActivityCode = true;
//        if (!setActivityToForegroundIfNeeded()) {
//            openApkInstallDialog(apkTargetFile);
//        }
//        //Will wait 5 min for the User to install the Apk. After the time out will send softwareUpgradeTimeOut event.
//        upgradeTimeoutHandler.postDelayed(upgradeTimoutScreenRunnable, TimeUnit.MINUTES.toMillis(5));
//        //	}
//    }
//
//    @Override
//    public void addLbsLocation(EntityGpsPoint lbsRecord) {
//        locationManager.handleNewLbsLocation(lbsRecord);
//
//    }
//
//    @Override
//    public boolean isLbsLocationRequestRequired() {
//        return locationManager.IsLbsRequestRequired();
//    }
//
//    @Override
//    public void onOffenderAtHomeStatusChanged(boolean isAtHome) {
//        boolean previousPureComZoneMode = LocationManager.isDeviceInPureComZoneState();
//        locationManager.handlePureComZone(isAtHome);
//        // turn on/off bluetooth scanning - to save battery when at PureCom beacon mode
//        if (isAtHome) {
//            stopBleActivities(true);
//        } else {
//            if (previousPureComZoneMode) {
//                startTagActivitiesIfNeeded();
//            }
//        }
//    }
//
//
//    @Override
//    public void enableFlightMode(int timeOut) {
//        boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
//        if (isKnoxLicenceActivated) {
//
//            KnoxUtil.getInstance().initDeviceToInitiatedFlightMode();
//
//            initInitiatedFlightModeAlarmIfNeeded(System.currentTimeMillis() +
//                    TimeUnit.SECONDS.toMillis(timeOut));
//
//            stopBleActivities(true);
//
//            TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_TIME_INITIATED_FLIGHT_MODE_END,
//                    System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeOut));
//
//            // UI actions
//            if (currentScreen == MainActivity.ScreenType.Call) {
//                // If during "call" screen and flight mode starts, go back to main screen
//                initScreen(MainActivity.ScreenType.Home);
//            } else {
//                // disable "call" button: home, schedule, msg screens
//                buttonAppaCall.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
//            }
//        }
//    }
//}
