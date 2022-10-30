package com.supercom.puretrack.util.general;

import android.app.admin.DevicePolicyManager;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.supercom.puretrack.data.broadcast_receiver.KnoxAdminReceiver;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.KnoxSdkManager;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.KnoxSdkManager.KnoxSDKImplementationListener;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.knox.BaseKnoxMode;
import com.supercom.puretrack.model.business_logic_models.knox.OffenderKnoxModeFactory;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.activity.BaseActivity;
import com.supercom.puretrack.ui.activity.LauncherActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

public class KnoxUtil implements KnoxSDKImplementationListener {

    private String knoxKlmProdKey = "KLM09-NTPNZ-QQGKT-I6SHY-5CL5E-X8PVG";

    public enum OffenderModeTypes {
        Init_Mode,
        Officer_Mode,
        Offender_Normal_Mode,
        Offender_Flight_Mode
    }

    private OffenderModeTypes currentOffenderModeType = OffenderModeTypes.Init_Mode;

    private KnoxUtilityListener knoxUtilityListener;

    public class CellularAPN {
        public int Enable;
        public String Apn;
        public String Name;
        public String Mcc;
        public String Mnc;
        public String User;
        public String password;
        public int Auth_Type;
    }

    public static final int DEVICE_ADMIN_ADD_RESULT_ENABLE = 2;

    DevicePolicyManager dpm;
    public ComponentName mDeviceAdmin = null;


    private static final KnoxUtil INSTANCE = new KnoxUtil();


    private KnoxUtil() {
    }

    public interface KnoxUtilityListener {
        void onDeviceAdminShouldInstalled();

        void onStartedActivateKnox();

        void onFailedToActivateKnox();

        void onSucceededToActivateKnox();
    }

    public void setknoxUtilityListener(KnoxUtilityListener knoxUtilityListener) {
        this.knoxUtilityListener = knoxUtilityListener;
    }


    public static KnoxUtil getInstance() {
        return INSTANCE;
    }

    private final KnoxSdkManager knoxSdkManager = new KnoxSdkManager(this);

    public KnoxSdkManager getKnoxSDKImplementation() {
        return knoxSdkManager;
    }

