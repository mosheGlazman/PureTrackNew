package com.supercom.puretrack.data.source.remote.parsers;

import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;

import android.app.enterprise.knoxcustom.CustomDeviceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.repositories.SyncRequestsRepository;
import com.supercom.puretrack.data.service.heart_beat.HeartBeatServiceJava2;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.AutoRestartManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.DeviceShieldingManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetDeviceConfigurationListener;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.SyncType;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;
import com.supercom.puretrack.model.database.entities.EntityDeviceJamming;
import com.supercom.puretrack.model.database.entities.EntityDeviceShielding;
import com.supercom.puretrack.model.database.entities.EntityScannerType;
import com.supercom.puretrack.model.database.entities.EntitySelfDiagnosticEvent;
import com.supercom.puretrack.model.database.entities.EntityTagMotion;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.objects.VoipSettings;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.KnoxConfigurations;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.ui.DeveloperUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GetDeviceConfigurationResultParser implements GetDeviceConfigurationListener {

    public static String TAG = "DeviceConfigurationReqHandler";

    private final ViewUpdateListener updateActivityListener;

    // pin code default
    private final int PIN_CODE_IS_ENABLED_DEFAULT = 0;
    private final int PIN_CODE_ATTEMPTS_DEFAULT = 5;
    private final String PIN_CODE_DEFAULT = "3215";
    private final int PIN_CODE_LOCK_TIME_DEFAULT = 120;

    public GetDeviceConfigurationResultParser(ViewUpdateListener updateUiListener) {
        this.updateActivityListener = updateUiListener;
    }

    public enum DeviceConfigurationType {
        Device_Configuration,
        Offender_Configuration
    }

    @Override
    public void handleResponse(String response, DeviceConfigurationType deviceConfigurationType, int versionNumber) {
        if (response == null) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");

            SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.DEVICE_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_ERR);
            return;
        }

        JSONObject result;

        int commInterval;

        JSONArray serversArray;
        String[] IP = new String[500];
        boolean[] IsHttps = new boolean[500];
        String[] Service_URL = new String[500];

        int Comm_Interval_Low;
        int Sched_Expire;
        int DebugInfoConfig;
        int DeviceInfoCycles;
        String Emergency;

        JSONArray jArrayText_Message;

        JSONArray jArrayBiometric;
        int[] BioTimeout = new int[100];
        int[] BioMin_Between = new int[100];

        JSONArray offenderDeviceConfiguration = null;
        int OffenderID;
        String PicPath;
        String TagRfIDFromServer = "-1";
        int Schedule_Grace;
        String OfficerNum;
        String AgencyNum;
        String AgencyName;
        String MapUrl;
        int IsEmergencyEnabled;
        int HomeRssiRange;
        int OutsideRssiRange;

        JSONArray jArrayPureBeacons = null;
        String[] PureBeaconName = new String[50];
        String[] PureBeaconSerialNumber = new String[50];

        int[] PureBeaconZoneId = new int[50];
        int[] PureBeaconZoneVersion = new int[50];
        JSONArray jArrayTagSettings;
        int IR_ON;
        int IR_OFF;
        int Case_Open;
        int Low_Batt;

        String Password;
        String currentNormalItem = "";
        /*********************
         *
         * Start the parsing
         *
         *******************/
        String afterDecode;
        afterDecode = response.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            result = new JSONObject(afterDecode);
            result = result.getJSONObject("GetDeviceConfigurationResult");
            JSONArray deviceConfiguration = result.getJSONArray("data");

            String lastTagRfId = "-1";
            boolean isTagIdChanged = false;
            boolean isBeaconIdChanged = false;
            boolean isLocationSettingsChanged = false;
            boolean isVoipSettingsEnableChanged = false;
            boolean isCaseTamperEnabled = false;
            boolean isLockScreenChanged = false;
            boolean isAppLanguageChanged = false;


            boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;

            if (deviceConfiguration.getJSONObject(0).has("zone_drifting_locations")) {
                currentNormalItem = "zone_drifting_locations";

                JSONArray jArrayLocationSettings = deviceConfiguration.getJSONObject(0).getJSONArray("zone_drifting_locations");

                if (jArrayLocationSettings.getJSONObject(0).has("enabled")) {
                    int drift_zone_enabled = jArrayLocationSettings.getJSONObject(0).getInt("enabled");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_ENABLE, drift_zone_enabled);
                }

                if (jArrayLocationSettings.getJSONObject(0).has("Confirm_GPS_interval")) {
                    int Confirm_GPS_interval = jArrayLocationSettings.getJSONObject(0).getInt("Confirm_GPS_interval");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_GPS_INT, Confirm_GPS_interval);
                }
                if (jArrayLocationSettings.getJSONObject(0).has("Confirm_duration")) {
                    int Confirm_duration = jArrayLocationSettings.getJSONObject(0).getInt("Confirm_duration");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_DURATION, Confirm_duration);
                }
                if (jArrayLocationSettings.getJSONObject(0).has("Confirm_locations")) {
                    int Confirm_locations = jArrayLocationSettings.getJSONObject(0).getInt("Confirm_locations");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_ZONE_DRIFT_LOCATIONS, Confirm_locations);
                }
            }


            if (deviceConfiguration.getJSONObject(0).has("Servers_Array")) {
                try {
                    currentNormalItem = "Servers_Array";

                    serversArray = deviceConfiguration.getJSONObject(0).getJSONArray("Servers_Array");

                    for (int i = 0; i < serversArray.length(); i++) {
                        IP[i] = serversArray.getJSONObject(i).getString("IP");
                        IsHttps[i] = serversArray.getJSONObject(i).getBoolean("IsHttps");
                        Service_URL[i] = serversArray.getJSONObject(i).getString("Service_URL");
                        Log.i("Servers_Array", "Receive url:   " + ((IsHttps[0]) ? "https://" + IP[0] + "/" + Service_URL[0] + "/" : "http://" + IP[0] + "/" + Service_URL[0] + "/"));
                    }

                    String receivedServerUrl = (IsHttps[0]) ? "https://" + IP[0] + "/" + Service_URL[0] + "/" : "http://" + IP[0] + "/" + Service_URL[0] + "/";
                    // add encrypt decrypt server url
                    String currentServerUrl = "";
                    try {
                        currentServerUrl = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_URL);
                        Log.i("Servers_Array", "decrypt currentServerUrl: " + currentServerUrl + " by key " + SERVER_URL_AES_KEY_BYTES);
                        currentServerUrl = AESUtils.decrypt(SERVER_URL_AES_KEY_BYTES, currentServerUrl);
                        Log.i("Servers_Array", "currentServerUrl:" + currentServerUrl);
                    } catch (Exception e) {
                        Log.e("Servers_Array", "decrypt error", e);
                    }

                    if (currentServerUrl != null && currentServerUrl.length() > 0 && !currentServerUrl.equals(receivedServerUrl)) {
                        Log.i("Servers_Array", "receivedServerUrl:" + receivedServerUrl);

                        // add encrypt decrypt server url
                        String encURL = "";
                        try {
                            Log.i("Servers_Array", "encrypt receivedServerUrl");
                            encURL = AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, receivedServerUrl);
                            Log.i("Servers_Array", "after encrypt receivedServerUrl:" + encURL);
                        } catch (Exception e) {
                            Log.e("Servers_Array", "encrypt error", e);
                        }
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_URL, encURL);
                    }
                } catch (Exception e) {
                    Log.e("Servers_Array", "error", e);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Cellular_APN")) {
                currentNormalItem = "Cellular_APN";
                JSONArray cellularApnArray = deviceConfiguration.getJSONObject(0).getJSONArray("Cellular_APN");
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.CELLULAR_APN, cellularApnArray.toString());
            }
            if (deviceConfiguration.getJSONObject(0).has("AccelerometrerMotion")) {
                String accelerometerJsonToDB = "[{\"";
                JSONArray accelerometerMotion = deviceConfiguration.getJSONObject(0).getJSONArray("AccelerometrerMotion");
                if (accelerometerMotion.getJSONObject(0).has("Enabled")) {
                    currentNormalItem = "Enabled";
                    int Enabled = accelerometerMotion.getJSONObject(0).getInt("Enabled");
                    accelerometerJsonToDB += currentNormalItem + "\":" + Enabled;
                }

                if (accelerometerMotion.getJSONObject(0).has("MotionWinSamples")) {
                    currentNormalItem = "MotionWinSamples";
                    int MotionWinSamples = accelerometerMotion.getJSONObject(0).getInt("MotionWinSamples");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + MotionWinSamples;
                }

                if (accelerometerMotion.getJSONObject(0).has("MotionWinPercentage")) {
                    currentNormalItem = "MotionWinPercentage";
                    int MotionWinPercentage = accelerometerMotion.getJSONObject(0).getInt("MotionWinPercentage");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + MotionWinPercentage;
                }
                if (accelerometerMotion.getJSONObject(0).has("MotionThreshold")) {
                    currentNormalItem = "MotionThreshold";
                    double MotionThreshold = accelerometerMotion.getJSONObject(0).getDouble("MotionThreshold");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + MotionThreshold;
                }
                if (accelerometerMotion.getJSONObject(0).has("StaticWinSamples")) {
                    currentNormalItem = "StaticWinSamples";
                    int StaticWinSamples = accelerometerMotion.getJSONObject(0).getInt("StaticWinSamples");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + StaticWinSamples;
                }
                if (accelerometerMotion.getJSONObject(0).has("StaticWinPercentage")) {
                    currentNormalItem = "StaticWinPercentage";
                    int StaticWinPercentage = accelerometerMotion.getJSONObject(0).getInt("StaticWinPercentage");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + StaticWinPercentage;
                }
                if (accelerometerMotion.getJSONObject(0).has("staticThreshold")) {
                    currentNormalItem = "staticThreshold";
                    double staticThreshold = accelerometerMotion.getJSONObject(0).getDouble("staticThreshold");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + staticThreshold;
                }
                if (accelerometerMotion.getJSONObject(0).has("motion_sample_time")) {
                    currentNormalItem = "motion_sample_time";
                    int motion_sample_time = accelerometerMotion.getJSONObject(0).getInt("motion_sample_time");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + motion_sample_time;
                }
                if (accelerometerMotion.getJSONObject(0).has("motion_window_time")) {
                    currentNormalItem = "motion_window_time";
                    int motion_window_time = accelerometerMotion.getJSONObject(0).getInt("motion_window_time");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + motion_window_time;
                }
                if (accelerometerMotion.getJSONObject(0).has("motion_window_level")) {
                    currentNormalItem = "motion_window_level";
                    int motion_window_level = accelerometerMotion.getJSONObject(0).getInt("motion_window_level");
                    accelerometerJsonToDB += "," + "\"" + currentNormalItem + "\":" + motion_window_level;
                }
                accelerometerJsonToDB += "}]";
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.ACCELEROMETER_SETTINGS,
                        accelerometerJsonToDB);
            }

            if (deviceConfiguration.getJSONObject(0).has("SuddenShutDown")) {
                try {
                    Log.i("bug652", "params");

                    JSONArray array = deviceConfiguration.getJSONObject(0).getJSONArray("SuddenShutDown");
                    OffenderPreferencesManager.getInstance().setSuddenShutDownParams(array.getJSONObject(0).toString());
                    Log.i("bug652", "params: " +array.getJSONObject(0).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("bug652", "error", e);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("auto_restart")) {
                try {
                    Log.i("bug653", "params");
                    JSONArray array = deviceConfiguration.getJSONObject(0).getJSONArray("auto_restart");
                    AutoRestartManager.getInstance().setAutoRestartParams(array.getJSONObject(0).toString());
                    Log.i("bug653", "json: " +array.getJSONObject(0).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("bug653", "error: " + deviceConfiguration.getJSONObject(0).getString("auto_restart"), e);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Offender_Configuration_Array")) {
                offenderDeviceConfiguration = deviceConfiguration.getJSONObject(0).getJSONArray("Offender_Configuration_Array");
                for (int i = 0; i < offenderDeviceConfiguration.length(); i++) {

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagVibrateOnDisconnect")) {
                        try {

                            JSONArray array = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("TagVibrateOnDisconnect");
                            OffenderPreferencesManager.getInstance().setTagVibrateOnDisconnectParams(array.getJSONObject(0).toString());
                            Log.i("bug652", "params: " +array.getJSONObject(0).toString());

                            if(OffenderPreferencesManager.getInstance().getTagVibrateOnDisconnectParams().enabled==1){
                                HeartBeatServiceJava2.start();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("bug652", "error", e);
                        }
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagType")) {
                        currentNormalItem = "TagType";
                        int tagType = offenderDeviceConfiguration.getJSONObject(i).getInt("TagType");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_TYPE, tagType);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagRfID")) {
                        currentNormalItem = "TagRfID";
                        TagRfIDFromServer = offenderDeviceConfiguration.getJSONObject(i).getString("TagRfID");
                        lastTagRfId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
                        if (!TagRfIDFromServer.equals(lastTagRfId) && isOffenderAllocated) {
                            TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS, "");
                            isTagIdChanged = true;

                            String messageToUpload = "\nNew Tag: " + TagRfIDFromServer;
                            LoggingUtil.updateNetworkLog(messageToUpload, false);
                            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                                    DebugInfoModuleId.Ble_Others.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                        }
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID, TagRfIDFromServer);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(0).has("AppLanguage")) {
                        currentNormalItem = "AppLanguage";
                        String oldAppLanguage = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.APP_LANGUAGE).toLowerCase();
                        String appLanguage = offenderDeviceConfiguration.getJSONObject(0).getString("AppLanguage");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.APP_LANGUAGE, appLanguage.toLowerCase());
                        if (!oldAppLanguage.equals(currentNormalItem)) {
                            isAppLanguageChanged = true;
                        }
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagID")) {
                        currentNormalItem = "TagID";
                        int tagID = offenderDeviceConfiguration.getJSONObject(i).getInt("TagID");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ID, tagID);
                    }



                    if (offenderDeviceConfiguration.getJSONObject(i).has("OffenderID")) {
                        currentNormalItem = "OffenderID";
                        OffenderID = offenderDeviceConfiguration.getJSONObject(i).getInt("OffenderID");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_ID, OffenderID);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("PicPath")) {
                        currentNormalItem = "PicPath";
                        PicPath = offenderDeviceConfiguration.getJSONObject(i).getString("PicPath");
                        String fullPicPath = (IsHttps[0]) ? "https://" + PicPath : "http://" + PicPath;
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_PICTURE_PATH, fullPicPath);
                    } else {
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_PICTURE_PATH, "NA");
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Encryption")) {
                        currentNormalItem = "Encryption";
                        String encryption = offenderDeviceConfiguration.getJSONObject(i).getString("Encryption");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ENCRYPTION, encryption);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Schedule_Grace")) {
                        currentNormalItem = "Schedule_Grace";
                        Schedule_Grace = offenderDeviceConfiguration.getJSONObject(i).getInt("Schedule_Grace");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_SCHEDULE_GRACE, Schedule_Grace);
                    }

