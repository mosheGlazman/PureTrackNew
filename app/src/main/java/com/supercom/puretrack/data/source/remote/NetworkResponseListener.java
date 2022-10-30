package com.supercom.puretrack.data.source.remote;

public interface NetworkResponseListener {

    // handleRequest
    void onOldHandleRequestResponseSucceeded();

    void onHandleRequestResponseFailed();

}
