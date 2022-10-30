/**
 *
 */
package com.supercom.puretrack.data.broadcast_receiver;

import static com.supercom.puretrack.data.source.local.table.TableScannerType.NORMAL_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MAC_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MANUFACTURER_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    protected abstract void handleOnReceive(Context context);

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isManufacturerIdEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MANUFACTURER_ID) > 0;
        boolean isMaScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MAC_SCAN_ENABLED) > 0;
        boolean isNormalScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(NORMAL_SCAN_ENABLED) > 0;
        boolean isDozeModeScanEnabled = isMaScanEnabled || isManufacturerIdEnabled;

        if (!isDozeModeScanEnabled || isNormalScanEnabled) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getSimpleName());
            wakeLock.acquire();

            handleOnReceive(context);

            wakeLock.release();
        }
    }
}
