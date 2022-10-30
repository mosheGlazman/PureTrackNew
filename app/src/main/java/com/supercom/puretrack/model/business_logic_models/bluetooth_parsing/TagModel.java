package com.supercom.puretrack.model.business_logic_models.bluetooth_parsing;

public class TagModel extends BaseDeviceModel {

    public interface HardwareTypeInt {
        int New_Tag = 0;
        int New_Beacon = 1;
    }

    private int tempTagId;
    private int intIrOffAdcValue;
    private int IrOnAdcValue;
    private long TimeFromTag;
    private int StickyTamper;
    private int CurrentTamper;
    private int StrapTamperIndexNew;
    private int MotionTamperIndexNew;

    private boolean isTagStrapOpen;
    private boolean isTagTamperCaseOpen;
    private boolean batteryTamperCurrent;
    private boolean isInMotionTamperCurrent;
    private boolean motionTamperSticky;
    private int hardwareType;
    private int AdvertisingStructureVersion;


    //other fields
    private long LongLastPureTagPacketRx;
    private byte[] scanRecord;
    private final int[] ConnectionRssiAvr = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    private boolean isMacAddressChanged;

    public int getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(int hardwareType) {
        this.hardwareType = hardwareType;
    }

    public int getTempTagId() {
        return tempTagId;
    }

    public void setTempTagId(int tempTagId) {
        this.tempTagId = tempTagId;
    }

    public int getIntIrOffAdcValue() {
        return intIrOffAdcValue;
    }

    public void setIntIrOffAdcValue(int intIrOffAdcValue) {
        this.intIrOffAdcValue = intIrOffAdcValue;
    }

    public int getIrOnAdcValue() {
        return IrOnAdcValue;
    }

    public void setIrOnAdcValue(int irOnAdcValue) {
        IrOnAdcValue = irOnAdcValue;
    }

    public long getTimeFromTag() {
        return TimeFromTag;
    }

    public void setTimeFromTag(long timeFromTag) {
        TimeFromTag = timeFromTag;
    }

    public int getStickyTamper() {
        return StickyTamper;
    }

    public void setStickyTamper(int stickyTamper) {
        StickyTamper = stickyTamper;
    }

    public int getCurrentTamper() {
        return CurrentTamper;
    }

    public void setCurrentTamper(int currentTamper) {
        CurrentTamper = currentTamper;
    }

    public int getStrapTamperIndexNew() {
        return StrapTamperIndexNew;
    }

    public void setStrapTamperIndexNew(int strapTamperIndexNew) {
        StrapTamperIndexNew = strapTamperIndexNew;
    }

    public int getMotionTamperIndexNew() {
        return MotionTamperIndexNew;
    }

    public void setMotionTamperIndexNew(int motionTamperIndexNew) {
        MotionTamperIndexNew = motionTamperIndexNew;
    }

    public boolean isTagStrapOpen() {
        return isTagStrapOpen;
    }

    public void setTagStrapOpen(boolean isTagStrapOpen) {
        this.isTagStrapOpen = isTagStrapOpen;
    }

    public boolean isTagTamperCaseOpen() {
        return isTagTamperCaseOpen;
    }

    public void setTagTamperCaseOpen(boolean isTagTamperCaseOpen) {
        this.isTagTamperCaseOpen = isTagTamperCaseOpen;
    }

    public boolean isBatteryTamperCurrent() {
        return batteryTamperCurrent;
    }

    public void setBatteryTamperCurrent(boolean batteryTamperCurrent) {
        this.batteryTamperCurrent = batteryTamperCurrent;
    }

    public boolean isInMotionTamperCurrent() {
        return isInMotionTamperCurrent;
    }

    public void setMotionTamperCurrent(boolean motionTamperCurrent) {
        this.isInMotionTamperCurrent = motionTamperCurrent;
    }

    public long getLongLastPureTagPacketRx() {
        return LongLastPureTagPacketRx;
    }

    public void setLongLastPureTagPacketRx(long longLastPureTagPacketRx) {
        LongLastPureTagPacketRx = longLastPureTagPacketRx;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    public boolean isMotionTamperSticky() {
        return motionTamperSticky;
    }

    public void setMotionTamperSticky(boolean motionTamperSticky) {
        this.motionTamperSticky = motionTamperSticky;
    }

    /**
     * @return the advertisingStructureVersion
     */
    public int getAdvertisingStructureVersion() {
        return AdvertisingStructureVersion;
    }

    /**
     * @param advertisingStructureVersion the advertisingStructureVersion to set
     */
    public void setAdvertisingStructureVersion(int advertisingStructureVersion) {
        AdvertisingStructureVersion = advertisingStructureVersion;
    }

    public boolean isMacAddressChanged() {
        return isMacAddressChanged;
    }

    public void setIsMacAddressChanged(boolean isMacAddressChange) {
        this.isMacAddressChanged = isMacAddressChange;
    }

}
