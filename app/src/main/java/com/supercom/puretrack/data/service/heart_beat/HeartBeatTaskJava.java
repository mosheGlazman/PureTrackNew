package com.supercom.puretrack.data.service.heart_beat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.util.general.LoggingUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HeartBeatTaskJava implements Runnable {
    public static String TAG = "HeartBeatTask";
    private long setStatusDate = new Date().getTime();
    byte OPCODE_ACTIVATE_HEART_BEAT = 0x40;
    private static int lastId = 0;
    private int id = 0;
    public static String tagAddress = "";//""0C:1C:57:B5:B2:A8";
    public static UUID characteristicUUID = UUID.fromString("f000f0c2-0451-4000-b000-000000000000");
    public static UUID serviceUUID = UUID.fromString("F000F0C0-0451-4000-B000-000000000000");
    private static int timeout = 10;
    private int heartBeatTimeOutParam = 10;


    public interface HeartBeatTaskListener {
        void onStatusChange(HeartBeatTaskJava task);

        void log(LogLine line);
    }

    public e_status status;
    public e_result result;
    public Date startTime;
    HeartBeatTaskListener listener;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mScanner;
    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;
    Context context;
    String error;

    public enum e_status {
        none,
        init,
        searchDevice,
        deviceFound,
        connectingToGatt,
        gattConnected,
        finish
    }

    public enum e_result {
        success,
        timeOut,
        searchDeviceTimeOut,
        connectingToGattTimeOut,
        serviceIsNull,
        failedToSendBytes,
        gattDisconnect,
        gattDisconnect_19,
        gattDisconnect_257,
        gattDisconnect_133,
        cancel;

        public boolean isDisconnect() {
            return this.ordinal() >= gattDisconnect.ordinal() && this.ordinal() <= gattDisconnect_133.ordinal();
        }
    }

    public void start(Context context,String tagAddress,int TagHeartBeatTimeoutToVibrate, HeartBeatTaskListener listener) {
        this.listener = listener;
        this.context = context;
        this.tagAddress = tagAddress;
        startTime = new Date();
        lastId++;
        id = lastId;
        heartBeatTimeOutParam = (int) Math.sqrt(TagHeartBeatTimeoutToVibrate);
        if (heartBeatTimeOutParam == 0) {
            heartBeatTimeOutParam = 10;
        }

        new Thread(this).start();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        int interval;

        if(com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager.getDevice()!= null){
            mDevice=com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager.getDevice();
            setStatus(e_status.deviceFound);
        }else {
            setStatus(e_status.init);
        }

        while (status != e_status.finish) {
            interval = 2000;
            if (secondsPassWithoutSetStatus() > timeout) {
                sendLogLine("Service", "Seconds pass without set status" + secondsPassWithoutSetStatus(), false);
                if (status == e_status.connectingToGatt) {
                    finish(e_result.connectingToGattTimeOut);
                } else if (status == e_status.searchDevice) {
                    finish(e_result.searchDeviceTimeOut);
                    timeout += 4;
                } else {
                    finish(e_result.timeOut);
                }
                return;
            }

            try {
                if (status == e_status.init) {
                    mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    mBtAdapter = mBluetoothManager.getAdapter();
                    mScanner = mBtAdapter.getBluetoothLeScanner();
                    ArrayList<ScanFilter> filters = new ArrayList<>();
                    ScanFilter tagFilter = new ScanFilter.Builder().setDeviceAddress(tagAddress).build();
                    filters.add(tagFilter);
                    ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                    setStatus(e_status.searchDevice);
                    mScanner.startScan(filters, settings, scanCallback);
                }

                if (status == e_status.deviceFound) {
                    setStatus(e_status.connectingToGatt);
                    mBluetoothGatt = mDevice.connectGatt(context, false, gattCallback);
                }

                if (status == e_status.gattConnected) {
                    sleep(1000);
                    mService = mBluetoothGatt.getService(serviceUUID);

                    if (mService == null) {
                        sendLogLine("HeartBeat", "Service is null", false);
                        finish(e_result.serviceIsNull);
                        return;
                    }

                    sendHeartBeat();
                }
            } catch (Exception ex) {
                Log.e(TAG, "[" + id + "] " + "error", ex);
                sendLogLine("Service", "error: " + ex.getMessage(), false);
            }catch (Throwable t) {
                Log.e(TAG, "[" + id + "] " + "error", t);
                sendLogLine("Service", "error t: " + t.getMessage(), false);
            }

            sleep(interval);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void sendLogLine(String tag, String text, Boolean action) {
        if(!BuildConfig.DEBUG){
            return;
        }

        if (action != null && action == false) {
            Log.e(TAG, "[" + id + "] " + text);
        } else {
            Log.i(TAG, "[" + id + "] " + text);
        }

        LogLine line = new LogLine();
        line.setActionStatus(action);
        line.setTag("[" + id + "]");
        line.setText(text);
        listener.log(line);
    }

    @SuppressLint("MissingPermission")
    public void finish(e_result result) {
        this.result = result;
        e_status lastStatus = status;
        setStatus(e_status.finish);
        sendLogLine("Service", "finish task: " + result, result == e_result.success);

        LoggingUtil.updateHeartBeatLog(this);


        if (result == e_result.success) {
            timeout = 10;
        }

        if (lastStatus.ordinal() >= e_status.connectingToGatt.ordinal()) {
            if (mBluetoothGatt != null) {
                try {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mBluetoothGatt = null;
        if (mScanner != null) {
            mScanner.stopScan(scanCallback);
            mScanner = null;
        }

        if (mBtAdapter != null) {
            mBtAdapter = null;
        }

        if (mBluetoothManager != null) {
            mBluetoothManager = null;
        }
    }

    private void setStatus(e_status newStatus) {
        sendLogLine("Status", status + " --> " + newStatus, null);

        status = newStatus;
        setStatusDate = new Date().getTime();

        listener.onStatusChange(this);
    }

    private long secondsPassWithoutSetStatus() {
        long passTime = new Date().getTime() - setStatusDate;
        return passTime / 1000;
    }

    @SuppressLint("MissingPermission")
    public void sendHeartBeat() {
        sendLogLine("HeartBeat", "sendHeartBeat", null);

        int[] pureTrackAddressInts = convertAddressToIntArray(tagAddress);
        if (pureTrackAddressInts == null) {
            sendLogLine("HeartBeat", "write failed: pureTrackAddressInts is null", false);
            error = "write failed: pureTrackAddressInts is null";
            finish(e_result.failedToSendBytes);
            return;
        }

        BluetoothGattCharacteristic characteristic = mService.getCharacteristic(characteristicUUID);
        byte[] value = new byte[]{
                OPCODE_ACTIVATE_HEART_BEAT,
                unsiIntToByte(heartBeatTimeOutParam),
                unsiIntToByte(heartBeatTimeOutParam),
                unsiIntToByte(pureTrackAddressInts[0]),
                unsiIntToByte(pureTrackAddressInts[1]),
                unsiIntToByte(pureTrackAddressInts[2]),
                unsiIntToByte(pureTrackAddressInts[3]),
                unsiIntToByte(pureTrackAddressInts[4]),
                unsiIntToByte(pureTrackAddressInts[5]),
        };

        characteristic.setValue(value);
        final int charaProp = characteristic.getProperties();

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) == 0 ||
                (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            sendLogLine("HeartBeat", "write failed: charaProp is null", false);
            error = "write failed: charaProp is null";
            finish(e_result.failedToSendBytes);
            return;
        }

        boolean write = mBluetoothGatt.writeCharacteristic(characteristic);
        sendLogLine("HeartBeat", "write: " + write, write);

        if (!write) {
            finish(e_result.failedToSendBytes);
        } else {
            finish(e_result.success);
        }
    }

    protected byte unsiIntToByte(int value) {
        return (byte) (value & 0xFF);
    }

    protected int[] convertAddressToIntArray(String deviceAddress) {
        String[] addrStrParts = deviceAddress.split(":");
        int[] addressInts = new int[addrStrParts.length];

        for (int i = 0; i < addrStrParts.length; i++) {
            try {
                addressInts[i] = Integer.parseInt(addrStrParts[i], 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }

        return addressInts;
    }

    ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (status != e_status.searchDevice) {
                return;
            }

            mDevice = mBtAdapter.getRemoteDevice(result.getDevice().getAddress());
            setStatus(e_status.deviceFound);
            mScanner.stopScan(this);
            sendLogLine("Device", "device found", null);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.i(TAG, "[" + id + "] " + "onPhyUpdate");
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.i(TAG, "[" + id + "] " + "onPhyRead");
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status1, int newState) {
           super.onConnectionStateChange(gatt, status1, newState);
            Log.i(TAG+"F" , "[" + id + "] " + "onConnectionStateChange: " + status1 + "," + newState);

            if (status != e_status.connectingToGatt) {
                return;
            }

             if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
                sendLogLine("Connection", "service connected", true);
                setStatus(e_status.gattConnected);
                return;
            }

            sendLogLine("Connection", "Connection status Change " + status1 + "," + newState, false);
            error = "gatt disconnect, status:" + status + " state" + newState;


            if (status1 == 257) {
                finish(e_result.gattDisconnect_257);
            } else if (status1 == 19) {
                finish(e_result.gattDisconnect_19);
            } else if (status1 == 133) {
                finish(e_result.gattDisconnect_133);
            } else {
                finish(e_result.gattDisconnect);
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "[" + id + "] " + "onServicesDiscovered");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "[" + id + "] " + "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "[" + id + "] " + "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "[" + id + "] " + "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(TAG, "[" + id + "] " + "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "[" + id + "] " + "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(TAG, "[" + id + "] " + "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(TAG, "[" + id + "] " + "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "[" + id + "] " + "onMtuChanged");
        }
    };
}
