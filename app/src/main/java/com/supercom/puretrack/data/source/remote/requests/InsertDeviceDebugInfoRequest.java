package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.model.database.entities.EntityDebugInfo;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceDebugInfoResultParser;

import java.util.List;

public class InsertDeviceDebugInfoRequest extends BaseAsyncTaskRequest {

    private final InsertDeviceDebugInfoResultParser insertDeviceDebugInfoResultParser;
    private final List<EntityDebugInfo> recordDebugInfoArray;

    public InsertDeviceDebugInfoRequest(InsertDeviceDebugInfoResultParser insertDeviceDebugInfoResultParser, List<EntityDebugInfo> recordDebugInfoArray) {
        this.insertDeviceDebugInfoResultParser = insertDeviceDebugInfoResultParser;
        this.recordDebugInfoArray = recordDebugInfoArray;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "InsertDeviceDebugInfo";
    }

    @Override
    protected String getBody() {
        String token = NetworkRepository.getInstance().getTokenKey();

        String debugInfoString =
                "<root type=\"object\">" +
                        "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                        "<token type=\"string\">" + token + "</token>" +
                        "<debug_info type=\"array\">";

        for (EntityDebugInfo recordDebugInfo : recordDebugInfoArray) {
            debugInfoString +=
                    "<item type=\"object\">" +
                            "<datetime type=\"string\">" + recordDebugInfo.dateTime + "</datetime>" +
                            "<debug_lvl_id type=\"string\">" + recordDebugInfo.debugLV1Id + "</debug_lvl_id>" +
                            "<message type=\"string\">" + recordDebugInfo.message.replaceAll("<|>|&", " ") + "</message>" +
                            "<message_id type=\"string\">" + recordDebugInfo.messageId + "</message_id>" +
                            "<module_id type=\"string\">" + recordDebugInfo.mouduleId + "</module_id>" +
                            "<object_id type=\"string\">" + recordDebugInfo.objectId + "</object_id>" +
                            "<request_id type=\"string\"></request_id>" +
                            "<sub_module_id type=\"string\">" + recordDebugInfo.subModuleId + "</sub_module_id>" +
                            "</item>";
        }

        debugInfoString +=
                "</debug_info>" +
                        "</root>";

        return debugInfoString;
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        insertDeviceDebugInfoResultParser.handleResponse(result);
    }

}
