package com.supercom.puretrack.data.source.local.local_managers.hardware.shielding;

import com.supercom.puretrack.data.source.local.table.TableDeviceShielding;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityDeviceShielding;

public class ShieldingInitializationManager {

    public static EntityDeviceShielding getDeviceShieldingEntity() {
        TableDeviceShielding tableDeviceShielding = DatabaseAccess.getInstance().tableDeviceShielding;
        if (tableDeviceShielding == null) return null;
        EntityDeviceShielding entityDeviceShielding = tableDeviceShielding.getDeviceShieldingEntity();
        if (entityDeviceShielding == null) return null;
        DeviceShieldingManager.getInstance().isEnabled = entityDeviceShielding.enabled > 0;
        if (! DeviceShieldingManager.getInstance().isEnabled) return null;
        return entityDeviceShielding;
    }


    public static void initConfigParams(final EntityDeviceShielding entityDeviceShielding) {
        DeviceShieldingManager.getInstance().isCellNetworkOpenEventEnabled = entityDeviceShielding.openEventCellEnabled > 0;
        DeviceShieldingManager.getInstance().isBleOpenEventEnabled = entityDeviceShielding.openEventBluetoothEnabled > 0;
        DeviceShieldingManager.getInstance().isWifiNetworkOpenEventEnabled = entityDeviceShielding.openEventWifiEnabled > 0;
        DeviceShieldingManager.getInstance().isBleClosingEventEnabled = entityDeviceShielding.closeEventBluetoothEnabled > 0;
        DeviceShieldingManager.getInstance().isCellNetworkClosingEventEnabled = entityDeviceShielding.closeEventCellEnabled > 0;
        DeviceShieldingManager.getInstance().isWifiNetworkClosingEventEnabled = entityDeviceShielding.closeEventWifiEnabled > 0;
        DeviceShieldingManager.getInstance().checkIntervalMilSec = entityDeviceShielding.checkIntervalSec * 1000L;
        DeviceShieldingManager.getInstance().bleThresholdMilSec = entityDeviceShielding.bleThresholdSec * 1000L;
        DeviceShieldingManager.getInstance().wifiThresholdMilSec = entityDeviceShielding.wifiThresholdSec * 1000L;
        DeviceShieldingManager.getInstance().mobileNetworkThresholdMilSec = entityDeviceShielding.mobileNetworkThresholdSec * 1000L;

        //If changed by server config or first time app running
        if ( DeviceShieldingManager.getInstance().threshold != entityDeviceShielding.openEventThreshold
                ||  DeviceShieldingManager.getInstance().remainingThreshold == -1){
            DeviceShieldingManager.getInstance().remainingThreshold = entityDeviceShielding.openEventThreshold;
        }

        DeviceShieldingManager.getInstance().threshold = entityDeviceShielding.openEventThreshold;
    }
}
