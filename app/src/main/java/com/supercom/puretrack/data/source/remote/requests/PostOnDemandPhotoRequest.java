package com.supercom.puretrack.data.source.remote.requests;

import com.google.gson.Gson;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityOffenderPhoto;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.parsers.PostOnDemandPhotoResultParser;
import com.supercom.puretrack.model.server_models.OnDemandPhotoRequestBody;

import java.util.ArrayList;
import java.util.List;

public class PostOnDemandPhotoRequest extends BaseAsyncTaskRequest {

    private final PostOnDemandPhotoResultParser requestHandler;
    private final List<EntityOffenderPhoto> offenderPhotos;
    private final List<EntityOffenderPhoto> offenderPhotosLeftToSend;

    private String requestIdOfPhotoSent;

    public PostOnDemandPhotoRequest(PostOnDemandPhotoResultParser requestHandler, List<EntityOffenderPhoto> offenderPhotos) {
        this.requestHandler = requestHandler;
        this.offenderPhotos = offenderPhotos;
        offenderPhotosLeftToSend = new ArrayList<>();
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "api/Devices/UploadPhotoOnDemand";
    }

    @Override
    protected String getBody() {
        httpURLConnection.addRequestProperty("DeviceId", NetworkRepository.getDeviceSerialNumber());
        httpURLConnection.addRequestProperty("token", NetworkRepository.getInstance().getTokenKey());
        Gson gson = new Gson();
        EntityOffenderPhoto offenderPhoto = offenderPhotos.get(0);
        offenderPhotosLeftToSend.addAll(offenderPhotos);
        offenderPhotosLeftToSend.remove(offenderPhoto);
        int offenderId = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId;
        String serialNumber = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber();
        String requestId = offenderPhoto.requestId;
        requestIdOfPhotoSent = requestId;
        OnDemandPhotoRequestBody onDemandPhotoRequestBody = new OnDemandPhotoRequestBody(offenderId,
                Integer.parseInt(requestId), offenderPhoto.photoEncodedToBase64, offenderPhoto.eventId, serialNumber);
        return gson.toJson(onDemandPhotoRequestBody);
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        requestHandler.handleResponse(result, offenderPhotosLeftToSend, responseCode, requestIdOfPhotoSent);
    }
}
