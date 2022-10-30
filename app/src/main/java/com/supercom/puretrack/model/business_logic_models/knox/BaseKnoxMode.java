/**
 *
 */
package com.supercom.puretrack.model.business_logic_models.knox;

import android.util.Log;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.KnoxSdkManager;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;

public class BaseKnoxMode {

    protected KnoxSettingsModel knoxSettingsModel;
    protected KnoxSdkManager knoxSdkManager;

    public BaseKnoxMode() {
        knoxSettingsModel = TableOffenderDetailsManager.sharedInstance().getCurKNOXSettingsConfiguration();
        knoxSdkManager = KnoxUtil.getInstance().getKnoxSDKImplementation();
    }

    public boolean enterMode() {

        printLogs();

        boolean isSucceeded = false;
        try {

            KnoxSettingsModel KnoxSettingsModel = TableOffenderDetailsManager.sharedInstance().getCurKNOXSettingsConfiguration();
            knoxSdkManager.setHideNotificationMessages(KnoxSettingsModel.getNotificationMessages());
            knoxSdkManager.setAllowBluetoothMode(KnoxSettingsModel.getBluetoothState() == 1);
            knoxSdkManager.setAutomaticTimeMode(KnoxSettingsModel.getAutomaticTime() == 1);
            if (KnoxSettingsModel.getAutomaticTime() != 1) {
                knoxSdkManager.setTimeZone(KnoxSettingsModel.getTimeZone());
            }
            knoxSdkManager.setScreenTimeout(KnoxSettingsModel.getScreentimout());
            knoxSdkManager.setNFCChangeable(KnoxSettingsModel.getNFC() == 1);
            knoxSdkManager.setAllowSVoiceMode(KnoxSettingsModel.getSVoice() == 1);
            knoxSdkManager.setOTAUpadte(KnoxSettingsModel.getOTAUpdate() == 1);
            knoxSdkManager.setMobileDataLimitChangeable(KnoxSettingsModel.getMobileDataLimit() == 1);
            knoxSdkManager.setLockScreenMode(KnoxSettingsModel.getLockScreen() == 1);
            knoxSdkManager.setSystemLocale(KnoxSettingsModel.getLocal().getLocaleLanguage(), KnoxSettingsModel.getLocal().getLocaleCountry());

            addMoreExtentions();

            isSucceeded = true;
        } catch (SecurityException e) {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                    "enterMode: SecurityException: " + e.getMessage(), DebugInfoModuleId.Knox);
            handleSecurityCatch(e);
        } catch (Exception e) {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                    "enterMode: Exception: " + e.getMessage(), DebugInfoModuleId.Knox);
            Log.i(getClass().getSimpleName(), "Exception: " + e);
        }

        return isSucceeded;
    }

    protected void printLogs() {
        App.writeToNetworkLogsAndDebugInfo(getClass().getCanonicalName(), getLogMessage(), DebugInfoModuleId.Knox);

    }

    protected String getLogMessage() {
        return "";

    }

    protected void addMoreExtentions() {

    }

    private void handleSecurityCatch(SecurityException e) {
        // This exception indicates that the ELM policy has not been activated, so we activate it now. Note that embedding the license in the code is unsafe and
        App.writeToNetworkLogsAndDebugInfo(KnoxUtil.class.getSimpleName(), "Exception: " + e, DebugInfoModuleId.Knox);
        App.writeToNetworkLogsAndDebugInfo(KnoxUtil.class.getSimpleName(), "Activating license. Have you remembered to change the key in the source code ?" + e,
                DebugInfoModuleId.Knox);
    }
}
