package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.model.database.enums.EnumCallLogType;
import com.supercom.puretrack.model.database.entities.EntityCallLog;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceCommunictionsResultParser;

import java.util.Calendar;
import java.util.List;

public class InsertDeviceCommunicationsRequest extends BaseAsyncTaskRequest {

    private final InsertDeviceCommunictionsResultParser insertDeviceCommunictionsHandler;
    private final List<EntityCallLog> recordCallLogArray;

    public InsertDeviceCommunicationsRequest(InsertDeviceCommunictionsResultParser insertDeviceCommunictionsHandler, List<EntityCallLog> recordCallLog) {
        this.insertDeviceCommunictionsHandler = insertDeviceCommunictionsHandler;
        this.recordCallLogArray = recordCallLog;

    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "InsertDeviceCommunications";
    }

    @Override
    protected String getBody() {
        String token = NetworkRepository.getInstance().getTokenKey();

        String callLogString =
                "<root type=\"object\">" +
                        "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +        //Device ID old=6, old=10, new=11 -> 20	old=11
                        "<token type=\"string\">" + token + "</token>" +
                        "<comm_info type=\"array\">";
        for (EntityCallLog recordCallLogItem : recordCallLogArray) {
            if (!recordCallLogItem.callNumber.startsWith("-")) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(recordCallLogItem.callTimeSystem));

                callLogString +=
                        "<item type=\"object\">" +
                                "<comm_id type=\"string\">" + recordCallLogItem.callId + "</comm_id>" +                                        // 1-OK, 2-VIO, 3-ALARM
                                "<phone_num type=\"string\">" + recordCallLogItem.callNumber.replace("+", "") + "</phone_num>" +
                                "<request_id type=\"string\">" + "" + "</request_id>" +
                                "<incoming type=\"string\">" + recordCallLogItem.callTypeFiltered + "</incoming>" +
                                "<comm_Type type=\"string\">" + EnumCallLogType.CALL.getValue() + "</comm_Type>" +
                                "<datetime type=\"string\">" + recordCallLogItem.callTimeFiltered + "</datetime>" +
                                "<text type=\"string\">" + "" + "</text>" +
                                "<duration type=\"string\">" + recordCallLogItem.callLength + "</duration>" +
                                "<conducted type=\"string\">" + recordCallLogItem.conducted + "</conducted>" +
                                "</item>";
            }
        }

        callLogString +=
                "</comm_info>" +
                        "</root>";

        return callLogString;
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        insertDeviceCommunictionsHandler.handleResponse(result, 0, 0);
    }


}
