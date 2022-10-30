package com.supercom.puretrack.data.source.remote.requests;

import static com.supercom.puretrack.model.business_logic_models.network.network_repository.CommunicationInterval.CommIntervalLow;

import android.util.Log;

import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.CommunicationInterval;
import com.supercom.puretrack.model.database.entities.EntityEventLog;
import com.supercom.puretrack.model.database.entities.EntityOffenderDetails;
import com.supercom.puretrack.model.database.entities.EntityOffenderStatus;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertDeviceEventsListener;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.general.LoggingUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class InsertDeviceEventsRequest extends BaseAsyncTaskRequest {
    InsertDeviceEventsListener mHandlerOpen;

    public InsertDeviceEventsRequest(InsertDeviceEventsListener handlerOpen) {
        mHandlerOpen = handlerOpen;
    }

    @Override
    protected String doInBackground(String... uri) {
        Log.w(getTagName(), "-> doInBackground() : SendingEvents");
        return super.doInBackground("");
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "InsertDeviceEvents";
    }

    @Override
    protected String getBody() {
        String Token = NetworkRepository.getInstance().getTokenKey();

        String PureBeaconString = "";
        EntityOffenderDetails recordOffenderDetails = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails();
        EntityOffenderStatus recordOffenderStatus = DatabaseAccess.getInstance().tableOffStatus.Get();
        List<EntityEventLog> recordEventLogArray = DatabaseAccess.getInstance().tableEventLog.getAllNonSentEventLogRecords();

        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
        if (isBeaconExistsInDBZone) {
            PureBeaconString = "<PurBeacon_Status type=\"array\">" +
                    "<item type=\"object\">" +
                    "<Beacon_Serial type=\"string\">" + recordOffenderDetails.beaconId + "</Beacon_Serial>" +
                    "<Last_Comm_Date type=\"string\">" + recordOffenderStatus.OffBeaconLastReceive + "</Last_Comm_Date>" +
                    "<Is_Last_Comm_Legal type=\"string\">" + 1 + "</Is_Last_Comm_Legal>" +
                    "<Batt_Lvl type=\"string\">0</Batt_Lvl>" +
                    "<Case_Tamper type=\"string\">" + recordOffenderStatus.OffBeaconStatCase + "</Case_Tamper>" +
                    "<Proximity_Tamper type=\"string\">" + recordOffenderStatus.OffBeaconStatProx + "</Proximity_Tamper>" +
                    "</item>" +
                    "</PurBeacon_Status>";
        }

        String eventString = "<root type=\"object\">" +
                "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                "<token type=\"string\">" + Token + "</token>" +


                "<Dev_Status type=\"array\">" +
                "<item type=\"object\">" +
                "<Power type=\"string\">" + getPower() + "</Power>" +
                "<cellularDataAvailable type=\"string\"></cellularDataAvailable>" +
                "</item>" +
                "</Dev_Status>" +


                "<Curr_Off_Stat type=\"array\">" +
                "<item type=\"object\">" +
                "<Id type=\"string\">" + DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId + "</Id>" +
                "<Off_Stat type=\"string\">" + recordOffenderStatus.OffVioStat + "</Off_Stat>" +
                "<Tag_Batt type=\"string\">" + DeviceStateManager.getInstance().getNewBatteryLevel() + "</Tag_Batt>" +
                "<Is_In type=\"string\">" + TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE) + "</Is_In>" + // in house or not in house
                "<Is_In_Sched type=\"string\"></Is_In_Sched>" +
                "<Sync_Version type=\"array\">" +
                "<item type=\"object\">" +
                "<Sync_Type type=\"string\">4</Sync_Type>" +
                "<Version_Number type=\"string\">" + DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion + "</Version_Number>" +
                "</item>" +
                "</Sync_Version>" +

                "<Tag_Status type=\"array\">" +
                "<item type=\"object\">" +
                "<Case type=\"string\">" + recordOffenderStatus.OffTagStatCase + "</Case>" +
                "<Strap type=\"string\">" + recordOffenderStatus.OffTagStatStrap + "</Strap>" +
                "<Battery type=\"string\">" + recordOffenderStatus.OffTagStatBattery + "</Battery>" +
                "</item>" +
                "</Tag_Status>" + PureBeaconString +

                "</item>" +
                "</Curr_Off_Stat>" +
                "<Events type=\"array\">";

        for (int i = 0; i < recordEventLogArray.size(); i++) {
            String PureBeaconStringArray = "";

            if (isBeaconExistsInDBZone) {
                PureBeaconStringArray = "<PurBeacon_Status type=\"array\">" +
                        "<item type=\"object\">" +
                        "<Beacon_Serial type=\"string\">" + recordOffenderDetails.beaconId + "</Beacon_Serial>" +
                        "<Last_Comm_Date type=\"string\">" + recordEventLogArray.get(i).BeaconLastCommunication + "</Last_Comm_Date>" +
                        "<Is_Last_Comm_Legal type=\"string\">1</Is_Last_Comm_Legal>" +
                        "<Batt_Lvl type=\"string\">0</Batt_Lvl>" +
                        "<Case_Tamper type=\"string\">" + recordEventLogArray.get(i).BeaconCaseTamperStat + "</Case_Tamper>" +
                        "<Proximity_Tamper type=\"string\">" + recordEventLogArray.get(i).BeaconProxTamperStat + "</Proximity_Tamper>" +
                        "</item>" +
                        "</PurBeacon_Status>";
            }


            // Device Events
            String relatedZoneId = (recordEventLogArray.get(i).EventZoneId == -1 || recordEventLogArray.get(i).EventZoneId == 0) ? "" : String.valueOf(recordEventLogArray.get(i).EventZoneId);
            String relatedEventId = (recordEventLogArray.get(i).RelatedEvId > -1) ? String.valueOf(recordEventLogArray.get(i).RelatedEvId) : "";
            String eventLogStr = "  Event[" + i + "]"
                    + "\n   EvType = " + recordEventLogArray.get(i).EvType
                    + "\n   PKEventId = " + recordEventLogArray.get(i).PKEventId
                    + "\n   RelatedEvId = " + relatedEventId
                    + "\n   RelatedZoneId db = " + recordEventLogArray.get(i).EventZoneId
                    + "\n   RelatedZoneId = " + relatedZoneId
                    + "\n   OffStatus = " + recordEventLogArray.get(i).OffStatus;
            Log.w(getTagName(), eventLogStr);
            LoggingUtil.updateNetworkLog("\n" + eventLogStr, false);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), eventLogStr,
                    DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            eventString = eventString + "<item type=\"object\">" +
                    "<ID type=\"string\">" + recordEventLogArray.get(i).PKEventId + "</ID>" +            //Event Counter
                    "<OId type=\"string\">" + DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId + "</OId>" +

                    "<Zone_Id type=\"string\">" + relatedZoneId + "</Zone_Id>" + //NEW!

                    "<Tag_ID type=\"string\">539</Tag_ID>" +            //TAG ID	Old=123123 new=42
                    "<Type type=\"string\">" + recordEventLogArray.get(i).EvType + "</Type>" +        //intEventId
                    "<Time type=\"string\">" + (recordEventLogArray.get(i).UtcTime / 1000) + "</Time>" +
                    "<Request_ID type=\"string\"></Request_ID>" +
                    "<Link_Event type=\"string\">" + relatedEventId + "</Link_Event>" +
                    "<Additional_Info type=\"string\">" + recordEventLogArray.get(i).additionalInfo.replaceAll("<|>|&", " ") + "</Additional_Info>" +
                    "<TimeZone type=\"string\">" + recordEventLogArray.get(i).Timezone + "</TimeZone>" +
                    "<Off_Status type=\"array\">" +
                    "<item type=\"object\">" +
                    "<Off_Stat type=\"string\">" + recordEventLogArray.get(i).OffStatus + "</Off_Stat>" +
                    "<Tag_Batt type=\"string\">" + recordEventLogArray.get(i).ExtraData + "</Tag_Batt>" + // **
                    "<Is_In type=\"string\">" + recordEventLogArray.get(i).OffIsInRange + "</Is_In>" +
                    "<Is_In_Sched type=\"string\">1</Is_In_Sched>" +
                    "<Sync_Version type=\"array\">" +
                    "<item type=\"object\">" +
                    "<Sync_Type type=\"string\">4</Sync_Type>";
            int OffZoneVersion = DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion;
            eventString = eventString + "<Version_Number type=\"string\">" + OffZoneVersion + "</Version_Number>" +
                    "</item>" +
                    "</Sync_Version>" +
                    "<Tag_Status type=\"array\">" +
                    "<item type=\"object\">" +
                    "<Case type=\"string\">" + recordEventLogArray.get(i).TagCaseTamperStat + "</Case>" +
                    "<Strap type=\"string\">" + recordEventLogArray.get(i).TagStrapTamperStat + "</Strap>" +
                    "<Battery type=\"string\">" + recordEventLogArray.get(i).TagBatteryTamperStat + "</Battery>" +
                    "</item>" +
                    "</Tag_Status>" + PureBeaconStringArray +

                    "</item>" +
                    "</Off_Status>" +
                    "</item>";
        }

        eventString = eventString +
                "</Events>" +
                "</root>";

        return eventString;
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerOpen.handleResponse(result);
    }

    private int getPower() {
        CommunicationInterval currentCommInterval = NetworkRepository.getInstance().calculateComInterval();
        if (currentCommInterval == CommIntervalLow) return 1;
        return 0;
    }

}
