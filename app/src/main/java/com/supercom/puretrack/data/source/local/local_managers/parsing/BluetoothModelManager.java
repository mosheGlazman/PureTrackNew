package com.supercom.puretrack.data.source.local.local_managers.parsing;

import android.bluetooth.BluetoothDevice;

import com.supercom.puretrack.data.source.local.local_managers.business_logic.TagMotionManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.BeaconModel;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.TagModel;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.TagModel.HardwareTypeInt;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableGuestTagManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.encryption.ConversionUtil;
import com.supercom.puretrack.util.encryption.EncryptionUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.general.LoggingUtil.HardwareTypeString;
import com.supercom.puretrack.util.general.LoggingUtil.OperationType;

import java.util.concurrent.TimeUnit;

public class BluetoothModelManager {

    private long LongLastPureBeaconPacketRx;
    private long tempLastPureBeaconPacketRx;
    private int LastRssiTagFix = 0;
    private byte[] scanRecord = new byte[100];
    private final BluetoothModelManagerListener bluetoothModelManagerListener;
    private long tempLastTagPacketRx;

    public int totalTagReceptions = 0;
    public int totalTagMotionReceptions = 0;
    public int totalTagNoMotionReceptions = 0;

    private final TagMotionManager tagMotionManager = new TagMotionManager();

    public interface BluetoothModelManagerListener {
        void onBluetoothDeviceModelsParsed(BeaconModel beaconModel, TagModel tagModel);
    }

    public BluetoothModelManager(BluetoothModelManagerListener bluetoothModelManagerListener) {
        this.bluetoothModelManagerListener = bluetoothModelManagerListener;
    }