//                    if (deviceConfiguration.getJSONObject(i).has("Device_Shielding")) {
//                        currentNormalItem = "Device_Shielding";
//                        JSONObject deviceShielding = deviceConfiguration.getJSONObject(i).getJSONObject("Device_Shielding");
//
//                        JSONObject shielding = deviceShielding.getJSONObject("Shielding");
//                        int enabled = shielding.getInt("enabled");
//                        int cellularReceptionSampleInterval = shielding.getInt("cellular_reception_sample_interval");
//                        int minGoodCellularLevelWcdma3G = shielding.getInt("min_good_cellular_level_wcdma_3G");
//                        int minGoodCellularLevelLte4G = shielding.getInt("min_good_cellular_level_lte_4G");
//                        int idleToTriggeredToIdleCellularLevelSensitivity = shielding.getInt("idle_to_triggered_to_idle_cellular_level_sensitivity");
//                        int triggeredGoodCellularLevelPercentage = shielding.getInt("triggered_good_cellular_level_percentage");
//                        int cellularReceptionDropThreshold = shielding.getInt("cellular_reception_drop_threshold");
//                        int cellularReceptionDropInterval = shielding.getInt("cellular_reception_drop_interval");
//                        int shieldEventTimerSensitivity = shielding.getInt("shield_event_timer_sensitivity");
//                        int stopShieldMinReceptionLevelPercentage = shielding.getInt("stop_shield_min_reception_level_percentage");
//                        int stopShieldMinReceptionLevelDuration = shielding.getInt("stop_shield_min_reception_level_duration");
//                        int stopShieldMinReceptionLevelWcdma3G = shielding.getInt("stop_shield_min_reception_level_wcdma_3G");
//                        int stopShieldMinReceptionLevelLte4G = shielding.getInt("stop_shield_min_reception_level_lte_4G");
//                        int stopShieldOpenEventConditionsNoBluetooth = shielding.getInt("stop_shield_open_event_conditions_no_bluetooth");
//                        int stopShieldOpenEventConditionsNoLocation = shielding.getInt("stop_shield_open_event_conditions_no_location");
//                        int stopShieldOpenEventConditionsNoLight = shielding.getInt("stop_shield_open_event_conditions_no_light");
//                        int stopShieldCloseEventConditionsNoBluetooth = shielding.getInt("stop_shield_close_event_conditions_no_bluetooth");
//                        int stopShieldCloseEventConditionsNoLocation = shielding.getInt("stop_shield_close_event_conditions_no_location");
//                        int stopShieldCloseEventConditionsNoLight = shielding.getInt("stop_shield_close_event_conditions_no_light");
//
//                        EntityDeviceShielding recordDeviceShielding = new EntityDeviceShielding(enabled, cellularReceptionSampleInterval,
//                                minGoodCellularLevelWcdma3G, minGoodCellularLevelLte4G, idleToTriggeredToIdleCellularLevelSensitivity,
//                                triggeredGoodCellularLevelPercentage, cellularReceptionDropThreshold, cellularReceptionDropInterval, shieldEventTimerSensitivity,
//                                stopShieldMinReceptionLevelPercentage, stopShieldMinReceptionLevelDuration, stopShieldMinReceptionLevelWcdma3G, stopShieldMinReceptionLevelLte4G,
//                                stopShieldOpenEventConditionsNoBluetooth, stopShieldOpenEventConditionsNoLocation, stopShieldOpenEventConditionsNoLight,
//                                stopShieldCloseEventConditionsNoBluetooth, stopShieldCloseEventConditionsNoLocation, stopShieldCloseEventConditionsNoLight
//                        );
//
//                        DatabaseAccess.getInstance().tableDeviceShielding.insertRecord(recordDeviceShielding);
//                    }


                    if (offenderDeviceConfiguration.getJSONObject(i).has("OfficerNum")) {
                        currentNormalItem = "OfficerNum";
                        OfficerNum = offenderDeviceConfiguration.getJSONObject(i).getString("OfficerNum");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONE_OFFICER, OfficerNum);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("AgencyNum")) {
                        currentNormalItem = "AgencyNum";
                        AgencyNum = offenderDeviceConfiguration.getJSONObject(i).getString("AgencyNum");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONE_AGENCY, AgencyNum);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("AgencyName")) {
                        currentNormalItem = "AgencyName";
                        AgencyName = offenderDeviceConfiguration.getJSONObject(i).getString("AgencyName");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_AGENCY_NAME, AgencyName);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Emergency_ON")) {
                        currentNormalItem = "Emergency_ON";
                        IsEmergencyEnabled = offenderDeviceConfiguration.getJSONObject(i).getInt("Emergency_ON");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_IS_EMERGENCY_ENABLED, IsEmergencyEnabled);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Home_Range")) {
                        currentNormalItem = "Home_Range";
                        HomeRssiRange = offenderDeviceConfiguration.getJSONObject(i).getInt("Home_Range");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_RSSI_HOME_RANGE, HomeRssiRange);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Attached_Range")) {
                        currentNormalItem = "Attached_Range";
                        OutsideRssiRange = offenderDeviceConfiguration.getJSONObject(i).getInt("Attached_Range");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE, OutsideRssiRange);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagRfID")) {
                        currentNormalItem = "TagRfID";
                        int newHbEnableFromServer = offenderDeviceConfiguration.getJSONObject(i).getInt("HbEnable");
                        boolean isCurrentHBEnableFromServer = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_ENABLE_FROM_SERVER) == 1;
                        if(isCurrentHBEnableFromServer){
                            HeartBeatServiceJava2.start();
                        }

                        TableOffenderDetailsManager.sharedInstance().updateColumnLong(OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_ENABLE_FROM_SERVER, newHbEnableFromServer);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagHeartBeatInterval")) {
                        currentNormalItem = "TagHeartBeatInterval";
                        int tagHBInterval = offenderDeviceConfiguration.getJSONObject(i).getInt("TagHeartBeatInterval");
                        long currentTagHbInterval = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_INTERVAL);
                        TableOffenderDetailsManager.sharedInstance().updateColumnLong(OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_INTERVAL,
                                tagHBInterval);

                        boolean isCurrentHBEnableFromServer = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                                (OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_ENABLE_FROM_SERVER) == 1;
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagHeartBeatTimeoutToVibrate")) {
                        currentNormalItem = "TagHeartBeatTimeoutToVibrate";
                        int tagHBTimeoutToVibrate = offenderDeviceConfiguration.getJSONObject(i).getInt("TagHeartBeatTimeoutToVibrate");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_TAG_HB_VIBRATE_TIMEOUT,
                                tagHBTimeoutToVibrate);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("mapUrl")) {
                        currentNormalItem = "mapUrl";
                        MapUrl = offenderDeviceConfiguration.getJSONObject(i).getString("mapUrl");
                        // fix /\ issue
                        MapUrl.replace("\\", "");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_MAP_URL, MapUrl);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Tag_Proximity_Grace_Time")) {
                        currentNormalItem = "Tag_Proximity_Grace_Time";
                        int tagProximityGraceTime = offenderDeviceConfiguration.getJSONObject(i).getInt("Tag_Proximity_Grace_Time");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME,
                                tagProximityGraceTime);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("PureBeacons")) {
                        currentNormalItem = "PureBeacons";
                        jArrayPureBeacons = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("PureBeacons");
                    }
                    String beaconId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID);

                    if (jArrayPureBeacons.length() == 0) {
                        // sprint 24
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID, "0");
                    }

                    //if beacon was allocate, and now unallocated
                    if (jArrayPureBeacons != null && jArrayPureBeacons.length() == 0 && !beaconId.isEmpty()) {
                        isBeaconIdChanged = true;
                    } else {
                        for (int m = 0; m < jArrayPureBeacons.length(); m++) {
                            if (jArrayPureBeacons.getJSONObject(m).has("Name")) {
                                PureBeaconName[m] = jArrayPureBeacons.getJSONObject(m).getString("Name");
                                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_NAME, PureBeaconName[m]); // New

                                if (jArrayPureBeacons.getJSONObject(m).has("S/N")) {
                                    PureBeaconSerialNumber[m] = jArrayPureBeacons.getJSONObject(m).getString("S/N");
                                    String oldBeaconSerialNumber = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID);
                                    if (!PureBeaconSerialNumber[m].equals(oldBeaconSerialNumber)) {
                                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_BEACON_ADDRESS, "");
                                        isBeaconIdChanged = true;
                                    }
                                    TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID, PureBeaconSerialNumber[m]); // New
                                }
                            }

                            //since there is a bug in PM while unallocated beacon, i update zoneId while get the zone and not here
                            if (jArrayPureBeacons.getJSONObject(m).has("Zone_ID")) {
                                PureBeaconZoneId[m] = jArrayPureBeacons.getJSONObject(m).getInt("Zone_ID");
                                // DBOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID, PureBeaconZoneId[m]);
                            }

                            if (jArrayPureBeacons.getJSONObject(m).has("Zone_Ver")) {
                                PureBeaconZoneVersion[m] = jArrayPureBeacons.getJSONObject(m).getInt("Zone_Ver");
                                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_VERSION, PureBeaconZoneVersion[m]); // New Allocate
                            }

                            if (jArrayPureBeacons.getJSONObject(m).has("Encryption")) {
                                String beaconEncryption = jArrayPureBeacons.getJSONObject(m).getString("Encryption");
                                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ENCRYPTION, beaconEncryption);
                            }
                        }
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("TagSettings")) {
                        currentNormalItem = "TagSettings";
                        jArrayTagSettings = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("TagSettings");
                        for (int k = 0; k < jArrayTagSettings.length() - 1; k++) {
                            if (jArrayTagSettings.getJSONObject(k).has("Txinterval")) {
                                int Txinterval = jArrayTagSettings.getJSONObject(k).getInt("Txinterval");
                                // DBOffenderDetailsManager.sharedInstance().updateOffenderDetailsColumnVal(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_SETTINGS_TXINTERVAL,
                                // Txinterval, "");
                            }

                            if (jArrayTagSettings.getJSONObject(k).has("IR_ON")) {
                                IR_ON = jArrayTagSettings.getJSONObject(k).getInt("IR_ON");
                                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_SETTINGS_IR_ON, IR_ON);
                            }

                            if (jArrayTagSettings.getJSONObject(k).has("IR_OFF")) {
                                IR_OFF = jArrayTagSettings.getJSONObject(k).getInt("IR_OFF");
                                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF, IR_OFF);
                            }

                            if (jArrayTagSettings.getJSONObject(k).has("Case_Open")) {
                                Case_Open = jArrayTagSettings.getJSONObject(k).getInt("Case_Open");
                                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN, Case_Open);
                            }

                            if (jArrayTagSettings.getJSONObject(k).has("Low_Batt")) {
                                Low_Batt = jArrayTagSettings.getJSONObject(k).getInt("Low_Batt");
                                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY, Low_Batt);
                            }
                        }
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("BleDebugInfoEnabled")) {
                        currentNormalItem = "BleDebugInfoEnabled";
                        int bleDebugInfoEnabled = offenderDeviceConfiguration.getJSONObject(i).getInt("BleDebugInfoEnabled");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_BLE_DEBUG_INFO_ENABLE,
                                bleDebugInfoEnabled);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Battery_Indication")) {
                        currentNormalItem = "Battery_Indication";
                        int isBatteryIndicationEnabled = offenderDeviceConfiguration.getJSONObject(i).getInt("Battery_Indication");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_IS_BATTERY_INDICATION_ENABLED, isBatteryIndicationEnabled);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("BLETagSettings"))
                        currentNormalItem = "BLETagSettings";
                    {
                        JSONArray bleTagSettingsJSONArr = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("BLETagSettings");
                        String bleTagSettingsJSONArrString = "{\"BLETagSettings\":[";
                        for (int j = 0; j < bleTagSettingsJSONArr.length(); j++) {
                            String objKey = bleTagSettingsJSONArr.getJSONObject(j).names().getString(0);
                            bleTagSettingsJSONArrString = bleTagSettingsJSONArrString + String.format("\"%s\"", bleTagSettingsJSONArr.getJSONObject(j).getString(objKey));
                            if (j != bleTagSettingsJSONArr.length() - 1) {
                                bleTagSettingsJSONArrString = bleTagSettingsJSONArrString + ",";
                            }
                        }
                        bleTagSettingsJSONArrString = bleTagSettingsJSONArrString + "]}";

                        JsonParser jsonParser = new JsonParser();
                        JsonObject jo = (JsonObject) jsonParser.parse(bleTagSettingsJSONArrString);
                        JsonArray bleTagSettingsJsonArr = jo.getAsJsonArray("BLETagSettings");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_TAG_CONFIGURATIONS, bleTagSettingsJsonArr.toString());

                        if (isOffenderAllocated) {
                            Gson gson = new Gson();
                            ArrayList<String> bleTagSettingsArr = gson.fromJson(bleTagSettingsJsonArr, new TypeToken<ArrayList<String>>() {
                            }.getType());

                        }
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("AgencyNum_ON")) {
                        currentNormalItem = "AgencyNum_ON";
                        int agencyNumOn = offenderDeviceConfiguration.getJSONObject(i).getInt("AgencyNum_ON");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_AGENCY_NUM_ON,
                                agencyNumOn);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("OfficerNum_ON")) {
                        currentNormalItem = "OfficerNum_ON";
                        int officerNumOn = offenderDeviceConfiguration.getJSONObject(i).getInt("OfficerNum_ON");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_OFFICER_NUM_ON,
                                officerNumOn);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("FirstName")) {
                        currentNormalItem = "FirstName";
                        String firstName = offenderDeviceConfiguration.getJSONObject(i).getString("FirstName");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_FIRST_NAME,
                                firstName);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("PureID")) {
                        currentNormalItem = "PureID";
                        String pureID = offenderDeviceConfiguration.getJSONObject(i).getString("PureID");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_SN, pureID);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("LastName")) {
                        currentNormalItem = "LastName";
                        String lastName = offenderDeviceConfiguration.getJSONObject(i).getString("LastName");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_LAST_NAME,
                                lastName);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Address")) {
                        currentNormalItem = "Address";
                        String address = offenderDeviceConfiguration.getJSONObject(i).getString("Address");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_ADDRESS, address);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Offender_Phones")) {
                        currentNormalItem = "Offender_Phones";
                        JSONArray org_OffenderPhonesJsonArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("Offender_Phones");
                        String recomposed_offenderPhonesJsonArrayStr = "{\"Offender_Phones\":[";
                        for (int j = 0; j < org_OffenderPhonesJsonArray.length(); j++) {
                            String phoneNumberKey = org_OffenderPhonesJsonArray.getJSONObject(j).names().getString(0);

                            recomposed_offenderPhonesJsonArrayStr = recomposed_offenderPhonesJsonArrayStr
                                    + String.format("\"%s\"", org_OffenderPhonesJsonArray.getJSONObject(j).getString(phoneNumberKey));

                            if (j != org_OffenderPhonesJsonArray.length() - 1) {
                                recomposed_offenderPhonesJsonArrayStr = recomposed_offenderPhonesJsonArrayStr + ",";
                            }
                        }
                        recomposed_offenderPhonesJsonArrayStr = recomposed_offenderPhonesJsonArrayStr + "]}";

                        Gson gson = new Gson();
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jo = (JsonObject) jsonParser.parse(recomposed_offenderPhonesJsonArrayStr);
                        JsonArray offenderPhonesJsonArray = jo.getAsJsonArray("Offender_Phones");
                        ArrayList<String> offenderPhonesArray = gson.fromJson(offenderPhonesJsonArray, new TypeToken<ArrayList<String>>() {
                        }.getType());

                        //Currently, as agreed with Refael, taking only first two indexes: index 0 = PrimaryPhone, index 1 = SecondaryPhone
                        if (offenderPhonesArray != null && offenderPhonesArray.size() > 0) {
                            if (offenderPhonesArray.get(0) != null && !offenderPhonesArray.get(0).equals("")) {
                                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_PRIMARY_PHONE, offenderPhonesArray.get(0));
                            } else {
                                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_PRIMARY_PHONE, "NA");
                            }

                            if (offenderPhonesArray.size() > 1 && offenderPhonesArray.get(1) != null && !offenderPhonesArray.get(1).equals("")) {
                                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_SECONDARY_PHONE, offenderPhonesArray.get(1));
                            } else {
                                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_SECONDARY_PHONE, "NA");
                            }

                        } else {
                            TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_PRIMARY_PHONE, "NA");
                            TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_SECONDARY_PHONE, "NA");
                        }
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("OfficerName")) {
                        currentNormalItem = "OfficerName";
                        String officerName = offenderDeviceConfiguration.getJSONObject(i).getString("OfficerName");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_OFFICER_NAME, officerName);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("EnrollmentWizard")) {
                        currentNormalItem = "EnrollmentWizard";
                        JSONArray enrollmnentScreensArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("EnrollmentWizard");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_ENROLLMENT_SCREEN_WIZARD,
                                enrollmnentScreensArray.toString());
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("DeviceLock")) {
                        currentNormalItem = "DeviceLock";
                        int deviceLock = offenderDeviceConfiguration.getJSONObject(i).getInt("DeviceLock");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVICE_LOCK, deviceLock);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("LauncherSettingsPassword")) {
                        currentNormalItem = "LauncherSettingsPassword";
                        Password = offenderDeviceConfiguration.getJSONObject(i).getString("LauncherSettingsPassword");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.LAUNCHER_CONFIG_SETTINGS_PASSWORD, Password);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("EventsAlarms")) {
                        currentNormalItem = "EventsAlarms";
                        JSONArray eventsAlarmsArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("EventsAlarms");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.EVENTS_ALARMS, eventsAlarmsArray.toString());
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("BeaconRange")) {
                        currentNormalItem = "BeaconRange";
                        int beaconRange = offenderDeviceConfiguration.getJSONObject(i).getInt("BeaconRange");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_RANGE, beaconRange);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("PMComProfiles")) {
                        currentNormalItem = "PMComProfiles";
                        JSONArray pmComProfileArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("PMComProfiles");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.PM_COM_PROFILE, pmComProfileArray.toString());
                        TableEventsManager.sharedInstance().profilingEventsConfig.updatePmComProfilesArrayListAfterNewConfiguration();
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("ProfileEvents")) {
                        currentNormalItem = "ProfileEvents";
                        JSONArray profileEventsArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("ProfileEvents");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.PROFILE_EVENTS, profileEventsArray.toString());
                        TableEventsManager.sharedInstance().profilingEventsConfig.updateProfileEventsArrayListAfterNewConfiguration();
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("HomeAddressSettings")) {
                        currentNormalItem = "HomeAddressSettings";
                        JSONArray homeAddressArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("HomeAddressSettings");
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.HOME_ADDRESS_SETTINGS, homeAddressArray.toString());
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Lat")) {
                        currentNormalItem = "Lat";
                        String homeLat = offenderDeviceConfiguration.getJSONObject(i).getString("Lat");
                        float homeLatFloat = new BigDecimal(homeLat).floatValue();
                        TableOffenderDetailsManager.sharedInstance().updateColumnFloat(OFFENDER_DETAILS_CONS.HOME_LAT, homeLatFloat);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("Long")) {
                        currentNormalItem = "Long";
                        String homeLong = offenderDeviceConfiguration.getJSONObject(i).getString("Long");
                        float homeLongFloat = new BigDecimal(homeLong).floatValue();
                        TableOffenderDetailsManager.sharedInstance().updateColumnFloat(OFFENDER_DETAILS_CONS.HOME_LONG, homeLongFloat);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("DialerBlocker")) {
                        currentNormalItem = "DialerBlocker";
                        int dialerBlocker = offenderDeviceConfiguration.getJSONObject(i).getInt("DialerBlocker");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DIALER_BLOCKER, dialerBlocker);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("VoipSettings")) {
                        currentNormalItem = "VoipSettings";
                        JSONArray voipSettingsArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("VoipSettings");
                        if (voipSettingsArray.getJSONObject(0).has("Enable")) {
                            int isVoipSettingsEnable = voipSettingsArray.getJSONObject(0).getInt("Enable");
                            VoipSettings voipSettings = TableOffenderDetailsManager.sharedInstance().getVoipSettingsObject();
                            if (voipSettings.getEnable() != isVoipSettingsEnable) {
                                isVoipSettingsEnableChanged = true;
                            }
                        }
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.VOIP_SETTINGS, voipSettingsArray.toString());
                    }

                    boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
                    if (isKnoxLicenceActivated && offenderDeviceConfiguration.getJSONObject(i).has("KnoxSettings")) {
                        String KNOXSettingsJsonToDB = "[{\"";
                        JSONArray knoxSettingsJsonArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("KnoxSettings");
                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.AIRPLANE)) {
                            currentNormalItem = KnoxConfigurations.AIRPLANE;
                            int isAirplaneModeBlocked = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.AIRPLANE);
                            KNOXSettingsJsonToDB += currentNormalItem + "\":" + isAirplaneModeBlocked;
                        } else {
                            KNOXSettingsJsonToDB += KnoxConfigurations.AIRPLANE + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.STATUS_BAR)) {
                            currentNormalItem = KnoxConfigurations.STATUS_BAR;
                            int isStatusBarEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.STATUS_BAR);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isStatusBarEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.STATUS_BAR + "\":" + 0;

                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.BLUETOOTH_STATE)) {
                            currentNormalItem = KnoxConfigurations.BLUETOOTH_STATE;
                            int isBluetoothStateEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.BLUETOOTH_STATE);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isBluetoothStateEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.BLUETOOTH_STATE + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.AUTOMATIC_TIME)) {
                            currentNormalItem = KnoxConfigurations.AUTOMATIC_TIME;
                            int setAutomaticTime = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.AUTOMATIC_TIME);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + setAutomaticTime;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.AUTOMATIC_TIME + "\":" + 1;
                        }
                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.WIFI)) {
                            currentNormalItem = KnoxConfigurations.WIFI;
                            int isWifiEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.WIFI);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isWifiEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.WIFI + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.NFC)) {
                            currentNormalItem = KnoxConfigurations.NFC;
                            int isNFCEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.NFC);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isNFCEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.NFC + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.SVOICE)) {
                            currentNormalItem = KnoxConfigurations.SVOICE;
                            int isSVoiceEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.SVOICE);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isSVoiceEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.SVOICE + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.POWER)) {
                            currentNormalItem = KnoxConfigurations.POWER;
                            int isPoweriEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.POWER);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isPoweriEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.POWER + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.RECENT_APPS)) {
                            currentNormalItem = KnoxConfigurations.RECENT_APPS;
                            int isRecentAppsEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.RECENT_APPS);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isRecentAppsEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.RECENT_APPS + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.MOBILE_DATA)) {
                            currentNormalItem = KnoxConfigurations.MOBILE_DATA;
                            int isMobileDataEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.MOBILE_DATA);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isMobileDataEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.MOBILE_DATA + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.DATA_ROAMING)) {
                            currentNormalItem = KnoxConfigurations.DATA_ROAMING;
                            int isDataRoamingEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.DATA_ROAMING);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isDataRoamingEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.DATA_ROAMING + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.LOCAL)) {
                            currentNormalItem = KnoxConfigurations.LOCAL;
                            JSONObject localJsonObject = knoxSettingsJsonArray.getJSONObject(0).getJSONObject(KnoxConfigurations.LOCAL);
                            String localeLanguageFromServer = null;
                            String localeCountryFromServer = null;
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + "{\"";
                            if (localJsonObject.has(KnoxConfigurations.LOCAL_LANGUAGE)) { //{"LocaleLanguage":"","LocaleCountry":""}
                                currentNormalItem = KnoxConfigurations.LOCAL_LANGUAGE;
                                localeLanguageFromServer = localJsonObject.getString(KnoxConfigurations.LOCAL_LANGUAGE);
                                if (localeLanguageFromServer.isEmpty()) {
                                    KNOXSettingsJsonToDB += currentNormalItem + "\":\"\"";
                                } else {
                                    KNOXSettingsJsonToDB += currentNormalItem + "\":" + localeLanguageFromServer;
                                }
                            } else {
                                KNOXSettingsJsonToDB += currentNormalItem + "\":" + "en";
                            }

                            if (localJsonObject.has(KnoxConfigurations.LOCAL_COUNTRY)) {
                                currentNormalItem = KnoxConfigurations.LOCAL_COUNTRY;
                                localeCountryFromServer = localJsonObject.getString(KnoxConfigurations.LOCAL_COUNTRY);
                                if (localeCountryFromServer.isEmpty()) {
                                    KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":\"\"" + "}";
                                } else {
                                    KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + localeCountryFromServer + "}";
                                }
                            } else {
                                KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + "us" + "}";
                            }

                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.SCREEN_TIMEOUT)) {
                            currentNormalItem = KnoxConfigurations.SCREEN_TIMEOUT;
                            int Screentimout = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.SCREEN_TIMEOUT);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + Screentimout;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.SCREEN_TIMEOUT + "\":" + 15;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.TOAST)) {
                            currentNormalItem = KnoxConfigurations.TOAST;
                            int isToastEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.TOAST);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isToastEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.TOAST + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.USB)) {
                            currentNormalItem = KnoxConfigurations.USB;
                            int isUSBEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.USB);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isUSBEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.USB + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.AUTOSTART)) {
                            currentNormalItem = KnoxConfigurations.AUTOSTART;
                            int isAutoStartEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.AUTOSTART);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isAutoStartEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.AUTOSTART + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.NETWORK_TYPE)) {
                            currentNormalItem = KnoxConfigurations.NETWORK_TYPE;
                            int networktype = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.NETWORK_TYPE);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + networktype;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.NETWORK_TYPE + "\":" + CustomDeviceManager.NETWORK_TYPE_WCDMA_PREF;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.HOME_LONG_PRESS)) {
                            currentNormalItem = KnoxConfigurations.HOME_LONG_PRESS;
                            int isRecentLongPressEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.HOME_LONG_PRESS);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isRecentLongPressEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.HOME_LONG_PRESS + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has("TimeZone")) {
                            currentNormalItem = "TimeZone";
                            String timeZone = KnoxConfigurations.TIME_ZONE;
                            boolean automaticTimeEnable = KnoxUtil.getInstance().getKnoxSDKImplementation().isAutomaticTimeEnable();
                            if (!timeZone.isEmpty() && !automaticTimeEnable) {
                                KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + timeZone;
                            } else {
                                KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.TIME_ZONE + "\":" + 0;
                            }
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.NOTIFICATION_MESSAGES)) {
                            currentNormalItem = KnoxConfigurations.NOTIFICATION_MESSAGES;
                            int notificationMessages = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.NOTIFICATION_MESSAGES);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + notificationMessages;
                        } else {
                            int mask = CustomDeviceManager.NOTIFICATIONS_BATTERY_LOW | CustomDeviceManager.NOTIFICATIONS_BATTERY_FULL;
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.NOTIFICATION_MESSAGES + "\":" + mask;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.DATE_TIME)) {
                            currentNormalItem = KnoxConfigurations.DATE_TIME;
                            long dateTimeInMillisSec = Long.parseLong(knoxSettingsJsonArray.getJSONObject(0).getString(KnoxConfigurations.DATE_TIME));
                            Date date = new Date(dateTimeInMillisSec * 1000);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            KnoxUtil.getInstance().getKnoxSDKImplementation().setDateTime(cal.get(Calendar.DATE), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + knoxSettingsJsonArray.getJSONObject(0).getString(KnoxConfigurations.DATE_TIME);
                        } else {
                            long dateTimeInMillisSec = System.currentTimeMillis();
                            Date date = new Date(dateTimeInMillisSec * 1000);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            KnoxUtil.getInstance().getKnoxSDKImplementation().setDateTime(cal.get(Calendar.DATE), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.DATE_TIME + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.SAFE_MODE)) {
                            currentNormalItem = KnoxConfigurations.SAFE_MODE;
                            int isSafeModeEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.SAFE_MODE);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isSafeModeEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.SAFE_MODE + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.FACTORY_RESET)) {
                            currentNormalItem = KnoxConfigurations.FACTORY_RESET;
                            int isFactoryReset = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.FACTORY_RESET);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isFactoryReset;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.FACTORY_RESET + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.OTA_UPDATE)) {
                            currentNormalItem = KnoxConfigurations.OTA_UPDATE;
                            int isOTAUpgradEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.OTA_UPDATE);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isOTAUpgradEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.OTA_UPDATE + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.SD_CARD)) {
                            currentNormalItem = KnoxConfigurations.SD_CARD;
                            int isSDCardEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.SD_CARD);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isSDCardEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.SD_CARD + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.INSTALL_APPS)) {
                            currentNormalItem = KnoxConfigurations.INSTALL_APPS;
                            int applicationInstallationMode = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.INSTALL_APPS);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + applicationInstallationMode;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.INSTALL_APPS + "\":" + 1;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.UNINSTALL_APPS)) {
                            currentNormalItem = KnoxConfigurations.UNINSTALL_APPS;
                            int applicationUninstallationMode = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.UNINSTALL_APPS);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + applicationUninstallationMode;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.UNINSTALL_APPS + "\":" + 1;
                        }
                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.MOBILE_DATA_LIMIT)) {
                            currentNormalItem = KnoxConfigurations.MOBILE_DATA_LIMIT;
                            int isMobileDataLimitEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.MOBILE_DATA_LIMIT);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isMobileDataLimitEnable;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.MOBILE_DATA_LIMIT + "\":" + 0;
                        }

                        if (knoxSettingsJsonArray.getJSONObject(0).has(KnoxConfigurations.LOCK_SCREEN)) {
                            currentNormalItem = KnoxConfigurations.LOCK_SCREEN;
                            int isLockScreenEnable = knoxSettingsJsonArray.getJSONObject(0).getInt(KnoxConfigurations.LOCK_SCREEN);
                            KNOXSettingsJsonToDB += "," + "\"" + currentNormalItem + "\":" + isLockScreenEnable;
                            isLockScreenChanged = true;
                        } else {
                            KNOXSettingsJsonToDB += "," + "\"" + KnoxConfigurations.LOCK_SCREEN + "\":" + 0;
                        }
                        KNOXSettingsJsonToDB += "}]";
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.KNOX_SETTINGS, KNOXSettingsJsonToDB);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("BackgroundAppWhiteList")) {
                        currentNormalItem = "BackgroundAppWhiteList";
                        JSONArray backgroundAppWhiteListJsonArray = offenderDeviceConfiguration.getJSONObject(i).getJSONArray("BackgroundAppWhiteList");
                        String backgroundAppWhiteList = "{\"BackgroundAppWhiteList\":[";
                        for (int z = 0; z < backgroundAppWhiteListJsonArray.length(); z++) {
                            String packageName = backgroundAppWhiteListJsonArray.getJSONObject(z).names().getString(0);
                            backgroundAppWhiteList = backgroundAppWhiteList + String.format("\"%s\"", backgroundAppWhiteListJsonArray.getJSONObject(z).getString(packageName));
                            if (z != backgroundAppWhiteListJsonArray.length() - 1) {
                                backgroundAppWhiteList = backgroundAppWhiteList + ",";
                            }
                        }
                        backgroundAppWhiteList = backgroundAppWhiteList + "]}";
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.BACKGROUND_APP_WHITE_LIST, backgroundAppWhiteList);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("OfficerModeTimeout")) {
                        currentNormalItem = "OfficerModeTimeout";
                        int officerModeTimeout = offenderDeviceConfiguration.getJSONObject(i).getInt("OfficerModeTimeout");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFICER_MODE_TIMEOUT, officerModeTimeout);
                    }


                    if (offenderDeviceConfiguration.getJSONObject(i).has("CustomCallInterface")) {
                        currentNormalItem = "CustomCallInterface";
                        int customCallInterface = offenderDeviceConfiguration.getJSONObject(i).getInt("CustomCallInterface");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.CUSTOM_CALL_INTERFACE, customCallInterface);
                    }

                    if (offenderDeviceConfiguration.getJSONObject(i).has("LBS_Enable")) {
                        currentNormalItem = "LBS_Enable";
                        int LBS_Enable = offenderDeviceConfiguration.getJSONObject(i).getInt("LBS_Enable");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LBS_ENABLE, LBS_Enable);
                    }
                    if (offenderDeviceConfiguration.getJSONObject(i).has("Start_LBS_Threshold_Normal")) {
                        currentNormalItem = "Start_LBS_Threshold_Normal";
                        int Start_LBS_Threshold_Normal = offenderDeviceConfiguration.getJSONObject(i).getInt("Start_LBS_Threshold_Normal");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.START_LBS_THRESHOLD_NORMAL, Start_LBS_Threshold_Normal);
                    }
                    if (offenderDeviceConfiguration.getJSONObject(i).has("Start_LBS_Threshold_in_Violation")) {
                        currentNormalItem = "Start_LBS_Threshold_in_Violation";
                        int Start_LBS_Threshold_in_Violation = offenderDeviceConfiguration.getJSONObject(i).getInt("Start_LBS_Threshold_in_Violation");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.START_LBS_THRESHOLD_IN_VIOLATION, Start_LBS_Threshold_in_Violation);
                    }
                    if (offenderDeviceConfiguration.getJSONObject(i).has("LBS_Interval_Normal")) {
                        currentNormalItem = "LBS_Interval_Normal";
                        int LBS_Interval_Normal = offenderDeviceConfiguration.getJSONObject(i).getInt("LBS_Interval_Normal");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LBS_INTERVAL_NORMAL, LBS_Interval_Normal);
                    }
                    if (offenderDeviceConfiguration.getJSONObject(i).has("LBS_Interval_in_Violation")) {
                        currentNormalItem = "LBS_Interval_in_Violation";
                        int LBS_Interval_in_Violation = offenderDeviceConfiguration.getJSONObject(i).getInt("LBS_Interval_in_Violation");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LBS_INTERVAL_IN_VIOLATION, LBS_Interval_in_Violation);
                    }
                    if (offenderDeviceConfiguration.getJSONObject(i).has("Stop_LBS_Validity")) {
                        currentNormalItem = "Stop_LBS_Validity";
                        int Stop_LBS_Validity = offenderDeviceConfiguration.getJSONObject(i).getInt("Stop_LBS_Validity");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LBS_STOP_VALIDITY, Stop_LBS_Validity);
                    }

                }
            }

            if (deviceConfiguration.getJSONObject(0).has("TimeZone")) {
                currentNormalItem = "TimeZone";
                int timeZone = deviceConfiguration.getJSONObject(0).getInt("TimeZone");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE, timeZone);
            }

            if (deviceConfiguration.getJSONObject(0).has("DstOffset")) {
                currentNormalItem = "DstOffset";
                int dstOffset = deviceConfiguration.getJSONObject(0).getInt("DstOffset");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DST_OFFSET, dstOffset);
            }

            if (deviceConfiguration.getJSONObject(0).has("DST")) {
                currentNormalItem = "DST";
                JSONArray futureDSTArray = deviceConfiguration.getJSONObject(0).getJSONArray("DST");
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_FUTURE_DST,
                        futureDSTArray.toString());
            }

            if (deviceConfiguration.getJSONObject(0).has("Phones")) {
                currentNormalItem = "Phones";
                JSONArray phonesJsonArray = deviceConfiguration.getJSONObject(0).getJSONArray("Phones");
                String incomingCallsWhiteListString = "{\"phones\":[";
                for (int i = 0; i < phonesJsonArray.length(); i++) {
                    String phoneNumberKey = phonesJsonArray.getJSONObject(i).names().getString(0);
                    incomingCallsWhiteListString = incomingCallsWhiteListString + String.format("\"%s\"", phonesJsonArray.getJSONObject(i).getString(phoneNumberKey));
                    if (i != phonesJsonArray.length() - 1) {
                        incomingCallsWhiteListString = incomingCallsWhiteListString + ",";
                    }
                }
                incomingCallsWhiteListString = incomingCallsWhiteListString + "]}";
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST,
                        incomingCallsWhiteListString);
            }

            if (deviceConfiguration.getJSONObject(0).has("Apps")) {
                currentNormalItem = "Apps";
                JSONArray appsJsonArray = deviceConfiguration.getJSONObject(0).getJSONArray("Apps");
                String appsListString = "{\"Apps\":[";
                for (int i = 0; i < appsJsonArray.length(); i++) {
                    String packageName = appsJsonArray.getJSONObject(i).names().getString(0);
                    appsListString = appsListString + String.format("\"%s\"", appsJsonArray.getJSONObject(i).getString(packageName));
                    if (i != appsJsonArray.length() - 1) {
                        appsListString = appsListString + ",";
                    }
                }
                appsListString = appsListString + "]}";
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_APPS_LIST, appsListString);
            }

            if (deviceConfiguration.getJSONObject(0).has("GPS_interval_no_motion")) {
                currentNormalItem = "GPS_interval_no_motion";
                int gpsIntervalNoMotion = deviceConfiguration.getJSONObject(0).getInt("GPS_interval_no_motion");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_INT_NO_MOTION, gpsIntervalNoMotion);
            }


            boolean isCommIntervalChanged = false;
            if (deviceConfiguration.getJSONObject(0).has("Comm_Interval")) {
                currentNormalItem = "Comm_Interval";
                commInterval = deviceConfiguration.getJSONObject(0).getInt("Comm_Interval");
                long currentCommInterval = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                        (OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL);
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL, commInterval);
                MainActivity.commIntervalTimeFromDB = currentCommInterval;
                if (currentCommInterval != commInterval) {
                    isCommIntervalChanged = true;
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Monitoring_Suspend_Settings")) {
                currentNormalItem = "Monitoring_Suspend_Settings";

                JSONArray jsonSuspendSettingsArr = deviceConfiguration.getJSONObject(0).getJSONArray("Monitoring_Suspend_Settings");
                JSONArray allowedUploadSuspendEventsArr = jsonSuspendSettingsArr.getJSONObject(0).getJSONArray("upload_events");
                String allowedSuspendEventsStr = allowedUploadSuspendEventsArr.toString().replace("[", "").replace("]", "").trim(); // instead of iterating over an array and create string - remove parentheses and spaces e.g "[x,y ,z]" -> "x,y,z"
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE, allowedSuspendEventsStr);
            }


            if (deviceConfiguration.getJSONObject(0).has("Turn_On_Screen_Motion")) {
                currentNormalItem = "Turn_On_Screen_Motion";
                int TurnOnScreen = deviceConfiguration.getJSONObject(0).getInt("Turn_On_Screen_Motion");
                int oldTurnOnScreen = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                        OFFENDER_DETAILS_CONS.OFFENDER_TURN_ON_SCREEN_MOTION);
                if (oldTurnOnScreen != TurnOnScreen) {
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_TURN_ON_SCREEN_MOTION, TurnOnScreen);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("GPS_Polling_Interval")) {
                currentNormalItem = "GPS_Polling_Interval";
                int GpsInteval = deviceConfiguration.getJSONObject(0).getInt("GPS_Polling_Interval");
                int oldGpsPollingInterval = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                        OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_INTERVAL);
                if (oldGpsPollingInterval != GpsInteval) {
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_INTERVAL, GpsInteval);
                    isLocationSettingsChanged = true;
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("GPS_Interval_In_Beacon")) {
                currentNormalItem = "GPS_Interval_In_Beacon";
                int GpsIntevalInBeacon = deviceConfiguration.getJSONObject(0).getInt("GPS_Interval_In_Beacon");
                int oldGpsIntervalInBeacon = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                        OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL);
                if (oldGpsIntervalInBeacon != GpsIntevalInBeacon) {
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL,
                            GpsIntevalInBeacon);
                    isLocationSettingsChanged = true;
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Guest_Tag")) {
                currentNormalItem = "Guest_Tag";
                JSONArray guestTag = deviceConfiguration.getJSONObject(0).getJSONArray("Guest_Tag");
                if (guestTag.getJSONObject(0).has("enabled")) {
                    currentNormalItem = "Enabled";
                    int enabled = guestTag.getJSONObject(0).getInt("enabled");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GUEST_TAG_ENABLED, enabled);
                }
                if (guestTag.getJSONObject(0).has("tag_left_sensitivity")) {
                    currentNormalItem = "time";
                    int enabled = guestTag.getJSONObject(0).getInt("tag_left_sensitivity");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GUEST_TAG_TIME, enabled);
                }

            }

            if (deviceConfiguration.getJSONObject(0).has("Bad_Gps_Accuracy_Counter")) {
                currentNormalItem = "Bad_Gps_Accuracy_Counter";
                int badGPSAccuracyCounter = deviceConfiguration.getJSONObject(0).getInt("Bad_Gps_Accuracy_Counter");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER,
                        badGPSAccuracyCounter);
            }

            if (deviceConfiguration.getJSONObject(0).has("Comm_Interval_Low")) {
                currentNormalItem = "Comm_Interval_Low";
                Comm_Interval_Low = deviceConfiguration.getJSONObject(0).getInt("Comm_Interval_Low");
                long currentNetworkCycleIntervalLow = TableOffenderDetailsManager.sharedInstance()
                        .getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW);
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW,
                        Comm_Interval_Low);
                if (Comm_Interval_Low != currentNetworkCycleIntervalLow) {
                    isCommIntervalChanged = true;
                }
            }


            if (deviceConfiguration.getJSONObject(0).has("Debug_Info_Config")) {
                currentNormalItem = "Debug_Info_Config";
                DebugInfoConfig = deviceConfiguration.getJSONObject(0).getInt("Debug_Info_Config");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_DEBUG_INFO_CONFIG, DebugInfoConfig);
                // update local var for frequent use
                TableOffenderDetailsManager.sharedInstance().setDebugInfoConfig(DebugInfoConfig);
            }

            if (deviceConfiguration.getJSONObject(0).has("Device_Info_Cycles")) {
                currentNormalItem = "Device_Info_Cycles";
                DeviceInfoCycles = deviceConfiguration.getJSONObject(0).getInt("Device_Info_Cycles");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_DEVICE_INFO_CYCLES, DeviceInfoCycles);
                // update local var for frequent use
                NetworkRepository.getInstance().setDeviceInfoCycles(DeviceInfoCycles);
            }

            if (deviceConfiguration.getJSONObject(0).has("BLE_Scan_Type")) {
                currentNormalItem = "BLE_Scan_Type";

                JSONArray scanTypeArray = deviceConfiguration.getJSONObject(0).getJSONArray("BLE_Scan_Type");

                int normalScanEnabled = scanTypeArray.getJSONObject(0).getInt("Normal_Scan");
                int macScanEnabled = scanTypeArray.getJSONObject(0).getInt("MAC_Scan");
                String manufacturerId = scanTypeArray.getJSONObject(0).getString("Manufacturer_Id");
                String tagMacAddress = "";
                if (offenderDeviceConfiguration.getJSONObject(0).has("Tag_Mac_Address")) {
                    currentNormalItem = "Tag_Mac_Address";

                    tagMacAddress = offenderDeviceConfiguration.getJSONObject(0).getString("Tag_Mac_Address");
                    EntityScannerType entityScannerType = new EntityScannerType(normalScanEnabled, macScanEnabled, manufacturerId, tagMacAddress, "");
                    DatabaseAccess.getInstance().tableScannerType.insertRecord(entityScannerType);

                    HeartBeatServiceJava2.start();
                }

                if (offenderDeviceConfiguration.getJSONObject(0).has("PureBeacons")) {
                    currentNormalItem = "Tag_Mac_Address";
                    JSONArray beaconsArray = offenderDeviceConfiguration.getJSONObject(0).getJSONArray("PureBeacons");
                    if (beaconsArray.length() > 0 && beaconsArray.getJSONObject(0).has("Tag_Mac_Address")) {
                        String beaconMacAddress = beaconsArray.getJSONObject(0).getString("Tag_Mac_Address");
                        EntityScannerType entityScannerType = new EntityScannerType(normalScanEnabled, macScanEnabled, manufacturerId, tagMacAddress, beaconMacAddress);
                        DatabaseAccess.getInstance().tableScannerType.insertRecord(entityScannerType);
                    }
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Device_Shielding")){
                currentNormalItem = "Device_Shielding";

                int openEventThreshold = 0;
                int openEventCell = 0;
                int openEventBluetooth = 0;
                int openEventWifi = 0;
                int closeEventThreshold = 0;
                int closeEventCell = 0;
                int closeEventBluetooth = 0;
                int closeEventWifi = 0;
                int checkIntervalSec = 30, bleThresholdSec = 30, wifiThresholdSec = 30 , mobileNetworkThresholdSec = 30;

                JSONObject deviceShielding = deviceConfiguration.getJSONObject(0).getJSONObject("Device_Shielding");
                int enabled = deviceShielding.getInt("enabled");




                if (deviceShielding.has("open_event")){
                    JSONObject shieldingOpenEvent = deviceShielding.getJSONObject("open_event");
                    checkIntervalSec = deviceShielding.optInt("check_interval_sec", 30);
                    openEventThreshold = shieldingOpenEvent.getInt("threshold");
                    openEventBluetooth = shieldingOpenEvent.getInt("bluetooth");
                    bleThresholdSec = deviceShielding.optInt("ble_threshold_sec", 30);
                    openEventWifi = shieldingOpenEvent.getInt("wifi");
                    wifiThresholdSec = deviceShielding.optInt("wifi_threshold_sec", 30);
                    openEventCell = shieldingOpenEvent.getInt("cell");
                    mobileNetworkThresholdSec = deviceShielding.optInt("cell_threshold_sec", 30);

                }
                if (deviceShielding.has("close_event")){
                    JSONObject shieldingOpenEvent = deviceShielding.getJSONObject("close_event");
                    closeEventCell = shieldingOpenEvent.getInt("cell");
                    closeEventBluetooth = shieldingOpenEvent.getInt("bluetooth");
                    closeEventWifi = shieldingOpenEvent.getInt("wifi");
                }

                //TODO - check if enabled was change and handle feature on / off
                boolean isShieldingFromServerEnabled = enabled > 0;
                if (DeviceShieldingManager.getInstance().isEnabled != isShieldingFromServerEnabled){
                    DeviceShieldingManager.getInstance().isEnabled = isShieldingFromServerEnabled;
                    if (isShieldingFromServerEnabled){
                        DeviceShieldingManager.getInstance().enableShielding();
                    }else {
                        DeviceShieldingManager.getInstance().disableShielding();
                    }
                }

                EntityDeviceShielding entityDeviceShieldingType = new EntityDeviceShielding(enabled, openEventThreshold,
                        openEventCell, openEventBluetooth, openEventWifi, closeEventThreshold, closeEventCell, closeEventBluetooth, closeEventWifi, checkIntervalSec, bleThresholdSec, wifiThresholdSec, mobileNetworkThresholdSec);

                DatabaseAccess.getInstance().tableDeviceShielding.insertRecord(entityDeviceShieldingType);

            }

            if (deviceConfiguration.getJSONObject(0).has("device_jamming")) {
                currentNormalItem = "device_jamming";

                JSONArray deviceJamming = deviceConfiguration.getJSONObject(0).getJSONArray("device_jamming");

                int enabled = deviceJamming.getJSONObject(0).getInt("enabled");
                int cellularLevelSampleInterval = deviceJamming.getJSONObject(0).getInt("cellular_level_sample_interval");
                int minGoodCellularLevelWcdma3G = deviceJamming.getJSONObject(0).getInt("min_good_cellular_level_wcdma_3G");
                int minGoodCellularLevelLte4G = deviceJamming.getJSONObject(0).getInt("min_good_cellular_level_lte_4G");
                int jammingEventTimerSensitivity = deviceJamming.getJSONObject(0).getInt("jamming_event_timer_sensitivity");

                EntityDeviceJamming entityDeviceJamming = new EntityDeviceJamming(enabled, minGoodCellularLevelWcdma3G,
                        minGoodCellularLevelLte4G, jammingEventTimerSensitivity, cellularLevelSampleInterval);
                DatabaseAccess.getInstance().tableDeviceJamming.insertRecord(entityDeviceJamming);

            }
            if (deviceConfiguration.getJSONObject(0).has("self_diagnostics")) {
                currentNormalItem = "self_diagnostics";

                JSONArray selfDiagnostics = deviceConfiguration.getJSONObject(0).getJSONArray("self_diagnostics");

                int enabled = selfDiagnostics.getJSONObject(0).getInt("enabled");
                int gyroscopeSensitivity = selfDiagnostics.getJSONObject(0).getInt("gyroscope_sensitivity");
                int magneticSensitivity = selfDiagnostics.getJSONObject(0).getInt("magnetic_sensitivity");

                EntitySelfDiagnosticEvent entitySelfDiagnosticEvent = new EntitySelfDiagnosticEvent(enabled, gyroscopeSensitivity, magneticSensitivity);
                DatabaseAccess.getInstance().tableSelfDiagnosticEvents.insertRecord(entitySelfDiagnosticEvent);

            }


            if (deviceConfiguration.getJSONObject(0).has("Sched_Expire")) {
                currentNormalItem = "Sched_Expire";
                Sched_Expire = deviceConfiguration.getJSONObject(0).getInt("Sched_Expire");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_SCHEDULE_EXPIRE, Sched_Expire);
                TableOffenderStatusManager.sharedInstance().updateColumnLong(OFFENDER_STATUS_CONS.OFF_LAST_SCHEUDLE_UPDATE, System.currentTimeMillis());
            }

            if (deviceConfiguration.getJSONObject(0).has("Emergency")) {
                currentNormalItem = "Emergency";
                Emergency = deviceConfiguration.getJSONObject(0).getString("Emergency");
                TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PHONE_EMERGENCY, Emergency);
            }

            if (deviceConfiguration.getJSONObject(0).has("Text_Message")) {
                currentNormalItem = "Text_Message";
                jArrayText_Message = deviceConfiguration.getJSONObject(0).getJSONArray("Text_Message");
                for (int i = 0; i < jArrayText_Message.length(); i++) {
                    if (jArrayText_Message.getJSONObject(i).has("Timeout")) {
                        int MsgTimeout = jArrayText_Message.getJSONObject(i).getInt("Timeout");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_MESSAGE_TIMEOUT, MsgTimeout);
                    }

                    if (jArrayText_Message.getJSONObject(i).has("Expire")) {
                        int MsgExpire = jArrayText_Message.getJSONObject(i).getInt("Expire");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_MESSAGE_EXPIRE, MsgExpire);
                    }
                    if (jArrayText_Message.getJSONObject(i).has("Enable_Message_Response")) {
                        int enableMessageResponse = jArrayText_Message.getJSONObject(i).getInt("Enable_Message_Response");
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_ENABLE_MESSAGE_RESPONSE, enableMessageResponse);
                    }
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Biometric")) {
                currentNormalItem = "Biometric";
                jArrayBiometric = deviceConfiguration.getJSONObject(0).getJSONArray("Biometric");
                for (int i = 0; i < 1; i++) {

                    BioTimeout[i] = jArrayBiometric.getJSONObject(i).getInt("Timeout");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BIOMETRIC_TIMEOUT, BioTimeout[i]);

                    BioMin_Between[i] = jArrayBiometric.getJSONObject(i).getInt("Min_Between");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BIOMETRIC_MIN_BETWEEN, BioMin_Between[i]);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("Good_Point_Threshold")) {
                currentNormalItem = "Good_Point_Threshold";
                int goodPointThreshold = deviceConfiguration.getJSONObject(0).getInt("Good_Point_Threshold");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_GOOD_POINT_THRESHOLD,
                        goodPointThreshold);
            }

            if (deviceConfiguration.getJSONObject(0).has("Bad_Point_Threshold")) {
                currentNormalItem = "Bad_Point_Threshold";
                int badPointThreshold = deviceConfiguration.getJSONObject(0).getInt("Bad_Point_Threshold");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BAD_POINT_THRESHOLD,
                        badPointThreshold);
            }

            if (deviceConfiguration.getJSONObject(0).has("Time_sensitivity_inside_beacon")) {
                currentNormalItem = "Time_sensitivity_inside_beacon";
                int timeSensitivityInsideBeacon = deviceConfiguration.getJSONObject(0).getInt("Time_sensitivity_inside_beacon");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON,
                        timeSensitivityInsideBeacon);
            }

            if (deviceConfiguration.getJSONObject(0).has("Time_sensitivity_outside_beacon")) {
                currentNormalItem = "Time_sensitivity_outside_beacon";
                int timeSensitivityOutsideBeacon = deviceConfiguration.getJSONObject(0).getInt("Time_sensitivity_outside_beacon");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON,
                        timeSensitivityOutsideBeacon);
            }

            if (deviceConfiguration.getJSONObject(0).has("BeaconTimeSensitivity")) {
                currentNormalItem = "BeaconTimeSensitivity";
                int timeSensitivityOutsideBeacon = deviceConfiguration.getJSONObject(0).getInt("BeaconTimeSensitivity");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME,
                        timeSensitivityOutsideBeacon);
            }

            if (deviceConfiguration.getJSONObject(0).has("DeveloperModeEnable")) {
                currentNormalItem = "DeveloperModeEnable";
                int developerModeEnable = deviceConfiguration.getJSONObject(0).getInt("DeveloperModeEnable");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE,
                        developerModeEnable);
            }

            if (deviceConfiguration.getJSONObject(0).has("PureCom_As_Home_Unit")) {
                currentNormalItem = "PureCom_As_Home_Unit";
                int enabled = deviceConfiguration.getJSONObject(0).getInt("PureCom_As_Home_Unit");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PURECOM_AS_HOME_UNIT, enabled);
            }


            if (deviceConfiguration.getJSONObject(0).has("PhonesActive")) {
                currentNormalItem = "PhonesActive";
                int phonesActive = deviceConfiguration.getJSONObject(0).getInt("PhonesActive");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONES_ACTIVE, phonesActive);
            }

            if (deviceConfiguration.getJSONObject(0).has("AllowedSpeed")) {
                currentNormalItem = "AllowedSpeed";
                int allowedSpeed = deviceConfiguration.getJSONObject(0).getInt("AllowedSpeed");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_ALLOWED_SPEED, allowedSpeed);
            }

            if (deviceConfiguration.getJSONObject(0).has("LocationValidity")) {
                currentNormalItem = "LocationValidity";
                int locationValidity = deviceConfiguration.getJSONObject(0).getInt("LocationValidity");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_LOCATION_VALIDITY, locationValidity);
            }


            if (deviceConfiguration.getJSONObject(0).has("PureTrackCaseTamper")) {
                currentNormalItem = "PureTrackCaseTamper";

                JSONArray jArrayPureTrackCaseTamper = deviceConfiguration.getJSONObject(0).getJSONArray("PureTrackCaseTamper");

                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("Enabled")) {
                    int caseTamperEnabled = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("Enabled");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED,
                            caseTamperEnabled);
                }

                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("Validity")) {
                    int validity = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("Validity");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_VALIDITY, validity);
                }

                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("Calibration")) {
                    int calibration = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("Calibration");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_CALIBRATION, calibration);
                }
                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("xMagnetThreshold")) {
                    int xMagnetThreshold = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("xMagnetThreshold");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD, xMagnetThreshold);
                }
                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("yMagnetThreshold")) {
                    int yMagnetThreshold = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("yMagnetThreshold");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD, yMagnetThreshold);
                }
                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("zMagnetThreshold")) {
                    int zMagnetThreshold = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("zMagnetThreshold");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD, zMagnetThreshold);
                }
                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("recalibrationEnabled")) {
                    int recalibrationEnabled = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("recalibrationEnabled");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_RECALIBRATION_ENABLED, recalibrationEnabled);
                }

                if (jArrayPureTrackCaseTamper.getJSONObject(0).has("recalibrationTimerInMinutes")) {
                    int recalibrationTimerInMinutes = jArrayPureTrackCaseTamper.getJSONObject(0).getInt("recalibrationTimerInMinutes");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_PURE_TRACK_CASE_TAMPER_RECALIBRATION_TIMER_IN_MINUTES, recalibrationTimerInMinutes);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("PureTrackCaseTamperV2")) {
                currentNormalItem = "PureTrackCaseTamperV2";

                JSONArray jArrayPureTrackCaseTamperV2 = deviceConfiguration.getJSONObject(0).getJSONArray("PureTrackCaseTamperV2");
                int enabled = 0;
                int threshold = 0;
                int magnetCalibrationOnRestart = 0;
                int accelerationThreshold=105;
                int accelerationMillisProximity=20000;

                Log.i("bug543",new Gson().toJson(jArrayPureTrackCaseTamperV2));

                if (jArrayPureTrackCaseTamperV2.getJSONObject(0).has("Enabled")) {
                    enabled = jArrayPureTrackCaseTamperV2.getJSONObject(0).getInt("Enabled");
                    isCaseTamperEnabled = enabled > 0;
                }

                if (jArrayPureTrackCaseTamperV2.getJSONObject(0).has("caseClosedThreshold")) {
                    threshold = jArrayPureTrackCaseTamperV2.getJSONObject(0).getInt("caseClosedThreshold");
                }

                if (jArrayPureTrackCaseTamperV2.getJSONObject(0).has("MagnetCalibrationOnRestart")) {
                    magnetCalibrationOnRestart = jArrayPureTrackCaseTamperV2.getJSONObject(0).getInt("MagnetCalibrationOnRestart");
                }

                if (jArrayPureTrackCaseTamperV2.getJSONObject(0).has("MotionSensitivity")) {
                    accelerationThreshold = jArrayPureTrackCaseTamperV2.getJSONObject(0).getInt("MotionSensitivity");
                }

                if (jArrayPureTrackCaseTamperV2.getJSONObject(0).has("MotionCheckPeriod")) {
                    accelerationMillisProximity = jArrayPureTrackCaseTamperV2.getJSONObject(0).getInt("MotionCheckPeriod");
                }

                DatabaseAccess.getInstance().tableCaseTamper.insertRecord(new EntityCaseTamper(enabled,
                        threshold,
                        magnetCalibrationOnRestart,
                        accelerationThreshold,
                        accelerationMillisProximity));
            }


            if (deviceConfiguration.getJSONObject(0).has("no_motion")) {
                currentNormalItem = "no_motion";

                JSONArray noMotionArray = deviceConfiguration.getJSONObject(0).getJSONArray("no_motion");

                int enabled = 0;
                int signalsToNoMotion = 0;
                int noMotionPercentage = 0;
                int signalsToMotion = 0;
                int motionPercentage = 0;
                if (noMotionArray.getJSONObject(0).has("enabled")) {
                    enabled = noMotionArray.getJSONObject(0).getInt("enabled");
                }
                if (noMotionArray.getJSONObject(0).has("signals_to_no_motion")) {
                    signalsToNoMotion = noMotionArray.getJSONObject(0).getInt("signals_to_no_motion");
                }
                if (noMotionArray.getJSONObject(0).has("no_motion_percentage")) {
                    noMotionPercentage = noMotionArray.getJSONObject(0).getInt("no_motion_percentage");
                }
                if (noMotionArray.getJSONObject(0).has("signals_to_motion")) {
                    signalsToMotion = noMotionArray.getJSONObject(0).getInt("signals_to_motion");
                }
                if (noMotionArray.getJSONObject(0).has("motion_percentage")) {
                    motionPercentage = noMotionArray.getJSONObject(0).getInt("motion_percentage");
                }

                EntityTagMotion entityTagMotion = new EntityTagMotion(enabled, signalsToNoMotion, noMotionPercentage, signalsToMotion, motionPercentage);
                DatabaseAccess.getInstance().tableTagMotion.deletePreviousRecord();
                DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_TAG_MOTION, entityTagMotion);
            }

            if (deviceConfiguration.getJSONObject(0).has("UsePincode")) {

                currentNormalItem = "UsePincode";

                JSONArray jArrayLocationSettings = deviceConfiguration.getJSONObject(0).getJSONArray("UsePincode");
                JSONObject settingsJsonObj = jArrayLocationSettings.getJSONObject(0);

                if (settingsJsonObj.has("Enable")) {
                    int isEnabled = jArrayLocationSettings.getJSONObject(0).optInt("Enable", PIN_CODE_IS_ENABLED_DEFAULT);
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ENABLE
                            ,
                            isEnabled);
                }

                if (jArrayLocationSettings.getJSONObject(0).has("Attempts")) {
                    int numAttempts = jArrayLocationSettings.getJSONObject(0).optInt("Attempts", PIN_CODE_ATTEMPTS_DEFAULT);
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ATTEMPTS,
                            numAttempts);
                }

                if (jArrayLocationSettings.getJSONObject(0).has("Pin")) {
                    String lockscreenPin = jArrayLocationSettings.getJSONObject(0).optString("Pin", PIN_CODE_DEFAULT);
                    TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_PIN,
                            lockscreenPin);
                }

                if (jArrayLocationSettings.getJSONObject(0).has("LockTime")) {
                    int lockTime = jArrayLocationSettings.getJSONObject(0).optInt("LockTime", PIN_CODE_LOCK_TIME_DEFAULT);
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_LOCK_TIME,
                            lockTime);
                }


            }

            if (deviceConfiguration.getJSONObject(0).has("UseFactoryReset")) {
                currentNormalItem = "UseFactoryReset";

                JSONArray jArrayLocationSettings = deviceConfiguration.getJSONObject(0).getJSONArray("UseFactoryReset");

                if (jArrayLocationSettings.getJSONObject(0).has("Enable")) {
                    int isEnabled = jArrayLocationSettings.getJSONObject(0).getInt("Enable");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_FACTORY_RESET_ENABLE
                            ,
                            isEnabled);
                }

                if (jArrayLocationSettings.getJSONObject(0).has("Timeout")) {
                    int numAttempts = jArrayLocationSettings.getJSONObject(0).getInt("Timeout");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_FACTORY_RESET_TIMEOUT,
                            numAttempts);
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("ScheduleDisplayDays")) {
                int isEnabled = deviceConfiguration.getJSONObject(0).getInt("ScheduleDisplayDays");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SCHEDULE_SETTINGS_NUM_OF_DAYS, isEnabled);
            }

            if (deviceConfiguration.getJSONObject(0).has("LocationSettings")) {
                currentNormalItem = "LocationSettings";

                JSONArray locationSettings = deviceConfiguration.getJSONObject(0).getJSONArray("LocationSettings");

                if (locationSettings.getJSONObject(0).has("GoodPointThreshold")) {
                    int goodPointThreshold = locationSettings.getJSONObject(0).getInt("GoodPointThreshold");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_GOOD_POINT_THRESHOLD,
                            goodPointThreshold);
                }

                if (locationSettings.getJSONObject(0).has("BadPointThreshold")) {
                    int badPointThreshold = locationSettings.getJSONObject(0).getInt("BadPointThreshold");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DETAILS_OFF_BAD_POINT_THRESHOLD,
                            badPointThreshold);
                }

                if (locationSettings.getJSONObject(0).has("SatelliteNum")) {
                    int satelliteNum = locationSettings.getJSONObject(0).getInt("SatelliteNum");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_SATELLITE_NUM, satelliteNum);
                }

                if (locationSettings.getJSONObject(0).has("AllowedSpeed")) {
                    int allowedSpeed = locationSettings.getJSONObject(0).getInt("AllowedSpeed");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_ALLOWED_SPEED, allowedSpeed);
                }

                if (locationSettings.getJSONObject(0).has("LocationValidity")) {
                    int locationValidity = locationSettings.getJSONObject(0).getInt("LocationValidity");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_LOCATION_VALIDITY, locationValidity);
                }

                if (locationSettings.getJSONObject(0).has("BadGpsAccuracyCounter")) {
                    int badGPSAccuracyCounter = locationSettings.getJSONObject(0).getInt("BadGpsAccuracyCounter");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER,
                            badGPSAccuracyCounter);
                }

                if (locationSettings.getJSONObject(0).has("LocationTypes")) {
                    String locationTypesFromServer = locationSettings.getJSONObject(0).getString("LocationTypes");
                    String oldLocationTypes = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(
                            OFFENDER_DETAILS_CONS.LOCATION_TYPES);
                    if (!oldLocationTypes.equals(locationTypesFromServer)) {
                        TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.LOCATION_TYPES, locationTypesFromServer);
                        isLocationSettingsChanged = true;
                    }
                }

                if (locationSettings.getJSONObject(0).has("WeightedAverage")) {
                    int weighteAverage = locationSettings.getJSONObject(0).getInt("WeightedAverage");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LOCATION_WEIGHTED_AVERAGE,
                            weighteAverage);
                }

                if (locationSettings.getJSONObject(0).has("LocationSmoothing")) {
                    int locationSmoothing = locationSettings.getJSONObject(0).getInt("LocationSmoothing");

                    int oldLocationSmoothing = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                            OFFENDER_DETAILS_CONS.LOCATION_SMOOTHING);
                    if (oldLocationSmoothing != locationSmoothing) {
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LOCATION_SMOOTHING, locationSmoothing);
                        isLocationSettingsChanged = true;
                    }
                }

                if (locationSettings.getJSONObject(0).has("SmoothingActivation")) {
                    int smoothingActivation = locationSettings.getJSONObject(0).getInt("SmoothingActivation");
                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LOCATION_SMOOTHING_ACTIVATION,
                            smoothingActivation);
                }

                if (locationSettings.getJSONObject(0).has("GpsServiceSampleIntervalX")) {
                    int serviceInterval = locationSettings.getJSONObject(0).getInt("GpsServiceSampleIntervalX");
                    int oldServiceInterval = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                            OFFENDER_DETAILS_CONS.LOCATION_SERVICE_INTERVAL);
                    if (oldServiceInterval != serviceInterval) {
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LOCATION_SERVICE_INTERVAL,
                                serviceInterval);
                        isLocationSettingsChanged = true;
                    }
                }

                if (locationSettings.getJSONObject(0).has("GpsServiceCalculationType")) {
                    int serviceCalcType = locationSettings.getJSONObject(0).getInt("GpsServiceCalculationType");
                    int oldServiceCalcType = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                            OFFENDER_DETAILS_CONS.LOCATION_SERVICE_CALC_TYPE);
                    if (oldServiceCalcType != serviceCalcType) {
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LOCATION_SERVICE_CALC_TYPE,
                                serviceCalcType);
                        isLocationSettingsChanged = true;
                    }
                }

                if (locationSettings.getJSONObject(0).has("GpsServiceAverageTimeSpan")) {
                    int locAverageTimeFrame = locationSettings.getJSONObject(0).getInt("GpsServiceAverageTimeSpan");
                    int oldLocAverageTimeFrame = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(
                            OFFENDER_DETAILS_CONS.LOCATION_AVERAGE_TIME_FRAME);
                    if (oldLocAverageTimeFrame != locAverageTimeFrame) {
                        TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.LOCATION_AVERAGE_TIME_FRAME,
                                locAverageTimeFrame);
                        isLocationSettingsChanged = true;
                    }
                }
            }

            if (deviceConfiguration.getJSONObject(0).has("CommNetworkTest")) {
                currentNormalItem = "CommNetworkTest";
                int commNetworkTest = deviceConfiguration.getJSONObject(0).getInt("CommNetworkTest");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.COMM_NETWORK_TEST, commNetworkTest);
            }

            // the ssl configuration set only if have server parameter.
            // if not, the parameter in settings dialog will be determent
            if (deviceConfiguration.getJSONObject(0).has("IgnoreSSLCert")) {
                currentNormalItem = "IgnoreSSLCert";
                int ignoreSSLCert = deviceConfiguration.getJSONObject(0).getInt("IgnoreSSLCert");
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.IGNORE_SSL_CERT, ignoreSSLCert);
            }else{
                currentNormalItem = "IgnoreSSLCert";
                int ignoreSSLCert = -1;
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.IGNORE_SSL_CERT, ignoreSSLCert);
            }

            if (deviceConfiguration.getJSONObject(0).has("Battery_Threshold")) {

                String batteryThresholdJsonToDB = "[{\"";
                JSONArray batteryThreshold = deviceConfiguration.getJSONObject(0).getJSONArray("Battery_Threshold");
                if (batteryThreshold.getJSONObject(0).has("Charger_Low")) {
                    currentNormalItem = "Charger_Low";
                    int chargerLow = batteryThreshold.getJSONObject(0).getInt("Charger_Low");
                    batteryThresholdJsonToDB += currentNormalItem + "\":" + chargerLow;
                }

                if (batteryThreshold.getJSONObject(0).has("Charger_Medium")) {
                    currentNormalItem = "Charger_Medium";
                    int chargerMedium = batteryThreshold.getJSONObject(0).getInt("Charger_Medium");
                    batteryThresholdJsonToDB += "," + "\"" + currentNormalItem + "\":" + chargerMedium;
                }

                if (batteryThreshold.getJSONObject(0).has("Charger_High")) {
                    currentNormalItem = "Charger_High";
                    int chargerHigh = batteryThreshold.getJSONObject(0).getInt("Charger_High");
                    batteryThresholdJsonToDB += "," + "\"" + currentNormalItem + "\":" + chargerHigh;
                }

                if (batteryThreshold.getJSONObject(0).has("No_Charger_Critical")) {
                    currentNormalItem = "No_Charger_Critical";
                    int noChargerCritical = batteryThreshold.getJSONObject(0).getInt("No_Charger_Critical");
                    batteryThresholdJsonToDB += "," + "\"" + currentNormalItem + "\":" + noChargerCritical;
                }

                if (batteryThreshold.getJSONObject(0).has("No_Charger_Low")) {
                    currentNormalItem = "No_Charger_Low";
                    int noChargerLow = batteryThreshold.getJSONObject(0).getInt("No_Charger_Low");
                    batteryThresholdJsonToDB += "," + "\"" + currentNormalItem + "\":" + noChargerLow;
                }

                if (batteryThreshold.getJSONObject(0).has("No_Charger_Medium")) {
                    currentNormalItem = "No_Charger_Medium";
                    int noChargerMedium = batteryThreshold.getJSONObject(0).getInt("No_Charger_Medium");
                    batteryThresholdJsonToDB += "," + "\"" + currentNormalItem + "\":" + noChargerMedium;
                }

                batteryThresholdJsonToDB += "}]";
                TableOffenderDetailsManager.sharedInstance().updateColumnString(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.BATTERY_THRESHOLD,
                        batteryThresholdJsonToDB);
            }

            currentNormalItem = "Finished to parse";


            if (updateActivityListener != null) {
                updateActivityListener.onGetDeviceConfigurationResultParserFinishedToParse(isTagIdChanged, isBeaconIdChanged, isCommIntervalChanged,
                        lastTagRfId, isLocationSettingsChanged, isVoipSettingsEnableChanged, isCaseTamperEnabled, isLockScreenChanged,
                        isAppLanguageChanged);
            }

            if (deviceConfigurationType == DeviceConfigurationType.Device_Configuration) {
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER, versionNumber);
                SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.DEVICE_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_OK);
            } else if (deviceConfigurationType == DeviceConfigurationType.Offender_Configuration) {
                TableOffenderDetailsManager.sharedInstance().updateColumnInt(OFFENDER_DETAILS_CONS.COLUMN_OFF_CONFIG_VER_NUMBER, versionNumber);
                SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.OFFENDER_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_OK);
            }

        } catch (SecurityException e) {
            handleException(deviceConfigurationType, currentNormalItem + " - SecurityException\n", e);
        } catch (JSONException e) {
            handleException(deviceConfigurationType, currentNormalItem + " - JSONException\n", e);
        } catch (Exception e) {
            handleException(deviceConfigurationType, "Exception\n", e);
        }
    }

    private void handleException(DeviceConfigurationType deviceConfigurationType, String errCause, Exception exception) {
        DeveloperUtil.showDeveloperToastMessage(exception.getMessage());
        Log.e("CycleLog","GetDeviceConfigurationResultParser ERROR",exception);


        exception.printStackTrace();
        String stuckTrace = ((App) App.getContext()).printStuckTraceToFile(exception, false);

        String messageToUpload = "Error in " + TAG + ", Cause: " + errCause + "\n" + stuckTrace;
        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Exceptions);

        String error = ((App) App.getContext()).printStuckTraceToFile(exception, false);
        NetworkRepository.getInstance().handleErrorDuringCycle(error);

        if (deviceConfigurationType == DeviceConfigurationType.Device_Configuration) {
            SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.DEVICE_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_ERR);
        } else if (deviceConfigurationType == DeviceConfigurationType.Offender_Configuration) {
            SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.OFFENDER_CONFIG, NetworkRepositoryConstants.REQUEST_RESULT_ERR);
        }

    }
}
