package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

public class DeviceInformationManager {
    public class DeviceInfoDetailsObj {
        public String sw_version;
        public String hw_version_phone_model;
        public String hw_components;
        public String imei;
        public String os_version;
        public String db_version;
        public String batteryCapacity;

        public DeviceInfoDetailsObj(String sw_version, String hw_version_phone_model, String hw_components, String imei,
                                    String os_version, String db_version, String batteryCapacity) {
            super();
            this.sw_version = sw_version;
            this.hw_version_phone_model = hw_version_phone_model;
            this.hw_components = hw_components;
            this.imei = imei;
            this.os_version = os_version;
            this.db_version = db_version;
            this.batteryCapacity = batteryCapacity;

        }
    }

    public class DeviceInfoStatusObj {
        public String knox_activated;
        public String kiosk_mode_enabled;

        public DeviceInfoStatusObj(String knox_activated, String kiosk_mode_enabled) {
            super();
            this.knox_activated = knox_activated;
            this.kiosk_mode_enabled = kiosk_mode_enabled;
        }
    }

    private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

    public DeviceInformationManager() {
    }

    public DeviceInfoDetailsObj getDeviceInfoDetailsObj() {
        Context context = App.getAppContext();
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String sw_version = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            sw_version = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String hw_version_phone_model = Build.HARDWARE + "/ " + Build.MANUFACTURER + " " + Build.MODEL;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        String wifiMessage = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable() ? "isAvailiable" : "Not Availiable";
        String landlineMessage = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) ? "isAvailiable" : "Not Availiable";
        String fingerprintMessage = "Not Availiable";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintMessage = ((FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE)).isHardwareDetected() ? "isAvailiable" : "Not Availiable";
        }
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        String magneticMessage = (mSensorManager != null && mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) ? "isAvailiable" : "Not Availiable";
        String temperatureMessage = (mSensorManager != null && mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) ? "isAvailiable" : "Not Availiable";
        String hw_components = "WiFi: " + wifiMessage + "; Landline: " + landlineMessage + "; Fingerprint: " + fingerprintMessage + "; Magnetic: " + magneticMessage +
                "; Temperature: " + temperatureMessage;
        @SuppressLint("MissingPermission")
        String imei = getImei(context);
        String os_version = Build.VERSION.RELEASE;
        String db_version = DatabaseAccess.getInstance().getDatabaseVersion();
        String batteryCapacity = "";
        try {
            Object mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context.class).newInstance(App.getAppContext());
            batteryCapacity = String.valueOf(Class.forName(POWER_PROFILE_CLASS).getMethod("getAveragePower", java.lang.String.class).invoke(mPowerProfile_, "battery.capacity"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DeviceInfoDetailsObj(sw_version, hw_version_phone_model, hw_components, imei, os_version, db_version, batteryCapacity);
    }

    private String getImei(Context context) {
        try {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public DeviceInfoStatusObj getDeviceInfoStatusObj() {
        String knox_activated = PureTrackSharedPreferences.isKnoxLicenceActivated() ? "activated" : "NOT activated";
        String kiosk_mode_enabled = KnoxUtil.getInstance().getKnoxSDKImplementation().isInKioskMode() ? "ON" : "OFF";
        return new DeviceInfoStatusObj(knox_activated, kiosk_mode_enabled);

    }
}
