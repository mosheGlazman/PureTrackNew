package com.supercom.puretrack.data.source.local.table_managers;

import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventTypes;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails.OffenderBeaconZoneStatus;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.model.database.entities.EntityZones;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.objects.HomeAddressSettings;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class TableZonesManager {
    public static final String TAG = "DBZonesManager";

    public static final int SCHEDULE_OF_ZONE_TYPE_MBI = 1; // Must Be in
    public static final int SCHEDULE_OF_ZONE_TYPE_CGO = 2; // Can Go Out
    public static final int SCHEDULE_OF_ZONE_TYPE_MBO = 3; // Must Be out
    public static final int SCHEDULE_OF_ZONE_TYPE_BIO = 4; // BIO
    public static final int SCHEDULE_OF_ZONE_TYPE_CGI = 5; // Can go in
    public static final int SCHEDULE_OF_ZONE_TYPE_ISP = 8; // inspection

    public static final int SCHEDULE_OF_ZONE_TYPE_ALL_EXCEPT_BIO = -1;

    private static final int ZONE_SHAPE_CIRCLE = 1;
    private static final int ZONE_SHAPE_RECTANGLE = 2;
    private static final int ZONE_SHAPE_POLYGON = 3;

    private static final int ZONE_TYPE_INCLUSION = 1;
    public static final int ZONE_TYPE_BEACON = 3;

    private static final int ZONE_OUTSIDE = 0;
    public static final int ZONE_INSIDE = 1;


    EntityGpsPoint _recordGpsPoint;

    private DBZonesManagerListener zonesManagerListener;


    private static final TableZonesManager INSTANCE = new TableZonesManager();


    private TableZonesManager() {
    }

    public static TableZonesManager sharedInstance() {
        return INSTANCE;
    }

    public interface DBZonesManagerListener {
        void onLeftBeaconZone();

        void onEnteredBeaconZone();
    }

    public void setZonesManagerListener(DBZonesManagerListener zonesManagerListener) {
        this.zonesManagerListener = zonesManagerListener;
    }


    private void checkRegularZonesIntersection(EntityGpsPoint recordGpsPoint) {

        Log.i(TAG, "*** checkZoneIntersection()\n -> Last Point :"
                + " Time = " + TimeUtil.GetTimeString(recordGpsPoint.time, TimeUtil.SIMPLE)
                + " Lat = " + recordGpsPoint.latitude
                + " Long = " + recordGpsPoint.longitude
                + " Accuracy = " + recordGpsPoint.accuracy);


        _recordGpsPoint = recordGpsPoint;
        EntityZones recordZone;

        Cursor cursorZones = DatabaseAccess.getInstance().tableZones.getZonesCursor();

        Log.i(TAG, " \n\n*** checkZoneIntersection()  -> Current Point : Lat = " + recordGpsPoint.latitude + " Long = " + recordGpsPoint.longitude + " Accuracy = " + recordGpsPoint.accuracy);
        Log.i(TAG, "checkZoneIntersection()" + " count = " + cursorZones.getCount());


        boolean isIntoZone = false;
        while (cursorZones.moveToNext()) {
            recordZone = DatabaseAccess.getInstance().tableZones.getZoneRecFromCursor(cursorZones);

            EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(recordZone.ZoneId);
            String appoinntmentTypeId = "None";
            if (currentScheduleOfZone != null) {
                appoinntmentTypeId = String.valueOf(currentScheduleOfZone.AppointmentTypeId);
            }
            if (recordZone.isIntoExclusionZoneState == 1) {
                Log.i(TAG, "\n----- Checking Zone [" + cursorZones.getPosition() + "] -----" +
                        "\n   ZoneId = " + recordZone.ZoneId +
                        "   TypeId (In=1,Excl=2,Beak=3) = " + recordZone.TypeId +
                        "   Current Schedule (MBI=1,CGO=2,MBO=3,CGI=5) = " + appoinntmentTypeId +
                        "   ShapeType (Circ=1,Rec=2,Poly=3) = " + recordZone.ShapeType +
                        "   IsInto = " + recordZone.isIntoExclusionZoneState);
                isIntoZone = true;
            }


            //Apply the Zone logic for all zones except the ZONE_TYPE_BEACON
            checkShapeIntersection(recordZone);
        }
        if (isIntoZone) {
            Log.i(TAG, "\n----- Checking Zone [" + cursorZones.getPosition() + "] -----"
                    + "\n   Outside all zones");
        }
        cursorZones.close();
    }


    public void checkZoneIntersection(EntityGpsPoint recordGpsPoint) {

        // calcualte if the offender entered or left zone
        checkRegularZonesIntersection(recordGpsPoint);
        checkDeletedZonesIntersection(recordGpsPoint);
        checkOffenderCloseToHomeAddressIntersection();
    }

    private void checkDeletedZonesIntersection(EntityGpsPoint recordGpsPoint) {
        List<EntityZones> allDeletedZones = DatabaseAccess.getInstance().tableZonesDeleted.getAllZonesThatShouldBeDeleted();

        for (EntityZones recordZoneItem : allDeletedZones) {
            checkShapeIntersection(recordZoneItem);
            if (recordZoneItem.TypeId != ZONE_TYPE_BEACON) {
                DatabaseAccess.getInstance().tableZonesDeleted.deleteZoneByZoneId(recordZoneItem.ZoneId);
            }
        }
    }

    private void checkOffenderCloseToHomeAddressIntersection() {
        calulateIfOffenderCloseToHomeAndBeaconNotExists();
    }

    private void calulateIfOffenderCloseToHomeAndBeaconNotExists() {
        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
        HomeAddressSettings homeAddressSettingsObject = TableOffenderDetailsManager.sharedInstance().getHomeAddressSettingsObject();
        if (!isBeaconExistsInDBZone && homeAddressSettingsObject != null && homeAddressSettingsObject.Enable == 1) {

            EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();

            float homeLat = TableOffenderDetailsManager.sharedInstance().getFloatValueByColumnName(OFFENDER_DETAILS_CONS.HOME_LAT);
            float homeLong = TableOffenderDetailsManager.sharedInstance().getFloatValueByColumnName(OFFENDER_DETAILS_CONS.HOME_LONG);

            if (offenderLastGpsPoint != null && homeLat != 0 && homeLong != 0) {
                int distance = calculateDistance(offenderLastGpsPoint.longitude, offenderLastGpsPoint.latitude, homeLong, homeLat);
                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.HOME_RADIUS) != -1;
                if (distance < homeAddressSettingsObject.Radius && !hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(EventTypes.enteredHomeRadius, -1, -1);
                } else if (distance > homeAddressSettingsObject.Radius && hasOpenEvent) {
                    TableEventsManager.sharedInstance().addEventToLog(EventTypes.leftHomeRadius, -1, -1);
                }

                App.writeToZoneLogsAndDebugInfo(TAG, "\nDistance between correct location to home is: " + distance
                        + " meters", DebugInfoModuleId.Zones);
            }
        }

    }

    private void checkShapeIntersection(EntityZones recordZone) {
        if (recordZone.TypeId != ZONE_TYPE_BEACON) {
            if (recordZone.ShapeType == ZONE_SHAPE_CIRCLE) {
                handleCircleIntersection(recordZone);
            } else if (recordZone.ShapeType == ZONE_SHAPE_POLYGON || recordZone.ShapeType == ZONE_SHAPE_RECTANGLE) {
                treatPolygonIntersection(recordZone);
            }
        }
    }

    void handleCircleIntersection(EntityZones zoneRecord) {
        if (isPointInsideExclusionWithBufferCircle(zoneRecord)) { //
            handlePointInside(zoneRecord);
        } else {
            handlePointOutside(zoneRecord);
        }
    }

    void treatPolygonIntersection(EntityZones zoneRecord) {
        Gson gson = new Gson();
        ArrayList<PolygonPoint> polygonPointsArr;

        polygonPointsArr = gson.fromJson(zoneRecord.PointsJsonStr, new TypeToken<ArrayList<PolygonPoint>>() {
        }.getType());
        if (isPointInsidePolygon(polygonPointsArr))
            handlePointInside(zoneRecord);
        else
            handlePointOutside(zoneRecord);
    }

    private void updateExclZoneRecordInDB(EntityZones zoneRecord) {
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableIsInto(zoneRecord.ZoneId, zoneRecord.isIntoExclusionZoneState);
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableIsIntoBuffer(zoneRecord.ZoneId, zoneRecord.isIntoBufferZoneState);
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableIncCnt(zoneRecord.ZoneId, zoneRecord.EnteringZoneCnt);
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableExcCnt(zoneRecord.ZoneId, zoneRecord.ExitingZoneCnt);
    }

    private void updateInclZoneRecordInDB(EntityZones zoneRecord) {
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableIsInto(zoneRecord.ZoneId, zoneRecord.isIntoExclusionZoneState);
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableIncCnt(zoneRecord.ZoneId, zoneRecord.EnteringZoneCnt);
        DatabaseAccess.getInstance().tableZones.UpdateZonesTableExcCnt(zoneRecord.ZoneId, zoneRecord.ExitingZoneCnt);
    }

    // ********************  START: TREAT POINT INSIDE  ******************** //
    public void handlePointInside(EntityZones zoneRecord) {
        if (zoneRecord != null) {
            int shape = zoneRecord.ShapeType;
            switch (shape) {
                case ZONE_SHAPE_CIRCLE:
                    boolean isPointInsideExclusionZoneCircleArea =  isPointInsideExclusionZoneCircleArea(zoneRecord); // A.K.A Prohibited/Violation/Restricted/Exclusion area
                    if (isPointInsideExclusionZoneCircleArea) {
                        if (zoneRecord.isIntoExclusionZoneState == ZONE_OUTSIDE) { // Last check the offender was Outside the Exclusion Zone
                            handleExclusionZoneEntry(zoneRecord);
                        } else { // Last check I was Inside the Zone
                            handleStillInside(zoneRecord);
                        }
                    }
                    else { // The offender is inside the buffer zone
                        if (zoneRecord.isIntoExclusionZoneState == ZONE_INSIDE) {  // Last check I was Inside the Exclusion Zone
                            validateAndTreatPointOutside_ExclZone(zoneRecord);
                        } else if (zoneRecord.isIntoBufferZoneState == ZONE_OUTSIDE) { // otherwise - Last check I was out off the buffer zone - new buffer zone entry
                            handleBufferZoneEntry(zoneRecord);
                        } else { // Last check I was Inside the buffer Zone
                            // TODO: chek if we need to handle logic for 'still inside buffer zone'
                        }
                    }
                    break;
                default: // Any other shape - currently has no buffer
                    // Last check I was Outside the Zone
                    if (zoneRecord.isIntoExclusionZoneState == ZONE_OUTSIDE) {
                        handleExclusionZoneEntry(zoneRecord);
                    } else { // Last check I was Inside the Zone
                        handleStillInside(zoneRecord);
                    }
            }
        }
    }

    private void handleStillInside(EntityZones zoneRecord) {
        // reset "zone drift" counter
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0;
        updateInclZoneRecordInDB(zoneRecord);

        //Do we need to check the zone type, possibly irrelevant for "still inside" by schedule violation.
        if (zoneRecord.TypeId == ZONE_TYPE_INCLUSION) {
            TableEventsManager.sharedInstance().addEventFor_StillInsideInclZone(zoneRecord.ZoneId);
        } else {
            TableEventsManager.sharedInstance().addEventFor_StillInsideExclZone(zoneRecord.ZoneId);
        }
    }

    private void handleExclusionZoneEntry(EntityZones zoneRecord) {
        boolean zoneDriftEnable = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_ENABLE) == 1);
        if (zoneDriftEnable) {
            int zoneDriftLocations = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_LOCATIONS);
            zoneRecord.EnteringZoneCnt++;
            updateInclZoneRecordInDB(zoneRecord);
            if (zoneRecord.EnteringZoneCnt < zoneDriftLocations) {
                App.writeToZoneLogsAndDebugInfo(TAG, "Waiting to enter Zone: Location: [ " + _recordGpsPoint.latitude + "] " + "[" + _recordGpsPoint.longitude + "], counter: " +
                        zoneRecord.EnteringZoneCnt + "/" + zoneDriftLocations, DebugInfoModuleId.Zones);
                // not enough points to enter zone
                return;
            } else {
                App.writeToZoneLogsAndDebugInfo(TAG, "Enter Zone after " + zoneRecord.EnteringZoneCnt + " points, Location: [ " + _recordGpsPoint.latitude + "] " + "[" + _recordGpsPoint.longitude + "]", DebugInfoModuleId.Zones);
            }
        }

        if (zoneRecord.TypeId == ZONE_TYPE_INCLUSION) {
            if (zoneRecord.isIntoExclusionZoneState == ZONE_OUTSIDE)
                validateAndTreatPointInside_InclZone(zoneRecord);
        } else { // Exclusion zone?
            int shape = zoneRecord.ShapeType;
            if (shape == ZONE_SHAPE_CIRCLE) {
                if (zoneRecord.isIntoBufferZoneState == ZONE_OUTSIDE) { // Didn't go through the buffer zone into the exclusion zone - can happen if the exclusion zone was moved/modified on PM
                    TableEventsManager.sharedInstance().addEventEntryOfBufferZone(zoneRecord.ZoneId);
                }
                TableEventsManager.sharedInstance().addEventExitOfBufferZone(zoneRecord.ZoneId); // add event to notify the buffer zone was left
            }
            validateAndHandlePointInsideExclusionZone(zoneRecord);
        }
    }

    private void handleBufferZoneEntry(EntityZones zoneRecord) {
        zoneRecord.EnteringZoneCnt = 0; // Todo: check whether ExitingZoneCnt should be increased by 1 or not
        zoneRecord.ExitingZoneCnt = 0;
        zoneRecord.isIntoExclusionZoneState = ZONE_OUTSIDE;
        zoneRecord.isIntoBufferZoneState = ZONE_INSIDE;
        TableEventsManager.sharedInstance().addEventEntryOfBufferZone(zoneRecord.ZoneId);
        updateExclZoneRecordInDB(zoneRecord);
    }

    private void handleBufferZoneExit(EntityZones zoneRecord) {
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0; // Todo: check whether ExitingZoneCnt should be increased by 1 or not
        zoneRecord.isIntoBufferZoneState = ZONE_OUTSIDE;
        TableEventsManager.sharedInstance().addEventExitOfBufferZone(zoneRecord.ZoneId);
        updateExclZoneRecordInDB(zoneRecord);
    }

    private void validateAndTreatPointInside_InclZone(EntityZones zoneRecord) {
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0;
        zoneRecord.isIntoExclusionZoneState = ZONE_INSIDE;
        updateInclZoneRecordInDB(zoneRecord);
        TableEventsManager.sharedInstance().addEventFor_EnteredInclZone(zoneRecord.ZoneId);
    }


    private void validateAndHandlePointInsideExclusionZone(EntityZones zoneRecord) {
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0;
        zoneRecord.isIntoExclusionZoneState = ZONE_INSIDE;
        zoneRecord.isIntoBufferZoneState = ZONE_OUTSIDE;
        TableEventsManager.sharedInstance().addEventEntryOfExclusionZone(zoneRecord.ZoneId);
        updateExclZoneRecordInDB(zoneRecord);
    }

    public void handlePointOutside(EntityZones zoneRecord) {
        if (zoneRecord != null) {
            //Last check the offender was Inside the exclusion or the buffer zone
            if (zoneRecord.isIntoExclusionZoneState == ZONE_INSIDE || zoneRecord.isIntoBufferZoneState == ZONE_INSIDE) {
                treatExitIntersection(zoneRecord);
            } else { //Last check I was Outside the Zone
                treatStillOutside(zoneRecord);
            }
        }
    }

    private void treatStillOutside(EntityZones zoneRecord) {
        // reset "zone drift" counters
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0;
        updateInclZoneRecordInDB(zoneRecord);

        if (zoneRecord.TypeId == ZONE_TYPE_INCLUSION) {
            TableEventsManager.sharedInstance().addEventFor_StillOutsideInclZone(zoneRecord.ZoneId);
        } else {
            TableEventsManager.sharedInstance().addEventFor_StillOutsideExclZone(zoneRecord.ZoneId);
        }
    }

    private void treatExitIntersection(EntityZones zoneRecord) {
        boolean zoneDriftEnable = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_ENABLE) == 1);
        if (zoneDriftEnable) {
            int zoneDriftLocations = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_LOCATIONS);
            zoneRecord.ExitingZoneCnt++;
            updateInclZoneRecordInDB(zoneRecord);
            if (zoneRecord.ExitingZoneCnt < zoneDriftLocations) {
                App.writeToZoneLogsAndDebugInfo(TAG, "Waiting to exit Zone: Location: [ " + _recordGpsPoint.latitude + "] " + "[" + _recordGpsPoint.longitude + "], counter: " +
                        zoneRecord.ExitingZoneCnt + "/" + zoneDriftLocations, DebugInfoModuleId.Zones);
                // not enough points to enter zone
                return;
            } else {
                App.writeToZoneLogsAndDebugInfo(TAG, "Exit Zone after " + zoneRecord.ExitingZoneCnt + " points, Location: [ " + _recordGpsPoint.latitude + "] " + "[" + _recordGpsPoint.longitude + "]", DebugInfoModuleId.Zones);
            }
        }

        if (zoneRecord.TypeId == ZONE_TYPE_INCLUSION) {
            validateAndTreatPointOutside_InclZone(zoneRecord);
        } else { // Exclusion zone?
            int shape = zoneRecord.ShapeType;
            if (shape == ZONE_SHAPE_CIRCLE) { // if its a circle - check extra logic for buffer
                if (zoneRecord.isIntoExclusionZoneState == ZONE_INSIDE) { // Last check the offender was Inside the Exclusion Zone Didn't go through the buffer zone into the exclusion zone - can happen if the exclusion zone was moved/modified on PM, send entry & exit event about buffer zone
                    validateAndTreatPointOutside_ExclZone(zoneRecord);
                    TableEventsManager.sharedInstance().addEventEntryOfBufferZone(zoneRecord.ZoneId); // add event to notify the buffer zone was entered
                    TableEventsManager.sharedInstance().addEventExitOfBufferZone(zoneRecord.ZoneId); // add event to notify the buffer zone was left
                } else if (zoneRecord.isIntoBufferZoneState == ZONE_INSIDE) { // Last check the offender was Inside the Buffer Zone
                    handleBufferZoneExit(zoneRecord);
                }
            } else { // Any other shape
                validateAndTreatPointOutside_ExclZone(zoneRecord);
            }
        }
    }

    private void validateAndTreatPointOutside_InclZone(EntityZones zoneRecord) {
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0;
        zoneRecord.isIntoExclusionZoneState = ZONE_OUTSIDE;
        updateInclZoneRecordInDB(zoneRecord);
        TableEventsManager.sharedInstance().addEventFor_ExitInclZone(zoneRecord.ZoneId, "");

    }

    private void validateAndTreatPointOutside_ExclZone(EntityZones zoneRecord) {
        zoneRecord.EnteringZoneCnt = 0;
        zoneRecord.ExitingZoneCnt = 0;
        zoneRecord.isIntoExclusionZoneState = ZONE_OUTSIDE;
        updateExclZoneRecordInDB(zoneRecord);
        TableEventsManager.sharedInstance().addEventFor_ExitExclZone(zoneRecord.ZoneId);

    }

    private boolean isPointInsideExclusionWithBufferCircle(EntityZones recordZone) {
        int distanceFromCircleCenter = calculateDistance(_recordGpsPoint.longitude, _recordGpsPoint.latitude, recordZone.Longitude, recordZone.Latitude);
        boolean isPointInsideCircleIncludeBuffer = distanceFromCircleCenter <= (recordZone.Radius + recordZone.bufferZone);

        Log.i(TAG, "\n      Radius = " + recordZone.Radius + "\n      Buffer = " + recordZone.bufferZone + "\n      DistanceFromCircleCenter = " + distanceFromCircleCenter + "\n      isPointInsideCircleIncludeBuffer = " + isPointInsideCircleIncludeBuffer);

        return isPointInsideCircleIncludeBuffer;
    }

    private boolean isPointInsideExclusionZoneCircleArea(EntityZones recordZone) {
        int distanceFromCircleCenter = calculateDistance(_recordGpsPoint.longitude, _recordGpsPoint.latitude, recordZone.Longitude, recordZone.Latitude);
        return distanceFromCircleCenter <= recordZone.Radius;
    }

    int calculateDistance(double longitudeOffender, double latitudeOffender, double longitudeZone, double latitudeZone) {
        // create offender location
        Location locationPointOffender = new Location("");
        locationPointOffender.setLongitude(longitudeOffender);
        locationPointOffender.setLatitude(latitudeOffender);
        // create center of zone location
        Location locationPointZone = new Location("");
        locationPointZone.setLongitude(longitudeZone);
        locationPointZone.setLatitude(latitudeZone);
        return ((int) locationPointOffender.distanceTo(locationPointZone));
    }

    private boolean isPointInsidePolygon(ArrayList<PolygonPoint> polygonPoints) {
        Log.i(TAG, "" + "\n      polygonPoints number =  " + polygonPoints.size());


        int i;
        int j;
        boolean result = false;
        for (i = 0, j = polygonPoints.size() - 1; i < polygonPoints.size(); j = i++) {
            if ((polygonPoints.get(i).Latitude > _recordGpsPoint.latitude) != (polygonPoints.get(j).Latitude > _recordGpsPoint.latitude)
                    && (_recordGpsPoint.longitude < (polygonPoints.get(j).Longitude - polygonPoints.get(i).Longitude) * (_recordGpsPoint.latitude - polygonPoints.get(i).Latitude)
                    / (polygonPoints.get(j).Latitude - polygonPoints.get(i).Latitude) + polygonPoints.get(i).Longitude)) {
                result = !result;
            }
        }

        Log.i(TAG, "      isPointInsidePolygon = " + result);
        return result;
    }

    class PolygonPoint {
        double Latitude;
        double Longitude;
    }


    //   ****   Beacon Zone handling    ****

    public void handleInsideBeaconZone() {
        // PureCare - if waiting for beacon installation, no Beacon events enabled (OFF_PENDING_HOME_LOCATION, OFF_PENDING_BEACON_INSTALLATION)
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isOffenderAllocated) {

            EntityZones zoneRecordByZoneId = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(
                    (int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID));

            if ((TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.OUTSIDE_BEACON_ZONE)) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE, TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE);
                zonesManagerListener.onEnteredBeaconZone();

                if (zoneRecordByZoneId != null) {
                    TableEventsManager.sharedInstance().addEventFor_EnteredInclZone(zoneRecordByZoneId.ZoneId);
                }
            } else if (zoneRecordByZoneId != null) {
                TableEventsManager.sharedInstance().addEventFor_StillInsideInclZone(zoneRecordByZoneId.ZoneId);
            }
        }
    }

    public void handleOutsideBeaconZone(boolean wasBeaconZoneDeleted, String additionalInfo) {
        // PureCare - if waiting for beacon installation, no Beacon events enabled (OFF_PENDING_HOME_LOCATION, OFF_PENDING_BEACON_INSTALLATION)
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isOffenderAllocated) {

            EntityZones zoneRecordByZoneId = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(
                    (int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID));

            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE, TableOffenderDetails.OffenderBeaconZoneStatus.OUTSIDE_BEACON_ZONE);
                zonesManagerListener.onLeftBeaconZone();

                if (zoneRecordByZoneId != null) {
                    TableEventsManager.sharedInstance().addEventFor_ExitInclZone(zoneRecordByZoneId.ZoneId, additionalInfo);
                }
            } else if (zoneRecordByZoneId != null) {
                TableEventsManager.sharedInstance().addEventFor_StillOutsideInclZone(zoneRecordByZoneId.ZoneId);
            }

        }
        if (wasBeaconZoneDeleted) {
            handleBeaconZoneDeleted(additionalInfo, isOffenderAllocated);
        }
    }

    private void handleBeaconZoneDeleted(String additionalInfo, boolean isOffenderAllocated) {
        List<EntityZones> allDeletedZones = DatabaseAccess.getInstance().tableZonesDeleted.getAllZonesThatShouldBeDeleted();

        for (EntityZones recordZoneItem : allDeletedZones) {

            if (recordZoneItem.TypeId == ZONE_TYPE_BEACON) {

                if (isOffenderAllocated) {
                    boolean hasOpenEvent = ((DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdForZoneViolationCategory
                            (ViolationCategoryTypes.ENTER_INCLUSION, recordZoneItem.ZoneId)) != -1);
                    if (hasOpenEvent) {
                        TableEventsManager.sharedInstance().addEventFor_ExitInclZone(recordZoneItem.ZoneId, additionalInfo);
                    }
                }

                DatabaseAccess.getInstance().tableZonesDeleted.deleteZoneByZoneId(recordZoneItem.ZoneId);
            }
        }

        initBeaconZoneToDefaultValuesIfNeeded();

    }

    private void initBeaconZoneToDefaultValuesIfNeeded() {
        boolean isZonesTableContainBeaconZone = false;

        List<EntityZones> allZones = DatabaseAccess.getInstance().tableZones.getAllZones();
        for (EntityZones recordZones : allZones) {
            if (recordZones.TypeId == TableZonesManager.ZONE_TYPE_BEACON) {
                isZonesTableContainBeaconZone = true;
                break;
            }
        }
        if (!isZonesTableContainBeaconZone) {
            TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID, 0);
            TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID, 0);
            TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_VERSION, 0);
            TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_NAME, "");
            TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_BEACON_ADDRESS, "");
            TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ENCRYPTION, "");
        }
    }

    public void checkBeaconZoneStatus() {
        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;

        if (isBeaconExistsInDBZone) {
            if ((TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE)
                    == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE)) {
                handleInsideBeaconZone();
            } else {
                handleOutsideBeaconZone(false, "");
            }
        }
    }
}

