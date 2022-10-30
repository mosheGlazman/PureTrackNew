package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;
import com.supercom.puretrack.model.database.entities.EntityOffenderPhoto;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.PostOnDemandPhotoListener;
import com.supercom.puretrack.data.source.remote.requests.PostOnDemandPhotoRequest;

import java.util.List;

public class PostOnDemandPhotoResultParser implements PostOnDemandPhotoListener {

    public NetworkRequestName nextRequestToSend;

    public PostOnDemandPhotoResultParser(NetworkRequestName nextRequestToSend) {
        this.nextRequestToSend = nextRequestToSend;
    }

    @Override
    public void handleResponse(String response,
                               List<EntityOffenderPhoto> offenderPhotosLeftToSend,
                               int responseCode, String requestIdOfPhotoSent) {

        if (offenderPhotosLeftToSend == null || offenderPhotosLeftToSend.isEmpty() || responseCode != 200) {
            if (responseCode != 404 && responseCode != 200){ // We treat 404 code at base async task class
                NetworkRepository.getInstance().handleErrorDuringCycle(String.valueOf(responseCode));
                return;
            }
            DatabaseAccess.getInstance().tableOffenderPhoto.deleteOffenderPhoto(requestIdOfPhotoSent);
            NetworkRepository.getInstance().sendNewGpsPoints();
            return;
        }

        DatabaseAccess.getInstance().tableOffenderPhoto.deleteOffenderPhoto(requestIdOfPhotoSent);
        new PostOnDemandPhotoRequest(new PostOnDemandPhotoResultParser(nextRequestToSend), offenderPhotosLeftToSend).execute();
    }
}
