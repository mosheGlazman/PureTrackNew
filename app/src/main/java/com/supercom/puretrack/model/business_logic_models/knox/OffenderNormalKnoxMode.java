/**
 *
 */
package com.supercom.puretrack.model.business_logic_models.knox;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.R;

public class OffenderNormalKnoxMode extends BaseKnoxMode {

    @Override
    public void addMoreExtentions() {

        knoxSdkManager.setKioskModeState(true);
        knoxSdkManager.setAirplaneModeChangeable(knoxSettingsModel.getAirplane() == 0);
        knoxSdkManager.setAutoStartupMode(knoxSettingsModel.getAutostart());
        knoxSdkManager.setUsbMassStorageMode(knoxSettingsModel.getUSB() == 1);
        knoxSdkManager.setAllowPowerOffAndRestart(knoxSettingsModel.getPower() == 1);
        knoxSdkManager.setRecentButtonMode(knoxSettingsModel.getRecentApps() == 1);
        knoxSdkManager.setWifiChangeable(knoxSettingsModel.getWifi() == 1);
        knoxSdkManager.setMobileDataMode(knoxSettingsModel.getMobileData() == 1);
        knoxSdkManager.setMobileDataStateRoamingMode(knoxSettingsModel.getDataRoaming() == 1);
        knoxSdkManager.setSafeMode(knoxSettingsModel.getSafeMode() == 1);
        knoxSdkManager.setFactoryReset(knoxSettingsModel.getFactoryReset() == 1);
        knoxSdkManager.setSDCardMode(knoxSettingsModel.getSDCard() == 1);
        knoxSdkManager.setApplicationInstallationMode(knoxSettingsModel.getInstallApps());
        knoxSdkManager.setApplicationUninstallationMode(knoxSettingsModel.getUninstallApps());
        knoxSdkManager.setAllowStatusBarMode(knoxSettingsModel.getStatusBar() == 1);
        knoxSdkManager.setKeyboardMode();
        knoxSdkManager.setUsbPortModeMtp(false);
        knoxSdkManager.setUsbPortModeDebugging(false);
        knoxSdkManager.setUsbPortModeTethering(false);
        knoxSdkManager.setUsbPortModeHostStorage(false);

    }

    @Override
    protected String getLogMessage() {
        return App.getContext().getString(R.string.launcher_text_enter_offender_mode);
    }

}