    public void runKnoxIfNeeded(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdmin = new ComponentName(context, KnoxAdminReceiver.class);
        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                "runKnoxIfNeeded: " + dpm.isAdminActive(mDeviceAdmin), DebugInfoModuleId.Knox);
        if (!dpm.isAdminActive(mDeviceAdmin)) {
            knoxUtilityListener.onDeviceAdminShouldInstalled();
        } else {
            if (!isKnoxActivated()) {
                activateKnoxLicence();
            } else {
                initDefaultKnoxBlockingFunctions();
            }
        }
    }


    /**
     * @param context
     */
    public void KnoxFactoryReset(Context context) {

        dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(context, KnoxAdminReceiver.class);

        if (dpm.isAdminActive(mDeviceAdmin)) {
            dpm.wipeData(0);
        }

    }


    public void activateKnoxLicence() {
        EnterpriseLicenseManager elm = EnterpriseLicenseManager.getInstance(App.getContext());
        elm.activateLicense(getELMLicence());

        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                "activateKnoxLicence: ", DebugInfoModuleId.Knox);


        knoxUtilityListener.onStartedActivateKnox();

        BaseActivity.isCameFromRegularActivityCode = true;
    }

    public void initDefaultKnoxBlockingFunctions() {

        boolean isEnterOffenderModeSucceeded = enterOffenderMode(true);
        if (isEnterOffenderModeSucceeded) {

            PureTrackSharedPreferences.setKnoxLicenceActivatedStatus(true);

            updateLauncherScreen();

            App.writeToNetworkLogsAndDebugInfo(KnoxUtil.class.getSimpleName(), "Finished to implement knox settings", DebugInfoModuleId.Knox);

            knoxUtilityListener.onSucceededToActivateKnox();
        } else {
            // This exception indicates that the ELM policy has not been activated, so we activate it now. Note that embedding the license in the code is unsafe and
            App.writeToNetworkLogsAndDebugInfo(KnoxUtil.class.getSimpleName(),
                    "Activating license. Have you remembered to change the key in the source code ?", DebugInfoModuleId.Knox);

            handleKnoxLicenceNotWorking();
        }

    }

    public String getELMLicence() {
        return "19C62A6CFBB7272106EEF0CBD9ADB494B4E67A6CE6EE729D4176A5A3CC31AAD091E41081531FE064259A111273505FA9B8BA3635A34DCEA186EFB91F1C6CA24F";
    }

    public String getKLMLicence() {
        return knoxKlmProdKey;
    }

    public boolean isKnoxActivated() {
        return PureTrackSharedPreferences.isKnoxLicenceActivated();
    }

    public String getKnoxKlmProdKey() {
        return knoxKlmProdKey;
    }

    public void setKnoxKlmProdKey(String knoxKlmProdKey) {
        this.knoxKlmProdKey = knoxKlmProdKey;
    }

    public void enterOfficerMode(boolean shouldForceEnterMode) {
        if (!KnoxUtil.getInstance().isKnoxActivated()) return;
        enterMode(OffenderModeTypes.Officer_Mode, shouldForceEnterMode);
    }

    private boolean enterOffenderFlightMode(boolean shouldForceEnterNode) {
        return enterMode(OffenderModeTypes.Offender_Flight_Mode, shouldForceEnterNode);
    }

    public boolean enterOffenderMode(boolean shouldForceEnterNode) {
        if (!KnoxUtil.getInstance().isKnoxActivated()) return true;

        boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(TableEventConfig.ViolationCategoryTypes.FLIGHT_MODE_STATE) != -1;
        if (hasOpenEvent) {
            return enterOffenderFlightMode(shouldForceEnterNode);
        } else {
            return enterOffenderNormalMode(shouldForceEnterNode);
        }

    }

    private boolean enterOffenderNormalMode(boolean shouldForceEnterMode) {
        return enterMode(OffenderModeTypes.Offender_Normal_Mode, shouldForceEnterMode);
    }

    private boolean enterMode(OffenderModeTypes offenderModeType, boolean shouldForceEnterMode) {
        if (!KnoxUtil.getInstance().isKnoxActivated()) return true;

        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                "enterMode: type: " + offenderModeType + " currentOffenderModeType: " + currentOffenderModeType + " shouldForceEnterMode: " + shouldForceEnterMode
                , DebugInfoModuleId.Knox);

        if (currentOffenderModeType != offenderModeType || shouldForceEnterMode) {
            currentOffenderModeType = offenderModeType;
            BaseKnoxMode baseKnoxMode = OffenderKnoxModeFactory.getOffenderMode(currentOffenderModeType);
            return baseKnoxMode.enterMode();
        } else {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                    "enterMode: failed", DebugInfoModuleId.Knox);
            return false;
        }
    }


    public void handleKnoxLicenceNotWorking() {
        if (knoxUtilityListener != null) {
            knoxUtilityListener.onFailedToActivateKnox();
        }
    }

    @Override
    public void onPowerOffStatusChanged(boolean isSuceededToChangePowerOffMode, boolean isPowerOffAllowed) {
        if (isSuceededToChangePowerOffMode) {
            if (isPowerOffAllowed) {
                KnoxUtil.getInstance().getKnoxSDKImplementation().rebootDevice();
            } else {
                App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "couldn't do restart, succeeded to change power off mode, "
                        + "but power off not allowed", DebugInfoModuleId.Network);
            }
        } else {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "couldn't do restart, since didn't succeed change power off status",
                    DebugInfoModuleId.Network);
        }
    }

    public void initDeviceToInitiatedFlightMode() {
        if (currentOffenderModeType == OffenderModeTypes.Officer_Mode) {
            knoxSdkManager.setFlightModeState(true);
        } else {
            enterOffenderFlightMode(false);

            updateLauncherScreen();
        }
    }

    public void handleDeviceWasInInitiatedFlightModeIfNeeded() {
        if (isKnoxActivated()) {
            boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(TableEventConfig.ViolationCategoryTypes.FLIGHT_MODE_STATE) != -1;
            if (hasOpenEvent) {

                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.flightModeDisabled, -1, -1);

                //if was in offender flight mode, we will change to offender normal mode
                if (currentOffenderModeType == OffenderModeTypes.Offender_Flight_Mode) {
                    enterOffenderMode(false);
                    updateLauncherScreen();
                }
                knoxSdkManager.setFlightModeState(false);
            }
        } else {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Flight mode message received but knox not activated",
                    DebugInfoModuleId.Network);
        }
    }

    private void updateLauncherScreen() {
        Intent intent = new Intent(LauncherActivity.LAUNCHER_ACTIVITY_MESSAGE_RECEIVER);
        intent.putExtra(LauncherActivity.LAUNCHER_ACTIVITY_EXTRA, LauncherActivity.UPDATE_LAUNCHER_EXTRA);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }


    public boolean isInOfficerMode() {
        return currentOffenderModeType == OffenderModeTypes.Officer_Mode;
    }

    public boolean isInInitializedOffenderFlightMode() {
        return currentOffenderModeType == OffenderModeTypes.Offender_Flight_Mode;
    }

}
