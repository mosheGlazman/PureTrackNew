package com.supercom.puretrack.model.business_logic_models.network.sync_requests;

import java.util.ArrayList;

public class ZonesRequestResult {
    private final ArrayList<ZoneResult> _zoneResultsList = new ArrayList<>();

    public ZoneResult getZoneResultByZoneId(int zoneId) {
        for (int i = 0; i < _zoneResultsList.size(); i++) {
            ZoneResult zoneResult = _zoneResultsList.get(i);
            if (zoneId == zoneResult.zoneId) {
                return zoneResult;
            }
        }

        return null;
    }

    public ArrayList<ZoneResult> getZoneResultsList() {
        return _zoneResultsList;
    }
}
