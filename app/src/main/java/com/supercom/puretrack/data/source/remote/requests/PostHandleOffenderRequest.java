package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.PostHandleOffenderRequestListener;

public class PostHandleOffenderRequest extends BaseAsyncTaskRequest {
    private final PostHandleOffenderRequestListener mHandlerPostMessage;
    int lastOffenderReuqestIdTreated;
    int mRequsetStatus;


    public PostHandleOffenderRequest(PostHandleOffenderRequestListener handlerOpen, int lastOffenderReuqestIdTreated, int requestStatus) {
        mHandlerPostMessage = handlerOpen;
        mRequsetStatus = requestStatus;
        this.lastOffenderReuqestIdTreated = lastOffenderReuqestIdTreated;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "HandleOffenderRequest";
    }

    @Override
    protected String getBody() {
        String Token = NetworkRepository.getInstance().getTokenKey();

        return "<root type=\"object\">" +
                "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +    //old=10
                "<token type=\"string\">" + Token + "</token>" +
                "<Requests_Array type=\"array\">" +
                "<item type=\"object\">" +
                "<Request_Id type=\"string\">" + lastOffenderReuqestIdTreated + "</Request_Id>" +    //requestID
                "<Status type=\"string\">" + mRequsetStatus + "</Status>" +
                "<Error type=\"string\"></Error>" +
                "</item>" +
                "</Requests_Array>" +
                "</root>";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerPostMessage.handleResponse(result, mRequsetStatus);
    }

}
