package com.supercom.puretrack.util.hardware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.supercom.puretrack.util.application.App;

public class NetworkHardwareUtil {

    public static  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isConnected(){
        ConnectivityManager
                cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null
                && activeNetwork.isAvailable();
    }

    public static boolean isMobileNetworkConnected(){
        ConnectivityManager
                cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null
                && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && activeNetwork.isConnectedOrConnecting();

    }

    public static boolean isWifiConnected(){
        ConnectivityManager
                cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getActiveNetworkInfo();

        return wifiNetwork != null
                && wifiNetwork.getType() == ConnectivityManager.TYPE_WIFI && wifiNetwork.isConnectedOrConnecting();
    }
}
