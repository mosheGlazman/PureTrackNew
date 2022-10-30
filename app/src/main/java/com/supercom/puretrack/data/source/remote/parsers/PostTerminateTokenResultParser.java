package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.model.ui_models.FlightModeData;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.PostTerminateRequestListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.util.general.LoggingUtil;

public class PostTerminateTokenResultParser implements PostTerminateRequestListener {

    private final ViewUpdateListener updateActivityListener;

    public PostTerminateTokenResultParser(ViewUpdateListener updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
    }

    @Override
    public void handleResponse(String response) {
        NetworkRepository.getInstance().handleCycleFinished();

        NetworkRepository.getInstance().initForNewCycle();

        FlightModeData flightModeData = NetworkRepository.getInstance().getFlightModeData();
        if (flightModeData.shouldEnableFlightMode()) {

            flightModeData.setShouldEnableFlightMode(false);
            if (updateActivityListener != null) {
                updateActivityListener.enableFlightMode(flightModeData.getFlightModeTimeOut());
            }
        } else {
            if (NetworkRepository.getInstance().shouldRestartAppInTheEndOfTheCycle()) {
                boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
                LoggingUtil.updateNetworkLog("\nhandleHttpResponseTerminate: restart app. knox " + isKnoxLicenceActivated + "\n", false);
                if (isKnoxLicenceActivated) {
                    KnoxUtil.getInstance().getKnoxSDKImplementation().rebootDevice();
                } else {
                    ((App) App.getContext()).restartApplication();
                }
            } else if (TableEventsManager.sharedInstance().hasEarlyNetworkCycleEvents() || NetworkRepository.getInstance().shouldUploadRecordsOrEventsToServerImmediately()) {
                App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Early cycle after post terminate", DebugInfoModuleId.Network);
                LoggingUtil.updateNetworkLog("\nCalling 'start new cycle' - handleHttpResponseTerminate" + "\n", false);
                NetworkRepository.getInstance().startNewCycle();
            } else {
                LoggingUtil.updateNetworkLog("\nhandleHttpResponseTerminate - nothing to do" + "\n", false);
            }
        }
    }

}
