package com.supercom.puretrack.model.business_logic_models.knox;

public class KnoxSettingsModel {

    public static class LocalObject {

        public String getLocaleLanguage() {
            return "";
        }

        public String getLocaleCountry() {
            return "";
        }
    }

    private final LocalObject Local = new LocalObject();

    public int getAirplane() {
        return 0;
    }

    public int getStatusBar() {
        return 0;
    }

    public int getBluetoothState() {
        return 1;
    }

    public int getAutomaticTime() {
        return 1;
    }

    public int getWifi() {
        return 0;
    }

    public int getNFC() {
        return 0;
    }

    public int getSVoice() {
        return 0;
    }

    public int getPower() {
        return 0;
    }

    public int getRecentApps() {
        return 0;
    }

    public int getMobileData() {
        return 1;
    }

    public int getDataRoaming() {
        return 1;
    }

    public LocalObject getLocal() {
        return Local;
    }

    public int getScreentimout() {
        return 15;
    }

    public int getToast() {
        return 0;
    }

    public int getUSB() {
        return 1;
    }

    public int getAutostart() {
        return 1;
    }


    public String getTimeZone() {
        return "0";
    }

    public int getNotificationMessages() {
        return 3;
    }

    public int getDateTime() {
        return 0;
    }

    public int getSafeMode() {
        return 0;
    }

    public int getFactoryReset() {
        return 0;
    }

    public int getOTAUpdate() {
        return 0;
    }

    public int getSDCard() {
        return 0;
    }

    public int getInstallApps() {
        return 0;
    }

    public int getUninstallApps() {
        return 0;
    }

    public int getMobileDataLimit() {
        return 0;
    }

    public int getLockScreen() {
        return 0;
    }
}