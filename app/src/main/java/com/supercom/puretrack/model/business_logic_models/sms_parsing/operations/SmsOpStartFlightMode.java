/**
 *
 */
package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import android.os.Build;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;

public class SmsOpStartFlightMode extends SmsOperation {

    private int Timeout;

    @Override
    public void performSmsOperation() {
        if (KnoxUtil.getInstance().isKnoxActivated()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkRepository.getInstance().getFlightModeData().setFlightModeTimeOut(Timeout);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.flightModeEnabled, -1, -1);
            } else {
                App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Current version os lower than 6 and knox not support "
                        + "flight mode changing", DebugInfoModuleId.Network);
            }
        } else {
            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Flight mode message received but knox not activated or "
                            + "knox in offender mode",
                    DebugInfoModuleId.Network);
        }

    }
}
