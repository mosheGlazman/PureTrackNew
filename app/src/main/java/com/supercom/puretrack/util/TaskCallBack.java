package com.supercom.puretrack.util;

import android.location.Location;

public interface TaskCallBack {
    boolean onReceived(Location location);
    void onStop(boolean locationReceived);
}
