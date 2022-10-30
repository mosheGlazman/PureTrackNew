package com.supercom.puretrack.ui.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.supercom.puretrack.data.source.local.local_managers.business_logic.MagnetCaseManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.knox.KnoxSettingsModel;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;
import com.supercom.puretrack.util.application.App;

public class LockScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            LockScreenActivity.start(context);
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            EntityCaseTamper caseTamperEntity = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity();
            if (caseTamperEntity == null) return;
            if (caseTamperEntity.magnetCalibrationOnRestart > 0) {
                MagnetCaseManager magnetCaseManager = new MagnetCaseManager();
                magnetCaseManager.handleMagnetRecalibration(App.getContext());
            }
            KnoxSettingsModel KnoxSettingsModel = TableOffenderDetailsManager.sharedInstance().getCurKNOXSettingsConfiguration();
            if (KnoxSettingsModel.getLockScreen() == 1) return;
            LockScreenActivity.start(context);
        }
    }
}
