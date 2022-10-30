package com.supercom.puretrack.ui.views;

import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.BATTERY_FILTER;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.BATTERY_IS_CHARGING_KEY;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.BATTERY_PERCENT_KEY;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.CELL_SIGNAL_STRENGTH_FILTER;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.CELL_SIGNAL_STRENGTH_KEY;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.MOBILE_DATA_FILTER;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.MOBILE_DATA_IS_ON_KEY;
import static com.supercom.puretrack.data.service.ToolbarInfoUtilsService.MOBILE_DATA_TYPE_KEY;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.supercom.puretrack.data.service.ToolbarInfoUtilsService;
import com.supercom.puretrack.data.source.local.local_managers.hardware.HardwareUtilsManager;

import java.util.Date;

public class ToolbarViewsDataManager {
    private static final String TAG = "ToolbarViewsDataManager";
    private static ToolbarViewsDataManager instance;
    Context context;

    private OnBatteryInfoListener batteryInfoListener;
    private OnMobileDataInfoListener mobileDataInfoListener;
    private OnCellularInfoListener cellularInfoListener;

    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver mobileDataReceiver;
    private BroadcastReceiver cellularReceiver;

    private int lastBatteryPercentage;
    private boolean lastBatteryIsCharging;

    private boolean lastMobileDataIsOn;
    private String lastMobileDataType;

    private int lastCellularSignalLevel;
    private Date lastCellularSignalLevelOk=new Date();

    private ToolbarViewsDataManager(Context context) {
        this.context = context;
    }

    public static ToolbarViewsDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new ToolbarViewsDataManager(context);
        }
        return instance;
    }

    public int getLastBatteryPercentage() {
        return lastBatteryPercentage;
    }

    public boolean isLastBatteryCharging() {
        return lastBatteryIsCharging;
    }

    public boolean isLastMobileDataOn() {
        return lastMobileDataIsOn;
    }

    public String getLastMobileDataType() {
        return lastMobileDataType;
    }

    public int getLastCellularSignalLevel() {
        return lastCellularSignalLevel;
    }
    public Date getLastCellularSignalLevelOk() {
        return lastCellularSignalLevelOk;
    }

    public void register() {
        try {
            ToolbarInfoUtilsService.start(context);

            registerToBatteryInfo();
            registerToCellularInfo();
            registerToMobileDataInfo();
        }catch (Exception ex){

        }
    }

    public void unregister() {
        context.unregisterReceiver(batteryReceiver);
        context.unregisterReceiver(mobileDataReceiver);
        context.unregisterReceiver(cellularReceiver);

        ToolbarInfoUtilsService.stop(context);
    }

    int pppppppp= 96;
    int ppppppppMu= 1;
    private void registerToBatteryInfo() {
        IntentFilter filter = new IntentFilter(BATTERY_FILTER);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras().containsKey(BATTERY_PERCENT_KEY)) {
                    int percent = intent.getExtras().getInt(BATTERY_PERCENT_KEY);
                    lastBatteryPercentage = percent;
                    if (batteryInfoListener != null) {
                        if(false){
                            pppppppp+=ppppppppMu;
                            percent=pppppppp;
                            if(percent>100){
                                percent=100;
                            }
                            if(percent<0){
                                percent=0;
                            }
                            if(pppppppp>104){
                                ppppppppMu=-1;
                            }
                            if(pppppppp < 90){
                                ppppppppMu=1;
                            }
                        }

                        Log.i(TAG, "percent: " + percent);
                        batteryInfoListener.onBatteryPercentageChanged(percent);
                    }
                    Log.d(TAG, "percent: " + percent);
                }
                if (intent.getExtras().containsKey(BATTERY_IS_CHARGING_KEY)) {
                    boolean isCharging = intent.getExtras().getBoolean(BATTERY_IS_CHARGING_KEY);
                    lastBatteryIsCharging = isCharging;
                    if (batteryInfoListener != null) {
                        batteryInfoListener.onBatteryChargingStatusChanged(isCharging);
                    }
                    Log.d(TAG, "isCharging: " + isCharging);
                }
            }
        };
        context.registerReceiver(batteryReceiver, filter);
    }

    private void registerToCellularInfo() {
        IntentFilter filter = new IntentFilter(CELL_SIGNAL_STRENGTH_FILTER);
        cellularReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int cellSignalStrength = intent.getExtras().getInt(CELL_SIGNAL_STRENGTH_KEY);
                lastCellularSignalLevel = cellSignalStrength;
                if(cellSignalStrength>0){
                    lastCellularSignalLevelOk = new Date();
                }
                if (cellularInfoListener != null) {
                    cellularInfoListener.onCellularSignalStrengthSampled(cellSignalStrength);
                }
                Log.d(TAG, "cellSignalStrength: " + cellSignalStrength);
            }
        };
        context.registerReceiver(cellularReceiver, filter);
    }


    private void registerToMobileDataInfo() {
        IntentFilter filter = new IntentFilter(MOBILE_DATA_FILTER);
        mobileDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isOn = intent.getExtras().getBoolean(MOBILE_DATA_IS_ON_KEY);
                HardwareUtilsManager.ReceptionType receptionType = (HardwareUtilsManager.ReceptionType) intent.getExtras().get(MOBILE_DATA_TYPE_KEY);
                String type = isOn ? receptionType.label : HardwareUtilsManager.ReceptionType.UNKNOWN.label;
                lastMobileDataIsOn = isOn;
                lastMobileDataType = type;
                if (mobileDataInfoListener != null) {
                    mobileDataInfoListener.onMobileDataConnectivityStatusSampled(type, isOn);
                }
                Log.d(TAG, "type: " + type + " isOne: " + isOn);
            }
        };
        context.registerReceiver(mobileDataReceiver, filter);
    }

    public void setMobileDataInfoListener(OnMobileDataInfoListener listener) {
        mobileDataInfoListener = listener;
    }

    public void setBatteryInfoListener(OnBatteryInfoListener listener) {
        batteryInfoListener = listener;
    }

    public void setCellularInfoListener(OnCellularInfoListener listener) {
        cellularInfoListener = listener;
    }

    public void removeBatteryInfoListener(OnBatteryInfoListener listener) {
      if(batteryInfoListener==listener)  {batteryInfoListener = null;}
    }

    public void removeMobileDataInfoListener(OnMobileDataInfoListener listener) {
        if(mobileDataInfoListener==listener)  {mobileDataInfoListener = null;}
    }

    public void removeCellularInfoListener(OnCellularInfoListener listener) {
        if(cellularInfoListener==listener)  {cellularInfoListener = null;}
    }

    public interface OnBatteryInfoListener {
        void onBatteryPercentageChanged(int percentage);

        void onBatteryChargingStatusChanged(boolean isCharging);
    }

    public interface OnMobileDataInfoListener {
        void onMobileDataConnectivityStatusSampled(String type, boolean isOn);
    }

    public interface OnCellularInfoListener {
        void onCellularSignalStrengthSampled(int signalLevel);
    }
}
