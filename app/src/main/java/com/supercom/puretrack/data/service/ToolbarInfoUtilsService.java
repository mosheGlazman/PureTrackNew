package com.supercom.puretrack.data.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.HardwareUtilsManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.model.business_logic_models.shielding.CellularStrengthModel;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ToolbarInfoUtilsService extends Service {
    private static final String TAG = "ToolbarService";
    private static final int SAMPLE_INFO_DEFAULT_PERIOD_SEC = 5;
    public static String CELL_SIGNAL_STRENGTH_FILTER = "supercom.cell.signal.strength";
    public static String CELL_SIGNAL_STRENGTH_KEY = "cell_signal_strength_key";
    public static String MOBILE_DATA_FILTER = "supercom.mobile.data";
    public static String MOBILE_DATA_TYPE_KEY = "mobile_data_type_key";
    public static String MOBILE_DATA_IS_ON_KEY = "mobile_data_is_on_key";
    public static String BATTERY_FILTER = "supercom.battery";
    public static String BATTERY_IS_CHARGING_KEY = "battery_is_charging_key";
    public static String BATTERY_PERCENT_KEY = "battery_percentage_key";
    private static boolean isRunning;
    private final ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    private final HardwareUtilsManager hardwareUtilsManager = new HardwareUtilsManager();
int batteryPercent;

    boolean allowRebind; // indicates whether onRebind should be used
    HardwareUtilsManager.ReceptionType receptionType;
    private final int sampleMobileInfoPeriodSec = SAMPLE_INFO_DEFAULT_PERIOD_SEC;
    private OnToolbarInfoListener toolbarInfoListener;
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private BroadcastReceiver batteryReceiver;

    private final NetworkCallback networkCallback = new NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Log.d(TAG, "onAvailable() - " + network);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Log.d(TAG, "onLost() - " + network);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            boolean isOn = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            Log.i(TAG, "onCapabilitiesChanged: mobile data is " + (isOn ? "on" : "off"));
            Log.d(TAG, "onCapabilitiesChanged() - " + network + "\n" + networkCapabilities);
        }
    };

    public static void stop(Context context) {
        Log.i(TAG, "stop");
        if (!isRunning) {
            return;
        }
        isRunning=false;

        Intent toolbarServiceIntent = new Intent(context, ToolbarInfoUtilsService.class);
        context.stopService(toolbarServiceIntent);
    }
    public static void start(Context context) {
        Log.i(TAG, "start");
        if (isRunning) {
            return;
        }
        isRunning=true;
        Intent toolbarServiceIntent = new Intent(context, ToolbarInfoUtilsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(toolbarServiceIntent);
        } else {
            context.startService(toolbarServiceIntent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.i(TAG, "onCreate");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelXXX();
        }
        batteryPercent = OffenderPreferencesManager.getInstance().getLastBatteryPercent();

        handleBatteryInfo();
        handleMobileNetworkInfo();
        runBatteryThread();
    }

    private void runBatteryThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (OffenderPreferencesManager.getInstance().isCheckedBatteryPercentForSuddenShutDown) {
                        OffenderPreferencesManager.getInstance().setLastBatteryPercent(batteryPercent);
                    }
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private NotificationManager mNotific = null;
    CharSequence name = "Ragav";
    String desc = "this is notific";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    final String ChannelID = "my_channel_04";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannelXXX() {
        App.writeToZoneLogsAndDebugInfo("ForLoc", "Foreground Service - createNotificationChannel", DebugInfoModuleId.Zones);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotific = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    importance);
            mChannel.setDescription(desc);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotific.createNotificationChannel(mChannel);
        }

        String Body = "ToolBar Service Is Running";

        Notification n = new Notification.Builder(this, ChannelID)
                .setContentTitle("ToolBar Service")
                .setContentText(Body)
                .setBadgeIconType(R.drawable.ic_baseline_battery_full_24)
                .setNumber(5)
                .setSmallIcon(R.drawable.ic_baseline_battery_full_24)
                .setAutoCancel(true)
                .build();

        startForeground(9, n);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setToolbarInfoListener(OnToolbarInfoListener toolbarInfoListener) {
        this.toolbarInfoListener = toolbarInfoListener;
    }

    private void removeToolbarInfoListener() {
        toolbarInfoListener = null;
    }

    private void handleBatteryInfo() {
        observeBatteryChanges();
    }


    private void handleMobileNetworkInfo() {
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    sampleCellularSignalStrength();
                    sampleMobileDataConnectivityStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, sampleMobileInfoPeriodSec, TimeUnit.SECONDS);
    }

    private void handleMobileNetworkInfo2() {
        observeMobileNetworkChanges();
    }

    private void observeMobileNetworkChanges() {
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }

    private void observeBatteryChanges() {
        batteryReceiver = new BroadcastReceiver() {
            private int currChargingState = DeviceStateManager.ChargingState.UNKNOWN;

            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "onReceive");
                int curPluggedStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                int newChargingState = (curPluggedStatus == 0 ? DeviceStateManager.ChargingState.NOT_CHARGING : DeviceStateManager.ChargingState.ON_CHARGING);
                boolean isCharging = newChargingState == DeviceStateManager.ChargingState.ON_CHARGING;
                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                if (rawLevel >= 0 && scale > 0) {
                    batteryPercent = (rawLevel * 100) / scale;
                }

                // because of the mess of this app we send broadcast receiver YAK :-(
                Intent i = new Intent(BATTERY_FILTER);
                i.putExtra(BATTERY_PERCENT_KEY, batteryPercent);
                i.putExtra(BATTERY_IS_CHARGING_KEY, isCharging);
                sendBroadcast(i);

                if (toolbarInfoListener != null) {
                    toolbarInfoListener.onBatteryPercentageChanged(batteryPercent);
                    if (currChargingState != newChargingState) {
                        toolbarInfoListener.onBatteryChargingStatusChanged(isCharging);
                    }
                }

                currChargingState = newChargingState;
                Log.i(TAG, "rawLevel:" + rawLevel + " scale:" + scale + " percentage: " + batteryPercent + "%"+ " chargingState: " + (isCharging ? "charging..." : "not charging!") + "(" + currChargingState + ")");
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void unregisterBatteryChanges() {
        unregisterReceiver(batteryReceiver);
    }

    private void sampleCellularSignalStrength() {
        Log.i(TAG, "sampleCellularSignalStrength");

        if (telephonyManager != null) {
            CellularStrengthModel cellularStrengthModel = hardwareUtilsManager.calculateCellularStrength(telephonyManager);

            int dbmToSignalStrength = cellularStrengthModel != null ? cellularStrengthModel.getSignalStrength() : 0;
            receptionType = cellularStrengthModel != null ? cellularStrengthModel.getReceptionType(): HardwareUtilsManager.ReceptionType.UNKNOWN;
            Log.i(TAG, "CellSignalStrength: " + (cellularStrengthModel != null ? cellularStrengthModel.getDbmStrength() : "?") + "(level " + dbmToSignalStrength + ")" + " type: " + receptionType.label);

            // because of the mess of this app we send broadcast receiver YAK :-(
            Intent intent = new Intent(CELL_SIGNAL_STRENGTH_FILTER);
            intent.putExtra(CELL_SIGNAL_STRENGTH_KEY, dbmToSignalStrength);
            sendBroadcast(intent);

            if (toolbarInfoListener != null) {
                toolbarInfoListener.onCellularSignalStrengthSampled(dbmToSignalStrength);
            }
        }
    }

    private void sampleMobileDataConnectivityStatus() {
        Log.i(TAG, "sampleMobileDataConnectivityStatus");

        NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        // because of the mess of this app we send broadcast receiver YAK :-(
        Intent intent = new Intent(MOBILE_DATA_FILTER);
        intent.putExtra(MOBILE_DATA_IS_ON_KEY, (mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnectedOrConnecting()));
        intent.putExtra(MOBILE_DATA_TYPE_KEY, receptionType);
        sendBroadcast(intent);

        boolean isOn = mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnectedOrConnecting();

        if (toolbarInfoListener != null) {
            toolbarInfoListener.onMobileDataConnectivityStatusSampled(receptionType, isOn);
        }

        Log.i(TAG, "sampleMobileDataConnectivityStatus: mobile data is " + (isOn ? "on" : "off"));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return allowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBatteryChanges();
        removeToolbarInfoListener();
        isRunning = false;
        Log.i(TAG, "onDestroy");
    }

    public interface OnToolbarInfoListener {

        // Battery
        void onBatteryPercentageChanged(int percentage);
        void onBatteryStatusChanged(int newStatus);
        void onBatteryChargingStatusChanged(boolean isCharging);

        // Cellular
        void onCellularSignalStrengthSampled(int signalLevel);

        // Mobile Data
        void onMobileDataConnectivityStatusSampled(HardwareUtilsManager.ReceptionType type, boolean isOn);
    }
}
