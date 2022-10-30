/**
 *
 */
package com.supercom.puretrack.model.business_logic_models.knox;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.R;

public class OfficerKnoxMode extends BaseKnoxMode {

    @Override
    public void addMoreExtentions() {

        knoxSdkManager.setAirplaneModeChangeable(true);
        knoxSdkManager.setAutoStartupMode(1);
        knoxSdkManager.setUsbMassStorageMode(true);
        knoxSdkManager.setAllowPowerOffAndRestart(true);
        knoxSdkManager.setRecentButtonMode(true);
        knoxSdkManager.setWifiChangeable(true);
        knoxSdkManager.setMobileDataMode(true);
        knoxSdkManager.setMobileDataStateRoamingMode(true);
        knoxSdkManager.setSafeMode(true);
        knoxSdkManager.setFactoryReset(true);
        knoxSdkManager.setSDCardMode(true);
        knoxSdkManager.setApplicationInstallationMode(1);
        knoxSdkManager.setApplicationUninstallationMode(1);
        knoxSdkManager.setKioskModeState(false);
        knoxSdkManager.setAllowStatusBarMode(true);
        knoxSdkManager.setAllowStatusBarExpansion(true);

        knoxSdkManager.setUsbPortModeMtp(true);
        knoxSdkManager.setUsbPortModeDebugging(true);
        knoxSdkManager.setUsbPortModeTethering(true);
        knoxSdkManager.setUsbPortModeHostStorage(true);
    }

    @Override
    protected String getLogMessage() {
        return App.getContext().getString(R.string.launcher_text_enter_officer_mode);
    }
}
