package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.app.enterprise.ApnSettings;
import android.app.enterprise.ApnSettingsPolicy;
import android.app.enterprise.ApplicationPolicy;
import android.app.enterprise.EnterpriseDeviceManager;
import android.app.enterprise.kioskmode.KioskMode;
import android.app.enterprise.knoxcustom.CustomDeviceManager;
import android.app.enterprise.knoxcustom.KnoxCustomManager;
import android.app.enterprise.knoxcustom.KnoxCustomPowerItem;
import android.app.enterprise.knoxcustom.SettingsManager;
import android.app.enterprise.knoxcustom.SystemManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.ui.activity.EnterPasswordActivity;
import com.supercom.puretrack.util.application.App;

import java.util.ArrayList;

public class KnoxSdkManager {

    private static final String KIOSK_PASSWORD = "12345678";

    private final KnoxSDKImplementationListener knoxSDKImplementationListener;

    public interface KnoxSDKImplementationListener {
        void onPowerOffStatusChanged(boolean isSuceededToChangePowerOffMode, boolean isPowerOffAllowed);
    }

    public KnoxSdkManager(KnoxSDKImplementationListener knoxSDKImplementationListener) {
        this.knoxSDKImplementationListener = knoxSDKImplementationListener;
    }

    /*
     *  turns on/off the Force Auto Start Up feature
     */
    public void setAutoStartupMode(int allowAutoStartupMode) throws SecurityException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CustomDeviceManager.getInstance().getSystemManager().setForceAutoStartUpState(allowAutoStartupMode);
        }
    }

    /*
     * turns on/off USB mass storage (MTP) when the device is connected to a PC
     * custom sdk
     */
    public void setUsbMassStorageMode(boolean allowUsbMassStorageState) throws SecurityException {
        KnoxCustomManager kcm = KnoxCustomManager.getInstance();
        kcm.setSealedUsbMassStorageState(allowUsbMassStorageState);
    }

    /*
     * sets the screen timeout to the specified number of seconds
     * custom sdk
     */
    public int setScreenTimeout(int timeInSeconds) throws SecurityException {
        return CustomDeviceManager.getInstance().getSystemManager().setScreenTimeout(timeInSeconds);

    }

    /*
     * This method gets the Kiosk Mode state of the device. The default Sealed Mode state is false.
     * custom sdk
     */
    public boolean isInKioskMode() throws SecurityException {
        return false;
    }

    /*
     * This method enables or disables the Sealed Mode functionality.
     * Kiosk Mode generally restricts the device to a single application and restricts the functionality of the device.
     * custom sdk
     */
    public int setKioskModeState(boolean status) throws SecurityException {

        KnoxCustomManager kcm = KnoxCustomManager.getInstance();
        if (status) {
            kcm.setSealedPowerDialogOptionMode(KnoxCustomManager.HIDE);
            kcm.setSealedStatusBarClockState(true);
            kcm.setSealedStatusBarIconsState(true);

            kcm.setSealedPowerDialogCustomItemsState(true);
            createCustomDialogPotionsModeButtons();
        } else {
            kcm.setSealedPowerDialogCustomItemsState(false);
        }
        int sealedState = kcm.setSealedState(status, KIOSK_PASSWORD);

        //failed. if kiosk mode already enable, in order to change it to disable we have to use the password that changed it to be enable;
        if (sealedState != 0) {
            String passCodeFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.LAUNCHER_CONFIG_SETTINGS_PASSWORD);
            sealedState = kcm.setSealedState(status, passCodeFromServer);
        }

        return sealedState;

    }

    private void createCustomDialogPotionsModeButtons() {
        KnoxCustomManager kcm = KnoxCustomManager.getInstance();

        try {
            ArrayList<KnoxCustomPowerItem> list = new ArrayList<KnoxCustomPowerItem>();

            Drawable vectorDrawable = ResourcesCompat.getDrawable(App.getContext().getResources(),
                    R.drawable.officer_mode_icon, null);
            BitmapDrawable myLogo = (BitmapDrawable) vectorDrawable;

            Intent intent1 = new Intent(App.getContext(), EnterPasswordActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            KnoxCustomPowerItem object = new KnoxCustomPowerItem(0, myLogo, intent1, KnoxCustomPowerItem.ACTION_START_ACTIVITY, "Enter Officer Mode");
            list.add(object);
            kcm.setSealedPowerDialogCustomItems(list);

            kcm.setSealedPowerDialogCustomItemsState(true);

        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * sets the default language and region for the device
     * custom sdk
     */
    public int setSystemLocale(String localeLanguage, String localeCountry) throws SecurityException {
        Toast.makeText(App.getContext(), "setSystemLocale", Toast.LENGTH_SHORT).show();
        return KnoxCustomManager.getInstance().setSystemLocale(localeLanguage, localeCountry);
    }

    /*
     * enables or disables mobile data roaming
     * custom sdk
     */
    public int setMobileDataStateRoamingMode(boolean isMobileDataRoamingEnable) throws SecurityException {
        return CustomDeviceManager.getInstance().getSettingsManager().setMobileDataRoamingState(isMobileDataRoamingEnable);
    }

    /*
     * switches mobile data on or off
     * custom sdk
     */

    /**
     * @param mobileDataStateToSet True - to enable the mobile data, False - to disable mobile data.
     */
    public int setMobileDataMode(boolean mobileDataStateToSet) throws SecurityException {
        return CustomDeviceManager.getInstance().getSettingsManager().setMobileDataState(mobileDataStateToSet);
    }

    /*
     * This method turns on/off individual system notification messages displayed to the user in when the device is in ProKiosk Mode.
     * custom sdk
     */
    public int setHideNotificationMessages(int flags) throws SecurityException {
        KnoxCustomManager kcm = KnoxCustomManager.getInstance();
        int sealedNotificationMessagesState = kcm.setSealedNotificationMessagesState(flags != 3);
        return sealedNotificationMessagesState;
    }

    /*
     * set the time automatically by enabling the Network Identity and Time Zone (NITZ) option
     * standard sdk
     */
    public void setAutomaticTimeMode(boolean isAutomaticTime) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getDateTimePolicy().setAutomaticTime(isAutomaticTime);
    }

    /*
     * set the time automatically by enabling the Network Identity and Time Zone (NITZ) option
     * standard sdk
     */
    public boolean isAutomaticTimeEnable() throws SecurityException {
        return false;
    }

    /*
     * allow or disallow Airplane Mode
     * standard sdk
     */
    public void setAirplaneModeChangeable(boolean isAirplaneModeEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowAirplaneMode(isAirplaneModeEnable);
    }

    /*
     * allow or disallow status bar expansion.
     * standard sdk
     */
    public void setAllowStatusBarMode(boolean isStatusBarEnable) throws SecurityException {
        KnoxCustomManager kcm = KnoxCustomManager.getInstance();

        kcm.setSealedStatusBarMode(isStatusBarEnable ? KnoxCustomManager.SHOW : KnoxCustomManager.HIDE);

    }

    /**
     * API to allow or disallow status bar expansion.
     */
    public void setAllowStatusBarExpansion(boolean isStatusBarExpansionEnable) {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowStatusBarExpansion(isStatusBarExpansionEnable);
    }

    /*
     * enable or disable Bluetooth access
     * standard sdk
     */
    public void setAllowBluetoothMode(boolean isBluetoothModeEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setBluetoothState(isBluetoothModeEnable);
    }

    /*
     * set the device time zone by providing a time zone Id.
     * standard sdk
     */
    public void setTimeZone(String timeZone) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getDateTimePolicy().setTimeZone(timeZone);
    }

    /*
     * set the device date and time
     * standard sdk
     */
    public void setDateTime(int day, int month, int year, int hour, int minute, int second) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getDateTimePolicy().setDateTime(day, month, year, hour, minute, second);
    }

    /*
     * API to allow or disallow the user to change the Wi-Fi state
     * standard sdk
     */
    public void setWifiChangeable(boolean wifiSateChangeAllowed) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getWifiPolicy().setWifiStateChangeAllowed(wifiSateChangeAllowed);
    }

    /*
     * enable or disable Near Field Communication (NFC).
     * standard sdk
     */
    public void setNFCChangeable(boolean isNFCModeEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setEnableNFC(isNFCModeEnable);
    }

    /*
     * allow or disallow the S Voice application on the device.
     * standard sdk
     */
    public void setAllowSVoiceMode(boolean isSVoiceEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowSVoice(isSVoiceEnable);
    }

    /*
     * allow or disallow the user to power off the device by pressing the power button.
     * standard sdk
     */
    public boolean setAllowPowerOffAndRestart(boolean isPowerOffEnable) throws SecurityException {
        return new EnterpriseDeviceManager(App.getContext()).
                getRestrictionPolicy().allowPowerOff(isPowerOffEnable);
    }

    /* API to check whether the user is allowed to power off the device by pressing the power button */
    public boolean isAllowPowerOffAndRestartAllowed() {
        return new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().isPowerOffAllowed();
    }

    /*
     * allow or disallow recent apps button
     * standard sdk
     */
    public void setRecentButtonMode(boolean allowRecentApps) throws SecurityException {

        KioskMode kioskMode = KioskMode.getInstance(App.getContext());
        ArrayList<Integer> a = new ArrayList<Integer>();
        a.add(187); // block recent buttons
        kioskMode.allowHardwareKeys(a, allowRecentApps);

    }

    public void rebootDevice() {
        try {
            boolean allowPowerOffAndRestartAllowed = isAllowPowerOffAndRestartAllowed();
            if (!allowPowerOffAndRestartAllowed) {
                boolean isSuceededToChangePowerOffMode = setAllowPowerOffAndRestart(true);
                knoxSDKImplementationListener.onPowerOffStatusChanged(isSuceededToChangePowerOffMode, true);
            } else {
                new EnterpriseDeviceManager(App.getContext()).reboot("Network is not avaliable");

                KnoxUtil.getInstance().getKnoxSDKImplementation().setAllowPowerOffAndRestart(false);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /*
     * Authentication type
        Value -1 - Not set
        Value 0 - None
        Value 1 - PAP
        Value 2 - CHAP
        Value 3 - PAP or CHAP
     */
    public void createCellularAPN(String APN, String name, String username, String password, int authType) throws SecurityException {
        ApnSettingsPolicy apnSettingsPolicy = new EnterpriseDeviceManager(App.getContext()).getApnSettingsPolicy();
        ApnSettings apnSettings = createApnSettingsObject(APN, name, username, password, authType);
        long uniqueId = apnSettingsPolicy.createApnSettings(apnSettings);
        String messageToUpload = "";
        if (uniqueId != -1) {
            apnSettingsPolicy.setPreferredApn(uniqueId);

            messageToUpload = "New APN created";
        } else {
            messageToUpload = "APN creation failed";
        }
        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), messageToUpload, DebugInfoModuleId.Knox);
    }

    private ApnSettings createApnSettingsObject(String APN, String name, String username, String password, int authType) throws SecurityException {
        ApnSettings apnSettings = new ApnSettings();

        apnSettings.apn = APN;
        apnSettings.name = name;
        apnSettings.user = username;
        apnSettings.password = password;
        apnSettings.authType = authType;

        TelephonyManager telephonyManager = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String plmn = telephonyManager.getSimOperator();
        if (plmn == null || plmn.equals("") || plmn.length() < 5) {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "APN should not be created since mnc or mcc not correct",
                    DebugInfoModuleId.Knox);
        } else {
            apnSettings.mcc = plmn.substring(0, 3);
            apnSettings.mnc = plmn.substring(3);
        }

        return apnSettings;
    }

    /*
     * allow or disallow putting the device into safe mode
     * standard sdk
     */
    public boolean setSafeMode(boolean isSafeModeEnable) throws SecurityException {
        return   new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowSafeMode(isSafeModeEnable);
    }

    /*
     * allow or disallow factory resetting the device
     * standard sdk
     */
    public boolean setFactoryReset(boolean isFactoryResetEnable) throws SecurityException {
        return new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowFactoryReset(isFactoryResetEnable);
    }

    /*
     * allow or disallow upgrading the operating system (OS) over-the-air (OTA).
     * standard sdk
     */
    public void setOTAUpadte(boolean isOTAUpgradEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowOTAUpgrade(isOTAUpgradEnable);
    }

    /*
     * allow or disallow Secure Digital (SD) card access.
     * standard sdk
     */
    public void setSDCardMode(boolean isSDCardEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setSdCardState(isSDCardEnable);
    }

    /*
     * allow or disallow application installation
     * standard sdk
     */
    public void setApplicationInstallationMode(int applicationUninstallationMode) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getApplicationPolicy().setApplicationInstallationMode(applicationUninstallationMode);
    }

    /*
     * allow or disallow application uninstallation
     * standard sdk
     */
    public void setApplicationUninstallationMode(int applicationUninstallationMode) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getApplicationPolicy().setApplicationUninstallationMode(applicationUninstallationMode);
    }

    /*
     * allow or disallow the user to set the mobile data limit.
     * standard sdk
     */
    public void setMobileDataLimitChangeable(boolean isMobileDataLimitChangeable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowUserMobileDataLimit(isMobileDataLimitChangeable);
    }

    /*
     * allow or disallow lockscreen menu in the device Settings.
     * standard sdk
     */
    public void setLockScreenMode(boolean isLockScreenEnable) throws SecurityException {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setLockScreenState(isLockScreenEnable);
    }

    /*
     * API to remotely stop an application. The application process is killed.
     * standard sdk
     */
    public boolean stopApp(String appToStop) throws SecurityException {
        if (KnoxUtil.getInstance().isKnoxActivated()) {
            ApplicationPolicy appPolicy = new EnterpriseDeviceManager(App.getContext()).getApplicationPolicy();
            try {
                boolean result = appPolicy.stopApp(appToStop);
                if (result) {
                    // application package is no longer running on the device
                    Log.i(getClass().getSimpleName(), "Proccess were stopped: " + appToStop);
                } else {
                    Log.i(getClass().getSimpleName(), "failed were stopped: " + appToStop);
                    // application package is still running on the device
                }
                return result;
            } catch (Exception exception) {

            }
        }

        return false;
    }

    /*
     * This method turns flight mode on and off.
     */
    public void setFlightModeState(boolean enableFlightMode) throws SecurityException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SettingsManager settingsManager = SettingsManager.getInstance();
            int status = settingsManager.setFlightModeState(enableFlightMode ? CustomDeviceManager.ON : CustomDeviceManager.OFF);
        }
    }

    public void setKeyboardMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                SystemManager kcsm = SystemManager.getInstance();
                kcsm.setKeyboardMode(CustomDeviceManager.KEYBOARD_MODE_SETTINGS_OFF);
                kcsm.setKeyboardMode(CustomDeviceManager.KEYBOARD_MODE_PREDICTION_OFF);
            } catch (SecurityException e) {

                Log.w(getClass().getSimpleName(), "SecurityException:" + e);
            }
        }
    }

    public void setUsbPortModeMtp(boolean isEnabled) {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setUsbMediaPlayerAvailability(isEnabled);
    }

    /*
     * enable or disable USB access
     * standard sdk
     */
    public void setUsbPortModeDebugging(boolean isEnabled) {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setUsbDebuggingEnabled(isEnabled);
    }

    /*
     * enable or disable USB access
     * standard sdk
     */
    public void setUsbPortModeTethering(boolean isEnabled) {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().setUsbTethering(isEnabled);
    }

    /*
     * enable or disable USB access
     * standard sdk
     */
    public void setUsbPortModeHostStorage(boolean isEnabled) {
        new EnterpriseDeviceManager(App.getContext()).getRestrictionPolicy().allowUsbHostStorage(isEnabled);
    }

}
