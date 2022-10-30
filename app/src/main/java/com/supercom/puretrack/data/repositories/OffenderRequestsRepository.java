package com.supercom.puretrack.data.repositories;

import static com.supercom.puretrack.util.constants.OffenderActivation.OFFENDER_STATUS_ALLOCATED;
import static com.supercom.puretrack.util.constants.OffenderActivation.OFFENDER_STATUS_PENDING_ACTIVATION_ENROLLMENT;
import static com.supercom.puretrack.util.constants.OffenderActivation.OFFENDER_STATUS_PENDING_ENROLMENT;
import static com.supercom.puretrack.util.constants.OffenderActivation.OFFENDER_STATUS_UNALLOCATED;
import static com.supercom.puretrack.util.constants.network.OffenderRequestsRepositoryConstants.COMMAND_FLIGHT_MODE;
import static com.supercom.puretrack.util.constants.network.OffenderRequestsRepositoryConstants.EXPECTED_PURE_TRACK_APK_FILE_NAME;

import android.util.Log;

import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.remote.DownloadTaskMain;
import com.supercom.puretrack.data.source.remote.DownloadTaskMain.Download_Task_Type;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.enums.ServerMessageType;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.OffenderRequestType;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.SingleOffenderRequest;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.SingleSyncRequest;
import com.supercom.puretrack.model.database.entities.EntityTextMessage;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.general.NumberComputationUtil;
import com.supercom.puretrack.util.hardware.FilesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class OffenderRequestsRepository {

    private static final OffenderRequestsRepository INSTANCE = new OffenderRequestsRepository();

    private OffenderRequestsRepository() {

    }

    public static synchronized OffenderRequestsRepository getInstance() {
        return INSTANCE;
    }


    private ArrayList<SingleOffenderRequest> curSingleOffenderRequestArr = new ArrayList<>();
    private JSONArray offenderRequestsResultJsonDataArray;
    private ViewUpdateListener viewUpdateListener;
    private boolean hasManualHandleTypeRequest;
    public boolean isManualMonitoringSuspendedFromRequest = false;

    public void setViewUpdateListener(ViewUpdateListener viewUpdateListener) {
        this.viewUpdateListener = viewUpdateListener;
    }

    public void setOffenderRequestsResultJsonDataArray(JSONArray offenderRequestsResultJsonDataArray) {
        this.offenderRequestsResultJsonDataArray = offenderRequestsResultJsonDataArray;
    }

    public void handleOffenderRequestArray() throws JSONException {

        if (offenderRequestsResultJsonDataArray == null || offenderRequestsResultJsonDataArray.length() == 0) {
            if (hasManualHandleTypeRequest) {
                hasManualHandleTypeRequest = false;
                NetworkRepository.getInstance().sendNewEventArray(NetworkRequestName.PostTerminate);
            } else {
                NetworkRepository.getInstance().httpTerminateToken();
            }
            return;
        }

        int offenderAllocatedStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS);
        if (offenderAllocatedStatus == OFFENDER_STATUS_UNALLOCATED) {
            handleOffenderUnallocated();
        } else if (offenderAllocatedStatus == OFFENDER_STATUS_PENDING_ENROLMENT ||
                offenderAllocatedStatus == OFFENDER_STATUS_PENDING_ACTIVATION_ENROLLMENT) {
            handlePendingEnrollment();
        } else if (offenderAllocatedStatus == OFFENDER_STATUS_ALLOCATED) {
            handleOffenderAllocated();
        }
    }

    private void handleOffenderUnallocated() throws JSONException {
        JSONObject itemRequestJsonObject = getOffenderRequestToHandleByPriority(new ArrayList<>(Arrays.asList(OffenderRequestType.SW_UPGRADE,
                OffenderRequestType.ACTIVATE)));
        if (itemRequestJsonObject != null) {

            String RequestId = itemRequestJsonObject.getString("RequestId");
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED,
                    Integer.parseInt(RequestId));

            int offenderRequestType = itemRequestJsonObject.getInt("OffenderRequestType");
            NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED = offenderRequestType;

            switch (offenderRequestType) {
                case OffenderRequestType.ACTIVATE:

                    partiallyParseOffenderRequestsDataArray(itemRequestJsonObject);
                    handleActivateRequest(viewUpdateListener);

                    break;

                case OffenderRequestType.SW_UPGRADE:
                    handleOffenderRequest(itemRequestJsonObject);
                    break;

                default:
                    NetworkRepository.getInstance().httpTerminateToken();
            }
        } else {
            NetworkRepository.getInstance().httpTerminateToken();
        }
    }

    private void handlePendingEnrollment() throws JSONException {
        JSONObject itemRequestJsonObject = getOffenderRequestToHandleByPriority(new ArrayList<>(Arrays.asList(OffenderRequestType.SYNC,
                OffenderRequestType.SW_UPGRADE, OffenderRequestType.TERMINATE)));
        if (itemRequestJsonObject == null) {
            NetworkRepository.getInstance().httpTerminateToken();
            return;
        }

        String RequestId = itemRequestJsonObject.getString("RequestId");
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED,
                Integer.parseInt(RequestId));

        NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED = itemRequestJsonObject.getInt("OffenderRequestType");

        switch (itemRequestJsonObject.getInt("OffenderRequestType")) {
            case OffenderRequestType.SYNC:
            case OffenderRequestType.SW_UPGRADE:
                handleOffenderRequest(itemRequestJsonObject);
                break;

            case OffenderRequestType.TERMINATE:
                if (shouldHandleTerminateRequest(itemRequestJsonObject)) {

                    handleOffenderRequest(itemRequestJsonObject);
                }
                break;

            default:
                NetworkRepository.getInstance().httpTerminateToken();
        }
    }


    private void handleOffenderAllocated() throws JSONException {
        int positionToDelete = 0;
        JSONObject JsonObjectData = offenderRequestsResultJsonDataArray.getJSONObject(positionToDelete);

        //we want to do upgrade only in the end of data array
        if (JsonObjectData.getInt("OffenderRequestType") == OffenderRequestType.SW_UPGRADE && offenderRequestsResultJsonDataArray.length() > 1) {
            for (int i = 1; i < offenderRequestsResultJsonDataArray.length(); i++) {
                if (offenderRequestsResultJsonDataArray.getJSONObject(i).getInt("OffenderRequestType") != OffenderRequestType.SW_UPGRADE) {
                    JsonObjectData = offenderRequestsResultJsonDataArray.getJSONObject(i);
                    positionToDelete = i;
                    break;
                }
            }
        }
        offenderRequestsResultJsonDataArray.remove(positionToDelete);

        String RequestId = JsonObjectData.getString("RequestId");
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED,
                Integer.parseInt(RequestId));

        NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED = JsonObjectData.getInt("OffenderRequestType");

        handleOffenderRequest(JsonObjectData);

    }


    private JSONObject getOffenderRequestToHandleByPriority(ArrayList<Integer> priorityRequestArray) {

        for (Integer offenderRequestItem : priorityRequestArray) {

            JSONObject itemRequestJsonObject = getRequestItemIfExists(offenderRequestItem);

            if (itemRequestJsonObject != null) {
                return itemRequestJsonObject;
            }
        }

        return null;
    }

    private boolean shouldHandleTerminateRequest(JSONObject JsonObjectData) throws JSONException {
        return JsonObjectData.getInt("OffenderRequestType") == OffenderRequestType.TERMINATE &&
                !NetworkRepository.getInstance().shouldUploadRecordsOrEventsToServerImmediately();
    }

    private JSONObject getRequestItemIfExists(int type) {
        for (int i = 0; i < offenderRequestsResultJsonDataArray.length(); i++) {
            JSONObject item;
            try {
                item = offenderRequestsResultJsonDataArray.getJSONObject(i);
                if (item.getInt("OffenderRequestType") == type) {
                    offenderRequestsResultJsonDataArray.remove(i);
                    return item;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    private void partiallyParseOffenderRequestsDataArray(JSONObject itemRequestJsonObject) {
        ArrayList<SingleSyncRequest> activateRequestDataArr = new ArrayList<>();
        SingleSyncRequest singleSyncRequest;
        curSingleOffenderRequestArr = new ArrayList<>();
        try {

            SingleOffenderRequest singleOffenderRequest = new SingleOffenderRequest();
            singleOffenderRequest.RequestId = itemRequestJsonObject.getString("RequestId");
            singleOffenderRequest.OffenderRequestType = itemRequestJsonObject.getInt("OffenderRequestType");
            singleOffenderRequest.OffenderId = itemRequestJsonObject.getInt("OffenderId");
            if (singleOffenderRequest.OffenderRequestType == OffenderRequestType.ACTIVATE) {
                JSONArray JsonOffenderActivateRequestData = itemRequestJsonObject.getJSONArray("RequestData");
                for (int j = 0; j < JsonOffenderActivateRequestData.length(); j++) {
                    singleSyncRequest = new SingleSyncRequest();
                    singleSyncRequest.requestDataType = JsonOffenderActivateRequestData.getJSONObject(j).getInt("Type");
                    singleSyncRequest.requestDataVersion = JsonOffenderActivateRequestData.getJSONObject(j).getString("Number");

                    activateRequestDataArr.add(singleSyncRequest);
                }
                singleOffenderRequest.RequestData = activateRequestDataArr;
            }
            curSingleOffenderRequestArr.add(singleOffenderRequest);
        } catch (JSONException e) {
            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);
            e.printStackTrace();
        }
    }

    private void handleActivateRequest(ViewUpdateListener updateActivityListener) {
        for (int i = 0; i < curSingleOffenderRequestArr.size(); i++) {
            if (curSingleOffenderRequestArr.get(i).OffenderRequestType == OffenderRequestType.ACTIVATE) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED,
                        Integer.parseInt(curSingleOffenderRequestArr.get(i).RequestId));
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED,
                        Integer.parseInt(curSingleOffenderRequestArr.get(i).RequestId));
                NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED = curSingleOffenderRequestArr.get(i).OffenderRequestType;
                SyncRequestsRepository.getInstance().setViewUpdateListener(updateActivityListener);
                SyncRequestsRepository.getInstance().treatActivateRequest(curSingleOffenderRequestArr.get(i).RequestData);
            }
        }
    }

    private void handleOffenderRequest(JSONObject jsonObject) {
        try {
            int offenderRequestType = jsonObject.getInt("OffenderRequestType");
            int requestId = jsonObject.getInt("RequestId");
            int offenderId = jsonObject.getInt("OffenderId");
            long currentOffId = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_ID);

            //we don't want to treat requests that not belong to current offender, therefore we want to skip those requests
            if (currentOffId != offenderId && offenderId > 0 && currentOffId > 0) {
                handleOffenderRequestArray();
                return;
            }
            switch (offenderRequestType) {
                case OffenderRequestType.PHOTO_ON_DEMAND_MESSAGE:
                case OffenderRequestType.MESSAGE:
                    String Message = "";

                    Log.i("bug799",jsonObject.toString());
                    try {
                        JSONObject JRequestData = jsonObject.getJSONObject("RequestData");
                        Message = JRequestData.getString("text");
                    } catch (Exception exp) {
                        System.out.println("Message from server error");
                        System.out.println(exp.toString());
                    }

                    String FixedMsg = "";
                    try {
                        FixedMsg = Message.replace("u0027", "'");
                    } catch (Exception exp) {
                        System.out.println("Message (FixedMsg) from server error");
                        System.out.println(exp.toString());
                    }

                    long time = jsonObject.optLong("Time", System.currentTimeMillis() / 1000); // We get the time in sec
                    time = time * 1000; // convert to millis
                    if(time==0) {
                        time = new Date().getTime();
                    }

                    EntityTextMessage entityTextMessage = new EntityTextMessage(
                            0,
                            time,
                            DateFormatterUtil.getCurDateStr(DateFormatterUtil.HM),
                            "Officer",
                            FixedMsg,
                            0);
                    if (offenderRequestType == OffenderRequestType.MESSAGE)
                        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_TEXT_MSG, entityTextMessage);

                    if (offenderRequestType == OffenderRequestType.PHOTO_ON_DEMAND_MESSAGE) {
                        viewUpdateListener.onMessageReceivedFromServer(ServerMessageType.PHOTO_ON_DEMAND, requestId);
                        return;
                    }
                    viewUpdateListener.onMessageReceivedFromServer(ServerMessageType.MESSAGE, requestId);
                    break;
                case OffenderRequestType.REMOTE_COMMAND:
                    JSONObject JRequestDataRemote = jsonObject.getJSONObject("RequestData");
                    JSONArray RemoteRqt = JRequestDataRemote.getJSONArray("text");
                    int CommandId = RemoteRqt.getJSONObject(0).getInt("Command");
                    if (CommandId == COMMAND_FLIGHT_MODE) { // flight mode request
                        JSONObject FlightModeDataObj = RemoteRqt.getJSONObject(0).getJSONObject("Data");
                        int FlightModeTimeout = FlightModeDataObj.getInt("Timeout");
                        NetworkRepository.getInstance().getFlightModeData().setFlightModeTimeOut(FlightModeTimeout);
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.flightModeEnabled, -1, -1);
                    }
                    NetworkRepository.getInstance().setOffenderRequestResultSuccess();
                    NetworkRepository.getInstance().sendHandleOffenderRequest();
                    break;

                case OffenderRequestType.SUSPEND:
                    isManualMonitoringSuspendedFromRequest = true;
                    NetworkRepository.getInstance().setOffenderRequestResultSuccess();
                    NetworkRepository.getInstance().sendHandleOffenderRequest();
                    break;
                case OffenderRequestType.SUSPEND_RESUME:
                    isManualMonitoringSuspendedFromRequest = false;
                    NetworkRepository.getInstance().setOffenderRequestResultSuccess();
                    NetworkRepository.getInstance().sendHandleOffenderRequest();
                    break;

                case OffenderRequestType.SYNC:
                case OffenderRequestType.SYNC_NOW:
                    JSONArray JsonGetOffenderRequestsRequestData = jsonObject.getJSONArray("RequestData");
                    TableOffenderStatusManager.sharedInstance().updateColumnString(OFFENDER_STATUS_CONS.OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON,
                            JsonGetOffenderRequestsRequestData.toString());
                    SyncRequestsRepository.getInstance().setViewUpdateListener(viewUpdateListener);
                    SyncRequestsRepository.getInstance().treatSyncRequests(JsonGetOffenderRequestsRequestData);
                    break;

                case OffenderRequestType.BIOMETRIC:
                    viewUpdateListener.onBiometricReceivedFromServer();
                    break;

                case OffenderRequestType.TERMINATE:

                    SingleOffenderRequest singleOffenderRequest = new SingleOffenderRequest();
                    singleOffenderRequest.OffenderRequestType = offenderRequestType;
                    singleOffenderRequest.RequestId = String.valueOf(requestId);
                    curSingleOffenderRequestArr.add(singleOffenderRequest);

                    viewUpdateListener.onUnallocatedReceivedFromServer(requestId);
                    break;

                case OffenderRequestType.SW_UPGRADE:
                    try {
                        JSONObject requestDataObject = jsonObject.getJSONObject("RequestData");
                        JSONArray textArray = requestDataObject.getJSONArray("text");
                        String verFromServer = textArray.getJSONObject(0).getString("Ver");
                        String path = textArray.getJSONObject(0).getString("Path");
                        String fileName = textArray.getJSONObject(0).getString("File");

                        String ptLatestDownloadedVersion = TableOffenderStatusManager.sharedInstance().getStringValueByColumnName(OFFENDER_STATUS_CONS.OFF_DEVICE_DOWNLOADED_VERSION);
                        String apkTargetFileName = fileName.replace("zip", "apk");
                        String downloadURL = path + fileName;

                        if (isVersionInFileNameIdenticalToVersionFromServer(fileName, verFromServer)) {
                            handleSoftwareUpgrade(downloadURL, apkTargetFileName, verFromServer, ptLatestDownloadedVersion);
                        } else {
                            String errorMessage = "Missmatch in the 'RequestData' object receved from the server. Expected version: " + verFromServer + ", Received File: " + fileName + " .";
                            handleSoftwareUpgradeError(errorMessage);
                        }
                    } catch (JSONException e) {
                        String errorMessage = "Error during parsing the 'RequestData'.";
                        handleSoftwareUpgradeError(errorMessage);
                    }
                    break;

                case OffenderRequestType.MANUAL_HANDLE:
                    JSONObject RequestDataJsonObject = jsonObject.getJSONObject("RequestData");
                    int openEventId = RequestDataJsonObject.getInt("text");

                    hasManualHandleTypeRequest = true;

                    viewUpdateListener.onManualHandleReceivedFromServer(openEventId);

                    break;

                default:
                    NetworkRepository.getInstance().sendHandleOffenderRequest();
                    break;
            }

        } catch (JSONException e) {
            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);

            NetworkRepository.getInstance().handleOffenderRequestError();
        }
    }

    private boolean isVersionInFileNameIdenticalToVersionFromServer(String fileName, String verFromServer) {
        return fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf(".zip")).equals(verFromServer);
    }

    private void handleSoftwareUpgradeError(String errorMessage) {
        String messageToUpload = "SW_UPGRADE: Error in " + "OffenderRequestsManager" + ": " + errorMessage;
        App.writeToNetworkLogsAndDebugInfo("OffenderRequestsManager", messageToUpload, DebugInfoModuleId.Exceptions);

        NetworkRepository.getInstance().handleErrorDuringCycle(messageToUpload);
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.softwareUpgradeFailed, -1, -1);

        NetworkRepository.getInstance().sendHandleOffenderRequest();
    }

    private void handleSoftwareUpgrade(String downloadURL, String apkTargetFileName, String versionFromServer, String ptLatestDownloadedVersion) {
        String receivedUpdateFileName = apkTargetFileName.substring(0, apkTargetFileName.indexOf("_"));
        if (!receivedUpdateFileName.equals(EXPECTED_PURE_TRACK_APK_FILE_NAME)) {
            if (!isApkAlreadyDownloaded(apkTargetFileName)) {
                viewUpdateListener.downloadApkFromServer(downloadURL, apkTargetFileName, versionFromServer, Download_Task_Type.All_Apk_Upgrade);
            } else {
                String messageToUpload = "SW_UPGRADE: The apk was already downloaded from server and installed [ " + apkTargetFileName + " ] . You can manually reinstall it from the Downloads folder. ";
                App.writeToNetworkLogsAndDebugInfo("OffenderRequestsManager", messageToUpload, DebugInfoModuleId.Network);
                NetworkRepository.getInstance().handleOffenderRequestSuccess();
            }
            return;
        }
        //Checks if last version was already downloaded from the server
        if (shouldDownloadNewPtVersionFromServer(apkTargetFileName, ptLatestDownloadedVersion, versionFromServer)) {
            //Download the latest PT version from the server.
            viewUpdateListener.downloadApkFromServer(downloadURL, apkTargetFileName, versionFromServer, Download_Task_Type.PT_Version_Upgrade);
        } else {
            String installedVersionNumber = App.getInstalledVersionNumber();
            boolean shouldInstallNewVersionFromServer = (NumberComputationUtil.compareBetweenVersions(installedVersionNumber, versionFromServer) < 0);
            if (shouldInstallNewVersionFromServer) {
                //Install the latest downloaded PT version from the folder on device.
                viewUpdateListener.installApk(apkTargetFileName);
            } else {
                //No need to update the PT, we are currently running on the latest version.
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceSoftwareUpgradeSuccessful, -1, -1);
                NetworkRepository.getInstance().handleOffenderRequestSuccess();
            }
        }
    }

    private boolean isApkAlreadyDownloaded(String apkTargetFileName) {
        File apkTargetFile = new File(FilesManager.getInstance().APK_LOCATION + apkTargetFileName);
        return apkTargetFile.exists();
    }

    private boolean shouldDownloadNewPtVersionFromServer(String apkTargetFileName, String ptLatestDownloadedVersion, String verFromServer) {
        boolean isPtLatestVerLowerThanFromServer = NumberComputationUtil.compareBetweenVersions(ptLatestDownloadedVersion, verFromServer) < 0;
        //Will also check if the file exists on device, for cases when user some how deletes the file.
        return !isApkAlreadyDownloaded(apkTargetFileName) || isPtLatestVerLowerThanFromServer;
    }
}
