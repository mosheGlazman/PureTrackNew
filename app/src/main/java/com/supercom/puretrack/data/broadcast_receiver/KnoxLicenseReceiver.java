package com.supercom.puretrack.data.broadcast_receiver;


import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.sec.enterprise.knox.license.KnoxEnterpriseLicenseManager;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.runnable.BaseFutureRunnable;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.util.concurrent.TimeUnit;

public class KnoxLicenseReceiver extends BroadcastReceiver {
    public static final String EXTRA_LICENSE_PERM_GROUP = "edm.intent.extra.license.perm_group";

    private static final long MAX_TIME_TO_SEARCH_KLM_ACTIVATION = 30;
    private final KLMLicenceRunnable KLMLicenceRunnable = new KLMLicenceRunnable();
    private final Handler futureTasksHandler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isKnoxLicenseActivated = KnoxUtil.getInstance().isKnoxActivated();
        if (!isKnoxLicenseActivated) {
            String action = intent.getAction();
            if (action.equals(EnterpriseLicenseManager.ACTION_LICENSE_STATUS)) {
                String extraLicencePermGroupResult = intent.getStringExtra(EXTRA_LICENSE_PERM_GROUP);

                if (extraLicencePermGroupResult != null) {
                    App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                            "KnoxLicenseReceiver: extraLicencePermGroupResult: " + extraLicencePermGroupResult, DebugInfoModuleId.Knox);
                } else {
                    App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                            "KnoxLicenseReceiver: extraLicencePermGroupResult is NULL ", DebugInfoModuleId.Knox);
                }

                //EnterpriseLicenseManager
                if (extraLicencePermGroupResult != null && extraLicencePermGroupResult.equals("SAFE")) {

                    String extraLicenseStatusresult = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
                    App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                            "KnoxLicenseReceiver: extraLicenseStatusresult: " + extraLicenseStatusresult, DebugInfoModuleId.Knox);

                    if (extraLicenseStatusresult.equals("success")) {
                        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                                "Enterprise License activation(SAFE,success): " + extraLicenseStatusresult, DebugInfoModuleId.Knox);

                        KnoxEnterpriseLicenseManager klmManager = KnoxEnterpriseLicenseManager.getInstance(App.getContext());
                        klmManager.activateLicense(KnoxUtil.getInstance().getKLMLicence());

                        KLMLicenceRunnable.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(MAX_TIME_TO_SEARCH_KLM_ACTIVATION));

                    } else {
                        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Licence is not correct for Enterprise"
                                , DebugInfoModuleId.Knox);
                        KnoxUtil.getInstance().handleKnoxLicenceNotWorking();
                    }
                }

                //KnoxEnterpriseLicenseManag
                else if (extraLicencePermGroupResult != null && extraLicencePermGroupResult.equals("KNOX")) {

                    String extraLicenseStatusresult = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);

                    App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                            "Enterprise License activation(KNOX): " + extraLicenseStatusresult, DebugInfoModuleId.Knox);

                    if (extraLicenseStatusresult.equals("success")) {
                        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(),
                                "Knox Enterprise License activation: " + extraLicenseStatusresult, DebugInfoModuleId.Knox);

                        KnoxUtil.getInstance().initDefaultKnoxBlockingFunctions();

                        futureTasksHandler.removeCallbacks(KLMLicenceRunnable);

                    } else {

                        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Knox Licence is not correct for KnoxEnterpriseLicenseManager"
                                , DebugInfoModuleId.Knox);

                        KnoxUtil.getInstance().handleKnoxLicenceNotWorking();
                    }
                } else {
                    String extraLicenseStatusresult = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
                    int error = intent.getIntExtra(EnterpriseLicenseManager.EXTRA_LICENSE_ERROR_CODE, -1);
                    App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Knox Licence is not correct " + extraLicenseStatusresult + " " + error
                            , DebugInfoModuleId.Knox);

                    KnoxUtil.getInstance().handleKnoxLicenceNotWorking();
                }
            }
        }
    }

    class KLMLicenceRunnable extends BaseFutureRunnable {

        @Override
        public void run() {
            boolean isKnoxLicenceActivated = PureTrackSharedPreferences.isKnoxLicenceActivated();

            if (!isKnoxLicenceActivated) {

                App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Knox Licence is not correct for KnoxEnterpriseLicenseManager"
                        , DebugInfoModuleId.Knox);

                ((App) App.getContext()).setActivityToForegroundIfNeeded();

                KnoxUtil.getInstance().handleKnoxLicenceNotWorking();
            }
        }
    }
}
	