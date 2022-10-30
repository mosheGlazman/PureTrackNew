package com.supercom.puretrack.ui.dialog;

import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;
import static com.supercom.puretrack.util.dialer.DialerUtils.startSuperComDialer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.repositories.KnoxProfileConfig;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.service.heart_beat.HeartBeatServiceJava2;
import com.supercom.puretrack.data.service.heart_beat.HeartBeatTaskJava;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.AndroidXAppManager;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.AutoRestartManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.FingerprintManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.MagneticManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.SensorDataSource;
import com.supercom.puretrack.data.source.local.table.TableDeviceDetails;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.build.KnoxConfig;
import com.supercom.puretrack.model.business_logic_models.build.ServerUrl;
import com.supercom.puretrack.model.database.entities.EntityDeviceDetails;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.util.LocationTask;
import com.supercom.puretrack.util.TaskCallBack;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.network.ServerUrls;
import com.supercom.puretrack.util.custom_implementations.OnOnlyAfterTextChangedListener;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.encryption.ScramblingTextUtils;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.LocaleUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

public class SettingsDialog extends DialogFragment implements SettingFragmentViewListener {
    private static boolean ll_hearth_beat_visible;

    private Spinner languageSpinner;
    private Spinner serversSpinner;
    private Spinner sp_servers_list;
    private EditText serverServerUrlAddressEditText;
    private EditText serverPassEditText;
    private EditText tagRdIfEditText;
    private EditText serialNumberEditText;
    private TextView applicationVersionInfo,tv_hearth_beat_status,tv_hearth_beat_address,tv_server_url;
    private CheckBox writeToFileCheckbox;
    private EditText knoxLicenseKeyEditText;
    Button start_hearth_beat_btn;
    Button stop_hearth_beat_btn;
    Button btn_sensors_log_enable;
    Button btn_sensors_log_disable;
    Button btnRegisterDevice;
    Button  btn_get_new_location_gps;
    Button btn_get_new_location_network;
    TextView tv_app_params;

    StringBuilder locationStringBuilder;
    TextView tv_location;
    LocationTask task;

    private SettingsDialogListener listener;

    @Override
    public void onDeviceRegistered(String deviceSerialNumber, String serverPassword) {
        serialNumberEditText.setText(deviceSerialNumber);
        serverPassEditText.setText(serverPassword);
        btnRegisterDevice.setVisibility(View.GONE);
        Toast.makeText(getActivity(), "Device has been successfully registered remotely!", Toast.LENGTH_SHORT).show();
    }

    public interface SettingsDialogListener {

        void handleKnoxSdkPressed();
        void onSettingsChange();
        void openSettingsDialogAndSetLocale(String locale);
    }

