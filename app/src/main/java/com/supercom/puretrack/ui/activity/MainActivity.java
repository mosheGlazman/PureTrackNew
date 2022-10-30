package com.supercom.puretrack.ui.activity;

import static com.supercom.puretrack.data.source.local.table.TableScannerType.NORMAL_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MAC_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MANUFACTURER_ID;
import static com.supercom.puretrack.ui.dialog.PhotoOnDemandDialog.REQUEST_ID_KEY;
import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;
import static com.supercom.puretrack.util.general.RootUtil.isDeviceRooted;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.DetectedActivity;
import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.broadcast_receiver.NewDayStartReceiver;
import com.supercom.puretrack.data.broadcast_receiver.UpdateUIReceiver;
import com.supercom.puretrack.data.broadcast_receiver.WakingAlarmBroadcastReceiver;
import com.supercom.puretrack.data.repositories.KnoxProfileConfig;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.repositories.OffenderRequestsRepository;
import com.supercom.puretrack.data.service.LocationService;
import com.supercom.puretrack.data.service.heart_beat.HeartBeatServiceJava2;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.BiometricManager;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.BiometricScheduleManager;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.BiometricScheduleManager.BiometricScheduleManagerListener;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager.ChargingState;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager.DEVICE_BATTERY_CONS;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager.DeviceStateListener;
import com.supercom.puretrack.data.source.local.local_managers.hardware.AccelerometerManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager.BluetoothManagerListener;
import com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.DeviceShieldingManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LightSensorManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager.LocationHandlerListener;
import com.supercom.puretrack.data.source.local.local_managers.hardware.MagneticManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.SensorDataSource;
import com.supercom.puretrack.data.source.local.local_managers.hardware.TemperatureManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableDeviceDetails;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventTypes;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventsAlarmsType;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.Restrictions;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table_managers.TableApnDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableCallLogManager;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableDeviceInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager.DBEventsManagerListener;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager;
import com.supercom.puretrack.data.source.local.table_managers.TableScheduleOfZones;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager.DBZonesManagerListener;
import com.supercom.puretrack.data.source.remote.DownloadTaskMain;
import com.supercom.puretrack.data.source.remote.DownloadTaskMain.DownloadTaskListener;
import com.supercom.puretrack.data.source.remote.DownloadTaskMain.Download_Task_Type;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.BeaconModel;
import com.supercom.puretrack.model.business_logic_models.bluetooth_parsing.TagModel;
import com.supercom.puretrack.model.business_logic_models.enums.ServerMessageType;
import com.supercom.puretrack.model.business_logic_models.network.communication_profile.ProfilingEventsConfig.PmComProfiles;
import com.supercom.puretrack.model.business_logic_models.network.communication_profile.ProfilingEventsConfig.ProfileEvents;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.FlowType;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.OffenderRequestType;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;
import com.supercom.puretrack.model.database.entities.EntityDeviceDetails;
import com.supercom.puretrack.model.database.entities.EntityEventConfig;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.model.database.entities.EntityOpenEventLog;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.model.database.entities.EntityTextMessage;
import com.supercom.puretrack.model.database.entities.EntityZones;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.objects.VoipSettings;
import com.supercom.puretrack.model.ui_models.Message;
import com.supercom.puretrack.model.ui_models.ScreenType;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.BeaconFutureUIRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.BeaconLowRssiRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.BeaconNoAdvertiseRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.MaxProfileDurationTimeRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.MessageReceiveRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.MinProfileDurationTimeRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.TagFutureUIRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.TagLowRssiRunnable;
import com.supercom.puretrack.ui.activity.MainActivity.NextFutureTask.TagNoAdvertiseRunnable;
import com.supercom.puretrack.ui.adapter.SchedulePagerAdapter;
import com.supercom.puretrack.ui.dialog.AcknowledgeWIthButtonDialog;
import com.supercom.puretrack.ui.dialog.MessageDialog;
import com.supercom.puretrack.ui.dialog.MessageDialog.MessageDialogListener;
import com.supercom.puretrack.ui.dialog.PhotoOnDemandDialog;
import com.supercom.puretrack.ui.dialog.SettingsDialog;
import com.supercom.puretrack.ui.dialog.SettingsPasswordDialog;
import com.supercom.puretrack.ui.dialog.UnallocateDialogManager;
import com.supercom.puretrack.ui.enrollment.activity.EnrollmentActivity;
import com.supercom.puretrack.ui.enrollment.fragments.OffenderFingerPrintFragment;
import com.supercom.puretrack.ui.lockscreen.LockScreenActivity;
import com.supercom.puretrack.ui.schedule.ScheduleTab;
import com.supercom.puretrack.ui.views.MessagesAdapterNew;
import com.supercom.puretrack.ui.views.SlidingTabLayout;
import com.supercom.puretrack.ui.views.ToolbarViewsDataManager;
import com.supercom.puretrack.ui.views.toolbar.BatteryView;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.dialer.DialerUtils;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.encryption.ScramblingTextUtils;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.KnoxUtil.KnoxUtilityListener;
import com.supercom.puretrack.util.general.LocaleUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.general.LoggingUtil.HardwareTypeString;
import com.supercom.puretrack.util.general.LoggingUtil.OperationType;
import com.supercom.puretrack.util.general.NumberComputationUtil;
import com.supercom.puretrack.util.hardware.AppsSharedDataManager;
import com.supercom.puretrack.util.hardware.FilesManager;
import com.supercom.puretrack.util.hardware.VoiceManager;
import com.supercom.puretrack.util.runnable.BaseFutureRunnable;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.io.File;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@TargetApi(23)
public class MainActivity extends BaseActivity implements android.view.View.OnClickListener, BluetoothManagerListener {

    public static final long TIME_TO_RESET_BLE = 50000;
    public static final long TIME_FOREGROUND_BLE_SERVICE = 30000;
    public static final int NO_RESULT_STATUS = -1;
    public static final int DELTA_TIME_TO_RESTART_BLE = 5000;
    public static final int MESSAGE_SCREEN_USER_MIN_CHARS = 0;
    public static final int MESSAGE_SCREEN_USER_MAX_CHARS = 140;
    public final static int DRAW_OVERLAY_PERMISSION_CODE = 6734;
    public final static int DRAW_WRITE_SETTINGS_PERMISSION_CODE = 6735;
    public static final int MESSAGE_SCREEN_ERROR_MSG_CYCLE = 1000;
    public static final int MESSAGE_SCREEN_ERROR_MSG_TIMEOUT = 2000;
    public static final String TAG = "MainActivity";
    public static final String MAIN_RECEIVER_EXTRA = "main_receiver";
    public static final String UPDATE_UI_RECEIVER_EXTRA = "update_ui_receiver";
    public static final String NEW_DAY_START_RECEIVER_EXTRA = "new_day_start_receiver";
    public static final String SHOULD_CREATE_STARTUP_EVENT = "should_create_startup_event";
    public static final String UPGRADE_TO_LATEST_VERSION_EXTRA = "UPGRADE_TO_LATEST_VERSION_EXTRA";
    public static final String IS_APPLICATION_STARTED_BY_WATCHDOG = "is_application_started_by_watchdog";
    private static final int ENROLMENT_EXTRA_CODE = 12345;
    private static final int PROXIMITY_AND_VIBRATE_TIME = 15;
    public static boolean playBleRxTone = true;
    public static boolean appInBackground = false;
    public static boolean playLocationRxTone = false;
    public static boolean playLocationDBTone = false;
    public static boolean isOffenderInSuspendSchedule = false;
    public static long lastBleTagRx = 0;
    public static long MojScreenTurnOn = 0;
    public static long ScreenOnBleTime = 0;
    public static long lastBleTagRxDebug = 0;
    public static long commLastCycleTime = 0;
    public static long lastTagReceivedTime = 0;
    public static long appInBackgroundTime = 0;
    public static long LongLastPureBeaconPacketRx;
    public static long commIntervalTimeFromDB = 60;
    public static BluetoothManager bluetoothManager;
    //Variables - Arrays
    public static String[] monthArray = new String[12];
    public static String[] dayOfWeekArray = new String[7];
    private final int PACKET_UI_TIME_OUT = 10000;
    private final Timer mainTimer = new Timer();
    private final Handler upgradeTimeoutHandler = new Handler();
    private final WakingAlarmBroadcastReceiver wakingAlarmBroadcastReceiver = new WakingAlarmBroadcastReceiver();

