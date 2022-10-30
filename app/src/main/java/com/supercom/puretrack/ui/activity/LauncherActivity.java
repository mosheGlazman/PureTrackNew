package com.supercom.puretrack.ui.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.enterprise.EnterpriseDeviceManager;
import android.app.enterprise.SecurityPolicy;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.model.database.entities.EntityDeviceDetails;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.model.ui_models.LauncherApplicationModel;
import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.WatchdogManager;
import com.supercom.puretrack.data.service.WatchdogService;
import com.supercom.puretrack.data.broadcast_receiver.WakingAlarmBroadcastReceiver;
import com.supercom.puretrack.ui.adapter.AppsListAdapter;
import com.supercom.puretrack.ui.dialog.SettingsPasswordDialog;
import com.supercom.puretrack.ui.dialog.SettingsPasswordDialog.IUnlockDialogCallbackListener;
import com.supercom.puretrack.ui.lockscreen.LockScreenActivity;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LauncherActivity extends BaseActivity {
    public static final String TAG = "LauncherActivity";

    public static final String LAUNCHER_ACTIVITY_MESSAGE_RECEIVER = "LauncherActivityMessageReceiver";
    public static final String LAUNCHER_ACTIVITY_WAKEUP_RECEIVER = "LauncherActivityWakeupReceiver";

    public static final String LAUNCHER_ACTIVITY_EXTRA = "lancher_activity_extra";
    public static final String MESSAGE_EXTRA = "message_extra";
    public static final String UPDATE_LAUNCHER_EXTRA = "init_knox_extra";
    public static final String ENTER_OFFICER_MODE_CLICK_EXTRA = "enter_officer_mode_click";
    public static final String APPLICATION_LANGUAGE_CHANGED_CLICK_EXTRA = "application_language_changed_click_extra";


    private List<String> allowedDefaultPackagesList;
    private final ArrayList<String> allowedPackagesFromServerList = new ArrayList<>();
    private PackageManager manager;

    private ListView appsListView;
    private AppsListAdapter appsListAdapter;
    private View activityAppsListFooter;
    private final List<String> runningProccessNotAllowedToBeStoppedLocalList = (Arrays.asList
            ("com.supercom.puretrack.usbhost", "com.google.android.gms.persistent", "com.sec.android.app.launcher", "com.samsung.android.sm", "com.android.settings"
                    , "com.sec.android.provider.badge", "com.android.bluetooth", "com.android.mms", "com.android.defcontainer"
                    , "android.process.acoree", "com.sec.android.cloudagent", "com.samsung.android.fingerprint.service", "android:ui"
                    , "com.android.contacts", "com.sec.android.provider.badge", "com.google.android.gms", "com.sec.android.provider.badge"
                    , "com.sec.android.provider.badge", "com.sec.spp.push:RemoteNotiProcess", "com.sec.android.provider.badge", "com.sec.pcw.device"
                    , "com.sec.android.service.health", "com.sec.esdk.elm", "com.samsung.klmsagent", "system:ui"
                    , "com.visionobjects.resourcemanager", "com.google.process.gapps", "com.sec.android.inputmethod", "com.android.providers.calendar"
                    , "com.android.vending", "com.sec.android.daemonapp", "com.samsung.android.providers.context", "com.google.android.apps.maps"
                    , "com.samsung.dcm:DCMService", "com.sec.android.automotive.drivelink", "com.sec.chaton", "com.samsung.aasaservice"
                    , "com.policydm", "com.android.systemui.imagewallpaper", "com.android.incallui", "com.android.phone", "com.android.server.telecom"
                    , "com.android.systemui", "system", "com.sec.android.app.shealth", "com.google.android.apps.plus", "com.sec.knox.knoxsetupwizardclient"
                    , "com.sec.knox.bridge", "com.samsung.android.app.FileShareServer", "com.sec.android.gallery3d", "com.samsung.android.app.galaxyfinder"
                    , "com.samsung.android.sdk.samsunglink", "com.sec.android.widgetapp.ap.hero.accuweather", "com.android.nfc", "com.android.calendar"
                    , "com.sec.android.widgetapp.SPlannerAppWidget", "com.samsung.android.sdk.samsunglink", "com.samsung.android.sdk.samsunglink", "com.samsung.android.sdk.samsunglink"
                    , "com.samsung.android.sdk.samsunglink", "com.sec.android.GeoLookout", "com.sec.android.widgetapp.activeapplicationwidget", "com.samsung.android.sconnect"
                    , "com.google.android.apps.magazines", "com.samsung.android.app.assistantmenu", "com.sec.android.app.soundalive", "com.osp.app.signin"
                    , "com.sec.android.app.samsungapps", "com.sec.spp.push", "com.samsung.android.provider.shootingmodeprovider", "com.sec.kidsplat.installer"
                    , "com.samsung.helphub", "com.sec.android.provider.logsprovider", "com.samsung.android.app.watchmanagerstub", "com.samsung.android.app.filterinstaller"
                    , "com.samsung.android.intelligenceservice", "com.vlingo.midas", "com.supercom.puretrack.usbhost:watchdog_process"));

    private final WakingAlarmBroadcastReceiver wakingUpReceiver = new WakingAlarmBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();

        setContentView(R.layout.launcher_allowed_apps_list);

        if (isAllDangerousPermissionsGranted) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onCreate() - LauncherActivity ", false);
            String messageToUpload = "onCreate() - LauncherActivity";
            Log.i(TAG, messageToUpload);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            updateAllowedPackagesListFromServer();
        }

        initAllowedDefaultPackages();
        initSupportedAppsList();

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "LAUNCHER_ACTIVITY_RECEIVER".
        LocalBroadcastManager.getInstance(this).registerReceiver(launcherMessageReceiver, new IntentFilter(LAUNCHER_ACTIVITY_MESSAGE_RECEIVER));
        LocalBroadcastManager.getInstance(this).registerReceiver(wakeupReceiver, new IntentFilter(LAUNCHER_ACTIVITY_WAKEUP_RECEIVER));

        startWatchdogServiceIfNeeded();
        final TextView pureTrackSerialN_et = this.findViewById(R.id.launcher_pureTrackSerialN);
        EntityDeviceDetails deviceDetails = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord();
        pureTrackSerialN_et.setText(deviceDetails.getDeviceSerialNumber());

        // CKOUT: Screenshot fix
        // MOJ: Screenshot fix
        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    private void prepareScreenToNewMode() {
        // if now we are in officer mode, we need change screen to offenderMode and opposite
        if (KnoxUtil.getInstance().isInOfficerMode()) {
            prepareScreenToOffenderMode();
        } else {
            if (KnoxUtil.getInstance().isInInitializedOffenderFlightMode()) {

                String titleMsg = getString(R.string.dialog_text_enter_the_password, DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber());
                SettingsPasswordDialog settingsPasswordDialog = new SettingsPasswordDialog(this);
                settingsPasswordDialog.createUnlockDialog(titleMsg, new IUnlockDialogCallbackListener() {
                    @Override
                    public void onTryUnlockWithCorrectPassword() {
                        prepareScreenToOfficerMode();
                    }

                    @Override
                    public void onTryUnlockWithWrongPassword() {
                        Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onBackButtonPressed() {

                    }
                });
            } else {
                prepareScreenToOfficerMode();
            }
        }
    }

    private void handleLanguageChanged() {
        setContentView(R.layout.launcher_allowed_apps_list);
        initSupportedAppsList();
    }

    private void initSupportedAppsList() {
        appsListView = findViewById(R.id.apps_list);

        activityAppsListFooter = LayoutInflater.from(this).inflate(R.layout.launcher_allowed_apps_list_footer, null);
        appsListView.addFooterView(activityAppsListFooter);

        ArrayList<LauncherApplicationModel> loadApps = loadApps();
        appsListAdapter = new AppsListAdapter(this, R.layout.launcher_allowed_apps_list_item, loadApps);
        appsListView.setAdapter(appsListAdapter);

        boolean isKnoxLicenceActivated = PureTrackSharedPreferences.isKnoxLicenceActivated();

        if (isKnoxLicenceActivated) {

            activityAppsListFooter.findViewById(R.id.officerModeButton).setVisibility(View.VISIBLE);

            if (KnoxUtil.getInstance().isInOfficerMode()) {
                prepareScreenToOfficerMode();
            } else {
                prepareScreenToOffenderMode();
            }
        } else {
            activityAppsListFooter.findViewById(R.id.officerModeButton).setVisibility(View.GONE);
            activityAppsListFooter.findViewById(R.id.showShutdownDialogButton).setVisibility(View.GONE);
        }

        setClickListenerToPureTrackApplication(loadApps);
    }


    /* start watchdog service in case that third party closed the application,
    then we want that application will start automatically */
    private void startWatchdogServiceIfNeeded() {
        if (!WatchdogManager.sharedInstance().isWatchdogeRunning()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Intent watchdogServiceIntent = new Intent(LauncherActivity.this, WatchdogService.class);
                    startService(watchdogServiceIntent);
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        if (TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ENABLE) == 1 && App.IS_PINCODE_TYPED == false) {
            LockScreenActivity.start(this);
        }
        if (isAllDangerousPermissionsGranted) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onResume() - LauncherActivity ", false);
            String messageToUpload = "onResume() - LauncherActivity";
            Log.i(TAG, messageToUpload);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
        final TextView pureTrackSerialN_et = this.findViewById(R.id.launcher_pureTrackSerialN);
        EntityDeviceDetails deviceDetails = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord();
        pureTrackSerialN_et.setText(deviceDetails.getDeviceSerialNumber());
        super.onResume();

    }

    @Override
    protected void onPause() {
        if (isAllDangerousPermissionsGranted) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onPause()- LauncherActivity ", false);
            String messageToUpload = "onPause() - LauncherActivity";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isAllDangerousPermissionsGranted) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onStop() - LauncherActivity ", false);
            String messageToUpload = "onStop() - LauncherActivity";
            Log.i(TAG, messageToUpload);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
        super.onStop();
    }

    private void initAllowedDefaultPackages() {
        allowedDefaultPackagesList = new ArrayList<>();
        initPackage(BuildConfig.APPLICATION_ID);
        initPackage("com.sci.trac.android.tracmobilemonitor");
    }

    private void initPackage(String packageName) {
        allowedDefaultPackagesList.add(packageName);
    }

    private ArrayList<LauncherApplicationModel> loadApps() {
        manager = getPackageManager();
        ArrayList<LauncherApplicationModel> apps = new ArrayList<LauncherApplicationModel>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {
            if (allowedDefaultPackagesList.contains(ri.activityInfo.packageName)) {
                addAppDetailObjectToAppsArray(apps, ri);
            } else if (allowedPackagesFromServerList.contains(ri.activityInfo.packageName)) {
                addAppDetailObjectToAppsArray(apps, ri);
            }
        }

        return apps;
    }

    private void addAppDetailObjectToAppsArray(ArrayList<LauncherApplicationModel> apps, ResolveInfo ri) {
        LauncherApplicationModel app = new LauncherApplicationModel();
        app.setLabel(ri.loadLabel(manager));
        app.setName(ri.activityInfo.packageName);
        app.setIcon(ri.activityInfo.loadIcon(manager));
        apps.add(app);
    }

    private void updateAllowedPackagesListFromServer() {
        String deviceConfigAppsList = DatabaseAccess.getInstance().tableOffenderDetails
                .getRecordOffDetails().DeviceConfigAppsList;
        try {
            if (!deviceConfigAppsList.isEmpty()) {
                JSONObject jsonObject = new JSONObject(deviceConfigAppsList);
                JSONArray appsListJsonArray = jsonObject.getJSONArray("Apps");
                for (int i = 0; i < appsListJsonArray.length(); i++) {
                    String packageName = appsListJsonArray.getString(i);
                    allowedPackagesFromServerList.add(packageName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<String> getUnAllowedProcessToGetKilledFromServer() {
        ArrayList<String> unallowedAppsToGetKilledFromServer = new ArrayList<>();
        String backgroundAppWhiteList = DatabaseAccess.getInstance().tableOffenderDetails
                .getRecordOffDetails().backgroundAppWhiteList;
        try {
            if (!backgroundAppWhiteList.isEmpty()) {
                JSONObject jsonObject = new JSONObject(backgroundAppWhiteList);
                JSONArray appsListJsonArray = jsonObject.getJSONArray("BackgroundAppWhiteList");
                for (int i = 0; i < appsListJsonArray.length(); i++) {
                    String packageName = appsListJsonArray.getString(i);
                    unallowedAppsToGetKilledFromServer.add(packageName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return unallowedAppsToGetKilledFromServer;
    }

    private void updateSupportedAppsList() {
        final ArrayList<LauncherApplicationModel> appsList = loadApps();

        appsListAdapter.setApps(appsList);
        appsListAdapter.notifyDataSetChanged();

        setClickListenerToPureTrackApplication(appsList);
    }

    private void setClickListenerToPureTrackApplication(final ArrayList<LauncherApplicationModel> appsList) {
        appsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {

                Intent intent = manager.getLaunchIntentForPackage(appsList.get(pos).getName().toString());

                // in case app was uninstalled, we want to update list
                if (intent == null) {
                    updateSupportedAppsList();
                } else {

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    LauncherActivity.this.startActivity(intent);

                    isCameFromLauncherActivityCode = true;
                }
            }
        });
    }

    public void showSettings(View v) {
        if (PureTrackSharedPreferences.isKnoxLicenceActivated()) {
            openSettingsApp();
            isCameFromLauncherActivityCode = true;
        } else {
            String titleMsg = getString(R.string.dialog_text_enter_the_password, DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber());
            SettingsPasswordDialog settingsPasswordDialog = new SettingsPasswordDialog(this);
            settingsPasswordDialog.createUnlockDialog(titleMsg, new IUnlockDialogCallbackListener() {
                @Override
                public void onTryUnlockWithCorrectPassword() {
                    openSettingsApp();
                }

                @Override
                public void onTryUnlockWithWrongPassword() {
                    Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onBackButtonPressed() {

                }
            });
        }
    }

    private void openSettingsApp() {
        Intent settingsIntent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
        startActivity(settingsIntent);
    }

    public void enterModeClick(View v) {
        prepareScreenToNewMode();
    }

    public void showShutdownDialog(View v) {

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.setContentView(R.layout.dialog_shutdown);

        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button buttonShutdown = dialog.findViewById(R.id.buttonShutdown);
        buttonShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventPowerOff, -1, -1, System.currentTimeMillis() +
                        TimeUnit.MINUTES.toMillis(1));
                TableOffenderDetailsManager.sharedInstance().updateColumnLong(OFFENDER_DETAILS_CONS.DETAILS_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI, 0);

                @SuppressLint("WrongConstant")
                EnterpriseDeviceManager edm = (EnterpriseDeviceManager) getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
                SecurityPolicy securityPolicy = edm.getSecurityPolicy();
                try {
                    securityPolicy.powerOffDevice();
                } catch (SecurityException e) {
                    Log.w(TAG, "SecurityException: " + e);
                }
                dialog.dismiss();
            }
        });

        dialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
                    Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    sendBroadcast(closeDialog);
                    return true;
                }

                return false;
            }
        });

        dialog.show();

    }

    private void prepareScreenToOfficerMode() {

        KnoxUtil.getInstance().enterOfficerMode(false);

        setDetailsToFooter(View.VISIBLE, View.VISIBLE, R.string.launcher_text_enter_offender_mode, R.drawable.top_bar_officer_mode);

        long officerModeTimeOut = TimeUnit.SECONDS.toMillis(TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.OFFICER_MODE_TIMEOUT));

        wakingUpReceiver.setAlaramClock(App.getContext(), System.currentTimeMillis() + officerModeTimeOut,
                WakingAlarmBroadcastReceiver.class, 25, LAUNCHER_ACTIVITY_WAKEUP_RECEIVER, LAUNCHER_ACTIVITY_WAKEUP_RECEIVER);
    }

    private void prepareScreenToOffenderMode() {

        KnoxUtil.getInstance().enterOffenderMode(false);

        stopRunningProccess();

        boolean isInInitiatedFlightMode = KnoxUtil.getInstance().isInInitializedOffenderFlightMode();
        if (isInInitiatedFlightMode) {
            setDetailsToFooter(View.VISIBLE, View.GONE, R.string.launcher_text_enter_officer_mode, R.drawable.top_bar);
        } else {
            setDetailsToFooter(View.GONE, View.GONE, R.string.launcher_text_enter_offender_mode, R.drawable.top_bar);
        }

        wakingUpReceiver.CancelAlarm(App.getContext(), WakingAlarmBroadcastReceiver.class, 25, LAUNCHER_ACTIVITY_WAKEUP_RECEIVER);
    }

    private void stopRunningProccess() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

        List<String> runningProcessNotAllowedToBeStoppedServerList = getUnAllowedProcessToGetKilledFromServer();
        for (int i = 0; i < runningAppProcessInfo.size(); i++) {
            if (!allowedDefaultPackagesList.contains(runningAppProcessInfo.get(i).processName) &&
                    !runningProccessNotAllowedToBeStoppedLocalList.contains(runningAppProcessInfo.get(i).processName) &&
                    !runningProcessNotAllowedToBeStoppedServerList.contains(runningAppProcessInfo.get(i).processName)) {
                KnoxUtil.getInstance().getKnoxSDKImplementation().stopApp(runningAppProcessInfo.get(i).processName);
            }
        }
    }

    private void setDetailsToFooter(int officerModeButtonVisability, int otherButtonsVisability, int officerModeRes, int topBarRes) {

        Button officerModeButton = activityAppsListFooter.findViewById(R.id.officerModeButton);
        officerModeButton.setVisibility(officerModeButtonVisability);
        officerModeButton.setText(getText(officerModeRes));

        Button showShutdownDialogButton = activityAppsListFooter.findViewById(R.id.showShutdownDialogButton);
        showShutdownDialogButton.setVisibility(otherButtonsVisability);

        View topBarLayout = findViewById(R.id.top_bar_layout);
        topBarLayout.setBackgroundResource(topBarRes);
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(launcherMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wakingUpReceiver);
        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onDestroy() - LauncherActivity ", false);
        String messageToUpload = "onDestroy() - LauncherActivity";
        Log.i(TAG, messageToUpload);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        super.onDestroy();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "LAUNCHER_ACTIVITY_RECEIVER" is broadcasted.
    private final BroadcastReceiver launcherMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                if (intent.getStringExtra(LAUNCHER_ACTIVITY_EXTRA).equals(MESSAGE_EXTRA)) {
                    updateAllowedPackagesListFromServer();
                    updateSupportedAppsList();
                } else if (intent.getStringExtra(LAUNCHER_ACTIVITY_EXTRA).equals(UPDATE_LAUNCHER_EXTRA)) {
                    prepareScreenToOffenderMode();
                } else if (intent.getStringExtra(LAUNCHER_ACTIVITY_EXTRA).equals(ENTER_OFFICER_MODE_CLICK_EXTRA)) {
                    prepareScreenToOfficerMode();
                } else if (intent.getStringExtra(LAUNCHER_ACTIVITY_EXTRA).equals(APPLICATION_LANGUAGE_CHANGED_CLICK_EXTRA)) {
                    handleLanguageChanged();
                }
            }
        }
    };

    private final BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null && intent.getStringExtra(WakingAlarmBroadcastReceiver.WAKING_ALARM_RECEIVER_CALLBACK_EXTRA_NAME)
                    .equals(LAUNCHER_ACTIVITY_WAKEUP_RECEIVER)) {
                prepareScreenToOffenderMode();
            }
        }
    };

    @Override
    public void onBackPressed() {

    }
}
