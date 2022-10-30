package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations;

import android.os.Handler;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;

import java.util.concurrent.TimeUnit;

public class SmsOpStartEarlyCycle extends SmsOperation {
    private static final long MAXIMUM_TIME_INTERVAL_TO_SEARCH_FOR_LOCATION_SEC = 30;
    private static final int MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_SEC = 20;
    private LocationManager locationManager;


    final Runnable onIntervalFinishedRunnable = new Runnable() {
        public void run() {
            locationManager.chooseBestLastPointInTimeIntervalAndInsertToDB(MAXIMUM_TIME_INTERVAL_TO_SEARCH_FOR_LOCATION_SEC);
            locationManager.stopLocationUpdate();
            continueToEarlyCycle();
        }
    };

    @Override
    public void performSmsOperation() {
        if (offenderHasPointInBackTimeInterval(MAXIMUM_TIME_INTERVAL_TO_SEARCH_FOR_LOCATION_SEC)) {
            continueToEarlyCycle();
        } else {
            new Handler().postDelayed(onIntervalFinishedRunnable, TimeUnit.SECONDS.toMillis(MAXIMUM_TIME_INTERVAL_TO_SEARCH_FOR_LOCATION_SEC));
            locationManager = new LocationManager(App.getContext(), null);
            locationManager.requestLocationUpdates(MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_SEC);
        }
    }

    private boolean offenderHasPointInBackTimeInterval(long backTimeIntervalToCheckInSec) {
        boolean offenderHasPointInBackTimeInterval = false;
        EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
        if (offenderLastGpsPoint != null && (System.currentTimeMillis() - offenderLastGpsPoint.time <= TimeUnit.SECONDS.toMillis(backTimeIntervalToCheckInSec))) {
            offenderHasPointInBackTimeInterval = true;
        }

        return offenderHasPointInBackTimeInterval;
    }

    private void continueToEarlyCycle() {
        NetworkRepository.getInstance().startNewCycle();
    }
}