    public SettingsDialog() {

    } 
    @SuppressLint("ValidFragment")
    public SettingsDialog(SettingsDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings, null);
        init(view);
        builder.setView(view);
        builder.setTitle("Application Settings");
        builder.setCancelable(true);
        return builder.create();
    }

    private void init(final View dialog) {
        initViews(dialog);

        setTextChangedListeners();

        EntityDeviceDetails deviceDetails = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord();
        serialNumberEditText.setText(deviceDetails.getDeviceSerialNumber());
        tagRdIfEditText.setText(String.valueOf(TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID)));
        serverPassEditText.setText(NetworkRepository.getInstance().getServerPassword());

        applicationVersionInfo.setText(App.getDeviceInfo());
        applicationVersionInfo.append("\n");
        applicationVersionInfo.append("BatteryCounter:"+OffenderPreferencesManager.getInstance().setLastBatteryPercentCounter);
        applicationVersionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applicationVersionInfo.setText(App.getDeviceInfo());
                applicationVersionInfo.append("\n");
                applicationVersionInfo.append("BatteryCounter:"+OffenderPreferencesManager.getInstance().setLastBatteryPercentCounter);
            }
        });

        tv_app_params= dialog.findViewById(R.id.tv_app_params);
        tv_app_params.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initParamsText();
            }
        });

        CheckBox checkUseClientCert = dialog.findViewById(R.id.checkUseClientCert);
        checkUseClientCert.setChecked( KnoxProfileConfig.getInstance().getUseClientCert());

        CheckBox cb_allow_power_button = dialog.findViewById(R.id.cb_allow_power_button);
        cb_allow_power_button.setOnCheckedChangeListener(
              new CompoundButton.OnCheckedChangeListener(){
                  @Override
                  public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                      try {
                          boolean b = KnoxUtil.getInstance().getKnoxSDKImplementation().setAllowPowerOffAndRestart(checked);
                          boolean b1 = KnoxUtil.getInstance().getKnoxSDKImplementation().setFactoryReset(checked);
                          boolean b2 = KnoxUtil.getInstance().getKnoxSDKImplementation().setSafeMode(checked);
                          Toast.makeText(getActivity(),"result:"+b +" " + b1 + " " + b2, Toast.LENGTH_LONG).show();
                      } catch (SecurityException e) {
                          e.printStackTrace();
                          Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_LONG).show();
                      }
                  }
              }
       );

        Button devConfig_start_google_play_process_btn = dialog.findViewById(R.id.devConfig_google_play_process_btn);
        Button devConfig_start_knox_process_btn = dialog.findViewById(R.id.devConfig_start_knox_process_btn);

        boolean isDeveloperModeEnable = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE) > 0;
        dialog.findViewById(R.id.ll_hearth_beat).setVisibility(isDeveloperModeEnable ? View.VISIBLE:View.GONE);
        start_hearth_beat_btn = dialog.findViewById(R.id.start_hearth_beat_btn);
        stop_hearth_beat_btn = dialog.findViewById(R.id.stop_hearth_beat_btn);
        tv_hearth_beat_status = dialog.findViewById(R.id.tv_hearth_beat_status);
        tv_hearth_beat_address = dialog.findViewById(R.id.tv_hearth_beat_address);
        tv_location= dialog.findViewById(R.id.tv_location);
        btn_get_new_location_gps= dialog.findViewById(R.id.btn_get_new_location_gps);
          btn_get_new_location_network= dialog.findViewById(R.id.btn_get_new_location_network);
       btn_get_new_location_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runLocationTask(true,false);
            }
        });
        btn_get_new_location_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runLocationTask(false,true);
            }
        });


        dialog.findViewById(R.id.btn_random_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverPassEditText.setText(String.format("%06d", new Random().nextInt(999999)));
            }
        });

        start_hearth_beat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HeartBeatServiceJava2.start();
                initHeartsBeatLastStatus();
            }
        });
        stop_hearth_beat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HeartBeatServiceJava2.stop(getActivity());
                initHeartsBeatLastStatus();
            }
        });
        dialog.findViewById(R.id.refresh_hearth_beat_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initHeartsBeatLastStatus();
            }
        });
        initHeartsBeatLastStatus();
        //boolean isKnoxLicenceActivated = PureTrackSharedPreferences.isKnoxLicenceActivated();
        //if (isKnoxLicenceActivated) {
        //    devConfig_start_knox_process_btn.setEnabled(false);
       // }
        knoxLicenseKeyEditText.setText(KnoxUtil.getInstance().getKnoxKlmProdKey());

        tagRdIfEditText.setEnabled(false);

        writeToFileCheckbox.setChecked(PureTrackSharedPreferences.getShouldWriteToFile());
        writeToFileCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    LoggingUtil.isNetworkLogEnabled = true;
                    LoggingUtil.isZonesLogEnabled = true;
                    LoggingUtil.isBleLogEnabled = true;

                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE, 2);
                } else {
                    LoggingUtil.isNetworkLogEnabled = false;
                    LoggingUtil.isZonesLogEnabled = false;
                    LoggingUtil.isBleLogEnabled = false;

                    TableOffenderDetailsManager.sharedInstance().updateColumnInt(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE, 1);
                }

                PureTrackSharedPreferences.setShouldWriteToFile(isChecked);
            }
        });


        dialog.findViewById(R.id.ll_hearth_beat).setVisibility(ll_hearth_beat_visible ? View.VISIBLE : View.GONE);
        dialog.findViewById(R.id.heart_beat_btn).setVisibility(ll_hearth_beat_visible ? View.GONE : View.VISIBLE);

        dialog.findViewById(R.id.heart_beat_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_hearth_beat_visible=true;
                dialog.findViewById(R.id.ll_hearth_beat).setVisibility(ll_hearth_beat_visible ? View.VISIBLE : View.GONE);
                dialog.findViewById(R.id.heart_beat_btn).setVisibility(ll_hearth_beat_visible ? View.GONE : View.VISIBLE);
            }
        });

        Button cycleNowBtn = dialog.findViewById(R.id.cycle_now_btn);
        Button openDialerButton = dialog.findViewById(R.id.settings_dialog_open_dialer);
        Button openSettingsButton = dialog.findViewById(R.id.settings_dialog_open_device_settings);


         btn_sensors_log_enable=dialog.findViewById(R.id.btn_sensors_log_enable);
         btn_sensors_log_disable=dialog.findViewById(R.id.btn_sensors_log_disable);
        btn_sensors_log_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorDataSource.getInstance().setShowSensors(false);
                initShowSensorsButton();
                listener.onSettingsChange();
            }
        });
        btn_sensors_log_disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorDataSource.getInstance().clear();
                SensorDataSource.getInstance().setShowSensors(true);
                initShowSensorsButton();
                listener.onSettingsChange();
            }
        });
        initShowSensorsButton();

        dialog.findViewById(R.id.btn_finger_print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidXAppManager.getInstance().startAuthenticate();
            }
        });
        dialog.findViewById(R.id.btn_finger_print_enroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidXAppManager.getInstance().startEnroll();
            }
        });
        dialog.findViewById(R.id.btn_finger_print_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidXAppManager.getInstance().getStatus();
            }
        });
        String deviceSerialNumber = deviceDetails.getDeviceSerialNumber();
        btnRegisterDevice = dialog.findViewById(R.id.btn_register_device);
        btnRegisterDevice.setVisibility(deviceSerialNumber==null || deviceSerialNumber.length() == 0 ? View.VISIBLE:View.GONE);
        btnRegisterDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkRepository.getInstance().registerDeviceRemotely(SettingsDialog.this);
            }
        });

        dialog.findViewById(R.id.btn_mock_gps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                dialog.findViewById(R.id.ll_mock_gps).setVisibility(View.VISIBLE);
            }
        });

        ((EditText)dialog.findViewById(R.id.et_mock_location_lat)).setText(LocationManager.Mock_location_lat+"");
        ((EditText)dialog.findViewById(R.id.et_mock_location_lat)).addTextChangedListener(new OnOnlyAfterTextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(editable.length()==0){
                        LocationManager.Mock_location_lat=0;
                    }else {
                        LocationManager.Mock_location_lat = Double.parseDouble(editable.toString());
                    }
                } catch (NumberFormatException e) {
                    LocationManager.Mock_location_lat=0;
                    if(editable.length() > 0){
                        ((EditText)dialog.findViewById(R.id.et_mock_location_lat)).setText("0");
                    }
                    e.printStackTrace();
                }
            }
        });

        ((EditText)dialog.findViewById(R.id.et_mock_location_lon)).setText(LocationManager.Mock_location_lon+"");
        ((EditText)dialog.findViewById(R.id.et_mock_location_lon)).addTextChangedListener(new OnOnlyAfterTextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(editable.length()==0){
                        LocationManager.Mock_location_lon=0;
                    }else {
                        LocationManager.Mock_location_lon = Double.parseDouble(editable.toString());
                    }
                } catch (NumberFormatException e) {
                    LocationManager.Mock_location_lon=0;
                    if(editable.length() > 0){
                        ((EditText)dialog.findViewById(R.id.et_mock_location_lon)).setText("0");
                    }
                    e.printStackTrace();
                }
            }
        });

        dialog.findViewById(R.id.magnetics_calibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MagneticManager.getInstance().saveMagneticValue();
            }
        });

        openDialerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSuperComDialer("en", false);
            }
        });

        cycleNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkRepository.getInstance().startNewCycle();
            }
        });

        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 10);
            }
        });

        View.OnClickListener devSettingsOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.checkUseClientCert:
                        KnoxProfileConfig.getInstance().setUseClientCert(((CheckBox) v).isChecked());
                        break;

                    case R.id.devConfig_start_knox_process_btn:
                        //listener.handleKnoxSdkPressed();
                        //dismiss();

                        Intent intent = null;
                        try {
                            intent = new Intent();
                            intent.setClassName("com.supercom.knox.appmanagement", "com.supercom.knox.appmanagement.ActivateActivity");
                            App.getContext().startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(App.getContext(), "failed to open knox app\n\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return;
                }

                InputMethodManager imm = (InputMethodManager) dialog.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        };

        checkUseClientCert.setOnClickListener(devSettingsOnClickListener);

        devConfig_start_google_play_process_btn.setOnClickListener(devSettingsOnClickListener);
        devConfig_start_knox_process_btn.setOnClickListener(devSettingsOnClickListener);

        languageSpinner.setSelection(new LocaleUtil().getCurrentCountryPosition());
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (new LocaleUtil().getCurrentCountryPosition() == pos) return;
                String[] countiesShortcutArray = App.getContext().getResources().getStringArray(R.array.counties_shortcut);
                TableOffenderDetailsManager.sharedInstance().updateColumnString(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.APP_LANGUAGE, countiesShortcutArray[pos]);
                new LocaleUtil().changeApplicationLanguageIfNeeded();
                String languageLocale = countiesShortcutArray[pos];
                listener.openSettingsDialogAndSetLocale(languageLocale);
                dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initUrls(dialog);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                ServerUrls.getInstance().getServerUrlNamesArray());
        serversSpinner.setAdapter(adapter);

        List<ServerUrl> serverUrlModelList = ServerUrls.getInstance().getServerUrlModelList();
        //String serverUrlFromDatabase = NetworkRepository.getInstance().getServerUrl();
        //if (serverUrlFromDatabase != null && !serverUrlFromDatabase.isEmpty()){
        //     migrateServerUrlFromDatabaseToSharedPrefs(serverUrlFromDatabase);
        //}
        String selectedServerUrlName = PureTrackSharedPreferences.getSelectedServerUrlName();
        ServerUrl serverUrlModelBy = ServerUrls.getInstance().getServerUrlModelByName(selectedServerUrlName);

        if(serverUrlModelBy!= null){
            for (int i = 0; i < serverUrlModelList.size(); i++) {
                if (serverUrlModelList.get(i).getServerName().equals(serverUrlModelBy.getServerName())) {
                    serversSpinner.setSelection(i);
                    break;
                }
            }
        }
        serversSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUrlName = ServerUrls.getInstance().getServerUrlModelList().get(position).getServerName();
                String selectedUrlAddress;
                if (selectedUrlName.equals(ServerUrls.getInstance().getServerUrlNamesArray()[ServerUrls.getInstance().getServerUrlNamesArray().length - 1])) {
                    selectedUrlAddress = PureTrackSharedPreferences.getCustomUrl();
                } else {
                    selectedUrlAddress = ServerUrls.getInstance().getServerUrlModelList().get(position).getUrl();
                }
                PureTrackSharedPreferences.setSelectedServerUrlName(selectedUrlName);
                serverServerUrlAddressEditText.setText(selectedUrlAddress);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initAndroidXAppManager();
    }

    private void initAndroidXAppManager() {
         AndroidXAppManager.getInstance().setBiometricsListener( new AndroidXAppManager.BiometricsListener() {
            @Override
            public void onAuthenticate() {
                Toast.makeText(getActivity(), "Authenticate success!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticateFailed() {
                Toast.makeText(getActivity(), "Authenticate Failed!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error, int errorCode) {
                Toast.makeText(getActivity(), error+" code:"+errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusReceived(AndroidXAppManager.BiometricStatus status) {
                Toast.makeText(getActivity(),  "status:"+status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initParamsText() {
        String[] params = new String[]{"Auto_restart"};
        String[] params_json = new String[]{
                AutoRestartManager.getInstance().getAutoRestartParams()
        };

        StringBuilder builder = new StringBuilder();
        builder.append(toHtmlBold(toHtmlRed(toHtmlH1(toHtmlUnderline("Params")))));

        for (int i = 0; i < params.length; i++) {
            builder.append(htmlEnter());
            builder.append(toHtmlBold(toHtmlBlue(toHtmlUnderline(params[i]))));
            String json = params_json[i];
            json = toHtmlJson(json);
            builder.append(json);
        }

        tv_app_params.setText(Html.fromHtml(builder.toString()));
    }

    private String toHtmlJson(String json) {
        String enter=htmlEnter();
        String start=toHtmlBlue("{");
        String psik=toHtmlViolet(",");
        String nn=toHtmlRed(":");
        String end=toHtmlBlue("}");
        boolean startGreen=false;

        String space=" ";
        String tab="\t\t\t";
        String tabss="";
        String res="";

        for(char c:json.toCharArray()){
            if(c=='{'){
                res+=enter+tabss+space+start+enter;
                tabss+=tab;
                res+=tabss+space;
            }else  if(c==','){
                if(startGreen){
                    startGreen=false;
                    res+=toHtmlGreen_end();
                }
                res+=psik+enter+tabss+space;
            }else if(c=='}'){
                if(startGreen){
                    startGreen=false;
                    res+=toHtmlGreen_end();
                }

                if(tabss.length()>0) {
                    tabss =tabss.substring(0,tabss.length()-tab.length());
                }
                res+=enter+tabss+space+end+enter+tabss;
            }else if(c==':'){
                if(startGreen){
                    res+=nn;
                }else {
                    res += nn + toHtmlGreen_start();
                    startGreen = true;
                }
            }else{
                if(c==']'){
                    if(startGreen){
                        startGreen=false;
                        res+=toHtmlGreen_end();
                    }
                }

                res+=c;
            }
        }

        if(startGreen){
            startGreen=false;
            res+=toHtmlGreen_end();
        }

        return res;
    }

    private void runLocationTask(final boolean byGPS,final boolean byNetwork) {

        if(task==null){
            task = new LocationTask();
        }

        if(!task.isRunning()) {
            locationStringBuilder = new StringBuilder();
            tv_location.setText(Html.fromHtml(
                    getTimeLine()+
                    toHtmlBlue("Starting...")+
                    htmlEnter()
            ));
        }

        if(byGPS){
            btn_get_new_location_gps.setEnabled(false);
        }
        if(byNetwork){
            btn_get_new_location_network.setEnabled(false);
        }
        task = new LocationTask();
        task.start(byGPS, byNetwork,20, new TaskCallBack() {
            @Override
            public boolean onReceived(final Location location) {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(location==null){
                            locationStringBuilder.append(toHtmlRed("Result is null"));
                            locationStringBuilder.append(htmlEnter());
                            setLocationText();
                        }else{
                            double lat = location.getLatitude()+LocationManager.Mock_location_lat;
                            double lon = location.getLongitude()+LocationManager.Mock_location_lon;
                            if(LocationManager.Mock_location_lat!=0 || + LocationManager.Mock_location_lon!=0){
                                location.setLatitude(location.getLatitude()+LocationManager.Mock_location_lat);
                                location.setLongitude(location.getLongitude()+LocationManager.Mock_location_lon);
                            }
                            location.setAccuracy(1);
                            locationStringBuilder.append(htmlEnter());
                            locationStringBuilder.append(htmlEnter());
                            locationStringBuilder.append(
                                            getTimeLine()+
                                            toHtmlBold("PROVIDER:")+
                                            location.getProvider() +
                                            htmlEnter() +
                                            lat+","+ lon);

                            locationStringBuilder.append(htmlEnter());
                            if(LocationManager.getInstance()!= null){


                                LocationManager.getInstance().onLocationChanged(location);
                            }
                        }

                        setLocationText();
                    }
                });

                return location!=null;
            }

            @Override
            public void onStop(final boolean locationReceived) {
                NetworkRepository.getInstance().startNewCycle();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(byGPS){
                            btn_get_new_location_gps.setEnabled(true);
                        }
                        if(byNetwork){
                            btn_get_new_location_network.setEnabled(true);
                        }

                        if(!locationReceived) {
                            locationStringBuilder.append(htmlEnter());
                            locationStringBuilder.append(toHtmlRed("task stop with no answer"));
                        }

                        setLocationText();
                    }
                });
            }
        });

    }

    private String getTimeLine() {
         return toHtmlBold("Time") + DateFormatterUtil.getCurDateStr(DateFormatterUtil.HMS)+ htmlEnter();
    }
    private void setLocationText() {
        tv_location.setText(Html.fromHtml(locationStringBuilder.toString()));
    }
    private String toHtmlBlue(String t) {
        return   "<font color=\"#00607F\">"+t+"</font>";
    }
    public static  String toHtmlGray(String text) { return "<font color=\"#cccccc\">" +  text+ "</font>";}
    public static  String toHtmlYellow(String text) { return "<font color=\"#ffd169\">" +  text+ "</font>";}
    public static  String toHtmlGreen(String text) { return toHtmlGreen_start() +  text+ toHtmlGreen_end();}
    public static  String toHtmlGreen_start() { return "<font color=\"#208030\">" ;}
    public static  String toHtmlGreen_end() { return "</font>";}
    public static  String toHtmlRed(String text) { return "<font color=\"#802030\">" +  text+ "</font>";}
    public static  String toHtmlViolet(String text) { return "<font color=\"#8e7bce\">" +  text+ "</font>";}
    public static  String toHtmlBlueSky(String text) { return "<font color=\"#93B4B1\">" +  text+ "</font>";}
    public static  String toHtmlPink(String text) { return "<font color=\"#ff9ed0\">" +  text+ "</font>";}
    KnoxConfig knoxConfig;
    ServerUrl serverUrl;

    public static  String toHtmlH1(String text) { return "<H1>" +  text+ "</H1>";}
    public static  String toHtmlUnderline(String text) { return "<U>" +  text+ "</U>";}
    public static  String toHtmlBold(String text) { return "<B>" +  text+ "</B>";}
    public static  String htmlEnter() { return "<BR>";}
    public static  String toHtmlGrayDarkDark(String text) { return "<font color=\"#303030\">" +  text+ "</font>";}
    private void initUrls(final View dialog) {
        sp_servers_list = dialog.findViewById(R.id.sp_servers_list);
        tv_server_url = dialog.findViewById(R.id.tv_server_url);
        knoxConfig = KnoxProfileConfig.getInstance().getKnoxConfig();
        serverUrl = KnoxProfileConfig.getInstance().getSelectedServerUrl();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                knoxConfig.getServerUrlNamesArray());

        sp_servers_list.setAdapter(adapter);
        sp_servers_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serverUrl = knoxConfig.urls.get(position);
                KnoxProfileConfig.getInstance().setSelectedServerUrl(serverUrl);
                tv_server_url.setText(Html.fromHtml(
                        toHtmlBold(
                                toHtmlGrayDarkDark("Server url (") +
                                     serverUrl.getServerName()+") :")+
                                htmlEnter()+
                                serverUrl.getUrl()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Integer index = knoxConfig.getServerIndex(serverUrl);
        if(index!= null){
            sp_servers_list.setSelection(index);
        }else{
            sp_servers_list.setSelection(0);
        }
    }

    private void initShowSensorsButton() {
        boolean showSensors= SensorDataSource.getInstance().showSensors();
        btn_sensors_log_disable.findViewById(R.id.btn_sensors_log_disable).setVisibility(showSensors ? View.GONE : View.VISIBLE);
        btn_sensors_log_enable.findViewById(R.id.btn_sensors_log_enable).setVisibility(!showSensors ? View.GONE : View.VISIBLE);

        btn_sensors_log_disable.setText(Html.fromHtml("sensors log disable<BR><font color=\"#60DC74\"><B>push to enable</B></font>"));
        btn_sensors_log_enable.setText(Html.fromHtml("sensors log enabled<BR><font color=\"#be3246\"><B>push to disable</B></font>"));
    }

    private void initHeartsBeatLastStatus() {
        tv_hearth_beat_address.setText(HeartBeatServiceJava2.pureTagMacAddress == null ? "":HeartBeatServiceJava2.pureTagMacAddress);

        String text ="";
        SimpleDateFormat dateFromatOutput = new SimpleDateFormat("HH:mm:ss");
        if(HeartBeatServiceJava2.lastSuccessConnect != null){
            text +="<font color=\"#009400\"><B><U>Last success:</U></B> "+  dateFromatOutput.format(HeartBeatServiceJava2.lastSuccessConnect ) + "</font>";

            if(HeartBeatServiceJava2.lastFailedCounter > 0){
                text +="<font color=\"red\"><B><U> failed count:</U></B> " + HeartBeatServiceJava2.lastFailedCounter+ "</font>";
            }

            text +="<BR>";
        }

        if(HeartBeatServiceJava2.lastError != null && HeartBeatServiceJava2.lastError.length() >0){
            text+= "<font color=\"red\"><B><U> last error: </U></B>"+HeartBeatServiceJava2.lastError+"</font>";
            text +="<BR>";
        }else {

            HeartBeatTaskJava taskJava = HeartBeatServiceJava2.activeTask;
            if (taskJava != null) {
                text += "<B><U>Last task</U> start:</B>" + dateFromatOutput.format(taskJava.startTime);
                if (taskJava.result != null) {
                    text += " <B>result:</B> " + taskJava.result;
                } else {
                    text += " <B>status:</B> " + taskJava.status;
                }
            }
        }

        tv_hearth_beat_status.setText(Html.fromHtml(text));

        start_hearth_beat_btn.setVisibility( !HeartBeatServiceJava2.running ? View.VISIBLE : View.GONE);
        stop_hearth_beat_btn .setVisibility( HeartBeatServiceJava2.running  ? View.VISIBLE : View.GONE);
    }

    private void initViews(View dialog) {
        languageSpinner = dialog.findViewById(R.id.language_spinner);
        serversSpinner = dialog.findViewById(R.id.servers_spinner);
        serverServerUrlAddressEditText = dialog.findViewById(R.id.selected_server_address);
        serverPassEditText = dialog.findViewById(R.id.server_password_edit_text);
        tagRdIfEditText = dialog.findViewById(R.id.tag_rf_id_edit_text);
        serialNumberEditText = dialog.findViewById(R.id.serial_number_edit_text);
        applicationVersionInfo = dialog.findViewById(R.id.app_version_info);
        writeToFileCheckbox = dialog.findViewById(R.id.write_to_file_checkbox);
        knoxLicenseKeyEditText = dialog.findViewById(R.id.knox_license_key_edit_text);
    }

    private void setTextChangedListeners() {
        serverServerUrlAddressEditText.addTextChangedListener(new OnOnlyAfterTextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                ServerUrl url=ServerUrls.getInstance().getServerUrlModelByUrl(editable.toString());
                if(url!= null) {
                    String serverName = url.getServerName();
                    PureTrackSharedPreferences.setSelectedServerUrlName(serverName);
                    if (ServerUrls.getInstance().isUrlExistsInServerUrlList(editable.toString())) {
                        return;
                    }

                    PureTrackSharedPreferences.setCustomUrl(editable.toString());
                }
            }
        });
        serverPassEditText.addTextChangedListener(new OnOnlyAfterTextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (serverPassEditText.getText().toString().isEmpty()) return;
                String scrambledEncPass = "";
                try {
                    String ScrambledPass = ScramblingTextUtils.scramble(editable.toString());
                    scrambledEncPass = AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, ScrambledPass);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                TableOffenderDetailsManager.sharedInstance().updateColumnString
                        (TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_PASS, scrambledEncPass);
            }
        });
        serialNumberEditText.addTextChangedListener(new OnOnlyAfterTextChangedListener() {

            @Override
            public void afterTextChanged(Editable editable) {
                boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == 1;
                if (isOffenderAllocated) return;
                String deviceSerialNumber = editable.toString();
                EntityDeviceDetails deviceDetails = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord();
                deviceDetails.SetDeviceSN(deviceSerialNumber);
                DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS, TableDeviceDetails.COLUMN_DEV_SN,
                        deviceSerialNumber);
            }
        });

        knoxLicenseKeyEditText.addTextChangedListener(new OnOnlyAfterTextChangedListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) return;
                KnoxUtil.getInstance().setKnoxKlmProdKey(editable.toString());
            }
        });
    }
}
