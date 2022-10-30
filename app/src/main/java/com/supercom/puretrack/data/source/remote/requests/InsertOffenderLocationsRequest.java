package com.supercom.puretrack.data.source.remote.requests;

import android.util.Log;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertOffenderLocationsListener;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

import java.util.List;

public class InsertOffenderLocationsRequest extends BaseAsyncTaskRequest {
    private final InsertOffenderLocationsListener mHandlerGps;
    int intEventCnt;
    int intEventId;

    private final List<EntityGpsPoint> mRecordGpsPointsArray;
    int mRecordGpsArrayCnt;
    private final boolean isAnOldPoint;
    public int timeZoneID;

    public InsertOffenderLocationsRequest(InsertOffenderLocationsListener handlerGps, List<EntityGpsPoint> recordGpsPointsArray, int recordGpsArrayCnt,
                                          boolean isAnOldPont, int TzId) {
        mHandlerGps = handlerGps;
        mRecordGpsPointsArray = recordGpsPointsArray;
        mRecordGpsArrayCnt = recordGpsArrayCnt;
        this.isAnOldPoint = isAnOldPont;
        this.timeZoneID = TzId;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "InsertOffenderLocations";
    }

    @Override
    protected String getBody() {
        String Token = NetworkRepository.getInstance().getTokenKey();

        String gpsString = "<root type=\"object\">" +
                "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                "<token type=\"string\">" + Token + "</token>" +
                "<Locations type=\"array\">";

        for (int i = 0; i < mRecordGpsArrayCnt; i++) {


            EntityGpsPoint recordGpsPoint = mRecordGpsPointsArray.get(i);

           String data= "<item type=\"object\">" +
                   "<LocId type=\"string\">" + recordGpsPoint.id + "</LocId>" +
                   "<OId type=\"string\">" + DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId + "</OId>" +
                   //"<longitude type=\"string\">" + (recordGpsPoint.longitude+ LocationManager.Mock_location_lon) + "</longitude>" +
                   //"<latitude type=\"string\">" + (recordGpsPoint.latitude+ LocationManager.Mock_location_lat)  + "</latitude>" +
                   "<longitude type=\"string\">" + (recordGpsPoint.longitude) + "</longitude>" +
                   "<latitude type=\"string\">" + (recordGpsPoint.latitude)  + "</latitude>" +
                   "<altitude type=\"string\">" + recordGpsPoint.altitude + "</altitude>" +
                   "<accuracy type=\"string\">" + recordGpsPoint.accuracy + "</accuracy>" +
                   "<bearing type=\"string\">" + recordGpsPoint.bearing + "</bearing>" +
                   "<sate_num type=\"string\">" + recordGpsPoint.satellitesNumber + "</sate_num>" +
                   "<num_of_sec type=\"string\">" + (recordGpsPoint.time / 1000) + "</num_of_sec>" +
                   "<mock_location type=\"string\">" + recordGpsPoint.isMockLocation + "</mock_location>" +
                   "<motion_type type=\"string\">" + recordGpsPoint.motionType + "</motion_type>" +
                   "<Loc_Type type=\"string\">" + recordGpsPoint.providerType + "</Loc_Type>" +
                   "<Avg_Type type=\"string\">1</Avg_Type>" +
                   "<Direction type=\"string\">1</Direction>" +
                   "<Speed type=\"string\">" + recordGpsPoint.speed + "</Speed>" +
                   "<accelerometerXYZ type=\"string\">" + recordGpsPoint.xyzString + "</accelerometerXYZ>" +
                   "<in_charging type=\"int\">" + recordGpsPoint.inCharging + "</in_charging>" +
                   "<tilt type=\"int\">" + recordGpsPoint.tilt + "</tilt>" +
                   "<TimeZone type=\"string\">" + this.timeZoneID + "</TimeZone>" +
                   "</item>";
            gpsString = gpsString +data;

            if(recordGpsPoint.id==0){
                Log.i("bugID0",data);
            }
        }

        gpsString = gpsString +
                "</Locations>" +
                "</root>";

        return gpsString;
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerGps.handleResponse(result, intEventCnt, intEventId);
    }

    @Override
    protected String getAdditionalInfo() {
        String additionalInfo = "";
        if (NetworkRepositoryConstants.IS_NETWORK_LOG_ON) {
            additionalInfo = " *** Is an old point: " + isAnOldPoint + " ***\n   ";
            return additionalInfo;
        }
        return additionalInfo;
    }

}