    public void parseResult(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

        //New Packet - Tag versions 2.3.3 and above
        if (isNewVersionOfTag(scanRecord)) {
            this.scanRecord = scanRecord;
            int tempHardwareId = (this.scanRecord[6] & 0xFF) + (this.scanRecord[7] & 0xFF) * 256 + (this.scanRecord[8] & 0xFF) * 65536;
            int siteCode = this.scanRecord[5] & 0xFF;
            if (tempHardwareId == 0 || siteCode != 71) {
                // Tag ID 0 is not a legal SuperCom Tag and siteCode != 71 is not a valid supercom device
                return;
            }

            String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
            String beaconIdFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID);
            String idAsString = String.valueOf(tempHardwareId);
            if (!idAsString.equals(tagRFIDFromServer) && !idAsString.equals(beaconIdFromServer)) {
                TableGuestTagManager.sharedInstance().onGuestTagDetected(tempHardwareId);
            }

            //tag
            if (idAsString.equals(tagRFIDFromServer)) {
                TagModel tagModel = getTagModel(device, rssi, scanRecord, HardwareTypeInt.New_Tag);
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS, device.getAddress());
                bluetoothModelManagerListener.onBluetoothDeviceModelsParsed(null, tagModel);
            }
            //beacon
            else if (idAsString.equals(beaconIdFromServer)) {
                LongLastPureBeaconPacketRx = System.currentTimeMillis();
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_BEACON_RECEIVE, (int) (LongLastPureBeaconPacketRx / 1000));
                boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
                isBeaconExistsInDBZone = true;
                if (isBeaconExistsInDBZone) {
                    BeaconModel beaconModel = (BeaconModel) getTagModel(device, rssi, scanRecord, HardwareTypeInt.New_Beacon);
                    TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_BEACON_ADDRESS, device.getAddress());
                    if (beaconModel != null) {
                        beaconModel.setLongLastPureBeaconPacketRx(LongLastPureBeaconPacketRx);
                    }
                    bluetoothModelManagerListener.onBluetoothDeviceModelsParsed(beaconModel, null);
                }
            }
        }
    }

    private boolean isNewVersionOfTag(final byte[] scanRecord) {
        return scanRecord[4] >= 0x02;
    }

    private TagModel getTagModel(final BluetoothDevice device, final int rssi, final byte[] scanRecord, int hardwareType) {
        if (rssi == 127) {
            LastRssiTagFix = 0;
        } else {
            LastRssiTagFix = rssi;
        }

        long longLastHardwarePacketRx = System.currentTimeMillis();
        TableOffenderStatusManager.sharedInstance().updateColumnLong(OFFENDER_STATUS_CONS.OFF_LAST_TAG_RECEIVE, (int) (longLastHardwarePacketRx / 1000));
        TagModel bleHardwarePacketData = createTagModel(LastRssiTagFix, hardwareType, device);
        if (bleHardwarePacketData != null) {
            bleHardwarePacketData.setHardwareType(hardwareType);
            bleHardwarePacketData.setConnectionRssi(LastRssiTagFix);
            bleHardwarePacketData.setLongLastPureTagPacketRx(longLastHardwarePacketRx);
            bleHardwarePacketData.setScanRecord(scanRecord);
        }
        return bleHardwarePacketData;
    }


    private TagModel createTagModel(int BroadcastRssi, int hardwareType, BluetoothDevice device) {

        int DiscoveryMode = scanRecord[1] & 0xFF;
        int LimitedDiscoveryMode = scanRecord[2] & 0xFF;
        int LengthOfDataByte3 = scanRecord[3] & 0xFF;
        int AdvertisingStructureVersion = scanRecord[4] & 0xFF;
        int tempTagId = (scanRecord[6] & 0xFF) + (scanRecord[7] & 0xFF) * 256 + (scanRecord[8] & 0xFF) * 65536;

        byte[] CtAESbuffer = new byte[16];

        for (int k = 0; k < 16; k++) {
            CtAESbuffer[k] = (byte) (scanRecord[k + 9] & 0xFF);
        }

        /*********************
         * AES128 Decrypt
         * *******************/
        byte[] AESKey;
        TagModel tagModel = null;
        if (hardwareType == HardwareTypeInt.New_Tag) {
            AESKey = ConversionUtil.convertHexStringToByteArray(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                    (OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ENCRYPTION));
            boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.TAG_ENCRYPTION) != -1;
            if (isCRCOk(AESKey, CtAESbuffer)) {
                if (hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.tagEncryptionRecovered, -1, -1);
                }
                // STEP 1
                tagModel = parseTagDataAndCreateObject(BroadcastRssi, hardwareType, DiscoveryMode, LimitedDiscoveryMode, LengthOfDataByte3, AdvertisingStructureVersion, tempTagId, device);
            } else {
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.tagEncryptionError, -1, -1);
                }
            }
        } else if (hardwareType == HardwareTypeInt.New_Beacon) {
            AESKey = ConversionUtil.convertHexStringToByteArray(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ENCRYPTION));
            boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.BEACON_ENCRYPTION) != -1;
            if (isCRCOk(AESKey, CtAESbuffer)) {
                if (hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.beaconEncryptionRecovered, -1, -1);
                }
                tagModel = parseBeaconDataAndCreateObject(BroadcastRssi, hardwareType, DiscoveryMode, LimitedDiscoveryMode, LengthOfDataByte3, AdvertisingStructureVersion, tempTagId, device);
            } else {
                if (!hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.beaconEncryptionError, -1, -1);
                }
            }
        }
        return tagModel;
    }

    private boolean isCRCOk(byte[] AESKey, byte[] CtAESbuffer) {

        boolean isCRCOk = false;
        byte[] PtAESbuffer;
        try {
            PtAESbuffer = EncryptionUtil.NewDecrypt(AESKey, CtAESbuffer);
            int generateChecksumCRC16 = ConversionUtil.GenerateChecksumCRC16(getCrcBufferToPassChecksum(PtAESbuffer));
            int crcFromPacket = ((PtAESbuffer[14] & 0xff) << 8) + (PtAESbuffer[15] & 0xff);
            if (generateChecksumCRC16 == crcFromPacket) {
                System.arraycopy(PtAESbuffer, 0, scanRecord, 9, 16);
                isCRCOk = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isCRCOk;
    }


    private TagModel parseBeaconDataAndCreateObject(int BroadcastRssi, int hardwareType, int DiscoveryMode, int LimitedDiscoveryMode, int LengthOfDataByte3, int AdvertisingStructureVersion,
                                                    int tempBeaconId, BluetoothDevice device) {

        int MASK = 0xFF;
        int LightAdcValue;
        int IrOffAdcValue;
        int IrOnAdcValue;

        BeaconModel beaconPacketData = new BeaconModel();

        beaconPacketData.setTempBeaconId(tempBeaconId);

        beaconPacketData.setStructureVersion(AdvertisingStructureVersion);

        String stringSiteCode = String.valueOf(scanRecord[5] & 0xFF);

        int rollingCode = scanRecord[9] & MASK;
        rollingCode = rollingCode + ((scanRecord[10] & MASK) << 8);
        beaconPacketData.setRollingCode(rollingCode);

        // STICKY
        int StickyTamper = scanRecord[11] & 0x01;
        beaconPacketData.setStickyTamper(StickyTamper);

        // TAG STRAP
        boolean isBeaconTamperProximityOpen = (scanRecord[11] & 0x10) != 0;
        beaconPacketData.setBeaconTamperProximityOpen(isBeaconTamperProximityOpen);

        // CASE TAMPER HANDELR
        // STICKY
        int BleCaseTamperSticky = scanRecord[11] & 0x02;
        beaconPacketData.setStickyTamper(BleCaseTamperSticky);

        // TAG CASE
        boolean isBeaconTamperCaseOpen = ((scanRecord[11] & 0x20) != 0);
        beaconPacketData.setBeaconTamperCaseOpen(isBeaconTamperCaseOpen);

        // BATTERY TAMPER HANDELR
        boolean BleBatteryTamperCurrent = ((scanRecord[11] & 0x40)) != 0;
        beaconPacketData.setBatteryTamperCurrent(BleBatteryTamperCurrent);

        /// MOTION TAMPER HANDELR
        boolean BleMotionTamperCurrent = ((scanRecord[11] & 0x80)) != 0;
        beaconPacketData.setMotionTamperCurrent(BleMotionTamperCurrent);

        /// MOTION TAMPER STICKY
        boolean BleMotionTamperSticky = ((scanRecord[11] & 0x08)) != 0;
        beaconPacketData.setMotionTamperSticky(BleMotionTamperSticky);


        int motionTamperIndex = scanRecord[14] & MASK;
        motionTamperIndex = motionTamperIndex + ((scanRecord[15] & MASK) << 8);
        beaconPacketData.setMotionTamperIndexNew(motionTamperIndex);

        IrOffAdcValue = scanRecord[15] & MASK;    // New
        beaconPacketData.setIntIrOffAdcValue(IrOffAdcValue);

        IrOnAdcValue = scanRecord[16] & MASK;    // New
        beaconPacketData.setIrOnAdcValue(IrOnAdcValue);

        LightAdcValue = scanRecord[16] & MASK;

        int x = (scanRecord[19] & MASK);
        int Step2 = x * 16;
        float floatBattery = (float) Step2 / 1000;
        TableOffenderStatusManager.sharedInstance().updateColumnFloat(OFFENDER_STATUS_CONS.OFF_BEACON_BATTERY_LEVEL, floatBattery);
        int CaseTamperIndex = scanRecord[20] & MASK;
        beaconPacketData.setCaseTamperIndexNew(CaseTamperIndex);
        //CaseTamperIndex = CaseTamperIndex + ((PureTagBroadCastPacket[20] & MASK) << 8);

        int BeaconProximityTamperIndexNew = scanRecord[21] & MASK;
        BeaconProximityTamperIndexNew = BeaconProximityTamperIndexNew + ((scanRecord[22] & MASK) << 8);
        beaconPacketData.setBeaconProximityTamperIndexNew(BeaconProximityTamperIndexNew);

        String motionToPrint = "";
        if (AdvertisingStructureVersion >= BluetoothManager.VERSION_2_5_X) {
            motionToPrint = "\nMotion Sticky:" + BleMotionTamperSticky;
        } else {
            motionToPrint = "\nMotion Current:" + BleMotionTamperCurrent;
        }

        String stringPacketUi = "Beacon 2.35+ Packet:" +
                "\nAdd: " + device.getAddress() +
                "\nRSSI: " + BroadcastRssi +
                "\nBeacon Range: " + TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_RANGE) +
                "\nLength: " + LengthOfDataByte3 +
                "\nDiscovery Mode: " + DiscoveryMode +
                "\nLimitedDiscovery: " + LimitedDiscoveryMode +
                "\nAdvertising Structure Ver: " + AdvertisingStructureVersion +
                "\nSite Code: " + stringSiteCode + //more info except Tag Id
                "\nTag ID: " + tempBeaconId +
                "\nIndex: " + rollingCode + //every time tag sends info --> index++
                "\nCase Current:  " + isBeaconTamperCaseOpen +
                "\nProximity Current: " + isBeaconTamperProximityOpen +
                "\nBattery Current: " + BleBatteryTamperCurrent +
                motionToPrint +
                "\nIR OFF: " + IrOffAdcValue +
                "\nIR ON: " + IrOnAdcValue + //should be more than 100
                "\nLight Sensor: " + LightAdcValue + //belongs to case
                "\nBattery: " + floatBattery + "[V]" + //2.5v - 3.2v
                "\nCaseTamperIdx: " + CaseTamperIndex +
                "\nMotionTamperIdx: " + motionTamperIndex +
                "\nProximityTamperIdx: " + BeaconProximityTamperIndexNew +
                "\n\n";

        beaconPacketData.setStringPacketUI(stringPacketUi);

        String csvFile = LoggingUtil.createStringForCSVFile(OperationType.ADV, HardwareTypeString.New_Beacon, String.valueOf(tempBeaconId), LastRssiTagFix, -1, device.getAddress(),
                rollingCode, floatBattery, BleMotionTamperSticky, BeaconProximityTamperIndexNew, CaseTamperIndex, motionTamperIndex,
                TimeUnit.MILLISECONDS.toSeconds(LongLastPureBeaconPacketRx - tempLastPureBeaconPacketRx), "");

        LoggingUtil.writeBleLogsToFile(csvFile);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFile,
                DebugInfoModuleId.Ble.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        String oldMacAddress = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_BEACON_ADDRESS);
        boolean isMacAddressChanged = isMacAddressChanged(oldMacAddress, device.getAddress());
        beaconPacketData.setIsMacAddressChanged(isMacAddressChanged);

        return beaconPacketData;
    }


    private TagModel parseTagDataAndCreateObject(int BroadcastRssi,
                                                 int hardwareType,
                                                 int DiscoveryMode,
                                                 int LimitedDiscoveryMode,
                                                 int LengthOfDataByte3,
                                                 int AdvertisingStructureVersion,
                                                 int tempTagId, BluetoothDevice device) {
        int MASK = 0xFF;
        int LightAdcValue = 0;
        int IrOffAdcValue = 0;
        int IrOnAdcValue = 0;

        TagModel tagModel = new TagModel();

        tagModel.setAdvertisingStructureVersion(AdvertisingStructureVersion);

        String stringSiteCode = String.valueOf(scanRecord[5] & 0xFF);

        int rollingCode = scanRecord[9] & MASK;
        rollingCode = rollingCode + ((scanRecord[10] & MASK) << 8);
        tagModel.setRollingCode(rollingCode);

        totalTagReceptions++;
        boolean isTagInMotionState = (scanRecord[11] & 0x80) != 0;
        if (isTagInMotionState) {
            totalTagMotionReceptions++;
        } else {
            totalTagNoMotionReceptions++;
        }

        if (DatabaseAccess.getInstance().tableTagMotion.isTagMotionEnabled()) {
            tagMotionManager.handleTagMotionsReceptions(totalTagReceptions, totalTagMotionReceptions, totalTagNoMotionReceptions, new TagMotionManager.TagMotionListener() {
                @Override
                public void clearData() {
                    resetTagMotionValues();
                }
            });
        } else {
            resetTagMotionValues();
        }

        // STICKY
        int StickyTamper = scanRecord[11] & 0x01;
        tagModel.setStickyTamper(StickyTamper);

        // TAG STRAP
        boolean isTagStrapOpen = ((scanRecord[11] & 0x10)) != 0;
        tagModel.setTagStrapOpen(isTagStrapOpen);

        // CASE TAMPER HANDELR
        // STICKY
        int BleCaseTamperSticky = scanRecord[11] & 0x02;
        tagModel.setStickyTamper(BleCaseTamperSticky);

        // TAG CASE
        boolean isTagTamperCaseOpen = ((scanRecord[11] & 0x20) != 0);
        tagModel.setTagTamperCaseOpen(isTagTamperCaseOpen);

        // BATTERY TAMPER HANDELR
        boolean BleBatteryTamperCurrent = ((scanRecord[11] & 0x40) != 0);
        tagModel.setBatteryTamperCurrent(BleBatteryTamperCurrent);

        /// MOTION TAMPER HANDELR
        boolean BleMotionTamperCurrent = ((scanRecord[11] & 0x80) != 0);
        tagModel.setMotionTamperCurrent(BleMotionTamperCurrent);

        /// MOTION TAMPER STICKY
        boolean BleMotionTamperSticky = ((scanRecord[11] & 0x08) != 0);
        tagModel.setMotionTamperSticky(BleMotionTamperSticky);

        int motionTamperIndex = scanRecord[14] & MASK;
        motionTamperIndex = motionTamperIndex + ((scanRecord[15] & MASK) << 8);
        tagModel.setMotionTamperIndexNew(motionTamperIndex);

        IrOnAdcValue = scanRecord[16] & MASK;    // New
        tagModel.setIrOnAdcValue(IrOnAdcValue);

        LightAdcValue = scanRecord[16] & MASK;

        int x = (scanRecord[19] & MASK);
        int Step2 = x * 16;
        float floatBattery = (float) Step2 / 1000;
        TableOffenderStatusManager.sharedInstance().updateColumnFloat(OFFENDER_STATUS_CONS.OFF_TAG_BATTERY_LEVEL, floatBattery);
        int CaseTamperIndex = scanRecord[20] & MASK;
        tagModel.setCaseTamperIndexNew(CaseTamperIndex);

        int StrapTamperIndex = scanRecord[21] & MASK;
        StrapTamperIndex = StrapTamperIndex + ((scanRecord[22] & MASK) << 8);
        tagModel.setStrapTamperIndexNew(StrapTamperIndex);

        long tabHBCounter = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_COUNTER) + 1;
        long tabADVCounter = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADV_COUNTER) + 1;

        boolean isInBeaconZone = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE)
                == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
        boolean isInHomeRadius = TableOffenderStatusManager.sharedInstance().isInHomeRadius();

        String stringPacketUi =
                "PureTag:" +
                        "\nMotion: " + isTagInMotionState +
                        "\nAdd: " + device.getAddress() +
                        "\nRSSI: " + BroadcastRssi +
                        "\nLength: " + LengthOfDataByte3 +
                        "\nDiscovery Mode: " + DiscoveryMode +
                        "\nLimitedDiscovery: " + LimitedDiscoveryMode +
                        "\nAdv Struct Ver: " + AdvertisingStructureVersion +
                        "\nSite Code: " + stringSiteCode + //more info except Tag Id
                        "\nTag ID: " + tempTagId +
                        "\nIndex: " + rollingCode + //every time tag sends info --> index++
                        "\nCase Current:  " + isTagTamperCaseOpen +
                        "\nStrap Current: " + isTagStrapOpen +
                        "\nBattery Current: " + BleBatteryTamperCurrent +
                        "\nMotion Sticky: " + BleMotionTamperSticky +
                        "\nIR OFF: " + IrOffAdcValue +
                        "\nIR ON: " + IrOnAdcValue + //should be more than 100
                        "\nLight Sensor: " + LightAdcValue + //belongs to case
                        "\nBattery: " + floatBattery + "[V]" + //2.5v - 3.2v
                        "\nCaseTamperIdx: " + CaseTamperIndex +
                        "\nStrapTamperIdx: " + StrapTamperIndex +
                        "\nMotionTamperIdx: " + motionTamperIndex +
                        "\nProximity: " + ((isInBeaconZone || isInHomeRadius) ? " home " : " outside ") +
                        TableOffenderStatusManager.sharedInstance().getProximityRssiLimit() +
                        "\nHB counter: " + tabHBCounter +
                        "\nADV counter: " + tabADVCounter +
                        "\n\n";
        tagModel.setStringPacketUI(stringPacketUi);

        // #MOJ
        MainActivity.ScreenOnBleTime = 0;
        MainActivity.lastTagReceivedTime = System.currentTimeMillis();
        MainActivity.lastBleTagRx = System.currentTimeMillis();
        MainActivity.lastBleTagRxDebug = System.currentTimeMillis();
        MainActivity.playBleRxTone = true;
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isOffenderAllocated) {
            TableOffenderDetailsManager.sharedInstance().updateColumnLong(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADV_COUNTER, tabADVCounter);
        }

        long currentTagPacketRs = System.currentTimeMillis();
        String csvFile = LoggingUtil.createStringForCSVFile(OperationType.ADV, HardwareTypeString.New_Tag, String.valueOf(tempTagId), LastRssiTagFix, tabADVCounter, device.getAddress(),
                rollingCode, floatBattery, BleMotionTamperSticky, StrapTamperIndex, CaseTamperIndex, motionTamperIndex,
                TimeUnit.MILLISECONDS.toSeconds(currentTagPacketRs - tempLastTagPacketRx), "");

        tempLastTagPacketRx = currentTagPacketRs;

        LoggingUtil.writeBleLogsToFile(csvFile);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFile,
                DebugInfoModuleId.Ble.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        String oldMacAddress = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS);
        boolean isMacAddressChanged = isMacAddressChanged(oldMacAddress, device.getAddress());
        tagModel.setIsMacAddressChanged(isMacAddressChanged);


        return tagModel;
    }

    private void resetTagMotionValues() {
        totalTagReceptions = 0;
        totalTagMotionReceptions = 0;
        totalTagNoMotionReceptions = 0;
    }

    private boolean isMacAddressChanged(String oldMacAddress, String newMacAddress) {
        return !oldMacAddress.equals(newMacAddress);
    }

    private int[] getCrcBufferToPassChecksum(byte[] PtAESbuffer) {
        byte[] crcBuffer = new byte[14];
        System.arraycopy(PtAESbuffer, 0, crcBuffer, 0, PtAESbuffer.length - 2);

        int[] intArray = new int[crcBuffer.length];

        // converting byteArray to intArray
        for (int i = 0; i < crcBuffer.length; i++) {
            intArray[i] = crcBuffer[i] & 0xff;
        }
        return intArray;
    }

}
