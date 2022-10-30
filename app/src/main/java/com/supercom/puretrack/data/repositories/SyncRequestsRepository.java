package com.supercom.puretrack.data.repositories;

import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED;
import static com.supercom.puretrack.util.constants.network.SyncRequestsRepositoryConstants.OFFENDER_REQUEST_SYNC_TYPE_TREATED;

import android.util.Log;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.data.source.remote.parsers.GetDeviceConfigurationResultParser.DeviceConfigurationType;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.OffenderRequestType;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.SingleSyncRequest;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.SyncType;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.ZoneResult;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.ZonesRequestResult;
import com.supercom.puretrack.model.database.entities.EntityZones;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SyncRequestsRepository {


    //Class Variables - Singleton Access
    private static final SyncRequestsRepository INSTANCE = new SyncRequestsRepository();

    private SyncRequestsRepository() {
    }

    public static synchronized SyncRequestsRepository getInstance() {
        return INSTANCE;
    }


    //Class Variables - Objects
    private JSONArray currentZonesArrayData;
    private int currentZoneSyncIndex;
    private ArrayList<SingleSyncRequest> curSyncReqArr = new ArrayList<>();
    private ZonesRequestResult zonesRequestResult = new ZonesRequestResult();
    private ViewUpdateListener viewUpdateListener;
    private int zoneVersionFromServer = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ZONE_VERSION);


    public void initSyncReqManagerForNewCycle() {
        zonesRequestResult = new ZonesRequestResult();
        OFFENDER_REQUEST_SYNC_TYPE_TREATED = 0;
    }

    public void updateScheduleResult_AndContinue(int zoneId, long scheduleSavedToDBResult) {
        ZoneResult zoneResultByZoneId = getInstance().zonesRequestResult.getZoneResultByZoneId(zoneId);
        if (zoneResultByZoneId != null) {
            if (scheduleSavedToDBResult == ZoneResult.DB_RESULT_ERR) {
                NetworkRepository.getInstance().handleErrorDuringCycle("Error in scheudles, ZoneId: " + zoneId);
                zoneResultByZoneId.scheduleResult = NetworkRepositoryConstants.REQUEST_RESULT_ERR;
            } else {
                zoneResultByZoneId.scheduleResult = NetworkRepositoryConstants.REQUEST_RESULT_OK;
            }
        }
        getInstance().treatNextZone();
    }

    public void updateSingleSyncReqResultAndContinue(int syncReqType, int syncRequestResult) {
        for (int i = 0; i < curSyncReqArr.size(); i++) {
            SingleSyncRequest singleSyncRequest = curSyncReqArr.get(i);
            if (singleSyncRequest.requestDataType == syncReqType) {
                singleSyncRequest.requestResult = syncRequestResult;
                break;
            }
        }

        treatNextSyncRequest();
    }

    public void treatSyncRequests(JSONArray JsonGetOffenderRequestsRequestData) throws JSONException {
        getInstance().curSyncReqArr = new ArrayList<>();
        for (int i = 0; i < JsonGetOffenderRequestsRequestData.length(); i++) {
            SingleSyncRequest singleSyncRequest = new SingleSyncRequest();
            singleSyncRequest.requestDataType = JsonGetOffenderRequestsRequestData.getJSONObject(i).getInt("Type");
            singleSyncRequest.requestDataVersion = JsonGetOffenderRequestsRequestData.getJSONObject(i).getString("Number");

            getInstance().curSyncReqArr.add(singleSyncRequest);
        }

        NetworkRepository.getInstance().handleOffenderRequestInProgress();
    }

    public void treatActivateRequest(ArrayList<SingleSyncRequest> activateRequestDataArray) {
        getInstance().curSyncReqArr = activateRequestDataArray;
        NetworkRepository.getInstance().handleOffenderRequestInProgress();
    }

    private void treatNextSyncRequest() {
        SingleSyncRequest singleSyncRequest = getSingleReqToTreat();
        //There are still sync requests left to treat
        if (singleSyncRequest != null) {
            treatSingleSyncRequest(singleSyncRequest);
            return;
        }
        //Finished treating all sync requests
        if (!isSyncSuccessful()) {
            NetworkRepository.getInstance().handleOffenderRequestError();
            return;
        }

        updateRequestStatusForSyncZones();

        if (OFFENDER_REQUEST_SYNC_TYPE_TREATED == SyncType.ZONES) {

            int offenderActivateStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS);

            if (offenderActivateStatus != OffenderActivation.OFFENDER_STATUS_UNALLOCATED) {

                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_ZONE_VERSION, zoneVersionFromServer);

                if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_ALLOCATED /*&& zoneVersionFromLocalDB < zoneVersionFromServer */) {
                    viewUpdateListener.onZonesRequestFinishedToParse();
                }
            }
        }

        //on activate we want to send handle OK, only after we made a few operations
        if (OFFENDER_REQUEST_TYPE_TREATED == OffenderRequestType.ACTIVATE) {
            viewUpdateListener.
                    onActivateReceivedFromServer();
        } else {
            NetworkRepository.getInstance().handleOffenderRequestSuccess();
        }


    }

    /**
     * @return If one of the single sync requests failed return False, otherwise - True.
     */
    private boolean isSyncSuccessful() {
        for (int i = 0; i < curSyncReqArr.size(); i++) {
            if (curSyncReqArr.get(i).requestResult == NetworkRepositoryConstants.REQUEST_RESULT_ERR) {
                return false;
            }
        }

        return true;
    }

    public SingleSyncRequest getSingleReqToTreat() {
        for (int i = 0; i < curSyncReqArr.size(); i++) {
            if (curSyncReqArr.get(i).requestResult == NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS) {
                return curSyncReqArr.get(i);
            }
        }
        return null;
    }


    public void treatSingleSyncRequest(SingleSyncRequest singleSyncRequest) {
        OFFENDER_REQUEST_SYNC_TYPE_TREATED = singleSyncRequest.requestDataType;

        switch (OFFENDER_REQUEST_SYNC_TYPE_TREATED) {
            case SyncType.OFFENDER_SCHEDULE:
                getInstance().updateSingleSyncReqResultAndContinue(SyncType.OFFENDER_SCHEDULE, NetworkRepositoryConstants.REQUEST_RESULT_OK);
                break;

            case SyncType.DEVICE_CONFIG:
                int offenderDeviceConfigVer = Integer.parseInt(singleSyncRequest.requestDataVersion);
                if (offenderDeviceConfigVer > DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderDeviceConfigVersionNumber) {
                    if (singleSyncRequest.requestResult == NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS) {
                        NetworkRepository.getInstance().httpGetDeviceConfiguration(DeviceConfigurationType.Device_Configuration, offenderDeviceConfigVer);
                    }
                } else {
                    getInstance().updateSingleSyncReqResultAndContinue(SyncType.DEVICE_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_OK);
                }
                break;

            case SyncType.OFFENDER_CONFIG:
                int offenderConfigVer = Integer.parseInt(singleSyncRequest.requestDataVersion);
                if (offenderConfigVer > DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderConfigVersionNumber) {
                    if (singleSyncRequest.requestResult == NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS) {
                        NetworkRepository.getInstance().httpGetDeviceConfiguration(DeviceConfigurationType.Offender_Configuration, offenderConfigVer);
                    }
                } else {
                    getInstance().updateSingleSyncReqResultAndContinue(SyncType.OFFENDER_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_OK);
                }
                break;

            case SyncType.ZONES:
                int getCurrentZoneVersion = DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion;
                if (singleSyncRequest.requestResult == NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS &&
                        Integer.parseInt(singleSyncRequest.requestDataVersion) > getCurrentZoneVersion) {

                    NetworkRepository.getInstance().httpGetOffenderZones(singleSyncRequest.requestDataVersion);
                } else {
                    getInstance().updateSingleSyncReqResultAndContinue(SyncType.ZONES, NetworkRepositoryConstants.REQUEST_RESULT_OK);
                }
                break;

            default:
                break;
        }
    }

    private void updateRequestStatusForSyncZones() {
        if (NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED == OffenderRequestType.SYNC) {
            if (OFFENDER_REQUEST_SYNC_TYPE_TREATED == SyncType.ZONES) {
                ArrayList<ZoneResult> zoneResultsList = getInstance().zonesRequestResult.getZoneResultsList();
                for (int i = 0; i < zoneResultsList.size(); i++) {
                    if (zoneResultsList.get(i).zoneResult == NetworkRepositoryConstants.REQUEST_RESULT_ERR || zoneResultsList.get(i).scheduleResult == NetworkRepositoryConstants.REQUEST_RESULT_ERR) {
                        NetworkRepository.getInstance().setOffenderReqResultError();

                        Log.e("SyncRequestsManager", "updateRequestStatusForSync() : REQUEST_RESULT_ERR ->"
                                + "\n  zoneId" + zoneResultsList.get(i).zoneId
                                + "\n  zoneResult" + zoneResultsList.get(i).zoneResult
                                + "\n  scheduleResult" + zoneResultsList.get(i).scheduleResult);
                        LoggingUtil.fileLogZonesUpdate("\n\n" + "SyncRequestsManager" + "updateRequestStatusForSync() : REQUEST_RESULT_ERR ->"
                                + "\n  zoneId" + zoneResultsList.get(i).zoneId
                                + "\n  zoneResult" + zoneResultsList.get(i).zoneResult
                                + "\n  scheduleResult" + zoneResultsList.get(i).scheduleResult);
                        String messageToUpload = "updateRequestStatusForSync() : REQUEST_RESULT_ERR ->"
                                + "\n  zoneId" + zoneResultsList.get(i).zoneId
                                + "\n  zoneResult" + zoneResultsList.get(i).zoneResult
                                + "\n  scheduleResult" + zoneResultsList.get(i).scheduleResult;
                        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                                DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                        return;

                    }
                }
                NetworkRepository.getInstance().setOffenderRequestResultSuccess();

            }
        }
    }

    public void updateZonesSingleSyncReqResult_AndContinue() {
        ArrayList<ZoneResult> zoneResultsList = getInstance().zonesRequestResult.getZoneResultsList();
        if (zoneResultsList.size() > 0) {
            //If one of ZoneResult had error during Zone request OR Zone Schedule request then set SyncType.ZONES SingleSyncReqResult to Err, and continue.
            for (int i = 0; i < zoneResultsList.size(); i++) {
                ZoneResult zoneResult = zoneResultsList.get(i);
                if (zoneResult.zoneResult != NetworkRepositoryConstants.REQUEST_RESULT_OK || zoneResult.scheduleResult != NetworkRepositoryConstants.REQUEST_RESULT_OK) {
                    getInstance().updateSingleSyncReqResultAndContinue(SyncType.ZONES, NetworkRepositoryConstants.REQUEST_RESULT_ERR);
                    return;
                }
            }
        }
        treatFinishedZonesRequestSuccessAndContinueNetCycle();
    }

    private void treatFinishedZonesRequestSuccessAndContinueNetCycle() {


        boolean isInScheduleCycle = NetworkRepository.getInstance().isInScheduleCycle();
        if (isInScheduleCycle) {
            NetworkRepository.getInstance().setIsInScheduleCycle(false);
            TableOffenderStatusManager.sharedInstance().updateColumnLong(OFFENDER_STATUS_CONS.OFF_LAST_SCHEUDLE_UPDATE, System.currentTimeMillis());
            NetworkRepository.getInstance().continueCycleAfterAuth();
        } else {
            getInstance().updateSingleSyncReqResultAndContinue(SyncType.ZONES, NetworkRepositoryConstants.REQUEST_RESULT_OK);
        }
    }

    public void treatZonesSync(JSONArray jArrayZonesResultData, int zoneVersionFromServer) {
        this.zoneVersionFromServer = zoneVersionFromServer;
        currentZoneSyncIndex = -1;
        currentZonesArrayData = jArrayZonesResultData;

        treatNextZone();

    }

    public void treatNextZone() {
        currentZoneSyncIndex++;

        //finished to check all zones
        if (currentZoneSyncIndex > currentZonesArrayData.length() - 1) {
            List<EntityZones> allDeletedZones = DatabaseAccess.getInstance().tableZones.getAllZonesThatShouldBeDeleted();

            for (EntityZones recordZoneItem : allDeletedZones) {

                //add record to deleted zones table
                DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_ZONES_DELETED, recordZoneItem);

                //remove record from main zones table
                DatabaseAccess.getInstance().tableZones.deleteZoneByZoneId(recordZoneItem.ZoneId);

                //remove all schedules that belong to main zone
                DatabaseAccess.getInstance().tableScheduleOfZones.deleteAllSchedulesOfZone(recordZoneItem.ZoneId);

                if (recordZoneItem.TypeId == TableZonesManager.ZONE_TYPE_BEACON) {
                    viewUpdateListener.onBeaconZoneDeletedFromDB();
                }
            }

            updateZonesSingleSyncReqResult_AndContinue();

            return;
        }

        int OffenderId = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId;
        int ZoneId = 0;
        String ZoneName = "";
        int TypeId;
        int newScheduleVersion;
        int defaultAppointmentTypeId;
        JSONObject jObjectShape;
        int ShapeType;
        float ShapeRadius;
        JSONArray jArrayPoints;
        double Latitude;
        double Longitude;
        int bufferZone = 0;

        try {
            JSONObject zoneJsonObject = currentZonesArrayData.getJSONObject(currentZoneSyncIndex);
            ZoneId = zoneJsonObject.getInt("ZoneId");
            if (zoneJsonObject.has("ZoneName")) {
                ZoneName = zoneJsonObject.getString("ZoneName");
            }
            TypeId = zoneJsonObject.getInt("TypeId");
            newScheduleVersion = zoneJsonObject.getInt("ScheduleVersion");
            defaultAppointmentTypeId = zoneJsonObject.getInt("DefaultAppointmentTypeId");
            jObjectShape = zoneJsonObject.getJSONObject("Shape");
            ShapeType = jObjectShape.getInt("Type");
            boolean isZoneHasBuffer = zoneJsonObject.optBoolean("HasBuffer", false);
            if (isZoneHasBuffer) {
                bufferZone = zoneJsonObject.optInt("BufferSize", 0);
            }

            getInstance().zonesRequestResult.getZoneResultsList().add(new ZoneResult(ZoneId));

            EntityZones recordByZoneId = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(ZoneId);
            int isInto = (recordByZoneId == null ? 0 : recordByZoneId.isIntoExclusionZoneState);
            int isIntoBuffer = (recordByZoneId == null ? 0 : recordByZoneId.isIntoBufferZoneState);

            int oldScheduleVersion = 1;
            if (recordByZoneId != null) {
                oldScheduleVersion = recordByZoneId.scheduleVersionOfZone;
            }

            long db_result = ZoneResult.DB_RESULT_ERR;
            switch (ShapeType) {
                case 1:

                    // Circle
                    ShapeRadius = jObjectShape.getLong("Radius");
                    jArrayPoints = jObjectShape.getJSONArray("Points");

                    Latitude = jArrayPoints.getJSONObject(0).getDouble("Latitude");
                    Longitude = jArrayPoints.getJSONObject(0).getDouble("Longitude");

                    //SAVE TO DB the new Zone
                    db_result = DatabaseAccess.getInstance().insertWithOnConflict(EnumDatabaseTables.TABLE_ZONES, new EntityZones(
                            ZoneId,
                            ZoneName,
                            OffenderId,
                            TypeId,
                            ShapeType,
                            "",
                            Latitude,
                            Longitude,
                            ShapeRadius,
                            0,
                            "No Comment",
                            isInto,
                            0,
                            0,
                            0,
                            0,
                            defaultAppointmentTypeId,
                            DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion,
                            oldScheduleVersion, //we will save to DB new schedule version if GetOffenderScheduleOfZoneRequest will succeed
                            bufferZone,
                            isIntoBuffer)
                    );

                    if (TypeId == TableZonesManager.ZONE_TYPE_BEACON) {
                        long currentZoneId = (int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
                        if (currentZoneId != ZoneId) {
                            TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID, ZoneId);
                            viewUpdateListener.onBeaconZoneAddedToDB();
                        }
                    }

                    break;

                case 2:

                case 3:
                    // Polygon & Rectangular
                    jArrayPoints = jObjectShape.getJSONArray("Points");

                    //SAVE TO DB the new Zone
                    db_result = DatabaseAccess.getInstance().insertWithOnConflict(EnumDatabaseTables.TABLE_ZONES, new EntityZones(
                            ZoneId,
                            ZoneName,
                            OffenderId,
                            TypeId,
                            ShapeType,
                            jArrayPoints.toString(),
                            0,
                            0,
                            0,
                            jArrayPoints.length(),
                            "No Comment",
                            isInto,
                            0,
                            0,
                            0,
                            0,
                            defaultAppointmentTypeId,
                            DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion,
                            oldScheduleVersion, //we will save to DB new schedule version if GetOffenderScheduleOfZoneRequest will succeed
                            bufferZone,
                            isIntoBuffer)
                    );
                    break;

                //SAVE TO DB the new Zone
            }

            updateZoneResult(ZoneId, db_result);
            treatZoneSchedule(ZoneId, newScheduleVersion, oldScheduleVersion);

        } catch (JSONException e) {
            Log.i("SyncRequestsManager", "\n\n" + "Error in zone: " + ZoneId);
            LoggingUtil.updateNetworkLog("\n\n" + "SyncRequestsManager" + "\n" + TimeUtil.getCurrentTimeStr() + " : " + "Error in zone: " + ZoneId, false);
            String messageToUpload = "Error in zone: " + ZoneId;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            NetworkRepository.getInstance().handleErrorDuringCycle("Error in zone: " + ZoneId);

            SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.ZONES, NetworkRepositoryConstants.REQUEST_RESULT_ERR);
            e.printStackTrace();

        }
    }

    private void treatZoneSchedule(int ZoneId, int newScheduleVersion, int oldScheduleVersion) {
        Log.i("SyncRequestsManager", "handleHttpResponseGetOffenderZones() : newScheduleVersion = " + newScheduleVersion);
        boolean isInScheduleCycle = NetworkRepository.getInstance().isInScheduleCycle();
        if (newScheduleVersion > oldScheduleVersion || isInScheduleCycle) {
            NetworkRepository.getInstance().httpGetScheduleOfZone(ZoneId, newScheduleVersion);
        } else {
            getInstance().zonesRequestResult.getZoneResultByZoneId(ZoneId).scheduleResult = NetworkRepositoryConstants.REQUEST_RESULT_OK;
            treatNextZone();
        }
    }

    private void updateZoneResult(int zoneId, long zoneSavedToDBResult) {
        if (zoneSavedToDBResult == ZoneResult.DB_RESULT_ERR) {
            NetworkRepository.getInstance().handleErrorDuringCycle("Error in zone: " + zoneId);
            getInstance().zonesRequestResult.getZoneResultByZoneId(zoneId).zoneResult = NetworkRepositoryConstants.REQUEST_RESULT_ERR;
        } else {
            getInstance().zonesRequestResult.getZoneResultByZoneId(zoneId).zoneResult = NetworkRepositoryConstants.REQUEST_RESULT_OK;
        }
    }

    public void setViewUpdateListener(ViewUpdateListener viewUpdateListener) {
        this.viewUpdateListener = viewUpdateListener;
    }

}
