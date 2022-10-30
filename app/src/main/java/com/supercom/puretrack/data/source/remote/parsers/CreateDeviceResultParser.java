package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.data.source.remote.requests_listeners.CreateDeviceListener;
import com.supercom.puretrack.ui.dialog.SettingFragmentViewListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class CreateDeviceResultParser implements CreateDeviceListener {

    public static String TAG = "CreateDeviceResultParser";

    private final WeakReference<SettingFragmentViewListener> weakRefUpdateFragmentViewListener;

    public CreateDeviceResultParser(SettingFragmentViewListener updateUiListener) {
        this.weakRefUpdateFragmentViewListener = new WeakReference<>(updateUiListener);
    }

    @Override
    public void handleResponse(String response) {
        if (response != null) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                String deviceSerialNumber = jsonResponse.optString("DeviceSN", "");
                String password = jsonResponse.optString("Password", "");
                SettingFragmentViewListener listener = weakRefUpdateFragmentViewListener.get();
                if (listener != null) {
                    listener.onDeviceRegistered(deviceSerialNumber, password);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
