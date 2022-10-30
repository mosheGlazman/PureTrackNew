package com.supercom.puretrack.data.source.local.local_managers.hardware;


import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.BEACON_MAC_ADDRESS;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MAC_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MANUFACTURER_ID;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.TAG_MAC_ADDRESS;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.supercom.puretrack.data.service.heart_beat.HeartBeatServiceJava2;
import com.supercom.puretrack.data.service.heart_beat.ServiceUtils;
import com.supercom.puretrack.data.source.local.local_managers.parsing.BluetoothModelManager;
import com.supercom.puretrack.data.source.local.local_managers.parsing.BluetoothModelManager.BluetoothModelManagerListener;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.BeaconModel;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.TagModel;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BluetoothManager implements BluetoothModelManagerListener {
    public static BluetoothDevice device;
    public static Date lastDeviceFound=new Date();
    public static BluetoothDevice getDevice(){
        return device;
    }

    public static final String NO_TAG = "0";

    private static final int VERSION_2_4_3 = 1;
    public static final int VERSION_2_5_X = 2;
    private static final long TIME_TO_CLOSE_IF_HAS_OPEN_BEACON_EVENT = TimeUnit.MINUTES.toMillis(10);
    private static final int MAX_FRAU_TAMPER_COUNT = 3;
    private int CaseTamperIndexOld;
    private int StrapTamperIndexOld;
    private int TagTxIndexOld;
    private int TagCounterOfTxIndex = 0;
    private int BeaconTxIndexOld;
    private int BeaconCounterOfTxIndex = 0;
    private int BeaconCaseTamperIndexOld;
    private int tagMotionTamperIndexOld;
    private int BeaconMotionTamperIndexOld;
    private int BeaconProximityTamperIndexOld;
    private final BluetoothManagerListener bluetoothManagerListener;
    private long isFirstTimeWereAllBeaconEventsClosed;
    private boolean isFirstTimeAfterAllBeaconEventsWereClosed = true;
    private final boolean shouldHandlePacketDataAfterParse;

    BluetoothModelManager bluetoothModelManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private android.bluetooth.BluetoothManager bluetoothManager;
    private boolean isBluetoothScanningActive = false;

    public static boolean didGetBleSignals = false;


    public interface BluetoothManagerListener {
        void onOpenBeaconEventStatusChanged();

        void onBluetoothManagerModelsHandled(BeaconModel beaconModel, TagModel tagModel);
    }

    public BluetoothManager(BluetoothManagerListener bluetoothManagerListener, boolean shouldHandlePacketDataAfterParse) {

        //tag
        StrapTamperIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP_INDEX);
        CaseTamperIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE_INDEX);
        TagTxIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_TX_INDEX);

        //beacon
        BeaconProximityTamperIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX_INDEX);
        BeaconCaseTamperIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE_INDEX);
        BeaconMotionTamperIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION_INDEX);
        tagMotionTamperIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_MOTION_INDEX);
        BeaconTxIndexOld = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_TX_INDEX);

        this.bluetoothManagerListener = bluetoothManagerListener;

        this.shouldHandlePacketDataAfterParse = shouldHandlePacketDataAfterParse;


        init();
    }

    private void init() {
        bluetoothModelManager = new BluetoothModelManager(this);
        try {
            bluetoothManager = (android.bluetooth.BluetoothManager) App.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();
        } catch (Exception ignored) {

        }
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                device = result.getDevice();
                lastDeviceFound = new Date();

                if (LocationManager.isDeviceInPureComZoneState()) return;
                bluetoothModelManager.parseResult(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
            }
        };
    }


    public void initBeaconIndexesToDefaultValues() {
        BeaconProximityTamperIndexOld = MainActivity.NO_RESULT_STATUS;
        BeaconCaseTamperIndexOld = MainActivity.NO_RESULT_STATUS;
        BeaconMotionTamperIndexOld = MainActivity.NO_RESULT_STATUS;
        BeaconTxIndexOld = MainActivity.NO_RESULT_STATUS;
    }

    public void initTagIndexesToDefaultValues() {
        StrapTamperIndexOld = MainActivity.NO_RESULT_STATUS;
        CaseTamperIndexOld = MainActivity.NO_RESULT_STATUS;
        TagTxIndexOld = MainActivity.NO_RESULT_STATUS;
        tagMotionTamperIndexOld = MainActivity.NO_RESULT_STATUS;
    }

    public void startScan() {

        String pureTagMacAddress = TableScannerTypeManager.sharedInstance().getStringValueByColumnName(TAG_MAC_ADDRESS);
        String beaconMacAddress = TableScannerTypeManager.sharedInstance().getStringValueByColumnName(BEACON_MAC_ADDRESS);

        boolean isManufacturerIdEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MANUFACTURER_ID) > 0;
        boolean isMacScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MAC_SCAN_ENABLED) > 0;
        boolean isPureTagMacAddressEmpty = pureTagMacAddress.isEmpty();
        boolean isPureBeaconMacAddressEmpty = false;
        if (beaconMacAddress == null  || beaconMacAddress.isEmpty()) {
             isPureBeaconMacAddressEmpty = true;
        }

        List<ScanFilter> scanFilterArray = new ArrayList<>();

        if (isManufacturerIdEnabled) {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setManufacturerData(0x0101, new byte[]{});
            ScanFilter filter = builder.build();
            scanFilterArray.add(filter);
        }

        if (isMacScanEnabled && !isPureTagMacAddressEmpty) {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(pureTagMacAddress);
            ScanFilter filter = builder.build();
            scanFilterArray.add(filter);
        }

        if (isMacScanEnabled && !isPureBeaconMacAddressEmpty) {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceAddress(beaconMacAddress);
            ScanFilter filter = builder.build();
            scanFilterArray.add(filter);
        }

        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        if (scanFilterArray.isEmpty()) {
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            try {
                bluetoothLeScanner.startScan(scanFilterArray, scanSettings, scanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isBluetoothScanningActive = true;
    }


    public void stopScan() {
        bluetoothLeScanner.stopScan(scanCallback);
        isBluetoothScanningActive = false;
    }

    @Override
    public void onBluetoothDeviceModelsParsed(BeaconModel beaconModel, TagModel tagModel) {

        int offenderActivateStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS);

        if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_ALLOCATED && shouldHandlePacketDataAfterParse) {

            // found beacon packet data
            if (beaconModel != null) {
                handleBeaconPacketData(beaconModel);
            }

            // found tag packet data
            if (tagModel != null) {
                handleTagPacketData(tagModel);
            }
        }
        if (isBluetoothScanningActive) {
            bluetoothManagerListener.onBluetoothManagerModelsHandled(beaconModel, tagModel);
        }
    }

    private void handleBeaconPacketData(BeaconModel bleBeaconPacketData) {

        int BeaconProximityTamperIndexNew = bleBeaconPacketData.getBeaconProximityTamperIndexNew();
        if (BeaconProximityTamperIndexOld == MainActivity.NO_RESULT_STATUS) {
            BeaconProximityTamperIndexOld = BeaconProximityTamperIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX_INDEX, BeaconProximityTamperIndexOld);
            if (bleBeaconPacketData.isBeaconTamperProximityOpen()) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperProximityOpen, -1, -1);
            }
        }

        int BeaconCaseTamperIndexNew = bleBeaconPacketData.getCaseTamperIndexNew();
        if (BeaconCaseTamperIndexOld == MainActivity.NO_RESULT_STATUS) {
            BeaconCaseTamperIndexOld = BeaconCaseTamperIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE_INDEX, BeaconCaseTamperIndexOld);
            if (bleBeaconPacketData.isBeaconTamperCaseOpen()) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperCaseOpen, -1, -1);
            }
        }

        int beaconMotionTamperIndexNew = bleBeaconPacketData.getMotionTamperIndexNew();
        if (BeaconMotionTamperIndexOld == MainActivity.NO_RESULT_STATUS) {
            BeaconMotionTamperIndexOld = beaconMotionTamperIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION_INDEX, BeaconMotionTamperIndexOld);
            if (bleBeaconPacketData.isTagTamperCaseOpen()) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconMotionTamperOpen, -1, -1);
            }
        }

        if (bleBeaconPacketData.isBeaconTamperProximityOpen()) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX, 1);
            if (BeaconProximityTamperIndexOld != BeaconProximityTamperIndexNew) {

                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_PROXIMITY) != -1;
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperProximityOpen, -1, -1);
                }
                BeaconProximityTamperIndexOld = BeaconProximityTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX_INDEX, BeaconProximityTamperIndexOld);
            }
        } else {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX) == 1) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX, 0);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperProximityClose, -1, -1);
            }
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX, 0); // Tamper
            if (BeaconProximityTamperIndexOld != BeaconProximityTamperIndexNew) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperProximityOpen, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperProximityClose, -1, -1);
                BeaconProximityTamperIndexOld = BeaconProximityTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX_INDEX, BeaconProximityTamperIndexOld);
            }
        }
        if (bleBeaconPacketData.isBeaconTamperCaseOpen()) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE, 1);
            if (BeaconCaseTamperIndexOld != BeaconCaseTamperIndexNew) {
                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_CASE) != -1;
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperCaseOpen, -1, -1);
                }
                BeaconCaseTamperIndexOld = BeaconCaseTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE_INDEX, BeaconCaseTamperIndexOld);
            }
        } else {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE) == 1) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE, 0);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperCaseClose, -1, -1);
            }
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE, 0);
            if (BeaconCaseTamperIndexOld != BeaconCaseTamperIndexNew) {

                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperCaseOpen, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconTamperCaseClose, -1, -1);

                BeaconCaseTamperIndexOld = BeaconCaseTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE_INDEX, BeaconCaseTamperIndexOld);
            }
        }
        boolean isInMotionTamper;
        if (bleBeaconPacketData.getStructureVersion() >= VERSION_2_5_X) {
            isInMotionTamper = bleBeaconPacketData.isMotionTamperSticky();
        } else {
            isInMotionTamper = bleBeaconPacketData.isInMotionTamperCurrent();
        }

        if (isInMotionTamper) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION, 1);
            if (BeaconMotionTamperIndexOld != beaconMotionTamperIndexNew) {
                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_BEACON_MOTION_TAMPER) != -1;
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconMotionTamperOpen, -1, -1);
                }
                BeaconMotionTamperIndexOld = beaconMotionTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION_INDEX, BeaconMotionTamperIndexOld);
            }
        } else {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION) == 1) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION, 0);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconMotionTamperClose, -1, -1);

                if (bleBeaconPacketData.getStructureVersion() == VERSION_2_4_3) {
                    BeaconMotionTamperIndexOld = beaconMotionTamperIndexNew;
                    TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION_INDEX, BeaconMotionTamperIndexOld);
                }
            }
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION, 0);

            if (BeaconMotionTamperIndexOld != beaconMotionTamperIndexNew) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconMotionTamperOpen, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconMotionTamperClose, -1, -1);
                BeaconMotionTamperIndexOld = beaconMotionTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION_INDEX, BeaconMotionTamperIndexOld);
            }
        }


        if (bleBeaconPacketData.isBatteryTamperCurrent()) {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_BATTERY) == 0) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.beaconBatteryTamper, -1, -1);
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_BATTERY, 1);
            }
        } else {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_BATTERY) == 1) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.beaconBatteryTamperClosed, -1, -1);
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_BATTERY, 0);
            }
        }

        handleIfHasAnyOpenBeaconEvent();
        handleBeaconFrauTamperEvents(bleBeaconPacketData.getRollingCode());
    }

    private void handleIfHasAnyOpenBeaconEvent() {

        boolean wasFoundAnyOpenEvent = false;
        if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX) == 1) {
            wasFoundAnyOpenEvent = true;
        }

        if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE) == 1) {
            wasFoundAnyOpenEvent = true;
        }

        if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_MOTION) == 1) {
            wasFoundAnyOpenEvent = true;
        }

        boolean hasAnyOpenBeaconEvent = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_HAS_OPEN_EVENT) == 1;
        if (wasFoundAnyOpenEvent) {
            if (!hasAnyOpenBeaconEvent) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_HAS_OPEN_EVENT, 1);
                bluetoothManagerListener.onOpenBeaconEventStatusChanged();
            }

            isFirstTimeAfterAllBeaconEventsWereClosed = true;
        } else if (hasAnyOpenBeaconEvent) {
            if (isFirstTimeAfterAllBeaconEventsWereClosed) {
                isFirstTimeWereAllBeaconEventsClosed = System.currentTimeMillis();
                isFirstTimeAfterAllBeaconEventsWereClosed = false;
            } else if (System.currentTimeMillis() - isFirstTimeWereAllBeaconEventsClosed >= TIME_TO_CLOSE_IF_HAS_OPEN_BEACON_EVENT) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_HAS_OPEN_EVENT, 0);
                bluetoothManagerListener.onOpenBeaconEventStatusChanged();
            }
        }

    }

    private void handleTagPacketData(TagModel tagModel) {
        int StrapTamperIndexNew = tagModel.getStrapTamperIndexNew();
        if (StrapTamperIndexOld == MainActivity.NO_RESULT_STATUS) {
            StrapTamperIndexOld = StrapTamperIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP_INDEX, StrapTamperIndexOld);
            if (tagModel.isTagStrapOpen()) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperStrapOpen, -1, -1);
            }
        }

        int CaseTamperIndexNew = tagModel.getCaseTamperIndexNew();
        if (CaseTamperIndexOld == MainActivity.NO_RESULT_STATUS) {
            CaseTamperIndexOld = CaseTamperIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE_INDEX, CaseTamperIndexOld);
            if (tagModel.isTagTamperCaseOpen()) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperCaseOpen, -1, -1);
            }
        }

        if (tagModel.isTagStrapOpen()) {

            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP, 1);

            if (StrapTamperIndexOld != StrapTamperIndexNew) {
                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_STRAP_TAMPER) != -1;
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperStrapOpen, -1, -1);
                }
                StrapTamperIndexOld = StrapTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP_INDEX, StrapTamperIndexOld);
            }
        } else {
            // Tamper is now Close
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP) == 1) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP, 0);
                // last time the tamper was open

                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperStrapClose, -1, -1);

            }
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP, 0);

            if (StrapTamperIndexOld != StrapTamperIndexNew) {
                Log.i("BLE_PROXIMITY_EVENT", " StrapTamperIndexOld: " + StrapTamperIndexOld + " StrapTamperIndexNew:  " + StrapTamperIndexNew);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperStrapOpen, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperStrapClose, -1, -1);

                StrapTamperIndexOld = StrapTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP_INDEX, StrapTamperIndexOld);
            }
        }

        if (tagModel.isTagTamperCaseOpen()) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE, 1);
            if (CaseTamperIndexOld != CaseTamperIndexNew) {
                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_CASE_TAMPER) != -1;
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperCaseOpen, -1, -1);
                }
                CaseTamperIndexOld = CaseTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE_INDEX, CaseTamperIndexOld);
            }
        } else {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE) == 1) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE, 0);

                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperCaseClose, -1, -1);

            }
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE, 0);

            if (CaseTamperIndexOld != CaseTamperIndexNew) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperCaseOpen, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperCaseClose, -1, -1);

                CaseTamperIndexOld = CaseTamperIndexNew;
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE_INDEX, CaseTamperIndexOld);
            }
        }

        if (tagModel.isBatteryTamperCurrent()) {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY) == 0) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperBatteryLow, -1, -1);
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY, 1);
            }
        } else {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY) == 1) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagTamperBatteryNormal, -1, -1);
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY, 0);
            }
        }
        handleTagFraudTamperEvents(tagModel.getRollingCode());
        int tagMotionTamperIndexNew = tagModel.getMotionTamperIndexNew();
        if (tagMotionTamperIndexOld == MainActivity.NO_RESULT_STATUS) {
            tagMotionTamperIndexOld = tagMotionTamperIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_STAT_MOTION_INDEX, tagMotionTamperIndexOld);
        }
    }

    private void handleTagFraudTamperEvents(int TxIndexNew) {
        if (TagTxIndexOld == TxIndexNew) {
            if (TagCounterOfTxIndex < MAX_FRAU_TAMPER_COUNT) {
                TagCounterOfTxIndex++;
            }

            if (TagCounterOfTxIndex == MAX_FRAU_TAMPER_COUNT) {
                TagCounterOfTxIndex++;
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagFrauTamperOpen, -1, -1);
            }
        } else {
            if (TagCounterOfTxIndex >= MAX_FRAU_TAMPER_COUNT) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventTagFrauTamperClose, -1, -1);
            }
            TagCounterOfTxIndex = 0;
            TagTxIndexOld = TxIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_TAG_TX_INDEX, TxIndexNew);
        }
    }

    private void handleBeaconFrauTamperEvents(int TxIndexNew) {
        if (BeaconTxIndexOld == TxIndexNew) {
            if (BeaconCounterOfTxIndex < MAX_FRAU_TAMPER_COUNT) {
                BeaconCounterOfTxIndex++;
            }

            if (BeaconCounterOfTxIndex == MAX_FRAU_TAMPER_COUNT) {
                BeaconCounterOfTxIndex++;
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconFrauTamperOpen, -1, -1);
            }
        } else {
            if (BeaconCounterOfTxIndex >= MAX_FRAU_TAMPER_COUNT) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventBeaconFrauTamperClose, -1, -1);
            }
            BeaconCounterOfTxIndex = 0;
            BeaconTxIndexOld = TxIndexNew;
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_BEACON_TX_INDEX, TxIndexNew);
        }
    }
}
