package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;

public class SmsOpOffenderLeftPureComZone extends SmsOperation {

    @Override
    protected void performSmsOperation() {
        TableOffenderStatusManager.sharedInstance().updateColumnInt(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFFENDER_IN_PURECOM_ZONE, TableOffenderDetails.OffenderBeaconZoneStatus.OUTSIDE_BEACON_ZONE);
        // start comm cycle, in order to update PureMonitor with the new status and comm interval
        NetworkRepository.getInstance().startNewCycle();

    }
}
