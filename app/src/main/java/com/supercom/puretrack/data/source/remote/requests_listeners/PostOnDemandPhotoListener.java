package com.supercom.puretrack.data.source.remote.requests_listeners;

import com.supercom.puretrack.model.database.entities.EntityOffenderPhoto;

import java.util.List;

public interface PostOnDemandPhotoListener {
    void handleResponse(String response,
                        List<EntityOffenderPhoto> offenderPhotosLeftToSend,
                        int responseCode,
                        String requestIdOfPhotoSent);
}
