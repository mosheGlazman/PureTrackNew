package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.model.business_logic_models.network.network_repository.FlowType;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityDebugInfo;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertDeviceDebugInfoListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class InsertDeviceDebugInfoResultParser implements InsertDeviceDebugInfoListener {

    public static final int MIN_RECORDS_TO_REPEAT_DEBUG_INFO_REQUEST = 10;
    public static final int MAX_BATCHES_TO_SEND_PER_CYCLE_OF_DEBUG_INFO_REQUEST = 10;

    private static final String DEBUG_INSERTED_SUCCESSFULLY = "Debug inserted suceessfully !";
    public static final String TAG = "InsertDeviceDebugInfoReqHandler";

    private int currentSequentSendingDebugInfoCounter;
    private int numbersOfRowsToQuery;
    private final FlowType flowType;
    private final ViewUpdateListener updateActivityListener;

    public InsertDeviceDebugInfoResultParser(int currentSequentSendingDebugInfoCounter, int numbersOfRowsToQuery, FlowType flowType,
                                             ViewUpdateListener updateActivityListener) {
        this.currentSequentSendingDebugInfoCounter = currentSequentSendingDebugInfoCounter;
        this.numbersOfRowsToQuery = numbersOfRowsToQuery;
        this.flowType = flowType;
        this.updateActivityListener = updateActivityListener;
    }

    @Override
    public void handleResponse(String response) {
        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");
            NetworkRepository.getInstance().sendInsertDeviceInfo();
            return;
        }

        int idItem;
        String errorItem;

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            JSONObject JsonInsertDeviceDebugInfo = new JSONObject(afterDecode);
            JSONObject JsonInsertDeviceDebugInfoResult = JsonInsertDeviceDebugInfo.getJSONObject("InsertDeviceDebugInfoResult");
            int status = JsonInsertDeviceDebugInfoResult.getInt("status");

            //succeeded
            if (status == 0) {
                JSONArray JsonInsertDeviceDebugInfoData = JsonInsertDeviceDebugInfoResult.getJSONArray("data");
                for (int i = 0; i < JsonInsertDeviceDebugInfoData.length(); i++) {
                    idItem = JsonInsertDeviceDebugInfoData.getJSONObject(i).getInt("ItemId");
                    errorItem = JsonInsertDeviceDebugInfoData.getJSONObject(i).getString("Error");

                    if (JsonInsertDeviceDebugInfoData.getJSONObject(i).has("status")) {
                        int innerStatus = JsonInsertDeviceDebugInfoData.getJSONObject(i).getInt("status");
                        if (innerStatus == 0) {
                            DatabaseAccess.getInstance().tableDebugInfo.deleteRowById(idItem);
                        } else {
                            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response error from item id " + idItem);
                        }
                    } else if (errorItem.equals(DEBUG_INSERTED_SUCCESSFULLY)) {
                        DatabaseAccess.getInstance().tableDebugInfo.deleteRowById(idItem);
                    }
                }

                List<EntityDebugInfo> recordDebugInfoArray = DatabaseAccess.getInstance().tableDebugInfo.getDeviceInfoRecordsForUpload();

                if (flowType == FlowType.Unallocated) {
                    updateActivityListener.onUnallocateRecordUploaded(flowType);
                } else {
                    //check if should debug info request againcatch (JSONException e)
                    if (recordDebugInfoArray.size() > MIN_RECORDS_TO_REPEAT_DEBUG_INFO_REQUEST && currentSequentSendingDebugInfoCounter <
                            MAX_BATCHES_TO_SEND_PER_CYCLE_OF_DEBUG_INFO_REQUEST) {
                        currentSequentSendingDebugInfoCounter++;
                        NetworkRepository.getInstance().sendInsertDeviceDebugInfo(currentSequentSendingDebugInfoCounter,
                                TableDebugInfo.MAX_LOG_PER_REQ, FlowType.RegularFlow);
                    } else {
                        currentSequentSendingDebugInfoCounter = 0;
                        NetworkRepository.getInstance().sendInsertDeviceInfo();
                    }
                }
            }

            //failed, then we are looking for the incorrect row
            else {

                if (flowType == FlowType.Unallocated) {
                    updateActivityListener.onUnallocateRecordUploaded(flowType);
                } else {
                    String error = JsonInsertDeviceDebugInfoResult.getString("error");
                    String messageToUpload = "";
                    if (numbersOfRowsToQuery <= 1) {
                        List<EntityDebugInfo> deviceInfoRecordsForUpload = DatabaseAccess.getInstance().tableDebugInfo.getDeviceInfoRecordsForUpload(numbersOfRowsToQuery);
                        if (!deviceInfoRecordsForUpload.isEmpty()) {
                            int numberOfAffectedRows = DatabaseAccess.getInstance().tableDebugInfo.deleteMoreRowsWithSameInfoMessage(deviceInfoRecordsForUpload.get(0).message);
                            messageToUpload = "Error in " + TAG + ". Deleted " + numberOfAffectedRows + " rows";
                            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Exceptions);

                            NetworkRepository.getInstance().handleErrorDuringCycle(messageToUpload);
                        }

                        NetworkRepository.getInstance().sendInsertDeviceInfo();
                    } else {
                        messageToUpload = "Error in " + TAG + ".\nNumber of potential incorrect rows: " + numbersOfRowsToQuery;
                        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Exceptions);

                        numbersOfRowsToQuery = numbersOfRowsToQuery / 2;

                        NetworkRepository.getInstance().sendInsertDeviceDebugInfo(currentSequentSendingDebugInfoCounter,
                                numbersOfRowsToQuery, FlowType.RegularFlow);

                        NetworkRepository.getInstance().handleErrorDuringCycle(messageToUpload);
                    }
                }
            }
        } catch (JSONException e) {
            String messageToUpload = "Error in " + TAG;
            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Exceptions);

            NetworkRepository.getInstance().sendInsertDeviceInfo();

            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);
        }
    }

}
