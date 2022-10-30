package com.supercom.puretrack.data.source.remote;

import com.supercom.puretrack.model.business_logic_models.enums.ServerMessageType;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.FlowType;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.data.source.remote.DownloadTaskMain.Download_Task_Type;

public interface ViewUpdateListener {

    // gps points request
    void onGpsPointsUploadedFinishedToParse();

    // debug info request
    void onUnallocateRecordUploaded(FlowType flowType);

    //event request
    void onEventResposeOkFromServer();

    // configuration
    void onGetDeviceConfigurationResultParserFinishedToParse(boolean isTagIdChanged, boolean isBeaconIdChanged, boolean isCommIntervalChanged,
                                                             String lastTagRfId, boolean isLocationSettingsChanged, boolean isVoipSettingsChanged, boolean isCaseTamperEnabled,
                                                             boolean isLockScreenChanged, boolean isAppLanguageChanged);

    // zones request
    void onBeaconZoneAddedToDB();

    void onBeaconZoneDeletedFromDB();

    void onZonesRequestFinishedToParse();

    // handle request
    void onHandleResponseSucceeded(int requestType);

    // offender request
    void onMessageReceivedFromServer(ServerMessageType serverMessageType, int requestId);

    void onBiometricReceivedFromServer();

    void onUnallocatedReceivedFromServer(int requestId);

    void onActivateReceivedFromServer();

    void downloadApkFromServer(String downloadURL, String apkTargetFileName, String versionFromServer, Download_Task_Type downloadTaskType);

    void installApk(String apkTargetFileName);

    void onManualHandleReceivedFromServer(int openEventId);


    void enableFlightMode(int timeOut);

    void addLbsLocation(EntityGpsPoint lbsRecord);

    boolean isLbsLocationRequestRequired();

    void onOffenderAtHomeStatusChanged(boolean isOffenderAtHome);

}
