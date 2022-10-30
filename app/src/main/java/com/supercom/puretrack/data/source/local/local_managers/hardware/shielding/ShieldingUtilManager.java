package com.supercom.puretrack.data.source.local.local_managers.hardware.shielding;

import static com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.ShieldingEventManager.closeEvent;
import static com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.ShieldingEventManager.openEvent;

import android.util.Log;

import com.supercom.puretrack.util.hardware.NetworkHardwareUtil;

import java.util.Calendar;
import java.util.Date;

public class ShieldingUtilManager {

    public static boolean hasReceptionForOpenEvent() {

/*        boolean result = true;

        if (DeviceShieldingManager.getInstance().isBleOpenEventEnabled) {
        Log.d("ShieldingUtilManager", "Ble: " + isBleConnected());
            result &= isBleConnected();
        }

        if (DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled) {
            Log.d("ShieldingUtilManager", "Wifi: " + NetworkHardwareUtil.isWifiConnected());
            result &= NetworkHardwareUtil.isWifiConnected();
        }

        if (DeviceShieldingManager.getInstance().isCellNetworkOpenEventEnabled) {
            Log.d("ShieldingUtilManager", "MobileNetwork: " + NetworkHardwareUtil.isMobileNetworkConnected());
            result &= NetworkHardwareUtil.isMobileNetworkConnected();
        }

        return result;*/

        if (DeviceShieldingManager.getInstance().isBleOpenEventEnabled && DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled
                && !DeviceShieldingManager.getInstance().isCellNetworkOpenEventEnabled) {
            return NetworkHardwareUtil.isWifiConnected() || isBleConnected();
        }

        if (DeviceShieldingManager.getInstance().isBleOpenEventEnabled && DeviceShieldingManager.getInstance().isCellNetworkOpenEventEnabled
                && !DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled) {
            return NetworkHardwareUtil.isConnected() || isBleConnected();
        }
        if (DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled && !DeviceShieldingManager.getInstance().isBleOpenEventEnabled
                && !DeviceShieldingManager.getInstance().isCellNetworkOpenEventEnabled) {
            return NetworkHardwareUtil.isWifiConnected();
        }
        if (DeviceShieldingManager.getInstance().isCellNetworkOpenEventEnabled && !DeviceShieldingManager.getInstance().isBleOpenEventEnabled
                && !DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled) {
            return NetworkHardwareUtil.isMobileNetworkConnected();
        }
        if (!DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled && !DeviceShieldingManager.getInstance().isBleOpenEventEnabled) {
            return false;
        }
        if (DeviceShieldingManager.getInstance().isBleOpenEventEnabled && !DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled){
            return isBleConnected();
        }
        return NetworkHardwareUtil.isWifiConnected() || isBleConnected() || NetworkHardwareUtil.isConnected();
    }

    public static boolean isGotReceptionForClosingEvent() {

/*        boolean result = true;

        if (DeviceShieldingManager.getInstance().isBleClosingEventEnabled) {
            result &= isBleConnected();
        }

        if (DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled) {
            result &= NetworkHardwareUtil.isWifiConnected();
        }

        if (DeviceShieldingManager.getInstance().isCellNetworkClosingEventEnabled) {
            result &= NetworkHardwareUtil.isMobileNetworkConnected();
        }

        return result;*/

        if (DeviceShieldingManager.getInstance().isBleClosingEventEnabled && DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled
                && !DeviceShieldingManager.getInstance().isCellNetworkClosingEventEnabled) {
            return NetworkHardwareUtil.isWifiConnected() || isBleConnected();
        }

        if (DeviceShieldingManager.getInstance().isBleClosingEventEnabled && DeviceShieldingManager.getInstance().isCellNetworkClosingEventEnabled
                && !DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled) {
            return NetworkHardwareUtil.isConnected() || isBleConnected();
        }
        if (DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled && !DeviceShieldingManager.getInstance().isBleClosingEventEnabled
                && !DeviceShieldingManager.getInstance().isCellNetworkClosingEventEnabled) {
            return NetworkHardwareUtil.isWifiConnected();
        }
        if (DeviceShieldingManager.getInstance().isCellNetworkClosingEventEnabled && !DeviceShieldingManager.getInstance().isBleClosingEventEnabled
                && !DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled) {
            return NetworkHardwareUtil.isMobileNetworkConnected();
        }
        if (!DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled && !DeviceShieldingManager.getInstance().isBleClosingEventEnabled) {
            return false;
        }

        if (DeviceShieldingManager.getInstance().isBleClosingEventEnabled && !DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled){
            return isBleConnected();
        }
        return NetworkHardwareUtil.isWifiConnected() || isBleConnected() || NetworkHardwareUtil.isConnected();
    }

    public static boolean isBleConnected(){
        Calendar lastBleCllCalendar = Calendar.getInstance();
        lastBleCllCalendar.setTime(new Date(DeviceShieldingManager.getInstance().lastSuccessfulBleMillis));
        lastBleCllCalendar.add(Calendar.SECOND, 2);

        return lastBleCllCalendar.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis();
    }

    public static void checkForClosingEvent(){
        if (isGotReceptionForClosingEvent()){
            closeEvent();
        }
    }

    public static void checkForOpenEvent() {
        if (hasReceptionForOpenEvent()) {
            DeviceShieldingManager.getInstance().isCheckingForOpenEvent = false;
            DeviceShieldingManager.getInstance().remainingThreshold = DeviceShieldingManager.getInstance().threshold;
            return;
        } else {

            if (DeviceShieldingManager.getInstance().remainingThreshold == 0) {
                DeviceShieldingManager.getInstance().remainingThreshold = DeviceShieldingManager.getInstance().threshold;
                DeviceShieldingManager.getInstance().isCheckingForOpenEvent = false;
                openEvent();
                return;
            }
        }
        DeviceShieldingManager.getInstance().remainingThreshold--;
    }
}
