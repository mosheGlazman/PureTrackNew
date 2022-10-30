package com.supercom.puretrack.model.business_logic_models.network.sync_requests;

public interface OffenderRequestType {
    int MESSAGE = 1;
    int SYNC = 2;
    int BIOMETRIC = 3;
    int SYNC_NOW = 4;
    int TERMINATE = 5;
    int SUSPEND = 6;
    int ACTIVATE = 7;
    int SW_UPGRADE = 8;
    int MANUAL_HANDLE = 10;
    int REMOTE_COMMAND = 14; // flight mode, reboot, etc.
    int SUSPEND_RESUME = 15;
    int PHOTO_ON_DEMAND_MESSAGE = 18;
}