    //Variables - lists
    private final List<Dialog> alertDialogArray = new ArrayList<>();
    private final Runnable upgradeTimeoutScreenRunnable = new Runnable() {

        @Override
        public void run() {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.softwareUpgradeTimeOut, -1, -1);
        }
    };
    //Variables - Objects
    public Locale myLocale;
    public Handler futureTasksHandler = new Handler();
    long LongLastPureTagPacketRx = System.currentTimeMillis();
    //Variables - booleans
    private boolean FlightModeEnabled = false;
    private boolean booleanPacketToast = false;
    private boolean isTagInProximityMode = false;
    private boolean booleanBeaconPacketToast = false;
    private boolean previousIsSuspendedValue = false;
    private boolean isUpgradeToNewVersionScreenShouldOpen = true;
    //Variables - long
    private long lastAdvertiseTag;
    //Variables - int
    private int tagRssi;
    private int screenCnt = 0;
    //Variables - String
    private String stringPacketBeacon;
    private String stringPacketBroadcast;
    private ViewPager mPager;
    private Button mapButton;
    private Button callButton;
    private Button backButton;
    private Sensor magneticSensor;
    private Button buttonYourOfficer;
    private Button buttonAgencyCenter;
    private Sensor mTemperatureSensor;
    private View bottomButtonsContainer;
    private Sensor mAccelerometerSensor;
    private SensorManager sensorManager;
    private Button pendingEnrolmentButton;
    private TextView textViewBeaconPacket;
    private LocationManager locationManager;
    private UpdateUIReceiver updateUIReceiver;
    private TableCallLogManager callLogManager;
    private TextView bluetoothConnectionTextView;
    private NewDayStartReceiver newDayStartReceiver;
    private NotificationManager notificationManager;
    private SchedulePagerAdapter schedulePagerAdapter;
    private ScreenType currentScreen = ScreenType.Home;
    private UnallocateDialogManager unallocateDialogManager;
    private BiometricScheduleManager biometricScheduleManager;
    //Dependency Injection
    private TemperatureManager temperatureManager;
    //Variables - Home Screen objects
    private ImageView buttonHome;
    private TextView textViewDate;
    private TextView textViewNext;
    private TextView textViewCanGo;
    private TextView textViewMustStay;
    private TextView textViewCurrently;
    private ImageView batteryStatus_iv;
    private BatteryView batteryView;
    private static boolean afterStartup=true;
    private Date lastLocationSendToServerCheckDate;
    private final BroadcastReceiver localMainActivityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String receiveExtra = intent.getStringExtra(MainActivity.MAIN_RECEIVER_EXTRA);
            switch (receiveExtra) {
                case MainActivity.NEW_DAY_START_RECEIVER_EXTRA:
                    textViewDate.setText(dayOfWeekArray[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1] + ", " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " "
                            + monthArray[Calendar.getInstance().get(Calendar.MONTH)]);

                    String messageToUpload = "NEW_DAY_START_RECEIVER_EXTRA";
                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                            DebugInfoModuleId.Receivers.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                    break;
                case MainActivity.UPDATE_UI_RECEIVER_EXTRA:
                    updateHomeScreenUI();
                    break;
                case EnrollmentActivity.ENROLMENT_DEVICES:
                    TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS,
                            OffenderActivation.OFFENDER_STATUS_PENDING_ACTIVATION_ENROLLMENT);

                    updateHomeScreenUI();

                    NetworkRepository.getInstance().startNewCycle();
                    break;
            }
        }
    };
    private final BroadcastReceiver scanBleDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            scanLeDeviceNew();
        }
    };

    //Variables -  Messages Screen objects
    private EditText etOffenderNewMsg;
    //Runnable and handlers
    private TagFutureUIRunnable tagFutureTaskManager;
    private TagLowRssiRunnable tagLowRssiTaskManager;
    private BeaconFutureUIRunnable beaconFutureTaskManager;
    private BeaconLowRssiRunnable beaconLowRssiTaskManager;
    private TagNoAdvertiseRunnable tagNoAdvertiseTaskManager;
    private final BroadcastReceiver wakeupReceiverReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null && intent.getStringExtra(WakingAlarmBroadcastReceiver.WAKING_ALARM_RECEIVER_CALLBACK_EXTRA_NAME)
                    .equals(MainActivity.class.getCanonicalName())) {

                KnoxUtil.getInstance().handleDeviceWasInInitiatedFlightModeIfNeeded();

                LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(wakeupReceiverReceiver);

                TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_TIME_INITIATED_FLIGHT_MODE_END, 0);

                startTagActivitiesIfNeeded();

                // enable "call" button
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_main_1), null, null);

                FlightModeEnabled = false;
            }
        }
    };
    private MessageReceiveRunnable messageReceiveFutureTaskManager;
    private BeaconNoAdvertiseRunnable beaconNoAdvertiseTaskManager;
    private MinProfileDurationTimeRunnable minProfileDurationTimeFutureManager;
    private MaxProfileDurationTimeRunnable maxProfileDurationTimeFutureManager;

    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleLanguageByConfig();

        if (DeviceShieldingManager.getInstance().isEnabled) {
            DeviceShieldingManager.getInstance().enableShielding();
        }

        setFullScreen();
        CheckPermissionActivity.startIfRequired(getApplicationContext());

        //deviceShieldingManager.startDeviceShieldingIfEnabled();
        if (TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS) < System.currentTimeMillis() && App.IS_PINCODE_TYPED != true) {
            LockScreenActivity.start(this);
        }

        int debugInfoConf = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_DEBUG_INFO_CONFIG);
        TableOffenderDetailsManager.sharedInstance().setDebugInfoConfig(debugInfoConf);

        //getActionBar().hide();


        checkDrawOverlayAndWritePermission();


        registerReceiver(scanBleDeviceReceiver, new IntentFilter(HeartBeatServiceJava2.ACTION_ScanDevice));

        try {
            String strLan = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
            setLocale(strLan);
        } catch (Exception lanExp) {
            System.out.println(lanExp);
        }
        init();
        Intent locationService = new Intent(App.getAppContext(), LocationService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(locationService);
        } else {
            startService(locationService);
        }

        if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1) {
            commIntervalTimeFromDB = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL);
            commLastCycleTime = System.currentTimeMillis();
        }


        MojScreenTurnOn = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TURN_ON_SCREEN_MOTION);

        startMainActivityObservers();

        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        OffenderFingerPrintFragment.enterOffenderModeIfNeededX();

    }


    private void startMainActivityObservers() {
        mainTimer.scheduleAtFixedRate(new mainTask(), 0, 1000);
    }

    private void handleLanguageByConfig() {
        String appLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE);
        if (!appLanguage.isEmpty()) return;

        String languageByBuild = KnoxProfileConfig.getInstance().getDefaultLanguage();
        TableOffenderDetailsManager.sharedInstance().updateColumnString(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.APP_LANGUAGE, languageByBuild);
        new LocaleUtil().changeApplicationLanguageIfNeeded();
    }

    public void registerAccelerometerListenerIfSupported() {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        AccelerometerManager.getInstance().setActivityListener(new SensorInterface());

        AccelerometerManager.getInstance().updateAccelerometerConfig(TableOffenderDetailsManager.sharedInstance().getAccelerometerSettings());

        mAccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(AccelerometerManager.getInstance(), mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * check if we already  have permission to draw over other apps
     */
    public boolean checkDrawOverlayAndWritePermission() {
        // check if we already  have permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            // if not construct intent to request permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            // request permission via start activity for result
            startActivityForResult(intent, DRAW_OVERLAY_PERMISSION_CODE);
            return false;
        }

        if (!Settings.System.canWrite(this)) {
            // if not construct intent to request permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            // request permission via start activity for result
            startActivityForResult(intent, DRAW_WRITE_SETTINGS_PERMISSION_CODE);
            return false;
        }

        return true;
    }


    @Override
    protected void init() {
        super.init();

        if (isDeviceRooted())
            TableEventsManager.sharedInstance().addEventToLogRetID(TableEventConfig.EventTypes.eventSecureBootIssue, -1, -1, "rooted");
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (!TableOffenderStatusManager.sharedInstance().getIsOffenderInRange()) {
            boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY) != -1;
            handleUserNotInProximityAnymore(hasOpenEvent);
        }

        App.setAppContext(this);

        TableZonesManager.sharedInstance().setZonesManagerListener(new DBZonesManagerListener() {
            @Override
            public void onLeftBeaconZone() {
                locationManager.startLocationUpdate(false);
                NetworkRepository.getInstance().scheduleNewCycleIfNeeded(false);
            }

            @Override
            public void onEnteredBeaconZone() {
                locationManager.startLocationUpdate(false);
                NetworkRepository.getInstance().scheduleNewCycleIfNeeded(false);
            }
        });
        TableEventsManager.sharedInstance().setEventsManagerListener(new DBEventsManagerListener() {
            @Override
            public void onEventCreated(EntityEventConfig recordEventConfig, int zoneId) {

                boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                        == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
                if (isOffenderAllocated) {
                    handleProfileModeIfNeeded(recordEventConfig, zoneId);
                }

                if (recordEventConfig.EventType != EventTypes.eventProximityOpen) {
                    manageEventSoundAndVibrate(recordEventConfig.EventType);
                }


            }
        });
        DeviceStateManager.getInstance().setDeviceStateListener(new DeviceStateListener() {
            @Override
            public void onBatteryPercentageChanged(int percentage) {
                updateUiBatteryStatusIfSupported();

            }

            public void onBatteryStatusChanged(int newStatus) {
                boolean isDBBatteryUiEnabled = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                        (OFFENDER_DETAILS_CONS.OFFENDER_IS_BATTERY_INDICATION_ENABLED) == 1;
                if (newStatus == DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS && isDBBatteryUiEnabled) {
                    new AcknowledgeWIthButtonDialog(MainActivity.this, R.drawable.ico_bat_low, R.string.dialog_ack_battery_title, R.string.dialog_ack_battery_please_charge).show();
                }
            }

            @Override
            public void onBatteryChargingStatusChanged(boolean isCharging) {

            }
        });
        NetworkRepository.getInstance().setActivityListener(new NetworkViewModel());

        new LocaleUtil().changeApplicationLanguageIfNeeded();

        isOffenderInSuspendSchedule = DatabaseAccess.getInstance().tableScheduleOfZones.getIsOffenderInSuspendedSchedule(9);
        startSuspendedScheduleTimerCheck();


        String startup = "\n---------  startup: " + TimeUtil.getCurrentTimeStr() + "  ---------" + App.getDeviceInfo() + "\n New password: "
                + NumberComputationUtil.getRandomPassword();
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), startup,
                DebugInfoModuleId.Application_Info.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        if (!tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
            lastTagReceivedTime = System.currentTimeMillis();
            lastBleTagRx = System.currentTimeMillis();
        } else {
            // no tag, no need to turn on screen for BLE activation
            lastTagReceivedTime = 0;
            lastBleTagRx = 0;
        }
        ScreenOnBleTime = 0;

        changeWriteToLogsSettingsIfNeeded();

        LoggingUtil.createNetworkLog();

        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onCreate() - MainActivity " + "\n" + DatabaseAccess.getInstance().getDbStates().toString(), false);
        String messageToUpload = "onCreate() - MainActivity";
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), DatabaseAccess.getInstance().getDbStates().toString(),
                DebugInfoModuleId.DB.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        LoggingUtil.createLogZonesFiles();
        LoggingUtil.createBleLogsToFile();
        LoggingUtil.createActivityLogsToFile();
        LoggingUtil.createWhiteListConfigFile();


        NextFutureTask nextFutureTask = new NextFutureTask();
        tagFutureTaskManager = nextFutureTask.new TagFutureUIRunnable();
        tagNoAdvertiseTaskManager = nextFutureTask.new TagNoAdvertiseRunnable();
        tagLowRssiTaskManager = nextFutureTask.new TagLowRssiRunnable();
        beaconFutureTaskManager = nextFutureTask.new BeaconFutureUIRunnable();
        beaconNoAdvertiseTaskManager = nextFutureTask.new BeaconNoAdvertiseRunnable();
        beaconLowRssiTaskManager = nextFutureTask.new BeaconLowRssiRunnable();
        messageReceiveFutureTaskManager = nextFutureTask.new MessageReceiveRunnable();
        minProfileDurationTimeFutureManager = nextFutureTask.new MinProfileDurationTimeRunnable();
        maxProfileDurationTimeFutureManager = nextFutureTask.new MaxProfileDurationTimeRunnable();

        //DeviceStateManager.getInstance().checkForSuddenShutDown();

        DeviceStateManager.getInstance().registerBatteryAndUsbChanges();

        TableApnDetailsManager.sharedInstance().createApnIfNotExists();

        boolean isApplicationStartedByWatchdog = getIntent().getBooleanExtra(IS_APPLICATION_STARTED_BY_WATCHDOG, false);
        if (isApplicationStartedByWatchdog) {
            LoggingUtil.fileLogZonesUpdate("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "] PowerOnAfterSuddenShutDown - sent since watchdog");
            String messageToUpload2 = "PowerOnAfterSuddenShutDown - sent since watchdog";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload2,
                    DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }

        boolean shouldCreateStartupEvent = getIntent().getBooleanExtra(SHOULD_CREATE_STARTUP_EVENT, false);
        if (shouldCreateStartupEvent) {
            TableEventsManager.sharedInstance().addDeviceSatrtupEventToLogIfNeed();
        }

        // Mobile Sensors
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        registerAccelerometerListenerIfSupported();

        // MagneticField
        registerMagneticListener();
        //TemperatureField
        registerTemperatureListener();

        // Location
        locationManager = new LocationManager(this, new LocationHandlerListener() {
            @Override
            public void onNewPointAddedToDB() {
                updateHomeScreenUI();
            }
        });

        BiometricManager.getInstance().setListener(new BiometricManager.BiometricManagerListener() {
            @Override
            public void onOpenDialog() {

            }

            @Override
            public void onCloseDialog() {
                notificationManager.cancel(0);
            }

            //@Override
            //public void onAuthenticateDialogRequired() {
              //  defineNotification(1);
              //  fingerPrintManager.createBiometricDialog(MainActivity.this);
              //  setActivityToForegroundIfNeeded();
            //}
        });


        unallocateDialogManager = new UnallocateDialogManager(this);

        DeviceStateManager.getInstance().validateIfSimCardIsReady();
        DeviceStateManager.getInstance().enableMobileDataIfOff();

        startBluetoothWithOutScreen();

        VoiceManager.getInstance(getApplicationContext()).init();
        callLogManager = new TableCallLogManager(this);

        newDayStartReceiver = new NewDayStartReceiver();
        newDayStartReceiver.startAlarmManagerReceiver(System.currentTimeMillis(), 0, 0, 0, 1, NewDayStartReceiver.class, 7);

        updateUIReceiver = new UpdateUIReceiver();

        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isOffenderAllocated) {
            updateUIReceiver.registerForNextClosestTimeAppointment();
        }

        biometricScheduleManager = new BiometricScheduleManager(new BiometricScheduleManagerListener() {
            @Override
            public void createBiometricDialog() {
                BiometricManager.getInstance().startAuthenticate(MainActivity.this);
            }
        });
        biometricScheduleManager.handleBiometricScheudleIfExists();

        int currentPmComProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);
        startPmComProfileByProfileID(currentPmComProfile);

        EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
        if (offenderLastGpsPoint != null) {
            TableZonesManager.sharedInstance().checkZoneIntersection(offenderLastGpsPoint);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(localMainActivityReceiver, new IntentFilter(MAIN_RECEIVER_EXTRA));
        NetworkRepository.getInstance().registerIdleModeReceiver();
        NetworkRepository.getInstance().scheduleNewCycleIfNeeded(true);

        TableDebugInfoManager.sharedInstance().startCyclicRowCounterLogRunnable();

        TableDeviceInfoManager.sharedInstance().deleteAllRecordsInTableDeviceInfoDetails();
        TableDeviceInfoManager.sharedInstance().saveDeviceDetailsToDB();

        long initiatedFlightModeEnd = TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(OFFENDER_STATUS_CONS.OFF_TIME_INITIATED_FLIGHT_MODE_END);
        initInitiatedFlightModeAlarmIfNeeded(initiatedFlightModeEnd);
        initScreens();
    }

    private void startSuspendedScheduleTimerCheck() {

        new CountDownTimer(20000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                checkForSuspendSchedule();
                start();
            }
        }.start();
    }

    private void checkForSuspendSchedule() {
        boolean hasSuspendSchedule = DatabaseAccess.getInstance().tableScheduleOfZones.getIsOffenderInSuspendedSchedule(9);
        boolean didGetManualMonitoringSuspendRequest = OffenderRequestsRepository.getInstance().isManualMonitoringSuspendedFromRequest;
        isOffenderInSuspendSchedule = didGetManualMonitoringSuspendRequest || hasSuspendSchedule;
        if (isOffenderInSuspendSchedule) {
            handleMainScreenViewsForSuspendedSchedule(View.INVISIBLE);
            showSuspendedScheduleUI();
        } else {
            handleMainScreenViewsForSuspendedSchedule(View.VISIBLE);
            if (previousIsSuspendedValue) {
                //Now there is no suspend appointment, but last time there was.
                //This is important because we want to activate the Bluetooth only once when the suspend schedule finished.
                startTagActivitiesIfNeeded();
            }
        }
        previousIsSuspendedValue = isOffenderInSuspendSchedule;
    }

    private void showSuspendedScheduleUI() {
        textViewMustStay.setText(R.string.monitoring_suspended);
        textViewMustStay.setTextColor(getResources().getColor(R.color.grey));
        tagLowRssiTaskManager.removeNextCallback();
        tagNoAdvertiseTaskManager.removeNextCallback();
        try {
            Glide.with(MainActivity.this).load(R.drawable.icon_offender_suspended).into(buttonHome);
        } catch (Exception ignored) {
        }
    }

    private void handleMainScreenViewsForSuspendedSchedule(int visibility) {
        textViewDate.setVisibility(visibility);
        textViewCurrently.setVisibility(visibility);
        textViewNext.setVisibility(visibility);
        textViewCanGo.setVisibility(visibility);
        updateHomeScreenUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initScreen(ScreenType current_Screen) {
        this.currentScreen = current_Screen;

        String mapUrl = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_MAP_URL);

        Button messagesButton;
        Button scheduleButton;

        if (current_Screen == ScreenType.Home) {
            setContentView(R.layout.activity_main);

            dayOfWeekArray = getResources().getStringArray(R.array.day_of_week_array);
            monthArray = getResources().getStringArray(R.array.month_array);
            textViewDate = findViewById(R.id.TextViewDate);
            textViewDate.setText(new StringBuilder().append(dayOfWeekArray[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]).append(", ").append(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).append(" ").append(monthArray[Calendar.getInstance().get(Calendar.MONTH)]).toString());
            textViewCurrently = findViewById(R.id.TextViewCurrently);
            textViewMustStay = findViewById(R.id.TextViewMustStay);
            textViewCanGo = findViewById(R.id.TextViewCanGo);
            bluetoothConnectionTextView = findViewById(R.id.bluetooth_connection_text_view);

            textViewBeaconPacket = findViewById(R.id.TextViewBeacon);
            textViewNext = findViewById(R.id.TextViewNext);

            callButton = findViewById(R.id.ButtonAppaCall);
            mapButton = findViewById(R.id.ButtonAppaMapImg);
            messagesButton = findViewById(R.id.ButtonAppaMessages);
            scheduleButton = findViewById(R.id.ButtonAppaSchedule);
            backButton = findViewById(R.id.ButtonAppaBack);
            buttonHome = findViewById(R.id.ButtonHome);
            pendingEnrolmentButton = findViewById(R.id.pendingEnrolmentButton);
            batteryStatus_iv = findViewById(R.id.batteryStatus_iv);
            bottomButtonsContainer = findViewById(R.id.container);

            findViewById(R.id.tv_log).setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    SensorDataSource.getInstance().clear();
                    return false;
                }
            });

            callButton.setOnClickListener(this);
            messagesButton.setOnClickListener(this);
            scheduleButton.setOnClickListener(this);
            mapButton.setOnClickListener(this);
            backButton.setOnClickListener(this);
            buttonHome.setOnClickListener(this);
            pendingEnrolmentButton.setOnClickListener(this);

            backButton.setVisibility(View.INVISIBLE);

            updateHomeScreenUI();

            // enable calling if NOT in flight mode
            if (FlightModeEnabled) {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
            } else {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_main_1), null, null);
            }

            checkForSuspendSchedule();


        } else if (current_Screen == ScreenType.Call) {
            setContentView(R.layout.call_screen);
            backButton.setVisibility(View.VISIBLE);
            buttonYourOfficer = findViewById(R.id.ButtonYourOfficer);
            buttonAgencyCenter = findViewById(R.id.ButtonAgencyCenter);
            buttonYourOfficer.setOnClickListener(this);
            buttonAgencyCenter.setOnClickListener(this);
            callButton = findViewById(R.id.ButtonAppaCall);
            messagesButton = findViewById(R.id.ButtonAppaMessages);
            scheduleButton = findViewById(R.id.ButtonAppaSchedule);
            backButton = findViewById(R.id.ButtonAppaBack);
            mapButton = findViewById(R.id.ButtonAppaMapImg);
            callButton.setOnClickListener(this);
            messagesButton.setOnClickListener(this);
            scheduleButton.setOnClickListener(this);
            backButton.setOnClickListener(this);
            mapButton.setOnClickListener(this);
            addEmergencyButtonIfNeeded();

        } else if (current_Screen == ScreenType.Messages) {
            setContentView(R.layout.message_screen_keyboard);


            backButton.setVisibility(View.VISIBLE);
            callButton = findViewById(R.id.ButtonAppaCall);
            messagesButton = findViewById(R.id.ButtonAppaMessages);
            scheduleButton = findViewById(R.id.ButtonAppaSchedule);
            backButton = findViewById(R.id.ButtonAppaBack);
            mapButton = findViewById(R.id.ButtonAppaMapImg);
            View btnSendMsg = findViewById(R.id.btnSendMsg);
            LinearLayout sendMessageLinearLayout = findViewById(R.id.loTexet);
            TextView textViewMessagesAndNoti = findViewById(R.id.TextViewMessages);
            // load listener
            callButton.setOnClickListener(this);
            mapButton.setOnClickListener(this);
            messagesButton.setOnClickListener(this);
            scheduleButton.setOnClickListener(this);
            backButton.setOnClickListener(this);

            if (!TableOffenderDetailsManager.sharedInstance().isOffenderMessageResponseEnabled()) {
                sendMessageLinearLayout.setVisibility(View.INVISIBLE);
            } else {
                sendMessageLinearLayout.setVisibility(View.VISIBLE);
            }

            int dataBaseMessageCnt = DatabaseAccess.getInstance().TableRecordCount(EnumDatabaseTables.TABLE_TEXT_MSG);
            textViewMessagesAndNoti.setText(R.string.messages_button_messages_notifications);
            etOffenderNewMsg = findViewById(R.id.etOffndNewMsg);

            etOffenderNewMsg.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                @TargetApi(26)
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    if (menu != null) {
                        menu.removeItem(android.R.id.shareText);
                        menu.removeItem(android.R.id.selectAll);
                        menu.removeItem(android.R.id.paste);
                        menu.removeItem(android.R.id.addToDictionary);
                        menu.removeItem(android.R.id.autofill);
                    }
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });

            if (dataBaseMessageCnt > 0) {
                int numOfMsg = DatabaseAccess.getInstance().tableMessages.GetMsgCount();
                if (numOfMsg > 0) {
                    ArrayList<Message> arrayOfMessages = new ArrayList<>();
                    for (int i = 1; i <= numOfMsg; i++) {
                        arrayOfMessages.add(new Message(DatabaseAccess.getInstance().tableMessages.GetMsg(i)));
                    }
                    MessagesAdapterNew adapter = new MessagesAdapterNew(this, arrayOfMessages);
                    ListView listView = findViewById(R.id.listview);
                    listView.setAdapter(adapter);
                    listView.setSelection(arrayOfMessages.size()-1);
                }
            }
            btnSendMsg.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    int msgLength = etOffenderNewMsg.getText().length();
                    if ((msgLength > MESSAGE_SCREEN_USER_MIN_CHARS) && (msgLength <= MESSAGE_SCREEN_USER_MAX_CHARS)) {
                        etOffenderNewMsg.setBackgroundColor(Color.TRANSPARENT);
                        String message = etOffenderNewMsg.getText().toString();
                        // effected row id will be the ID in messages
                        // this is used for visual confirm V the message
                        int msgID = TableEventsManager.sharedInstance().addEventToLogRetID(TableEventConfig.EventTypes.eventNewOffenderMessage, -1, -1, message);
                        EntityTextMessage recMsg = new EntityTextMessage(
                                1,
                                System.currentTimeMillis(),
                                DateFormatterUtil.getCurDateStr(DateFormatterUtil.HM),
                                "Offender",
                                message,
                                msgID);
                        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_TEXT_MSG, recMsg);
                        etOffenderNewMsg.setText("");
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        initScreen(ScreenType.Messages);

                    } else if (msgLength > MESSAGE_SCREEN_USER_MAX_CHARS) {
                        final Drawable drawable = etOffenderNewMsg.getBackground();
                        drawable.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
                        etOffenderNewMsg.setBackgroundColor(Color.RED);
                        new CountDownTimer(MESSAGE_SCREEN_ERROR_MSG_TIMEOUT, MESSAGE_SCREEN_ERROR_MSG_CYCLE) {
                            @Override
                            public void onTick(long arg0) {
                            }

                            @Override
                            public void onFinish() {
                                etOffenderNewMsg.setBackground(drawable);
                            }
                        }.start();
                    }
                }
            });

            // enable calling if NOT in flight mode
            if (FlightModeEnabled) {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
            } else {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_main_1), null, null);
            }
        } else if (currentScreen == ScreenType.Schedules) {
            setContentView(R.layout.schedule_screen);
            // Load buttons
            callButton = findViewById(R.id.ButtonAppaCall);
            messagesButton = findViewById(R.id.ButtonAppaMessages);
            scheduleButton = findViewById(R.id.ButtonAppaSchedule);
            backButton = findViewById(R.id.ButtonAppaBack);
            mapButton = findViewById(R.id.ButtonAppaMapImg);
            // Buttons listener
            callButton.setOnClickListener(this);
            messagesButton.setOnClickListener(this);
            scheduleButton.setOnClickListener(this);
            backButton.setOnClickListener(this);
            mapButton.setOnClickListener(this);

            schedulePagerAdapter = new SchedulePagerAdapter(MainActivity.this.getSupportFragmentManager(), getScheduleDays());
            mPager = findViewById(R.id.pager);
            mPager.setAdapter(schedulePagerAdapter);
            mPager.setCurrentItem(1);

            SlidingTabLayout slidingTabLayout = findViewById(R.id.sliding_tabs);
            slidingTabLayout.setDistributeEvenly(true);
            slidingTabLayout.setViewPager(mPager);

            // enable calling if NOT in flight mode
            if (FlightModeEnabled) {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
            } else {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_main_1), null, null);
            }
        } else if (currentScreen == ScreenType.Map) {
            String appLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
            setLocale(appLanguage);
            setContentView(R.layout.map_screen);
            currentScreen = ScreenType.Map;
            callButton = findViewById(R.id.ButtonAppaCall);
            messagesButton = findViewById(R.id.ButtonAppaMessages);
            scheduleButton = findViewById(R.id.ButtonAppaSchedule);
            backButton = findViewById(R.id.ButtonAppaBack);
            mapButton = findViewById(R.id.ButtonAppaMapImg);

            WebView mapWebView = findViewById(R.id.webView);
            mapWebView.getSettings().setJavaScriptEnabled(true);

            mapWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed(); // Ignore SSL certificate errors
                }
            });

            String OffID = String.valueOf(TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_ID));//DBOffenderDetailsManager.sharedInstance().getStringValByColumnName(DBOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_ID);
            String urlString = mapUrl + OffID;

            if (mapUrl.isEmpty()) {
                mapButton.setVisibility(View.GONE);
            }

            if (urlString.length() > 10) {
                mapWebView.loadUrl(urlString);
            }
            callButton.setOnClickListener(this);
            messagesButton.setOnClickListener(this);
            scheduleButton.setOnClickListener(this);
            backButton.setOnClickListener(this);
            mapButton.setOnClickListener(this);

            // enable calling if NOT in flight mode
            if (FlightModeEnabled) {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
            } else {
                callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_main_1), null, null);
            }
        }

        if (mapUrl.isEmpty()) {
            mapButton.setVisibility(View.GONE);
        }

        if (isOffenderInSuspendSchedule) {
            showSuspendedScheduleUI();
        }

        batteryView = (BatteryView)findViewById(R.id.batteryView);
    }

    private void updateUiBatteryStatusIfSupported() {
        boolean isDBBatteryUiEnabled = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                (OFFENDER_DETAILS_CONS.OFFENDER_IS_BATTERY_INDICATION_ENABLED) == 1;
        if (isDBBatteryUiEnabled) {
            updateUiBatteryStatus();
        }
    }

    private void updateUiBatteryStatus() {
        updateBatteryStatusImageIfNeeded();

        int curPluggedStatus = App.getContext().registerReceiver
                (null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        int batteryStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT);
        //Plugged to USB charger
        if (curPluggedStatus == BatteryManager.BATTERY_PLUGGED_AC || curPluggedStatus == BatteryManager.BATTERY_PLUGGED_USB) {
            //Open OnCharge dialog only once, so open it if the Previous state was: Not charging
            if (DeviceStateManager.getInstance().getPrevWasChargingState() == ChargingState.NOT_CHARGING
                    || DeviceStateManager.getInstance().getPrevWasChargingState() == ChargingState.UNKNOWN) {
                if (batteryStatus < DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS)//Not critical
                {
                    new AcknowledgeWIthButtonDialog(this, R.drawable.ico_bat_ok_charging, R.string.dialog_ack_battery_title, R.string.dialog_ack_battery_is_charging_msg).show();
                } else {
                    new AcknowledgeWIthButtonDialog(this, R.drawable.ico_bat_low_charging, R.string.dialog_ack_battery_title, R.string.dialog_ack_battery_is_charging_msg).show();
                }
            }
        }
        //Running on the battery power
        else if (curPluggedStatus == 0) {
            //Open OffCharge dialog only once, so open it if the Previous state was On charging
            if (DeviceStateManager.getInstance().getPrevWasChargingState() == ChargingState.ON_CHARGING
                    || DeviceStateManager.getInstance().getPrevWasChargingState() == ChargingState.UNKNOWN) {
                if (batteryStatus < DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS)// Not critical
                {
                    new AcknowledgeWIthButtonDialog(this, R.drawable.ico_bat_ok, R.string.dialog_ack_battery_title, R.string.dialog_ack_battery_stopped_charging_msg).show();
                } else {
                    new AcknowledgeWIthButtonDialog(this, R.drawable.ico_bat_low, R.string.dialog_ack_battery_title, R.string.dialog_ack_battery_please_charge).show();
                }
            }
        } else {
            // intent didnt include extra info
        }
    }

    private void updateBatteryStatusImageIfNeeded() {
        boolean isDBBatteryUiEnabled = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                (OFFENDER_DETAILS_CONS.OFFENDER_IS_BATTERY_INDICATION_ENABLED) == 1;
        if (isDBBatteryUiEnabled) {
            if (batteryStatus_iv.getVisibility() == View.GONE) {
                batteryStatus_iv.setVisibility(View.VISIBLE);
            }

            int batteryStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT);
            boolean isUSBConnected = DeviceStateManager.getInstance().curChargingState == DeviceStateManager.ChargingState.ON_CHARGING;
            if (isUSBConnected) {
                //Not in critical state
                if (batteryStatus < DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS) {
                    updateBatteryIcon(R.drawable.ico_bat_ok_charging);
                } else {
                    updateBatteryIcon(R.drawable.ico_bat_low_charging);
                }
            }
            //Not in critical state
            else {
                if (batteryStatus < DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS) {
                    updateBatteryIcon(R.drawable.ico_bat_ok);
                } else {
                    updateBatteryIcon(R.drawable.ico_bat_low);
                }
            }
        } else { // Battery Status Ui Disabled
            if (batteryStatus_iv != null && batteryStatus_iv.getVisibility() == View.VISIBLE) {
                batteryStatus_iv.setVisibility(View.GONE);
            }
        }
    }

    private void updateBatteryIcon(final int iconResourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (batteryStatus_iv != null && iconResourceId != -1) {
                    batteryStatus_iv.setBackground(getResources().getDrawable(iconResourceId));
                }
            }
        });
    }

    private void handleMessageDialogSwipe() {
        alertDialogArray.get(alertDialogArray.size() - 1).cancel();
        notificationManager.cancel(alertDialogArray.size() - 1);
        alertDialogArray.remove(alertDialogArray.size() - 1);
        futureTasksHandler.removeCallbacks(messageReceiveFutureTaskManager);

        initScreen(ScreenType.Messages);
    }

    private boolean setActivityToForegroundIfNeeded() {
        // in case that LauncherActivity or any other activity on top, and we
        // want that message will be shown on MainActivity
        boolean shouldBringMainActivityToForeground = !isMainActivityOnForegroundTop();
        if (shouldBringMainActivityToForeground) {

            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            for (int i = 0; i < recentTasks.size(); i++) {
                // bring to front
                if (recentTasks.get(i).baseActivity.toShortString().indexOf(getApplicationContext().getPackageName()) > -1) {
                    activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                }
                isCameFromRegularActivityCode = true;
            }
        }
        return shouldBringMainActivityToForeground;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra(UPGRADE_TO_LATEST_VERSION_EXTRA) != null) {
            isUpgradeToNewVersionScreenShouldOpen = true;
            NetworkRepository.getInstance().startNewCycle();
            upgradeTimeoutHandler.removeCallbacks(upgradeTimeoutScreenRunnable);
        }
    }

    public boolean isMainActivityOnForegroundTop() {
        final int MAX_OF_ENTRIES = 1;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(MAX_OF_ENTRIES);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return topActivity.getClassName().equals(MainActivity.class.getName());
        }
        return false;
    }

    private void stopProximity() {
        VoiceManager.getInstance(getApplicationContext()).stop();


        String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        String csvFile = LoggingUtil.createStringForCSVFile(OperationType.PROXIMITY, HardwareTypeString.New_Tag, tagRFIDFromServer, -1, -1, "",
                -1, -1, false, -1, -1, -1, -1, "Proximity Alert Stop Sound And Vibrate");

        LoggingUtil.writeBleLogsToFile(csvFile);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFile,
                DebugInfoModuleId.Ble.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "Proximity Alert Stop Sound And Vibrate",
                DebugInfoModuleId.Ble_Others.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public void defineNotification(int NotificationNumber) {
        notificationManager.cancel(NotificationNumber); // clear previous notification
        NotificationCompat.Builder messageNotification = new NotificationCompat.Builder(this);
        String recordTextMessage = "";
        if (DatabaseAccess.getInstance().tableMessages.GetLastMsg() != null) {
            recordTextMessage = DatabaseAccess.getInstance().tableMessages.GetLastMsg().Text;
        }
        messageNotification.setContentTitle(getString(R.string.dialog_button_from_officer));
        messageNotification.setContentText(recordTextMessage);
        messageNotification.setLights(Color.BLUE, 500, 500);
        messageNotification.setSmallIcon(R.drawable.ico_msg);

        Intent intent = getPackageManager().getLaunchIntentForPackage(App.getContext().getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        messageNotification.setContentIntent(PendingIntent.getActivity(this, 25, intent, 0));
        notificationManager.notify(NotificationNumber, messageNotification.build());

        VoiceManager.getInstance(getApplicationContext()).runSoundAndVibrate(EventsAlarmsType.DEVICE_SETTINGS);
    }

    private void updateHomeScreenUI() {
        // if user didn't confirm all dangerous permission while application run in the first time
        if (!isAllDangerousPermissionsGranted) {
            initOffenderUnallocated();
            return;
        }
        updateBatteryStatusImageIfNeeded();
        AppsSharedDataManager.getInstance().sendDataToDialer();

        int offenderActivateStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS);
        if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_UNALLOCATED) {
            initOffenderUnallocated();
            return;
        } else if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_PENDING_ENROLMENT) {
            try {
                Glide.with(this).load(R.drawable.ico_main_enrolment).into(buttonHome);
                textViewCurrently.setText("");
                textViewMustStay.setText(R.string.enrolment_text_pending_enrolment);
                textViewMustStay.setTextColor(getResources().getColor(R.color.Black));

                textViewCanGo.setText("");
                textViewNext.setText("");

                pendingEnrolmentButton.setVisibility(View.VISIBLE);
                bottomButtonsContainer.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }


            return;
        } else if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_PENDING_ACTIVATION_ENROLLMENT) {
            try {
                Glide.with(this).load(R.drawable.ico_main_enrolment).into(buttonHome);
            } catch (Exception ignored) {
            }
            textViewCurrently.setText("");
            textViewMustStay.setText(R.string.enrolment_text_pending_activation);
            textViewMustStay.setTextColor(getResources().getColor(R.color.Black));
            textViewCanGo.setText("");
            textViewNext.setText("");

            pendingEnrolmentButton.setVisibility(View.GONE);
            bottomButtonsContainer.setVisibility(View.INVISIBLE);

            return;
        }

        initTvLog();

        // Offender is active

        if (isTagInProximityMode) {
            Log.i("bug95_updateUI","updateHomeScreenUI -> setProximityAlertUI");
            try {
                setProximityAlertUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        Log.i("bug95_updateUI","updateHomeScreenUI -> no proximity");

        EntityScheduleOfZones currentMbiSchedule = DatabaseAccess.getInstance().tableScheduleOfZones.GetCurrentMBISchedule();

        EntityScheduleOfZones nextMBISchedule = DatabaseAccess.getInstance().tableScheduleOfZones.GetNextMBIAppointment();

        EntityZones zoneWithDefaultScheduleAsMBI = DatabaseAccess.getInstance().tableZones.getZoneWithDefaultScheduleAsMustBeIn();

        EntityScheduleOfZones presentScheduleAsMBOWhileOffenderLocatedInsideZone = DatabaseAccess.getInstance().tableScheduleOfZones
                .getPresentScheduleAsMBOWhileOffenderLocatedInsideZone();

        // **** current Appointments *****

        // if DB contains zone as MBI
        if (zoneWithDefaultScheduleAsMBI != null) {
            EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneWithDefaultScheduleAsMBI.ZoneId);

            // no appointments in default MBI zone
            if (currentScheduleOfZone == null) {
                showPresentMustBeInText(null, null);
            } else {
                switch (currentScheduleOfZone.AppointmentTypeId) {
                    case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI:
                    case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGO:

                        // if has other current MBI appointment in other zone
                        if (currentMbiSchedule != null) {
                            showPresentMustBeInText(currentMbiSchedule, nextMBISchedule);
                        }

                        // if has other MBO appointments in default MBI zone, and
                        // offender located inside this zone
                        else if (presentScheduleAsMBOWhileOffenderLocatedInsideZone != null) {
                            showPresentMustBeOutText(presentScheduleAsMBOWhileOffenderLocatedInsideZone, nextMBISchedule);
                        } else {
                            showPresentCanGoOutText(currentScheduleOfZone, nextMBISchedule);
                        }
                        break;

                    case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI:
                        showPresentMustBeInText(null, null);
                        break;
                    case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO:

                        boolean isOffenderInCurrentAppointmentLocatedInsideZone = DatabaseAccess.getInstance().tableScheduleOfZones
                                .isOffenderInCurrentAppointmentLocatedInsideZone(currentScheduleOfZone);

                        // if has MBO appointment inside default MBI zone,, and
                        // offender located inside zone
                        if (isOffenderInCurrentAppointmentLocatedInsideZone) {
                            showPresentMustBeOutText(currentScheduleOfZone, nextMBISchedule);
                        } else {
                            showPresentCanGoOutText(currentScheduleOfZone, nextMBISchedule);
                        }

                        break;
                }
            }
        } else {

            // if no default MBI zone exists, and current MBI occur
            if (currentMbiSchedule != null) {
                showPresentMustBeInText(currentMbiSchedule, nextMBISchedule);
            } else {

                EntityScheduleOfZones futureScheduleAsMBOWhileOffenderLocatedInsideZone = DatabaseAccess.getInstance().tableScheduleOfZones
                        .getPresentScheduleAsMBOWhileOffenderLocatedInsideZone();

                // if has MBO appointment , and offender located inside zone
                if (futureScheduleAsMBOWhileOffenderLocatedInsideZone != null) {
                    showPresentMustBeOutText(futureScheduleAsMBOWhileOffenderLocatedInsideZone, nextMBISchedule);
                } else {

                    boolean isInsideExclusionZoneAndNoAppointmentFound = (DatabaseAccess.getInstance().tableZones.
                            getExclusionZoneWithoutCurrentAppointmentWhereOffenderInside() != null);
                    //if inside buffer zone, and no current appointment to this zone
                    boolean isInsideBufferZoneAndNoAppointmentFound = (DatabaseAccess.getInstance().tableZones.
                            getExclusionZoneWithoutCurrentAppointmentWhereOffenderInsideBuffer() != null);
                    //if inside exclusion zone, and no current appointment to this zone
                    if (isInsideBufferZoneAndNoAppointmentFound) {
                        showWarningStayOffBufferText(null, null);
                    }
                    else if (isInsideExclusionZoneAndNoAppointmentFound) {
                        showPresentMustBeOutText(null, null);
                    } else {
                        // no order
                        showPresentCanGoOutText(null, nextMBISchedule);
                    }
                }
            }
        }

        try {
            // TODO: Check whether this code is a duplication?
            // add by moshe
            EntityScheduleOfZones futureScheduleAsMBOWhileOffenderLocatedInsideZone = DatabaseAccess.getInstance().tableScheduleOfZones
                    .getPresentScheduleAsMBOWhileOffenderLocatedInsideZone();

            // if has MBO appointment , and offender located inside zone
            if (futureScheduleAsMBOWhileOffenderLocatedInsideZone != null) {
                showPresentMustBeOutText(futureScheduleAsMBOWhileOffenderLocatedInsideZone, nextMBISchedule);
            } else {

                boolean isInsideExclusionZoneAndNoAppointmentFound = (DatabaseAccess.getInstance().tableZones.
                        getExclusionZoneWithoutCurrentAppointmentWhereOffenderInside() != null);

                //if inside exclusion zone, and no current appointment to this zone
                if (isInsideExclusionZoneAndNoAppointmentFound) {
                    showPresentMustBeOutText(null, null);
                } else {
                    //if inside buffer zone, and no current appointment to this zone
                    boolean isInsideBufferZoneAndNoAppointmentFound = (DatabaseAccess.getInstance().tableZones.
                            getExclusionZoneWithoutCurrentAppointmentWhereOffenderInsideBuffer() != null);
                    if (isInsideBufferZoneAndNoAppointmentFound) {
                        showWarningStayOffBufferText(null, null);
                    }
                    else {
                        showPresentMustBeOutText(null, null);
                    }
                }
            }
        } catch (Exception ex) {

        }
        // **** next Appointments *****

        StringBuilder stringBuilderNextSchedule = new StringBuilder(512);
        StringBuilder stringBuilderNextTextView = new StringBuilder(512);

        EntityScheduleOfZones futureScheduleAsMBOWhileOffenderLocatedInsideZone = DatabaseAccess.getInstance().tableScheduleOfZones
                .getFutureScheduleAsMBOWhileOffenderLocatedInsideZone();

        // if DB contains zone as MBI
        if (zoneWithDefaultScheduleAsMBI != null) {

            long startTimeOfDefaultMBIZoneWithoutAppointments = DatabaseAccess.getInstance().tableScheduleOfZones.getNextStartTimeOfDefaultMBIZoneWithoutAppointmens();

            // if offender located inside zone, checks if closer appointment is
            // MBO (and not appointment in other zone as MBI or default zone as
            // MBI)
            if (futureScheduleAsMBOWhileOffenderLocatedInsideZone != null
                    && ((nextMBISchedule != null && futureScheduleAsMBOWhileOffenderLocatedInsideZone.StartTime < nextMBISchedule.StartTime
                    && futureScheduleAsMBOWhileOffenderLocatedInsideZone.StartTime < startTimeOfDefaultMBIZoneWithoutAppointments)
                    || (nextMBISchedule == null && futureScheduleAsMBOWhileOffenderLocatedInsideZone.StartTime < startTimeOfDefaultMBIZoneWithoutAppointments))) {
                showFutureMustBeOutText(futureScheduleAsMBOWhileOffenderLocatedInsideZone);
            }

            /*
             * if default MBI zone with future appointment which not MBI or BIO
             * and in other zone MBI appointment exists if the future, and MBI
             * appointment is closer than time of default MBI zone
             */
            else if (nextMBISchedule != null && nextMBISchedule.StartTime < startTimeOfDefaultMBIZoneWithoutAppointments) {
                showFutureMustBeInText(nextMBISchedule);
            } else {

                /*
                 * if default MBI zone with appointment in the future which not
                 * MBI or BIO, we want to show the start time of appointment
                 * that will end
                 */
                if (startTimeOfDefaultMBIZoneWithoutAppointments != 0) {
                    int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(this);
                    if (currentTimeFormatByDeviceSettings == 12) { // am pm
                        stringBuilderNextTextView.append(getString(R.string.home_text_next_from)).append(TimeUtil.GetTimeString(startTimeOfDefaultMBIZoneWithoutAppointments, TimeUtil.getDateFormatFromDevice(this))).append(" ").append(TimeUtil.getTimeInAmPm(startTimeOfDefaultMBIZoneWithoutAppointments)).append(" ").append(TimeUtil.getAmOrPmString(startTimeOfDefaultMBIZoneWithoutAppointments));
                    } else { // 24
                        stringBuilderNextTextView.append(getString(R.string.home_text_next_from)).append(TimeUtil.GetTimeString
                                (startTimeOfDefaultMBIZoneWithoutAppointments, TimeUtil.getDateFormatFromDevice(this))).append(" ").append(TimeUtil.GetTimeString(startTimeOfDefaultMBIZoneWithoutAppointments, DateFormatterUtil.HM));
                    }

                    String zoneName = zoneWithDefaultScheduleAsMBI.ZoneName;
                    stringBuilderNextSchedule.append(zoneName);

                    setTextToFutureOffenderMessage(getString(R.string.home_text_must_be_at_zone), zoneName, stringBuilderNextTextView.toString(),
                            Color.parseColor("#B63513"));
                } else {
                    textViewNext.setText("");
                    textViewCanGo.setText(stringBuilderNextSchedule.toString());
                    textViewCanGo.setTextColor(Color.parseColor("#B63513"));
                }
            }
        } else {

            /*
             * if no default MBI zone exits in DB, and DB contain appointment as
             * MBI in the future or MBO appointment and offender located inside
             * zone
             */
            if (nextMBISchedule != null || futureScheduleAsMBOWhileOffenderLocatedInsideZone != null) {

                // if DB contain appointment as MBI in the future or MBO
                // appointment and offender located inside zone
                if (nextMBISchedule != null && futureScheduleAsMBOWhileOffenderLocatedInsideZone != null) {

                    // if appointment as MBO is closer than MBI appointment
                    if (futureScheduleAsMBOWhileOffenderLocatedInsideZone.StartTime < nextMBISchedule.StartTime) {
                        showFutureMustBeOutText(futureScheduleAsMBOWhileOffenderLocatedInsideZone);
                    } else {
                        showFutureMustBeInText(nextMBISchedule);
                    }
                }

                // if DB contain appointment as MBI in the future
                else if (nextMBISchedule != null) {
                    showFutureMustBeInText(nextMBISchedule);
                }

                // if DB contain appointment as MBO in the future, and offender
                // located inside zone
                else if (futureScheduleAsMBOWhileOffenderLocatedInsideZone != null) {
                    showFutureMustBeOutText(futureScheduleAsMBOWhileOffenderLocatedInsideZone);
                }
            }

            // if no default MBI zone exits in DB, and DB contain appointment as
            // MBI in the present
            else if (currentMbiSchedule != null) {
                showFutureCanGoOutText(currentMbiSchedule);
            }

            // if no default MBI zone exits in DB, and DB contain appointment as
            // MBO and offender located inside zone
            else if (presentScheduleAsMBOWhileOffenderLocatedInsideZone != null) {
                showFutureCanGoOutText(presentScheduleAsMBOWhileOffenderLocatedInsideZone);
            }

            // if no default MBI zone exits in DB, and no appointments in the
            // future
            else {
                stringBuilderNextSchedule.append(getString(R.string.home_text_no_new_appointments));
                try {
                    textViewNext.setText("");
                    textViewCanGo.setText(stringBuilderNextSchedule.toString());
                    textViewCanGo.setTextColor(Color.parseColor("#00008B")); // Dark																			// Blue
                } catch (Exception ex) {}

            }
        }

        if (NetworkRepositoryConstants.isGpsProximityViolationOpened) {
//                buttonHome.setBackgroundResource(R.drawable.ico_proximity);
            Glide.with(this).load(R.drawable.ico_proximity).into(buttonHome);
            textViewCurrently.setText(R.string.home_text_proximity_detected);
            textViewCurrently.setTextColor(getResources().getColor(R.color.darkRed)); // Dark
            // Red
            textViewCurrently.setTextSize(25);
            textViewCurrently.setTypeface(null, Typeface.BOLD);

            textViewMustStay.setText(R.string.home_text_leave_the_area);
            textViewMustStay.setTextSize(23);
            textViewMustStay.setTextColor(getResources().getColor(R.color.darkRed));
            // Red
            textViewCanGo.setText(R.string.home_text_your_officer_has_been_notified);
            textViewCanGo.setTextColor(getResources().getColor(R.color.grey));
        }

        if (NetworkRepositoryConstants.isGpsProximityWarningOpened & !NetworkRepositoryConstants.isGpsProximityViolationOpened) {
//                buttonHome.setBackgroundResource(R.drawable.ico_proximity);
            Glide.with(this).load(R.drawable.ico_proximity).into(buttonHome);
            textViewCurrently.setText(R.string.home_text_proximity_detected);
            textViewCurrently.setTextColor(getResources().getColor(R.color.darkOrange));
            // Red
            textViewCurrently.setTextSize(25);
            textViewCurrently.setTypeface(null, Typeface.BOLD);

            textViewMustStay.setText(R.string.home_text_leave_the_area);
            textViewMustStay.setTextSize(23);
            textViewMustStay.setTextColor(getResources().getColor(R.color.darkOrange));
            textViewCanGo.setText(R.string.home_text_your_officer_has_been_notified);
            textViewCanGo.setTextColor(getResources().getColor(R.color.grey));
        }
        if (!NetworkRepositoryConstants.isGpsProximityWarningOpened & !NetworkRepositoryConstants.isGpsProximityViolationOpened) {
            textViewCurrently.setTextColor(getResources().getColor(R.color.grey));
            textViewCurrently.setTypeface(null, Typeface.NORMAL);

            textViewMustStay.setTextSize(32);
        }

        textViewCanGo.setGravity(0x01);

        pendingEnrolmentButton.setVisibility(View.GONE);
        bottomButtonsContainer.setVisibility(View.VISIBLE);
    }

    private void initOffenderUnallocated() {
//        buttonHome.setBackgroundResource(R.drawable.ico_proximity);
        Glide.with(App.getContext()).load(R.drawable.ico_proximity).into(buttonHome);
        textViewCurrently.setText(R.string.home_text_currently);
        textViewCurrently.setTextColor(getResources().getColor(R.color.lightGrey));
        textViewCurrently.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textViewCurrently.setTypeface(null, Typeface.NORMAL);

        textViewMustStay.setText(R.string.home_text_device_unallocated);
        textViewMustStay.setTextColor(Color.parseColor("#8B0000"));
        textViewMustStay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.tv_must_stay_text_size));
        textViewCanGo.setText("");
        textViewNext.setText("");

        pendingEnrolmentButton.setVisibility(View.GONE);
        bottomButtonsContainer.setVisibility(View.INVISIBLE);
        DatabaseAccess.getInstance().tableGuestTag.wipeTable();
        DatabaseAccess.getInstance().tableOffenderPhoto.wipeTable();
    }

    private void setProximityAlertUI() {
        Glide.with(this).load(R.drawable.ico_proximity).into(buttonHome);
        textViewCurrently.setText("");
        textViewCanGo.setText("");
        textViewNext.setText("");

        textViewMustStay.setText(R.string.home_text_proximity_alert);
        textViewMustStay.setTextColor(Color.parseColor("#8B0000")); // Dark RED
        textViewMustStay.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.tv_must_stay_text_size));

        pendingEnrolmentButton.setVisibility(View.GONE);

    }

    private void showPresentCanGoOutText(EntityScheduleOfZones currentScheduleOfZone, EntityScheduleOfZones recordScheduleOfZonesNext) {
        Log.d("MainActivity2", "showPresentCanGoOutText");

        StringBuilder strBuilderCurrentlyuntil = getPresentMessageToShowOffender(currentScheduleOfZone, recordScheduleOfZonesNext);

        setTextToPresentOffenderMessage(R.drawable.ico_main_cgo, strBuilderCurrentlyuntil.toString(), getString(R.string.home_text_can_go_out), Color.parseColor("#006400"));
    }

    private void showPresentMustBeOutText(EntityScheduleOfZones currentScheduleOfZone, EntityScheduleOfZones recordScheduleOfZonesNext) {
        Log.d("MainActivity2", "showPresentMustBeOutText");
        StringBuilder strBuilderCurrentlyUntil = getPresentMessageToShowOffender(currentScheduleOfZone, recordScheduleOfZonesNext);

        String zoneName = "";
        if (currentScheduleOfZone == null) {
            zoneName = DatabaseAccess.getInstance().tableZones.getExclusionZoneWithoutCurrentAppointmentWhereOffenderInside().ZoneName;
        } else {
            zoneName = DatabaseAccess.getInstance().tableZones.getZoneInfoToShow(currentScheduleOfZone);
        }

        setTextToPresentOffenderMessage(R.drawable.ico_main_mgo, strBuilderCurrentlyUntil.toString(), getString(R.string.home_text_must_leave_zone) + " " + zoneName, Color.parseColor("#8B0000"));
    }

    private void showWarningStayOffBufferText(EntityScheduleOfZones currentScheduleOfZone, EntityScheduleOfZones recordScheduleOfZonesNext) {
        Log.d("MainActivity", "showPresentMustBeOutIntoBufferText");
        StringBuilder strBuilderCurrentlyUntil = getPresentMessageToShowOffender(currentScheduleOfZone, recordScheduleOfZonesNext);

        String zoneName = "";
        if (currentScheduleOfZone == null) {
            zoneName = DatabaseAccess.getInstance().tableZones.getExclusionZoneWithoutCurrentAppointmentWhereOffenderInsideBuffer().ZoneName;
        } else {
            zoneName = DatabaseAccess.getInstance().tableZones.getZoneInfoToShow(currentScheduleOfZone);
        }

        setTextToPresentOffenderMessage(R.drawable.ico_main_so, strBuilderCurrentlyUntil.toString(), getString(R.string.entered_buffer_zone) + " " + zoneName, Color.parseColor("#FFD4A40B"));
    }

    private void showPresentMustBeInText(EntityScheduleOfZones currentScheduleOfZone, EntityScheduleOfZones recordScheduleOfZonesNext) {
        Log.d("MainActivity2", "showPresentMustBeInText");
        StringBuilder strBuilderCurrentlyUntil = getPresentMessageToShowOffender(currentScheduleOfZone, recordScheduleOfZonesNext);

        String zoneName = "";
        if (currentScheduleOfZone == null) {
            zoneName = DatabaseAccess.getInstance().tableZones.getZoneWithDefaultScheduleAsMustBeIn().ZoneName;
        } else {
            zoneName = DatabaseAccess.getInstance().tableZones.getZoneInfoToShow(currentScheduleOfZone);
        }

        setTextToPresentOffenderMessage(R.drawable.ico_main_mbi, strBuilderCurrentlyUntil.toString(),
                getString(R.string.home_text_must_stay_at_zone) + " " + zoneName, Color.parseColor("#8B0000"));
    }

    private StringBuilder getPresentMessageToShowOffender(EntityScheduleOfZones currentScheduleOfZone, EntityScheduleOfZones recordScheduleOfZonesNext) {
        StringBuilder strBuilderCurrentlyUntil = new StringBuilder(512);

        if (currentScheduleOfZone != null) {
            long closerTime = currentScheduleOfZone.EndTime;
            if (recordScheduleOfZonesNext != null) {
                if (recordScheduleOfZonesNext.StartTime < currentScheduleOfZone.EndTime) {
                    closerTime = recordScheduleOfZonesNext.StartTime;
                }
            }

            int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(this);
            if (currentTimeFormatByDeviceSettings == 12) {
                strBuilderCurrentlyUntil.append(getString(R.string.home_text_until)).append(" ").append(TimeUtil.GetTimeString(closerTime, TimeUtil.getDateFormatFromDevice(this))).append(" ").append(TimeUtil.getTimeInAmPm(closerTime)).append(" ").append(TimeUtil.getAmOrPmString(closerTime));
            } else { // 24
                strBuilderCurrentlyUntil.append(getString(R.string.home_text_until)).append(" ").append(TimeUtil.GetTimeString(closerTime, TimeUtil.getDateFormatFromDevice(this))).append(" ").append(TimeUtil.GetTimeString(closerTime, DateFormatterUtil.HM));
            }
        }
        return strBuilderCurrentlyUntil;
    }

    private void setTextToPresentOffenderMessage(int homeDrawableResource, String currentlyText, String orderText, int colorOrderResource) {
        try {
            Glide.with(App.getContext()).load(homeDrawableResource).into(buttonHome);
            textViewCurrently.setText(getText(R.string.home_text_currently) + " " + currentlyText);
            textViewMustStay.setText(orderText);
            textViewMustStay.setTextColor(colorOrderResource);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //**** future offender message ****
    private void showFutureCanGoOutText(EntityScheduleOfZones futureSchedule) {

        StringBuilder stringBuilderNextTextView = new StringBuilder(512);

        int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(this);
        if (currentTimeFormatByDeviceSettings == 12) {
            stringBuilderNextTextView.append(getString(R.string.home_text_next_from)).append(TimeUtil.GetTimeString(futureSchedule.EndTime, "MM/dd/yy")).append(" ").append(TimeUtil.getTimeInAmPm(futureSchedule.EndTime)).append(" ").append(TimeUtil.getAmOrPmString(futureSchedule.EndTime));
        } else {
            stringBuilderNextTextView.append(getString(R.string.home_text_next_from)).append(TimeUtil.GetTimeString(futureSchedule.EndTime, "dd/MM/yy")).append(" ").append(TimeUtil.GetTimeString(futureSchedule.EndTime, DateFormatterUtil.HM));
        }

        setTextToFutureOffenderMessage(getString(R.string.home_text_can_go_out), "", stringBuilderNextTextView.toString(), Color.parseColor("#006400"));
    }

    private void showFutureMustBeInText(EntityScheduleOfZones futureSchedule) {

        StringBuilder stringBuilderNextTextView = getFutureDataTimeToShowOffender(futureSchedule); //getFutureMessageToShowOffender(futureSchedule);

        String zoneName = "";
        if (futureSchedule != null) {
            zoneName = DatabaseAccess.getInstance().tableZones.getZoneInfoToShow(futureSchedule);
        }

        setTextToFutureOffenderMessage(getString(R.string.home_text_must_be_at_zone), zoneName, stringBuilderNextTextView.toString(),
                Color.parseColor("#B63513"));
    }

    private void showFutureMustBeOutText(EntityScheduleOfZones futureSchedule) {

        StringBuilder stringBuilderNextTextView = getFutureMessageToShowOffender(futureSchedule);

        String zoneName = "";
        if (stringBuilderNextTextView != null) {
            zoneName = DatabaseAccess.getInstance().tableZones.getZoneInfoToShow(futureSchedule);
        }

        setTextToFutureOffenderMessage(getString(R.string.home_text_must_leave_zone), zoneName, stringBuilderNextTextView.toString(),
                Color.parseColor("#8B0000"));
    }

    private void setTextToFutureOffenderMessage(String zoneOrder, String zoneName, String currentlyNextText, int colorOrderResource) {

        textViewNext.setText(currentlyNextText);
        textViewCanGo.setText(zoneOrder + " " + zoneName);
        textViewCanGo.setTextColor(colorOrderResource);

    }

    private StringBuilder getFutureMessageToShowOffender(EntityScheduleOfZones schedule) {

        StringBuilder stringBuilderNextTextView = new StringBuilder(512);

        int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(this);
        if (currentTimeFormatByDeviceSettings == 12) { // am pm
            stringBuilderNextTextView.append(getString(R.string.home_text_next_from)).append(TimeUtil.GetTimeString(schedule.StartTime, "MM/dd/yy")).append(" ").append(TimeUtil.getTimeInAmPm(schedule.StartTime)).append(" ").append(TimeUtil.getAmOrPmString(schedule.StartTime));
            stringBuilderNextTextView.append("\n");
            stringBuilderNextTextView.append(getString(R.string.home_text_to)).append(TimeUtil.GetTimeString(schedule.EndTime, "MM/dd/yy")).append(" ").append(TimeUtil.getTimeInAmPm(schedule.EndTime)).append(" ").append(TimeUtil.getAmOrPmString(schedule.EndTime));
        } else { // 24
            stringBuilderNextTextView.append(getString(R.string.home_text_next_from)).append(TimeUtil.GetTimeString(schedule.StartTime, "dd/MM/yy")).append(" ").append(TimeUtil.GetTimeString(schedule.StartTime, DateFormatterUtil.HM));
            stringBuilderNextTextView.append("\n");
            stringBuilderNextTextView.append(getString(R.string.home_text_to)).append(TimeUtil.GetTimeString(schedule.EndTime, "dd/MM/yy")).append(" ").append(TimeUtil.GetTimeString(schedule.EndTime, DateFormatterUtil.HM));
        }
        return stringBuilderNextTextView;
    }

    private StringBuilder getFutureDataTimeToShowOffender(EntityScheduleOfZones schedule) {

        StringBuilder stringBuilderNextTextView = new StringBuilder(512);
        stringBuilderNextTextView.append(getString(R.string.home_text_next_from));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(TimeUtil.GetTodayEndTime());
        Date today = c.getTime();
        c.setTimeInMillis(schedule.StartTime);
        Date scheduleStartTime = c.getTime();
        int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(this);
        if (scheduleStartTime.after(today)) {
            if (currentTimeFormatByDeviceSettings == 12) { // am pm, US formats
                stringBuilderNextTextView.append(TimeUtil.GetTimeString(schedule.StartTime, "MM/dd/yy")).append(" ").append(TimeUtil.getTimeInAmPm(schedule.StartTime)).append(" ").append(TimeUtil.getAmOrPmString(schedule.StartTime));
            } else { // 24, EU format
                stringBuilderNextTextView.append(TimeUtil.GetTimeString(schedule.StartTime, "dd/MM/yy")).append(" ").append(TimeUtil.GetTimeString(schedule.StartTime, DateFormatterUtil.HM));
            }
        } else {
            if (currentTimeFormatByDeviceSettings == 12) { // am pm, US formats
                stringBuilderNextTextView.append(getString(R.string.home_text_today)).append(TimeUtil.GetTimeString(schedule.StartTime, "MM/dd/yy")).append(" ").append(TimeUtil.getTimeInAmPm(schedule.StartTime)).append(" ").append(TimeUtil.getAmOrPmString(schedule.StartTime));
            } else { // 24, EU format
                stringBuilderNextTextView.append(getString(R.string.home_text_today)).append(TimeUtil.GetTimeString(schedule.StartTime, "dd/MM/yy")).append(" ").append(TimeUtil.GetTimeString(schedule.StartTime, DateFormatterUtil.HM));
            }
        }

        return stringBuilderNextTextView;
    }

    @Override
    public void onBackPressed() {
        if (currentScreen == ScreenType.Home) return;
        initScreen(ScreenType.Home);
    }

    int testLocationCounter=0;

    @Override
    public void onClick(View v) {
        backButton.setVisibility(View.VISIBLE);
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        switch (v.getId()) {
            case (R.id.ButtonAppaCall):
                // enable calling if NOT in flight mode
                if (!KnoxUtil.getInstance().isInInitializedOffenderFlightMode()) {
                    initScreen(ScreenType.Call);
                }
                break;

            case (R.id.ButtonAppaMessages):
                initScreen(ScreenType.Messages);
                break;

            case (R.id.ButtonAppaSchedule):
                initScreen(ScreenType.Schedules);
                break;

            case (R.id.ButtonAppaMapImg):
                initScreen(ScreenType.Map);
                break;

            case (R.id.ButtonAppaBack):
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                initScreen(ScreenType.Home);
                break;

      //      case (R.id.TextViewDate):
      //          if (BuildConfig.DEBUG) {
      //              int i = testLocationCounter / 2;
      //              double adding_to_lon = ((double) i) / 100d;
      //              boolean isBack = testLocationCounter % 2 != 0;
      //              Log.i("bug709","start adding_to_lon:"+adding_to_lon+" isBack:"+isBack);
      //              Toast.makeText(getApplicationContext(), "start adding_to_lon:"+adding_to_lon+" isBack:"+isBack, Toast.LENGTH_LONG).show();
      //              locationManager.addTestLocationsFromWork10KM(isBack, adding_to_lon);
      //              testLocationCounter++;
      //              break;
      //          }
            case (R.id.TextViewTimeHour):
                backButton.setVisibility(View.INVISIBLE);
                EntityDeviceDetails deviceDetails = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord();

                if (BuildConfig.DEBUG || (deviceDetails.getDeviceSerialNumber().length()==0)) {
                    openSettingsDialog();
                    break;
                }

                long isDeveloperModeEnable = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE);
                if ((isDeveloperModeEnable > 0)) {
                    SettingsPasswordDialog settingsPasswordDialog = new SettingsPasswordDialog(this);
                    settingsPasswordDialog.createUnlockDialog(getString(R.string.dialog_edittext_enter_password), new SettingsPasswordDialog.IUnlockDialogCallbackListener() {

                        @Override
                        public void onTryUnlockWithCorrectPassword() {
                            openSettingsDialog();
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
                break;

            case (R.id.ButtonYourOfficer):

                if (isOffenderAllocated) {
                    VoipSettings voipSettings = TableOffenderDetailsManager.sharedInstance().getVoipSettingsObject();
                    boolean isOfficerNumEnabled = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_OFFICER_NUM_ON) == 1;
                    if (isOfficerNumEnabled) {
                        String OfficerNumber = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().DeviceConfigPhoneOfficer;
                        if (voipSettings.getEnable() == 0) {
                            callDefaultDialer(OfficerNumber);
                        } else {
                            if (voipSettings.getOutgoingCalls() == VoipSettings.VOICE_CALL) {
                                callDefaultDialer(OfficerNumber);
                            }
                        }
                        isCameFromRegularActivityCode = true;
                    }
                }
                break;

            case (R.id.ButtonAgencyCenter):
                if (isOffenderAllocated) {
                    VoipSettings voipSettings = TableOffenderDetailsManager.sharedInstance().getVoipSettingsObject();

                    boolean isAgencyNumEnabled = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_AGENCY_NUM_ON) == 1;
                    if (isAgencyNumEnabled) {
                        String AgencyNumber = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONE_AGENCY);
                        if (voipSettings.getEnable() == 0) {
                            callDefaultDialer(AgencyNumber);
                        } else {
                            if (voipSettings.getOutgoingCalls() == VoipSettings.VOICE_CALL) {
                                callDefaultDialer(AgencyNumber);
                            }
                        }
                        isCameFromRegularActivityCode = true;
                    }
                }
                break;

            case (R.id.ButtonHome):
                initScreen(ScreenType.Home);
                bluetoothConnectionTextView = findViewById(R.id.bluetooth_connection_text_view);
                textViewBeaconPacket = findViewById(R.id.TextViewBeacon);

                isDeveloperModeEnable = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE);
                if ((isDeveloperModeEnable > 0)) {
                    booleanPacketToast = !(booleanPacketToast);
                    boolean isReceivedAdvertiseFromTag = ((System.currentTimeMillis() - LongLastPureTagPacketRx) <= PACKET_UI_TIME_OUT);
                    if (isReceivedAdvertiseFromTag) {
                        bluetoothConnectionTextView.setVisibility(booleanPacketToast ? View.VISIBLE : View.INVISIBLE);
                        bluetoothConnectionTextView.setText(stringPacketBroadcast);
                    }

                    booleanBeaconPacketToast = !(booleanBeaconPacketToast);
                    boolean isReceivedAdvertiseFromBeacon = ((System.currentTimeMillis() - LongLastPureBeaconPacketRx) <= PACKET_UI_TIME_OUT);
                    if (isReceivedAdvertiseFromBeacon) {
                        textViewBeaconPacket.setVisibility(booleanBeaconPacketToast ? View.VISIBLE : View.INVISIBLE);
                        textViewBeaconPacket.setText(stringPacketBeacon);
                    }
                }
                AccelerometerManager.getInstance().handleDebugRequestButton();


                break;

            case (R.id.pendingEnrolmentButton):
                backButton.setVisibility(View.INVISIBLE);

                ArrayList<String> enrolmentScreensToShowList = TableOffenderDetailsManager.sharedInstance().getEnrollmentScreensToShow();
                if (enrolmentScreensToShowList.isEmpty()) {
                    if (!DatabaseAccess.getInstance().tableEventLog.isEventExistsInDB(EventTypes.eventMonitoringStarted)) {
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventMonitoringStarted, -1, -1,
                                NumberComputationUtil.createRandomPassword().toString());
                    }
                } else {
                    openEnrolmentScreen();
                }
                break;

            case R.id.ButtonEmergency:
                if (isOffenderAllocated) {
                    VoipSettings voipSettings = TableOffenderDetailsManager.sharedInstance().getVoipSettingsObject();
                    boolean isEmergencyEnabled = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_IS_EMERGENCY_ENABLED) == 1;
                    if (isEmergencyEnabled) {
                        String phoneEmergency = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PHONE_EMERGENCY);

                        if (voipSettings.getEnable() == 0) {
                            callDefaultDialer(phoneEmergency);
                        } else {
                            if (voipSettings.getOutgoingCalls() == VoipSettings.VOICE_CALL) {
                                callDefaultDialer(phoneEmergency);
                            }
                        }
                        isCameFromRegularActivityCode = true;
                    }
                }
                break;
        }

    }

    private void callDefaultDialer(String phoneNumber) {
        long isDeveloperModeEnable = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE);
        if ((isDeveloperModeEnable > 0)) {
            Toast.makeText(getApplicationContext(), "Call: " + phoneNumber, Toast.LENGTH_LONG).show();
        }

        AppsSharedDataManager.getInstance().sendDataToDialer();

        Intent YourOfficerCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(YourOfficerCallIntent);
    }

    private void addEmergencyButtonIfNeeded() {
        if (currentScreen != ScreenType.Call) return;
        final boolean isEmergency = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_IS_EMERGENCY_ENABLED) == 1;
        LinearLayout llButtonEmergency = findViewById(R.id.llButtonEmergency);
        if (isEmergency) {
            llButtonEmergency.setVisibility(View.VISIBLE);
            buttonYourOfficer.setBackground(getResources().getDrawable(R.drawable.rsz_btn_call_supervisor_small));
            buttonAgencyCenter.setBackground(getResources().getDrawable(R.drawable.rsz_btn_call_supervisor_small));
            Button btnEmergency = findViewById(R.id.ButtonEmergency);
            btnEmergency.setOnClickListener(this);
        } else {
            TextView myTextView2 = findViewById(R.id.textView2);
            myTextView2.setVisibility(View.GONE);
            llButtonEmergency.setVisibility(View.GONE);
            buttonYourOfficer.setBackground(getResources().getDrawable(R.drawable.rsz_btn_call_supervisor));
            buttonAgencyCenter.setBackground(getResources().getDrawable(R.drawable.rsz_btn_call_supervisor));
        }
    }

    private void openEnrolmentScreen() {
        Intent intent = new Intent(this, EnrollmentActivity.class);
        startActivityForResult(intent, ENROLMENT_EXTRA_CODE);
    }

    private ArrayList<String> getScheduleDays() {
        ArrayList<String> scheduleDays = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        c.add(Calendar.DATE, -1);
        String day = df.format(c.getTime());
        scheduleDays.add(day);
        for (int i = 1; i < TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SCHEDULE_SETTINGS_NUM_OF_DAYS); i++) {
            c.add(Calendar.DATE, 1);
            day = df.format(c.getTime());
            scheduleDays.add(day);
        }
        return scheduleDays;
    }

    void startBluetoothWithOutScreen() {

        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isOffenderAllocated) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startBleScan();
                    LongLastPureTagPacketRx = System.currentTimeMillis();
                    tagLowRssiTaskManager.setLastGoodRssiTagPacketRx(System.currentTimeMillis());

                    String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
                    if (!tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
                        long timeInMills = (TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()) - TimeUnit.SECONDS.toMillis(7));
                        tagNoAdvertiseTaskManager.scheduleFutureRun(futureTasksHandler, timeInMills);
                    }

                    boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                            .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
                    if (isBeaconExistsInDBZone) {

                        // will start timer to handle offender outside beacon zone  if needed
                        long beaconOutsideRangeGraceTime = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                                (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME);
                        beaconNoAdvertiseTaskManager.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(beaconOutsideRangeGraceTime));
                    }
                }
            }, TimeUnit.SECONDS.toMillis(10));
        }
    }

    void startBleScan() {
        String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
        int ZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
        boolean isBeaconExistsInDBZone = ((DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(ZoneId)) != null);
        boolean hasOpenInitiatedFlightModeEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(TableEventConfig.ViolationCategoryTypes.FLIGHT_MODE_STATE) != -1;
        if (isOffenderAllocated && (!tagRFIDFromServer.equals(BluetoothManager.NO_TAG) || isBeaconExistsInDBZone) && !hasOpenInitiatedFlightModeEvent) {
            scanLeDeviceNew();
        }
    }

    @SuppressLint("NewApi")
    private void scanLeDeviceNew() {
        if (bluetoothManager == null) {
            bluetoothManager = new BluetoothManager(this, true);
        }
        bluetoothManager.startScan();
    }

    /**
     * BLE-RESET-STEP1
     */
    private void restartBleScan() {
        stopBleScan();
        startBleScan();
    }

    /**
     * BLE-RESET-STEP2
     */
    @SuppressLint("NewApi")
    private void stopBleScan() {
        if (bluetoothManager != null) {
            bluetoothManager.stopScan();
        }
    }

    // Activity result handling
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case KnoxUtil.DEVICE_ADMIN_ADD_RESULT_ENABLE:

                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        break;
                    case Activity.RESULT_OK:
                        App.writeToNetworkLogsAndDebugInfo(KnoxUtil.class.getSimpleName(), "Device administrator activated", DebugInfoModuleId.Knox);
                        KnoxUtil.getInstance().activateKnoxLicence();
                        break;
                }

                break;

            case DRAW_OVERLAY_PERMISSION_CODE:
                if (checkDrawOverlayAndWritePermission()) {
                    finishActivity(DRAW_OVERLAY_PERMISSION_CODE);
                }
            case DRAW_WRITE_SETTINGS_PERMISSION_CODE:
                if (checkDrawOverlayAndWritePermission()) {
                    finishActivity(DRAW_WRITE_SETTINGS_PERMISSION_CODE);
                }
            default:
                break;
        }
    }

    public void registerMagneticListener() {
        Log.i("MMCT","registerMagneticListener1");
        EntityCaseTamper caseTamperEntity = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity();
        if (caseTamperEntity == null) return;
        boolean caseTamperEnabled = caseTamperEntity.enabled > 0;
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
        if (!caseTamperEnabled || !isOffenderAllocated) return;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(MagneticManager.getInstance(), magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterMagneticListener() {
        if (magneticSensor == null) return;
        sensorManager.unregisterListener(MagneticManager.getInstance(), magneticSensor);
    }

    public void registerTemperatureListener() {
        temperatureManager = new TemperatureManager();
        mTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(temperatureManager, mTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterTemperatureListener() {
        if (temperatureManager != null && mTemperatureSensor != null) {
            sensorManager.unregisterListener(temperatureManager, mTemperatureSensor);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //startCellSignalService();
    }

    boolean tv_log_enabled;
    @Override
    protected void onResume() {
        super.onResume();

        HeartBeatServiceJava2.start();
        LightSensorManager.getInstance();
        initTvLog();

        ToolbarViewsDataManager.getInstance(this).setBatteryInfoListener(new ToolbarViewsDataManager.OnBatteryInfoListener() {
            @Override
            public void onBatteryPercentageChanged(int percentage) {
                if (batteryView!=null){
                    batteryView.setPercent(percentage);
                }
            }

            @Override
            public void onBatteryChargingStatusChanged(boolean isCharging) {
                if (batteryView!=null){
                    batteryView.setCharging(isCharging);
                }
            }
        });

        if (TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ENABLE) != 1 || App.IS_PINCODE_TYPED) {
            if (isAllDangerousPermissionsGranted) {

                LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onResume() - MainActivity ", false);
                String messageToUpload = "onResume() - MainActivity";
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                        DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);


                isCameFromRegularActivityCode = false;

                if (!isUpgradeToNewVersionScreenShouldOpen) {
                    isUpgradeToNewVersionScreenShouldOpen = true;

                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.offenderDeclinedUpgrade, -1, -1);

                    upgradeTimeoutHandler.removeCallbacks(upgradeTimeoutScreenRunnable);
                }
            }
        } else {
            LockScreenActivity.start(this);
        }

        //If user not in proximity anymore and has good rssi
        boolean hasOpenEventInProximityCategory = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY) != -1;
        if ((isTagInProximityMode) && ((System.currentTimeMillis() - tagLowRssiTaskManager.getLastGoodRssiTagPacketRx()) < TimeUnit.SECONDS.toMillis(
                TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()))) {
            handleUserNotInProximityAnymore(hasOpenEventInProximityCategory);
            Log.i("bug95_onResume","call handleUserNotInProximityAnymore. ");
        }else {
            if (!isTagInProximityMode) {
                Log.i("bug95_onResume", "no required call handleUserNotInProximityAnymore. isTagInProximityMode is false");
            } else {
                Log.i("bug95_onResume", "no required call handleUserNotInProximityAnymore. "
                        + (System.currentTimeMillis() - tagLowRssiTaskManager.getLastGoodRssiTagPacketRx()) +
                        ">=" +
                        TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()));
            }
        }
        AppsSharedDataManager.getInstance().sendDataToDialer();
        OffenderPreferencesManager.getInstance().checkForSuddenShutDown();
    }


    private void initTvLog() {
        if (findViewById(R.id.tv_log) == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (findViewById(R.id.tv_log) != null) {
                    findViewById(R.id.tv_log).setVisibility(SensorDataSource.getInstance().showSensors() ? View.VISIBLE : View.GONE);
                }
            }
        });
        if (!SensorDataSource.getInstance().showSensors()) {
            return;
        }
        if (tv_log_enabled) {
            return;
        }
        tv_log_enabled = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (tv_log_enabled && SensorDataSource.getInstance().showSensors()) {
                        if (findViewById(R.id.tv_log) == null) {
                            tv_log_enabled = false;
                            break;
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (findViewById(R.id.tv_log) != null) {
                                        ((TextView) findViewById(R.id.tv_log)).setText(Html.fromHtml(MagneticManager.getInstance().toHtmlLog()));
                                    } else {
                                        tv_log_enabled = false;
                                    }
                                }
                            });
                        }

                        Thread.sleep(1000);
                    }
                } catch (Exception ex) {

                } finally {
                    tv_log_enabled = false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (findViewById(R.id.tv_log) != null) {
                            findViewById(R.id.tv_log).setVisibility(SensorDataSource.getInstance().showSensors() ? View.VISIBLE : View.GONE);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        tv_log_enabled=false;
        appInBackgroundTime = System.currentTimeMillis();
        appInBackground = true;
        if (isAllDangerousPermissionsGranted) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onPause() - MainActivity ", false);
            String messageToUpload = "onPause() - MainActivity";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        if (isAllDangerousPermissionsGranted) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onStop() - MainActivity ", false);
            String messageToUpload = "onStop() - MainActivity";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
    }

    @Override
    protected void onDestroy() {

        Log.e("MainActivityDestroyed",
                "on destroyed main activity -> should never ever close main activity until we'll refactoring the code -> We cannot exit from MainActivity because proximity thread has callbacks on MainActivity");

        try {
            unregisterReceiver(scanBleDeviceReceiver);
        }catch (Exception ex){

        }

        if (isAllDangerousPermissionsGranted) {

            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onDestroy() - MainActivity ", false);
            String messageToUpload = "onDestroy() - MainActivity";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Application_States.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);


            if (newDayStartReceiver != null) {
                newDayStartReceiver.CancelAlarm(App.getContext(), NewDayStartReceiver.class, 7, null);
            }
            locationManager.stopLocationUpdate();
            locationManager.unregisterBroadcastReceiver();

            if (updateUIReceiver != null) {
                updateUIReceiver.CancelAlarm(App.getContext(), UpdateUIReceiver.class, 9, null);
            }

            LocalBroadcastManager.getInstance(this).unregisterReceiver(localMainActivityReceiver);
            NetworkRepository.getInstance().unregisterIdleModeReceiver();
            DeviceStateManager.getInstance().unregisterBatteryAndUsbChanges();
            callLogManager.unregisterCallLogContentObserver();


            unregisterAccelerometerListener();
            unregisterMagneticListener();
            unregisterTemperatureListener();
        }

        super.onDestroy();
    }

    private void unregisterAccelerometerListener() {
        if (mAccelerometerSensor == null) return;
        sensorManager.unregisterListener(AccelerometerManager.getInstance(), mAccelerometerSensor);
    }

    private void handleProfileModeIfNeeded(EntityEventConfig recordEventConfig, int zoneId) {
        PmComProfiles pmComProfile = TableEventsManager.sharedInstance().profilingEventsConfig.
                getPmComObjectByType(recordEventConfig.EventType);
        boolean isProfileEvent = (recordEventConfig.EventType == EventTypes.startProfile || recordEventConfig.EventType == EventTypes.endProfile);
        if (pmComProfile != null && !isProfileEvent) {

            ProfileEvents profileEvent = TableEventsManager.sharedInstance().profilingEventsConfig.getProfileEvent(recordEventConfig.EventType);

            if (profileEvent.Restrictions == Restrictions.NORMAL) {
                startPmComProfileByProfileID(pmComProfile.ID);
            } else {
                int beaconZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
                boolean isEventBelongsToBeacon = (beaconZoneId == zoneId);
                if ((profileEvent.Restrictions == Restrictions.BEACON_ONLY && isEventBelongsToBeacon) ||
                        profileEvent.Restrictions == Restrictions.NOT_BEACON && !isEventBelongsToBeacon) {
                    startPmComProfileByProfileID(pmComProfile.ID);
                } else {
                    startPmComProfileByProfileID(-1); //found profile,but not restricted
                }
            }
        } else {
            startPmComProfileByProfileID(-1); //not found profile or profile event
        }
    }

    private void manageEventSoundAndVibrate(int eventType) {
        int eventAlramType = TableOffenderDetailsManager.sharedInstance().getEventsAlarmType(eventType);
        boolean shouldMakeShoundAndVibrate = (eventAlramType != EventsAlarmsType.SILENT);
        if (shouldMakeShoundAndVibrate) {
            VoiceManager.getInstance(getApplicationContext()).runSoundAndVibrate(eventAlramType);
            setActivityToForegroundIfNeeded();
        }
    }

    public void startPmComProfileByProfileID(int newProfileId) {

        int currentProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);
        PmComProfiles pmComProfile = TableEventsManager.sharedInstance().profilingEventsConfig.getPmComProfileObjectByProfileId(newProfileId);
        if (pmComProfile != null && pmComProfile.MinDuration > 0 && (newProfileId <= currentProfile || currentProfile == -1)) {

            String messageToUpload = "Started profile: " + newProfileId + ".\n"
                    + "Min duration is: " + pmComProfile.MinDuration + " seconds, Max duration is: " + pmComProfile.MaxDuration + " seconds.\n"
                    + "Will use CommInterval every " + pmComProfile.CommInterval + " seconds, "
                    + "Will get location updates every " + pmComProfile.LocationInterval + " seconds.\n";

            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Network);

            boolean hasAnOpenEvent = TableEventsManager.sharedInstance().hasOpenEventInViolationCategory(ViolationCategoryTypes.START_PROFILE);
            if (!hasAnOpenEvent && (newProfileId == currentProfile || currentProfile == -1)) {
                TableEventsManager.sharedInstance().addEventToLog(EventTypes.startProfile, -1, -1, String.valueOf(newProfileId));
            } else if (hasAnOpenEvent && newProfileId < currentProfile) {
                TableEventsManager.sharedInstance().addEventToLog(EventTypes.endProfile, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(EventTypes.startProfile, -1, -1, String.valueOf(newProfileId));
            }
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE, newProfileId);

            minProfileDurationTimeFutureManager.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(pmComProfile.MinDuration));
            minProfileDurationTimeFutureManager.setTimerIsRunning(true);

            if (pmComProfile.MaxDuration != -1) {
                maxProfileDurationTimeFutureManager.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(pmComProfile.MaxDuration));
            }

            locationManager.startLocationUpdate(false);
            NetworkRepository.getInstance().scheduleNewCycleIfNeeded(true);
        } else if (shouldBackToNormalProfile()) {
            handlePMComProfileEnded("no more open events with last profile id and above min duration time");
        }
    }

    @Override
    public void onBluetoothManagerModelsHandled(BeaconModel beaconModel, TagModel tagModel) {

        boolean isOffenderActivated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        if (isOffenderActivated) {

            // beacon
            if (beaconModel != null) {
                LongLastPureBeaconPacketRx = beaconModel.getLongLastPureBeaconPacketRx();
                stringPacketBeacon = beaconModel.getStringPacketUI();
/*                if (DeviceShieldingManager.cellularReceptionString != null) {
                    stringPacketBeacon += DeviceShieldingManager.cellularReceptionString;
                }*/
                int beaconRssi = beaconModel.getConnectionRssi();

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textViewBeaconPacket.setText(stringPacketBeacon);
                        if (booleanBeaconPacketToast && textViewBeaconPacket.getVisibility() == View.INVISIBLE) {
                            textViewBeaconPacket.setVisibility(View.VISIBLE);
                        }
                    }
                });

                // will start timer to turn off beacon ui
                beaconFutureTaskManager.scheduleFutureRun(futureTasksHandler, PACKET_UI_TIME_OUT);


                // will start timer to handle offender outside beacon zone
                long beaconOutsideRangeGraceTime = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                        (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME);
                beaconNoAdvertiseTaskManager.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(beaconOutsideRangeGraceTime));

                // PACKET LOW RSSI
                if (beaconRssi <= TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_RANGE)) {

                    if (!beaconLowRssiTaskManager.didStartLowRssiSchedule()) {

                        // create future schedule if RSSI < beaconOutsideRangeGraceTime
                        long lowRssiRangeGraceTime = TimeUnit.SECONDS.toMillis(beaconOutsideRangeGraceTime);
                        beaconLowRssiTaskManager.scheduleFutureRun(futureTasksHandler, lowRssiRangeGraceTime);

                        beaconLowRssiTaskManager.setDidStartLowRssiSchedule(true);
                    }

                } else {
                    beaconLowRssiTaskManager.setLastGoodRssiBeaconPacketRx(System.currentTimeMillis());

                    beaconLowRssiTaskManager.setDidStartLowRssiSchedule(false);
                    beaconLowRssiTaskManager.removeNextCallback();

                    TableZonesManager.sharedInstance().handleInsideBeaconZone();
                }

                if (beaconModel.isMacAddressChanged()) {
                    restartBleScan();
                }
            }

            // tag
            if (tagModel != null) {
                LongLastPureTagPacketRx = tagModel.getLongLastPureTagPacketRx();
                lastAdvertiseTag = tagModel.getLongLastPureTagPacketRx();
                tagRssi = tagModel.getConnectionRssi();
                stringPacketBroadcast = tagModel.getStringPacketUI();
/*                if (DeviceShieldingManager.cellularReceptionString != null) {
                    stringPacketBroadcast += DeviceShieldingManager.cellularReceptionString;
                }*/

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        bluetoothConnectionTextView.setText(stringPacketBroadcast);
                        if (booleanPacketToast && bluetoothConnectionTextView.getVisibility() == View.INVISIBLE) {
                            bluetoothConnectionTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                //create future schedule to close tag and beacon screens
                tagFutureTaskManager.scheduleFutureRun(futureTasksHandler, PACKET_UI_TIME_OUT);

                // create future schedule if didn't get advertise in the last getCurrentProximityTimeToOpenEvent()) - TimeUnit.SECONDS.toMillis(7)
                long timeInMills = (TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()) - TimeUnit.SECONDS.toMillis(7));

                tagNoAdvertiseTaskManager.scheduleFutureRun(futureTasksHandler, timeInMills);
                // PACKET LOW RSSI
                if (tagRssi <= TableOffenderStatusManager.sharedInstance().getProximityRssiLimit()) {
                    if (!tagLowRssiTaskManager.isDidStartLowRssiSchedule()) {
                        // create future schedule if RSSI < funcGetProximtyRssiLimit
                        long lowRssiTimeToOpenEvent = TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent());
                        tagLowRssiTaskManager.scheduleFutureRun(futureTasksHandler, lowRssiTimeToOpenEvent);

                        tagLowRssiTaskManager.setDidStartLowRssiSchedule(true);
                    }

                } else {
                    tagLowRssiTaskManager.setLastGoodRssiTagPacketRx(System.currentTimeMillis());

                    tagLowRssiTaskManager.setDidStartLowRssiSchedule(false);
                    tagLowRssiTaskManager.removeNextCallback();

                }

                //if user not in proximity anymore and has good rssi
                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY) != -1;
                if ((hasOpenEvent || isTagInProximityMode) && ((System.currentTimeMillis() - tagLowRssiTaskManager.getLastGoodRssiTagPacketRx()) < TimeUnit.SECONDS.toMillis(
                        TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()))) {
                    handleUserNotInProximityAnymore(hasOpenEvent);
                    Log.i("bug95_onBluetooth", "call handleUserNotInProximityAnymore. ");
                } else {
                    Log.i("bug95_onBluetooth", "no required call handleUserNotInProximityAnymore. "
                            + (System.currentTimeMillis() - tagLowRssiTaskManager.getLastGoodRssiTagPacketRx()) +
                            ">=" +
                            TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()));
                }

                if (tagModel.isMacAddressChanged()) {
                    restartBleScan();
                }


            }
        }
    }

    private void handleUserNotInProximityAnymore(boolean hasOpenEvent) {
        String log=
        "handleUserNotInProximityAnymore(hasOpenEvent = " + hasOpenEvent + ") ->    isTagInProximityMode = false";

        isTagInProximityMode = false;
        Log.i("bug95_handle","set isTagInProximityMode = false");

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),log, DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
        Log.i("bug95_handle",log);

        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE, 1);

        if (hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventProximityClose, -1, -1);
        }

        String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        String csvFile = LoggingUtil.createStringForCSVFile(OperationType.PROXIMITY, HardwareTypeString.New_Tag, tagRFIDFromServer, -1, -1, "",
                -1, -1, false, -1, -1, -1, -1, "Proximity Alert Stop UI Update");

        LoggingUtil.writeBleLogsToFile(csvFile);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFile,
                DebugInfoModuleId.Ble.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "Proximity Alert Stop UI Update",
                DebugInfoModuleId.Ble_Others.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("bug95_handle","call stopProximity and updateUi");
                    try {
                        updateHomeScreenUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stopProximity();
                }
            });
        } catch (Exception e) {
            Log.e("bug95_handle","error",e);
            e.printStackTrace();
        }
    }

    @Override
    public void onOpenBeaconEventStatusChanged() {
        locationManager.startLocationUpdate(false);
    }

    private void openSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(new SettingsDialog.SettingsDialogListener() {

            @Override
            public void handleKnoxSdkPressed() {
                App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Start Knox Process Pressed", DebugInfoModuleId.Knox);
                KnoxUtil.getInstance().setknoxUtilityListener(new KnoxUtilityListener() {
                    @Override
                    public void onDeviceAdminShouldInstalled() {

                        // This activity asks the user to grant device administrator rights to the app.
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, KnoxUtil.getInstance().mDeviceAdmin);
                        startActivityForResult(intent, KnoxUtil.DEVICE_ADMIN_ADD_RESULT_ENABLE);

                        BaseActivity.isCameFromRegularActivityCode = true;
                    }

                    @Override
                    public void onStartedActivateKnox() {

                    }

                    @Override
                    public void onFailedToActivateKnox() {

                    }

                    @Override
                    public void onSucceededToActivateKnox() {

                    }
                });
                KnoxUtil.getInstance().runKnoxIfNeeded(MainActivity.this);
            }

            @Override
            public void openSettingsDialogAndSetLocale(String locale) {
                openSettingsDialog();
                setLocale(locale);
                handleDialerLanguageChanged();
            }

            @Override
            public void onSettingsChange() {
                initTvLog();
            }
        });
        settingsDialog.show(getFragmentManager(), "");
    }

    private void initDeviceState(boolean isDatabaseSuccessfullyDeleted) {
        if (isDatabaseSuccessfullyDeleted) {
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    "Proximity tracking: NetworkRequestsManager -> onUnallocateRecordUploaded -> initDeviceState methode before stopBleActivities ", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
            stopBleActivities(true);

            unregisterAccelerometerListener();
            unregisterMagneticListener();
            unregisterTemperatureListener();

            locationManager.stopLocationUpdate();

            updateHomeScreenUI();

            NetworkRepository.getInstance().scheduleNewCycleIfNeeded(true);
        }
    }

    public boolean resetDBToInitialDeviceState() {
        String DeviceSn = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber();
        String url = "";
        try {
            url = AESUtils.decrypt(SERVER_URL_AES_KEY_BYTES, TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_URL));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        String password = "";
        try {
            String scrambledPass = AESUtils.decrypt(SERVER_URL_AES_KEY_BYTES, TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_PASS));
            password = ScramblingTextUtils.unscramble(scrambledPass);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        String tagRfId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                (OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        String tagId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                (OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ID);
        String tagAddress = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                (OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS);
        String tagEncryption = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                (OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ENCRYPTION);
        int lastOffenderRequestIdTreated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED);
        String appLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                (OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();

        return tryResetToInitialDeviceState(DeviceSn, url, password, tagRfId, tagEncryption, tagId, tagAddress,
                lastOffenderRequestIdTreated, appLanguage);

    }

    public void tryResetOffenderParams(String deviceSn, String url, String password) {
        boolean isColumnResetSuccesfull = true;
        if (DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS, TableDeviceDetails.COLUMN_DEV_SN, deviceSn) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update COLUMN_DEV_SN", DebugInfoModuleId.DB);
            isColumnResetSuccesfull = false;
        }
        String encURL = "";
        try {
            encURL = AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, url);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_URL, encURL) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DEVICE_CONFIG_SERVER_URL", DebugInfoModuleId.DB);
            isColumnResetSuccesfull = false;
        }

        String scrambledEncPass = "";
        try {
            scrambledEncPass = ScramblingTextUtils.scramble(AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, password));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_PASS, scrambledEncPass) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DEVICE_CONFIG_SERVER_PASS", DebugInfoModuleId.DB);
            isColumnResetSuccesfull = false;
        }

        if (isColumnResetSuccesfull) {
            ((App) App.getContext()).restartApplication();
        } else {
            App.writeToNetworkLogsAndDebugInfo(TAG, TAG + " - SmsOp-3 Reset offender params did not succeded!", DebugInfoModuleId.Receivers);
        }

    }

    public boolean tryResetToInitialDeviceState(String deviceSn, String url, String password, String tagRfId, String tagEncryption, String tagId, String tagAddress,
                                                int lastOffenderRequestIdTreated, String appLanguage) {
        boolean isDatabaseSucceesfullyDeleted = DatabaseAccess.getInstance().resetDatabase(deviceSn, url, password, tagRfId, tagEncryption, tagId, tagAddress,
                lastOffenderRequestIdTreated, appLanguage);
        initDeviceState(isDatabaseSucceesfullyDeleted);
        return isDatabaseSucceesfullyDeleted;
    }

    private void startTagActivitiesIfNeeded() {
        String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
        if (!tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {

            if (bluetoothManager == null) {
                bluetoothManager = new BluetoothManager(this, true);
            }
            bluetoothManager.initTagIndexesToDefaultValues();

            restartBleScan();


            LongLastPureTagPacketRx = System.currentTimeMillis();
            tagLowRssiTaskManager.setLastGoodRssiTagPacketRx(System.currentTimeMillis());

            long timeInMills = (TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent()));
            tagNoAdvertiseTaskManager.scheduleFutureRun(futureTasksHandler, timeInMills);
            return;

        }
        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
        if (isBeaconExistsInDBZone) {
            restartBleScan();
        }
    }

    private void stopBleActivities(boolean shouldForceStopBleScan) {

        Log.i("bug95_stopBle","stopBleActivities");

        tagLowRssiTaskManager.removeNextCallback();
        tagNoAdvertiseTaskManager.removeNextCallback();

        isTagInProximityMode = false;
        Log.i("bug95_stopBle","set isTagInProximityMode = false");

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                "Proximity tracking: stopBleActivities(shouldForceStopBleScan = " + shouldForceStopBleScan + ")  Guess-On replace/remove PureTag From Pure Monitor)", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);

        boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
        if (!isBeaconExistsInDBZone || shouldForceStopBleScan) {
            if (bluetoothManager == null) return;
            bluetoothManager.stopScan();
            Log.i("bug95_stopBle","stopScan");
        }
    }

    /**
     * in case in initiated flight mode, and was was restarted, then we want to init alarm manager again
     */
    private void initInitiatedFlightModeAlarmIfNeeded(long timeToFinish) {
        boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(TableEventConfig.ViolationCategoryTypes.FLIGHT_MODE_STATE) != -1;

        if (hasOpenEvent && timeToFinish > 0) {

            FlightModeEnabled = true;

            wakingAlarmBroadcastReceiver.setAlaramClock(App.getContext(), timeToFinish,
                    WakingAlarmBroadcastReceiver.class, 41, MainActivity.class.getCanonicalName(),
                    MainActivity.class.getCanonicalName());

            LocalBroadcastManager.getInstance(App.getContext()).registerReceiver
                    (wakeupReceiverReceiver, new IntentFilter(MainActivity.class.getCanonicalName()));

            // disable "call" button
            callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
        } else {
            FlightModeEnabled = false;
            callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_main_1), null, null);
        }
    }

    private void handleDialerLanguageChanged() {
        String localeLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
        new LocaleUtil().changeApplicationLanguageIfNeeded();
        setLocale(localeLanguage);
        DialerUtils.startSuperComDialer(localeLanguage, true);
    }

    private void initScreens() {
        if (TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS) == 0) {
            initScreen(currentScreen);

            Intent intent = new Intent(LauncherActivity.LAUNCHER_ACTIVITY_MESSAGE_RECEIVER);
            intent.putExtra(LauncherActivity.LAUNCHER_ACTIVITY_EXTRA, LauncherActivity.APPLICATION_LANGUAGE_CHANGED_CLICK_EXTRA);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
        } else {
            LockScreenActivity.start(this);
        }
    }

    private void openApkInstallDialog(File apkTargetFile) {
        App.writeToNetworkLogsAndDebugInfo(TAG, "SW_UPGRADE: Start installation of the  " + apkTargetFile.getName(), DebugInfoModuleId.Network);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkTargetFile), "application/vnd.android.package-archive");
        intent.putExtra(UPGRADE_TO_LATEST_VERSION_EXTRA, "latestVersionExtra");
        startActivity(intent);
    }

    private boolean shouldBackToNormalProfile() {

        boolean shouldBackToNormalProfile;

        int currentProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);

        //if we are in normal profile or min profile duration time already running
        if (currentProfile == -1 || minProfileDurationTimeFutureManager.isTimerIsRunning) {
            shouldBackToNormalProfile = false;
        } else {
            List<EntityOpenEventLog> allOpenEventLogRecords = DatabaseAccess.getInstance().tableOpenEventsLog.getAllOpenEventLogRecordsThatNotHandeledManually();

            shouldBackToNormalProfile = true;

            // we will be back to normal profile, only if open event not exists in openEventTable with last profileId
            for (int i = 0; i < allOpenEventLogRecords.size(); i++) {
                EntityEventConfig recordByEventType = DatabaseAccess.getInstance().tableEventConfig.getRecordByEventType(allOpenEventLogRecords.get(i).OpenEventType);

                PmComProfiles pmComProfile = TableEventsManager.sharedInstance().profilingEventsConfig.
                        getPmComObjectByType(recordByEventType.EventType);

                int currentPmComProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);

                //if found in open event table profile with same id as current profile id
                if (pmComProfile != null && pmComProfile.ID == currentPmComProfile) {
                    shouldBackToNormalProfile = false;
                }
            }
        }

        return shouldBackToNormalProfile;
    }

    public void finishPMComProfileByID(int pmComProfileIDToEnd, String reasonToEndProfile) {
        int currentPmComProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);
        if (currentPmComProfile == pmComProfileIDToEnd) {
            handlePMComProfileEnded(reasonToEndProfile);
        }
    }

    private void handlePMComProfileEnded(String reasonToEndProfile) {
        futureTasksHandler.removeCallbacks(minProfileDurationTimeFutureManager);
        futureTasksHandler.removeCallbacks(maxProfileDurationTimeFutureManager);

        String messageToUpload = "Profile ended since " + reasonToEndProfile;
        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Network);

        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE, -1);

        TableEventsManager.sharedInstance().addEventToLog(EventTypes.endProfile, -1, -1);

        locationManager.startLocationUpdate(false);

        NetworkRepository.getInstance().scheduleNewCycleIfNeeded(true);
    }

    private void changeWriteToLogsSettingsIfNeeded() {

        long isDeveloperModeEnable = (int) TableOffenderDetailsManager.sharedInstance().
                getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE);
        if (isDeveloperModeEnable == 2) {
            LoggingUtil.isNetworkLogEnabled = true;
            LoggingUtil.isZonesLogEnabled = true;
            LoggingUtil.isBleLogEnabled = true;
        } else {
            LoggingUtil.isNetworkLogEnabled = false;
            LoggingUtil.isZonesLogEnabled = false;
            LoggingUtil.isBleLogEnabled = false;

            textViewBeaconPacket.setVisibility(View.INVISIBLE);
            bluetoothConnectionTextView.setVisibility(View.INVISIBLE);
            booleanBeaconPacketToast = false;
            booleanPacketToast = false;
        }
        PureTrackSharedPreferences.setShouldWriteToFile((isDeveloperModeEnable == 2));

    }

    public void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        initScreen(ScreenType.Home);
    }

    public void onBtnHomeClick(View view) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    public void onBtnBackClick(View view) {
        super.onBackPressed();
    }

    private class mainTask extends TimerTask {
        public void run() {
            try {

                if(lastLocationSendToServerCheckDate== null || (new Date().getTime() -  lastLocationSendToServerCheckDate.getTime() > 20000)){
                    Log.i("PTRCK-69","mainTask Check locations");
                    lastLocationSendToServerCheckDate=new Date();
                    List<EntityGpsPoint> recordGpsPointsArray = DatabaseAccess.getInstance().tableGpsPoint.getGpsPointRecordsForUpload();
                    if (!recordGpsPointsArray.isEmpty() && recordGpsPointsArray.size() > 5) {
                        NetworkRepository.getInstance().startNewCycle();
                    }
                }

                DatabaseAccess.getInstance().tableGuestTag.checkForInvalidTags();
                //deviceShieldingManager.startDeviceShieldingIfEnabled();

                boolean isManufacturerIdEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MANUFACTURER_ID) > 0;
                boolean isMaScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MAC_SCAN_ENABLED) > 0;
                boolean isNormalScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(NORMAL_SCAN_ENABLED) > 0;
                boolean isDozeModeScanEnabled = isMaScanEnabled || isManufacturerIdEnabled;

                if (!isDozeModeScanEnabled || isNormalScanEnabled) {
                    if (MojScreenTurnOn > 0) {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isInteractive();
                        if (!isScreenOn) {
                            screenCnt++;
                            if (screenCnt % MojScreenTurnOn == 0) {
                                if (LocationManager.getDeviceActivity() != DetectedActivity.STILL) {
                                    // in motion, so turn ON the screen to improve GPS
                                    LoggingUtil.updateNetworkLog("\n[" + TimeUtil.getCurrentTimeStr() + "] turn on screen (motion): activity: " + LocationManager.getDeviceActivity() + " \n", false);


                                    NetworkRepository.turnOnScreenZ();

                                }
                            }
                        }
                    }
                }

                long commDevTime = (System.currentTimeMillis() - commLastCycleTime);
                long commDevTimeMax = (commIntervalTimeFromDB * 1000 + 10);
                if (commDevTime > commDevTimeMax) {
                    commLastCycleTime = System.currentTimeMillis();
                    NetworkRepository.getInstance().startNewCycle();
                }

                if (afterStartup || ((System.currentTimeMillis() - commLastCycleTime) > (commIntervalTimeFromDB * 1000 + 10))){
                    commLastCycleTime = System.currentTimeMillis();
                    NetworkRepository.getInstance().startNewCycle();
                    afterStartup=false;
                }


                if (MainActivity.playLocationRxTone) {
                    MainActivity.playLocationRxTone = false;
                }

                if (MainActivity.playLocationDBTone) {
                    MainActivity.playLocationDBTone = false;
                }


                if (MainActivity.playBleRxTone) {
                    MainActivity.playBleRxTone = false;
                }


                if (MainActivity.appInBackground) {
                    if ((System.currentTimeMillis() - MainActivity.appInBackgroundTime) > DELTA_TIME_TO_RESTART_BLE) {
                        MainActivity.appInBackground = false;
                        restartBleScan();
                    }
                }


                if (lastTagReceivedTime != 0) {

                    long ProximityTime = TimeUnit.SECONDS.toMillis(TableOffenderStatusManager.sharedInstance().getCurrentProximityTimeToOpenEvent());
                    long TimeDelta = TIME_FOREGROUND_BLE_SERVICE;
                    if (ProximityTime > TIME_FOREGROUND_BLE_SERVICE) {
                        TimeDelta = ProximityTime - TIME_FOREGROUND_BLE_SERVICE;
                    }

                    if (MainActivity.ScreenOnBleTime != 0) {
                        // already no tag signals, turn on every 30 seconds
                        if ((System.currentTimeMillis() - MainActivity.ScreenOnBleTime) >= TIME_FOREGROUND_BLE_SERVICE) {
                            LoggingUtil.updateNetworkLog("\n[" + TimeUtil.getCurrentTimeStr() + "] turn on screen (ble) 1.0: time: " + System.currentTimeMillis() +
                                    ", last: " + MainActivity.ScreenOnBleTime + ".\n", false);

                            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                    "No BLE signals - Turn on screen - 1", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);

                            MainActivity.ScreenOnBleTime = System.currentTimeMillis();
                            //Feature background scan
                            if (!isDozeModeScanEnabled || isNormalScanEnabled) {
                                NetworkRepository.turnOnScreenX();
                            }
                        }
                    } else {
                        // turn on screen 30 seconds before proximity should arrive, and then every 30 seconds
                        if ((System.currentTimeMillis() - lastTagReceivedTime) >= TimeDelta) {
                            LoggingUtil.updateNetworkLog("\n[" + TimeUtil.getCurrentTimeStr() + "] turn on screen (ble) 1.1: time: " + System.currentTimeMillis() +
                                    ", Rx: " + lastTagReceivedTime + ", Delta: " + TimeDelta + ".\n", false);

                            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                    "No BLE signals - Turn on screen - 2", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);

                            MainActivity.ScreenOnBleTime = System.currentTimeMillis();
                            //Feature background scan
                            if (!isDozeModeScanEnabled || isNormalScanEnabled) {
                                NetworkRepository.turnOnScreenX();
                            }
                        } else {
                            MainActivity.ScreenOnBleTime = 0;
                        }
                    }
                }


                if (lastBleTagRx != 0) {
                    if ((System.currentTimeMillis() - lastBleTagRx) >= (TIME_TO_RESET_BLE)) {
                        lastBleTagRx = System.currentTimeMillis();
                        restartBleScan();
                    }
                }

            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    class SensorInterface implements AccelerometerManager.IUpdateActivitySensor {
        @Override
        public void onAccelerometerStateChanged(boolean isMotionMode, boolean stateChanged) {
            if (locationManager == null) return;
            locationManager.setMotionState(isMotionMode, stateChanged);
            if (stateChanged) {
                locationManager.startLocationUpdate(false);
            }
        }
    }

    class NextFutureTask {

        class BeaconFutureUIRunnable extends BaseFutureRunnable {

            @Override
            public void run() {
                textViewBeaconPacket.setVisibility(View.INVISIBLE);
            }
        }

        class BeaconNoAdvertiseRunnable extends BaseFutureRunnable {

            @Override
            public void run() {
                String additionalInfo = "No Signal";
                TableZonesManager.sharedInstance().handleOutsideBeaconZone(false, additionalInfo);
            }

        }

        class BeaconLowRssiRunnable extends BaseFutureRunnable {

            private long lastGoodRssiBeaconPacketRx;
            private boolean didStartLowRssiSchedule;

            @Override
            public void run() {
                String additionalInfo = "Low RSSI";
                TableZonesManager.sharedInstance().handleOutsideBeaconZone(false, additionalInfo);
            }


            public void setLastGoodRssiBeaconPacketRx(long lastGoodRssiTagPacketRx) {
                this.lastGoodRssiBeaconPacketRx = lastGoodRssiTagPacketRx;
            }

            public boolean didStartLowRssiSchedule() {
                return didStartLowRssiSchedule;
            }

            public void setDidStartLowRssiSchedule(boolean didStartLowRssiSchedule) {
                this.didStartLowRssiSchedule = didStartLowRssiSchedule;
            }

            public void removeNextCallback() {
                futureTasksHandler.removeCallbacks(beaconLowRssiTaskManager);
            }

        }

        class TagFutureUIRunnable extends BaseFutureRunnable {

            @Override
            public void run() {
                bluetoothConnectionTextView.setVisibility(View.INVISIBLE);
            }
        }

        class TagNoAdvertiseRunnable extends BaseFutureRunnable {
            // PT doesn't recive tag

            private final TagHandleProximityRunnable tagHandleProximityRunnable = new TagHandleProximityRunnable();

            @Override
            public void run() {
                String messageToUpload = "MainActivity - Restarted bluetooth";
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                        DebugInfoModuleId.Ble_Others.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                tagHandleProximityRunnable.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(14));

            }

            @Override
            public void scheduleFutureRun(Handler handler, long timeToMills) {
                removeNextCallback();
                super.scheduleFutureRun(handler, timeToMills);
            }

            public void removeNextCallback() {
                futureTasksHandler.removeCallbacks(tagNoAdvertiseTaskManager);
                futureTasksHandler.removeCallbacks(tagHandleProximityRunnable);
                tagHandleProximityRunnable.removeNextCallbacks();
            }
        }

        class TagLowRssiRunnable extends BaseFutureRunnable {

            private final TagHandleProximityRunnable tagHandleProximityRunnable = new TagHandleProximityRunnable();
            // PT recive the tag but low RSSI
            private long lastGoodRssiTagPacketRx = System.currentTimeMillis();
            private boolean didStartLowRssiSchedule;

            @Override
            public void run() {
                Log.i("bug95_TagLowRssi","tagHandleProximityRunnable.scheduleFutureRun");
                tagHandleProximityRunnable.scheduleFutureRun(futureTasksHandler, 0);
            }

            public long getLastGoodRssiTagPacketRx() {
                return lastGoodRssiTagPacketRx;
            }

            public void setLastGoodRssiTagPacketRx(long lastGoodRssiTagPacketRx) {
                this.lastGoodRssiTagPacketRx = lastGoodRssiTagPacketRx;
            }

            public boolean isDidStartLowRssiSchedule() {
                return didStartLowRssiSchedule;
            }

            public void setDidStartLowRssiSchedule(boolean didStartLowRssiSchedule) {
                this.didStartLowRssiSchedule = didStartLowRssiSchedule;
            }

            @Override
            public void scheduleFutureRun(Handler handler, long timeToMills) {
                removeNextCallback();
                super.scheduleFutureRun(handler, timeToMills);
            }

            public void removeNextCallback() {
                futureTasksHandler.removeCallbacks(tagLowRssiTaskManager);
                futureTasksHandler.removeCallbacks(tagHandleProximityRunnable);
                tagHandleProximityRunnable.removeNextCallbacks();
            }
        }

        class TagHandleProximityRunnable extends BaseFutureRunnable {

            private final TagOpenProximityEventRunnable tagOpenProximityEventRunnable = new TagOpenProximityEventRunnable();

            @Override
            public void run() {
                Log.i("bug95_HandleTask","run()");

                if (!isTagInProximityMode && (!LocationManager.isDeviceInPureComZoneState())) {
                    isTagInProximityMode = true;
                    Log.i("bug95_HandleTask","set isTagInProximityMode = true");

                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                            "MainActivity - Proximity Alert Start UI Update, TagHandleProximityRunnable -> isTagInProximityMode = true;", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);

                    Log.i(TAG, "MainActivity - Proximity Alert Start UI Update, Start Sound And Vibrate");

                    String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
                    String csvFile = LoggingUtil.createStringForCSVFile(OperationType.PROXIMITY, HardwareTypeString.New_Tag, tagRFIDFromServer, -1, -1, "",
                            -1, -1, false, -1, -1, -1, -1, "Proximity Alert Start UI Update, Start Sound And Vibrate");

                    LoggingUtil.writeBleLogsToFile(csvFile);

                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFile,
                            DebugInfoModuleId.Ble.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "Proximity Alert Start UI Update"
                                    + ", Start Sound And Vibrate",
                            DebugInfoModuleId.Ble_Others.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            updateHomeScreenUI();
                            manageEventSoundAndVibrate(EventTypes.eventProximityOpen);
                        }
                    });

                    setActivityToForegroundIfNeeded();

                    boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog
                            .getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY) != -1;
                    if (!hasOpenEvent) {
                        long tagProximityGraceTime = TableOffenderDetailsManager.sharedInstance()
                                .getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME);
                        tagOpenProximityEventRunnable.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(tagProximityGraceTime));
                    }
                }
            }

            public void removeNextCallbacks() {
                futureTasksHandler.removeCallbacks(tagOpenProximityEventRunnable);
            }

        }

        class TagOpenProximityEventRunnable extends BaseFutureRunnable {

            @Override
            public void run() {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE, 0);

				/* we check if we should take last time application got broadcast from tag or last time application
				 got good rssi from tag */
                long timeToOpenEvent = LongLastPureTagPacketRx < tagLowRssiTaskManager.getLastGoodRssiTagPacketRx() ?
                        LongLastPureTagPacketRx : tagLowRssiTaskManager.getLastGoodRssiTagPacketRx();

                boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY) != -1;
                if (!hasOpenEvent) { // && !MainApplication.AMIR_DEV_MODE) {
                    String additionalInfo = "Last Advertise: " + TimeUtil.GetTimeString(lastAdvertiseTag, DateFormatterUtil.HMS) + " RSSI: " + tagRssi;
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventProximityOpen, -1, -1, timeToOpenEvent, additionalInfo);
                }
            }
        }

        class MessageReceiveRunnable extends BaseFutureRunnable {

            @Override
            public void run() {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventMessageTimeOut, -1, -1);
            }
        }

        class MinProfileDurationTimeRunnable extends BaseFutureRunnable {

            private boolean isTimerIsRunning;

            public void setTimerIsRunning(boolean isTimerIsRunning) {
                this.isTimerIsRunning = isTimerIsRunning;
            }

            @Override
            public void run() {

                minProfileDurationTimeFutureManager.setTimerIsRunning(false);

                if (shouldBackToNormalProfile()) {
                    handlePMComProfileEnded("no more open events with non normal profile id and above min duration time");
                }
            }

        }

        class MaxProfileDurationTimeRunnable extends BaseFutureRunnable {

            @Override
            public void run() {

                handlePMComProfileEnded("max duration ended");
            }
        }

    }

    class NetworkViewModel implements ViewUpdateListener, DownloadTaskListener {

        @Override
        public void onGpsPointsUploadedFinishedToParse() {
            updateHomeScreenUI();
        }

        @Override
        public void onEventResposeOkFromServer() {
            NetworkRepository.getInstance().switchBetweenCommIntervalModeIfNeeded(NetworkRepository.getInstance().calculateComInterval());
        }

        @Override
        public void onGetDeviceConfigurationResultParserFinishedToParse(boolean isTagIdChanged, boolean isBeaconIdChanged, boolean isCommIntervalChanged,
                                                                        String lastTagRfId, boolean isLocationSettingsChanged, boolean isVoipSettingsChanged, boolean isCaseTamperEnabled,
                                                                        boolean isLockScreenChanged, boolean isAppLanguageChanged) {
            boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
            boolean isPureComZoneEnabled = DatabaseAccess.getInstance().tableOffenderDetails.getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PURECOM_AS_HOME_UNIT) > 0;
            if (!isPureComZoneEnabled) {
                locationManager.handlePureComZone(false);
                startTagActivitiesIfNeeded();
            }

            if (isOffenderAllocated) {
                Intent intent = new Intent(LauncherActivity.LAUNCHER_ACTIVITY_MESSAGE_RECEIVER);
                intent.putExtra(LauncherActivity.LAUNCHER_ACTIVITY_EXTRA, LauncherActivity.MESSAGE_EXTRA);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                treatBleDeviceStatusChanged(isTagIdChanged);
                treatLocationSettingsChanged(isLocationSettingsChanged);
                treatCommIntervalChanged(isCommIntervalChanged);
            }

            //update white list config file - used by external dialer application
            LoggingUtil.createWhiteListConfigFile();

            MojScreenTurnOn = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TURN_ON_SCREEN_MOTION);

            handleCaseTamperEnabledChanged(isCaseTamperEnabled);

            treatLockScreenChanged(isLockScreenChanged);

            addEmergencyButtonIfNeeded();

            changeWriteToLogsSettingsIfNeeded();

            updateHomeScreenUI();

            // start accelerometer if required
            AccelerometerManager.getInstance().updateAccelerometerConfig(TableOffenderDetailsManager.sharedInstance().getAccelerometerSettings());


            boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
            if (isKnoxLicenceActivated) {
                boolean inKioskMode = KnoxUtil.getInstance().getKnoxSDKImplementation().isInKioskMode();
                if (inKioskMode) {
                    KnoxUtil.getInstance().enterOffenderMode(true);
                } else {
                    KnoxUtil.getInstance().enterOfficerMode(true);
                }
            }

            if (isAppLanguageChanged) {
                String localeLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
                setLocale(localeLanguage);
                handleDialerLanguageChanged();
            }
        }

        private void treatLockScreenChanged(boolean isLockScreenChanged) {

            boolean isManufacturerIdEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MANUFACTURER_ID) > 0;
            boolean isMaScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MAC_SCAN_ENABLED) > 0;
            boolean isNormalScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(NORMAL_SCAN_ENABLED) > 0;
            boolean isDozeModeScanEnabled = isMaScanEnabled || isManufacturerIdEnabled;

            if (isLockScreenChanged) {
                if (!isDozeModeScanEnabled || isNormalScanEnabled) {
                    handleLockScreen();
                }
            }
        }

        private void handleCaseTamperEnabledChanged(boolean isCaseTamperEnabled) {
            if (isCaseTamperEnabled) {
                registerMagneticListener();
            } else {
                unregisterMagneticListener();
            }
        }

        private void treatCommIntervalChanged(boolean isCommIntervalChanged) {
            if (isCommIntervalChanged) {
                NetworkRepository.getInstance().scheduleNewCycleIfNeeded(true);
            }
        }

        private void treatLocationSettingsChanged(boolean isLocationSettingsChanged) {
            if (isLocationSettingsChanged) {
                locationManager.startLocationUpdate(false);
            }
        }

        private void treatBleDeviceStatusChanged(boolean isTagIdChanged) {

            if (isTagIdChanged) {

                //if PM changed tag type from regular tag to virtual tag an no beacon exists in DB
                String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
                if (tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
                    // stop mechanism to turn on screen / BLE restart
                    lastTagReceivedTime = 0;
                    lastBleTagRx = 0;
                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                            "Proximity tracking: NetworkRequestsManager -> onGetDeviceConfigurationResultParserFinishedToParse -> treatBleDeviceStatusChanged methode before stopBleActivities ", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);

                    stopBleActivities(false);
                } else {  //if PM changed tag type from virtual tag to regular tag or from regular tag to regular tag
                    // start mechanism to turn on screen / BLE restart
                    lastTagReceivedTime = System.currentTimeMillis();
                    lastBleTagRx = System.currentTimeMillis();
                    startTagActivitiesIfNeeded();
                }
            }
        }

        @Override
        public void onMessageReceivedFromServer(ServerMessageType serverMessageType, int requestId) {
            NetworkRepository.getInstance().setOffenderRequestResultSuccess();
            NetworkRepository.getInstance().sendHandleOffenderRequest();

            String recordTextMessage = "";
            if (DatabaseAccess.getInstance().tableMessages.GetLastMsg() != null && serverMessageType == ServerMessageType.MESSAGE) {
                recordTextMessage = DatabaseAccess.getInstance().tableMessages.GetLastMsg().Text;
            }
            defineNotification(alertDialogArray.size());
            MessageDialog messageDialog;
            MessageDialogListener messageDialogListener = new MessageDialogListener() {
                @Override
                public void onMessageDialogSwipeComplete(ServerMessageType serverMessageType, int requestId) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventMessageAck, -1, -1);
                    if (serverMessageType == ServerMessageType.MESSAGE)
                        handleMessageDialogSwipe();
                    else
                        handlePhotoOnDemandDialog(requestId);
                }
            };
            if (serverMessageType == ServerMessageType.MESSAGE) {
                messageDialog = new MessageDialog(MainActivity.this, messageDialogListener, recordTextMessage, serverMessageType, requestId);
            } else {
                messageDialog = new MessageDialog(MainActivity.this, messageDialogListener, null, serverMessageType, requestId);
            }


            alertDialogArray.add(messageDialog);
            messageDialog.show();
            NetworkRepository.turnOnScreenX();


            long messageTimeOut = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_MESSAGE_TIMEOUT);
            if (serverMessageType == ServerMessageType.MESSAGE)
                messageReceiveFutureTaskManager.scheduleFutureRun(futureTasksHandler, TimeUnit.SECONDS.toMillis(messageTimeOut));
            setActivityToForegroundIfNeeded();
        }

        private void handlePhotoOnDemandDialog(int requestId) {
            FragmentManager fragmentManager = MainActivity.this.getFragmentManager();
            PhotoOnDemandDialog photoOnDemandDialog = new PhotoOnDemandDialog();
            Bundle bundle = new Bundle();
            bundle.putInt(REQUEST_ID_KEY, requestId);
            photoOnDemandDialog.setArguments(bundle);
            photoOnDemandDialog.show(fragmentManager, "");
        }

        @Override
        public void onBiometricReceivedFromServer() {
            BiometricManager.getInstance().startAuthenticate(MainActivity.this);
            NetworkRepository.getInstance().setOffenderRequestResultSuccess();
            NetworkRepository.getInstance().sendHandleOffenderRequest();
        }

        @Override
        public void onUnallocatedReceivedFromServer(int requestId) {
            String serialNumber = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber();
            int databaseSize = DatabaseAccess.getInstance().getDatabaseSize();
            int lastCreatedEventType = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                    (OFFENDER_STATUS_CONS.OFF_LAST_CREATED_EVENT_TYPE);

            String messageToUpload = "Received Unallocate request from PM on: " + TimeUtil.getCurrentTimeStr() + ", Request id: " +
                    requestId + ", Serial number: " + serialNumber + ", Size of db: " + databaseSize + " bytes " +
                    ", Last created event type: " + lastCreatedEventType;

            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + messageToUpload, false);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            onUnallocateRecordUploaded(FlowType.Unallocated);
        }

        @Override
        public void onUnallocateRecordUploaded(FlowType flowType) {

            boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                    == OffenderActivation.OFFENDER_STATUS_ALLOCATED;

            if (!isOffenderAllocated && App.isActivityOnForegroundTop(EnrollmentActivity.class.getName())) {
                finishActivity(ENROLMENT_EXTRA_CODE);
            }

            NetworkRepositoryConstants.isGpsProximityViolationOpened = false;
            NetworkRepositoryConstants.isGpsProximityWarningOpened = false;

            boolean isDeviceResetSuccesfull = resetDBToInitialDeviceState();

            if (isDeviceResetSuccesfull) {
                NetworkRepository.getInstance().sendHandleOffenderRequest();

                String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
                if (isOffenderAllocated && !tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
                    unallocateDialogManager.showNoticeScreen();
                }
            } else {
                NetworkRepository.getInstance().setOffenderReqResultError();
                NetworkRepository.getInstance().sendHandleOffenderRequest();
            }

        }

        @Override
        public void onActivateReceivedFromServer() {

            if (TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS,
                    OffenderActivation.OFFENDER_STATUS_PENDING_ENROLMENT) >= 1) {

                if (!DatabaseAccess.getInstance().tableEventLog.isEventExistsInDB(EventTypes.pendingEnrolment)) {
                    TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS, "");
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.pendingEnrolment, -1, -1);
                    NetworkRepository.getInstance().sendNewEventArray(NetworkRequestName.PostTerminate);

                    updateHomeScreenUI();
                } else {
                    NetworkRepository.getInstance().httpTerminateToken();
                }

            } else {
                App.writeToNetworkLogsAndDebugInfo(TAG, "MainActivity - Can't update OFF_IS_OFFENDER_PENDING_ENROLMENT", DebugInfoModuleId.DB);
                NetworkRepository.getInstance().httpTerminateToken();
            }

        }

        @Override
        public void downloadApkFromServer(String downloadURL, String apkTargetFileName, String versionFromServer, Download_Task_Type downloadTaskType) {
            new DownloadTaskMain(MainActivity.this, apkTargetFileName, this, versionFromServer, downloadTaskType).downloadFromURL(downloadURL);
            App.writeToNetworkLogsAndDebugInfo(TAG, "SW_UPGRADE: Started downloading the new version of [" + apkTargetFileName + "] .", DebugInfoModuleId.Network);
        }

        @Override
        public void installApk(String apkTargetFileName) {
            File apkTargetFile = new File(FilesManager.getInstance().APK_LOCATION + apkTargetFileName);
            NetworkRepository.getInstance().httpTerminateToken();
            installDownloadedApk(apkTargetFile);
        }

        @Override
        public void onManualHandleReceivedFromServer(int openEventId) {

            EntityOpenEventLog openEventByEventId = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenEventByEventId(openEventId);
            if (openEventByEventId != null) {
                boolean isCategoryHasCloseEvent = DatabaseAccess.getInstance().tableEventConfig.isCategoryHasCloseEvent
                        (openEventByEventId.OpenEventViolationCategory);

                StringBuilder log = new StringBuilder();
				/* if has close event, we will update on open event table that this event was handled manually, and then we will not use it while calculate
				offender status, else we will delete this event from open event table */
                if (isCategoryHasCloseEvent) {
                    DatabaseAccess.getInstance().tableOpenEventsLog.updateIsHandleColumnByOpenEventId(openEventId);
                    log.append("manual handle, updated event ").append(openEventId).append(" type ").append(openEventByEventId.OpenEventType).append(" to handle status, since he has close event");
                } else {
                    DatabaseAccess.getInstance().tableOpenEventsLog.deleteOpenEventFromDB(openEventId);
                    log.append("manual handle, deleted open event ").append(openEventId).append(" type ").append(openEventByEventId.OpenEventType);
                }

                TableEventsManager.sharedInstance().updateOffenderStatus();
                App.writeToNetworkLogsAndDebugInfo(TAG, log.toString(), DebugInfoModuleId.Network);
            } else {
                App.writeToNetworkLogsAndDebugInfo(TAG, "Failed to do manual handle, since openEventId " + openEventId + " not exists in local DB",
                        DebugInfoModuleId.Network);
            }

            if (shouldBackToNormalProfile()) {
                handlePMComProfileEnded("Manual handle received from server. No more open events with last profile id and above min duration time");
            }

            NetworkRepository.getInstance().sendHandleOffenderRequest();
        }

        @Override
        public void onHandleResponseSucceeded(int requestType) {
            if (NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED == OffenderRequestType.ACTIVATE) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS, OffenderActivation.OFFENDER_STATUS_ALLOCATED);

                locationManager.startLocationUpdate(false);

                registerMagneticListener();
                registerTemperatureListener();
                startTagActivitiesIfNeeded();

                TableZonesManager.sharedInstance().checkBeaconZoneStatus();

                biometricScheduleManager.handleBiometricScheudleIfExists();

                updateAppointmentsInScheduleScreen();

                updateUIReceiver.registerForNextClosestTimeAppointment();

                updateHomeScreenUI();

                if (!DatabaseAccess.getInstance().tableEventLog.isEventExistsInDB(EventTypes.eventMonitoringStarted)) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventMonitoringStarted, -1, -1,
                            NumberComputationUtil.createRandomPassword().toString());
                }
            }
        }

        @Override
        public void onBeaconZoneAddedToDB() {
            if (bluetoothManager == null) {
                bluetoothManager = new BluetoothManager(MainActivity.this, true);
            }
            bluetoothManager.initBeaconIndexesToDefaultValues();

            restartBleScan();
        }

        @Override
        public void onBeaconZoneDeletedFromDB() {
            String additionalInfo = "Beacon was deleted from server";
            TableZonesManager.sharedInstance().handleOutsideBeaconZone(true, additionalInfo);
            String tagRFIDFromServer = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
            boolean isBeaconExistsInDBZone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId((int) TableOffenderDetailsManager.sharedInstance()
                    .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID)) != null;
            if (tagRFIDFromServer.equals(BluetoothManager.NO_TAG)) {
                stopBleScan();
            } else if (!isBeaconExistsInDBZone) {
                restartBleScan();
            }

        }

        @Override
        public void onZonesRequestFinishedToParse() {
            updateUIReceiver.registerForNextClosestTimeAppointment();

            EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
            if (offenderLastGpsPoint != null) {
                TableZonesManager.sharedInstance().checkZoneIntersection(offenderLastGpsPoint);
            }

            TableZonesManager.sharedInstance().checkBeaconZoneStatus();

            if (!isOffenderInSuspendSchedule)
                updateHomeScreenUI();

            biometricScheduleManager.handleBiometricScheudleIfExists();

            updateAppointmentsInScheduleScreen();
        }

        private void updateAppointmentsInScheduleScreen() {
            if (schedulePagerAdapter != null) {
                ScheduleTab fragment = (ScheduleTab) schedulePagerAdapter.getItemAt(mPager.getCurrentItem());
                if (fragment != null) { // could be null if not instantiated yet

                    if (fragment.getView() != null) {

                        List<EntityScheduleOfZones> recordsScheduleOfZonesList = null;

                        int startTimeOffset = (mPager.getCurrentItem() - 2) * 24;
                        int endTimeOffset = (mPager.getCurrentItem() - 1) * 24;

                        recordsScheduleOfZonesList = TableScheduleOfZones.sharedInstance().getDayScheduleWhereUserMustBeIn(startTimeOffset, endTimeOffset);
                        fragment.updateAppointmentScheduleList(recordsScheduleOfZonesList);
                    }
                }
            }
        }

        @Override
        public void onSucceededToDownloadFileFromServer(File apkTargetFile, String apkTargetShortFileName, String versionFromServer, Download_Task_Type downloadTaskType) {
            String messageToUpload = "SW_UPGRADE: Success downloading new version of [ " + apkTargetShortFileName + " " + versionFromServer + " ] .";
            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Network);

            if (downloadTaskType == Download_Task_Type.PT_Version_Upgrade) {
                NetworkRepository.getInstance().httpTerminateToken();
                TableOffenderStatusManager.sharedInstance().updateColumnString(OFFENDER_STATUS_CONS.OFF_DEVICE_DOWNLOADED_VERSION, versionFromServer);
                installDownloadedApk(apkTargetFile);
            } else if (downloadTaskType == Download_Task_Type.All_Apk_Upgrade) {
                NetworkRepository.getInstance().httpTerminateToken();
                openApkInstallDialog(apkTargetFile);
            } else if (downloadTaskType == Download_Task_Type.Google_Play_Version) {
                openApkInstallDialog(apkTargetFile);
            }
        }

        @Override
        public void onFailedToDownloadFileFromServer(String result, String apkTargetShortFileName, String versionFromServer, Download_Task_Type downloadTaskType) {
            String messageToUpload = "SW_UPGRADE: Failed downloading new version of [ " + apkTargetShortFileName + " " + versionFromServer + " ] . Error : " + result;
            App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.errors);

            if (downloadTaskType == Download_Task_Type.PT_Version_Upgrade || downloadTaskType == Download_Task_Type.All_Apk_Upgrade) {
                NetworkRepository.getInstance().handleOffenderRequestError();
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.softwareUpgradeFailed, -1, -1);
            }
        }

        private void installDownloadedApk(File apkTargetFile) {

            isUpgradeToNewVersionScreenShouldOpen = false;
            isCameFromRegularActivityCode = true;
            if (!setActivityToForegroundIfNeeded()) {
                openApkInstallDialog(apkTargetFile);
            }
            //Will wait 5 min for the User to install the Apk. After the time out will send softwareUpgradeTimeOut event.
            upgradeTimeoutHandler.postDelayed(upgradeTimeoutScreenRunnable, TimeUnit.MINUTES.toMillis(5));
            //	}
        }

        @Override
        public void addLbsLocation(EntityGpsPoint lbsRecord) {
            locationManager.handleNewLbsLocation(lbsRecord);
        }

        @Override
        public boolean isLbsLocationRequestRequired() {
            return locationManager.IsLbsRequestRequired();
        }

        @Override
        public void onOffenderAtHomeStatusChanged(boolean isOffenderAtHome) {
            boolean previousPureComZoneMode = LocationManager.isDeviceInPureComZoneState();
            locationManager.handlePureComZone(isOffenderAtHome);
            // turn on/off bluetooth scanning - to save battery when at PureCom beacon mode
            if (isOffenderAtHome) {
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        "Proximity tracking: NetworkRequestsManager -> onOffenderAtHomeStatusChanged method before stopBleActivities ", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
                stopBleActivities(true);
            } else {
                if (previousPureComZoneMode) {
                    startTagActivitiesIfNeeded();
                }
            }
        }


        @Override
        public void enableFlightMode(int timeOut) {
            boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
            if (isKnoxLicenceActivated) {

                KnoxUtil.getInstance().initDeviceToInitiatedFlightMode();

                initInitiatedFlightModeAlarmIfNeeded(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeOut));

                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        "Proximity tracking: NetworkRequestsManager -> enableFlightMode methode before stopBleActivities ", DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
                stopBleActivities(true);

                TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_TIME_INITIATED_FLIGHT_MODE_END,
                        System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeOut));

                // UI actions
                if (currentScreen == ScreenType.Call) {
                    // If during "call" screen and flight mode starts, go back to main screen
                    initScreen(ScreenType.Home);
                } else {
                    // disable "call" button: home, schedule, msg screens
                    callButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.btn_call_disabled), null, null);
                }
            }
        }
    }
}


