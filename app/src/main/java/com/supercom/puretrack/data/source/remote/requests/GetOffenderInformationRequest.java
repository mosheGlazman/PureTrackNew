package com.supercom.puretrack.data.source.remote.requests;


import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderInformationListener;

public class GetOffenderInformationRequest extends BaseAsyncTaskRequest {

    GetOffenderInformationListener mHttpResponseHandlerGetOffenderInformation;
    int mTagId;

    public GetOffenderInformationRequest(GetOffenderInformationListener handlerOffenderInformationRequest) {
        String tagRfId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        mHttpResponseHandlerGetOffenderInformation = handlerOffenderInformationRequest;
        mTagId = Integer.parseInt(tagRfId);
    }


    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "GetOffenderInformation";
    }

    @Override
    protected String getBody() {
		return "<root type=\"object\"> " +
				"<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
				"<token type=\"string\">" + NetworkRepository.getInstance().getTokenKey() + "</token>" +
				"<tags type=\"array\">" +
				"<item type=\"object\">" +
				"<tagId type=\"string\">" + mTagId + "</tagId>" +
				"</item>" +
				"</tags>" +
				"</root>";
    }

	@Override
	protected void startHttpResponseHandle(String result, int responseCode) {
		mHttpResponseHandlerGetOffenderInformation.handleResponse(result);
	}
}