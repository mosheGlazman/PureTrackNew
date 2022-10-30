package com.supercom.puretrack.database;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class CallLogObserver extends ContentObserver {

    private final Handler handler;

    public CallLogObserver(Handler handler) {
        super(handler);
        this.handler = handler;
    }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        handler.sendEmptyMessage(1);
    }

}
