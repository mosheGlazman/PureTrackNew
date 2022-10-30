package com.supercom.puretrack.data.repositories;

import android.provider.ContactsContract;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.source.local.local_managers.hardware.PreferencesBase;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.PreferencesBase;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.model.business_logic_models.build.KnoxConfig;
import com.supercom.puretrack.model.business_logic_models.build.ServerUrl;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.constants.network.ServerUrls;
import com.supercom.puretrack.util.hardware.FilesManager;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class KnoxProfileConfig extends PreferencesBase {
    private static KnoxProfileConfig instance;
    private KnoxConfig config;

    private KnoxProfileConfig() {
        super(App.applicationContext, "KnoxProfileConfig");
        config = getDefault();
        loadConfigListFromFile();
        loadConfigListFromPreferences();
    }

    private KnoxConfig getDefault() {
        KnoxConfig config = new KnoxConfig();
        config.urls = new ArrayList<>();
        config.defaultLanguage = "en";
        config.lbsLocationRequired = true;
        config.defaultUseSSLCertificate = false;
        ServerUrl customUrl = new ServerUrl("", ServerUrls.CustomURL, false);

        config.urls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceRomania/RestfulService.svc/", "Romania", true));
        config.urls.add(new ServerUrl("https://sime.stsisp.ro/PureMonitorWCFService/RestfulService.svc/", "Romania Local", false));
        config.urls.add(customUrl);

        if (BuildConfig.DEBUG) {
             config.urls = getAllServers();
        }

        return config;
    }

    public static KnoxProfileConfig getInstance() {
        if (instance == null) {
            instance = new KnoxProfileConfig();
        }

        return instance;
    }

    public KnoxConfig getKnoxConfig(){
        if(loadConfigListFromFile()){
            loadConfigListFromPreferences();
        }

        return config;
}

    public void loadConfigListFromPreferences() {
        String j = get("KnoxProfile", "");
        if (j.length() > 0) {
            try {
                KnoxConfig conf = new GsonBuilder().create().fromJson(j, KnoxConfig.class);
                if(conf != null){
                    config = conf;
                }
            } catch (Exception e) {
                Log.e("KnoxProfileConfig", "failed to parse/n" + j);
            }
        }
    }

    public boolean loadConfigListFromFile() {
        File dir = new File(FilesManager.getInstance().KNOX_CONFIG_FOLDER);
        if (!dir.exists()) {
            return false;
        }

        File file = null;
        try {
            for (File f : dir.listFiles()) {
                try {
                    String path = f.getAbsolutePath();
                    String filename = path.substring(path.lastIndexOf("/") + 1);
                    String extension = path.substring(path.lastIndexOf(".") + 1);

                    if (filename.startsWith(FilesManager.getInstance().KNOX_CONFIG_FILE_START)) {
                        if (extension.equals(FilesManager.getInstance().KNOX_CONFIG_FILE_EXT)) {
                            file = f;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (file == null) {
            return false;
        }

        //boolean isOffenderAllocated = false;
        // try {
        //     isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        //if (isOffenderAllocated) {
        //    return false;
        //}

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                file.delete();
            } catch (Exception e) {
                Log.i("KnoxProfileConfig", "Delete File error", e);
            }
        }

        String data = text.toString();
        try {
            KnoxConfig newConfig = new GsonBuilder().create().fromJson(data, KnoxConfig.class);
            if (newConfig != null) {
                for (ServerUrl url : newConfig.urls) {
                    if (url.getServerName().toLowerCase().contains("custom url")) {
                        newConfig.urls.remove(url);
                        break;
                    }
                }
                ServerUrl customUrl = new ServerUrl("", ServerUrls.CustomURL, false);
                newConfig.urls.add(customUrl);

                if (config != null) {
                    if (newConfig.useSSLCertificate == null) {
                        newConfig.useSSLCertificate = config.useSSLCertificate;
                    }
                }

                String selectedServer = PureTrackSharedPreferences.getSelectedServerUrlName();
                boolean foundSelected = false;
                ServerUrl defaultServerUrl = null;

                for (ServerUrl url : newConfig.urls) {
                    if (url.isDefault() && defaultServerUrl == null) {
                        defaultServerUrl = url;
                    }

                    if (url.getServerName().equals(selectedServer)) {
                        foundSelected = true;
                        break;
                    }
                }

                if (!foundSelected && defaultServerUrl != null) {
                    PureTrackSharedPreferences.setSelectedServerUrlName(defaultServerUrl.getServerName());
                }

                data = new Gson().toJson(newConfig);
            } else {
                Log.e("KnoxProfileConfig", "failed to parse/n" + data);
                return false;
            }
        } catch (Exception e) {
            Log.e("KnoxProfileConfig", "failed to parse/n" + data, e);
        }

        put("KnoxProfile", data);
        return true;
    }

    private void saveCurrentConfig(){
        String data  = new Gson().toJson(config);
        put("KnoxProfile", data);
    }

    public boolean getUseClientCert() {
        getKnoxConfig();

        if(config.useSSLCertificate!= null){
            return config.useSSLCertificate;
        }

        //BuildConfig.FLAVOR.equals("Finland") ? 1 : 0;
        return config.defaultUseSSLCertificate;
    }

    public void setUseClientCert(boolean use) {
         if(config==null){
             return;
         }

        config.useSSLCertificate = use;
        saveCurrentConfig();
    }

    public String getDefaultLanguage() {
        return config.defaultLanguage;
    }

    public boolean lbsLocationRequired() {
       // return (BuildConfig.FLAVOR != "USA")
        return config.lbsLocationRequired;
    }

    public ArrayList<ServerUrl> getAllServers() {
        ArrayList<ServerUrl> serverUrls = new ArrayList<>();

        serverUrls.add(new ServerUrl("https://appaz.puremonitor.supercom.com/PureMonitorWCFServiceMain/restfulservice.svc/",
                "Main" , false));

        serverUrls.add(new ServerUrl("https://svcdemoaz.puremonitor.supercom.com/PureMonitorWCFServiceDemo/RestfulService.svc/",
                "Belgium(Demo)", false));

        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceRomania/RestfulService.svc/",
                "Romania", false));

        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceFinland/restfulservice.svc/",
                "Finland-Test(Azure)", false));
        serverUrls.add(new ServerUrl("https://emsdevice-qat.om.fi/puremonitorwcfservice/restfulservice.svc/",
                "Finland-QAT", true));
        serverUrls.add(new ServerUrl("https://emsdevice.om.fi/puremonitorwcfservice/restfulservice.svc/",
                "Finland-Production", false));

        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceMOJSat/restfulservice.svc/",
                "MOJ-Test(Azure)", false));
        serverUrls.add(new ServerUrl("https://172.24.11.34/PureMonitorWCFService/RestfulService.svc/",
                "MOJ-Pre-Release", false));
        serverUrls.add(new ServerUrl("https://172.24.11.43/PureMonitorWCFService/RestfulService.svc/",
                "MOJ-Train", false));
        serverUrls.add(new ServerUrl("https://172.22.8.37/PureMonitorWCFservice/restfulService.svc/",
                "MOJ-Production", true));

        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceUSNew/restfulservice.svc/",
                "US-Test(Azure)", false));
        serverUrls.add(new ServerUrl("https://52.32.241.136/PureMonitorWCFService/RestfulService.svc/",
                "US-Demo", true));
        serverUrls.add(new ServerUrl("https://52.24.218.129/PureMonitorWCFService/RestfulService.svc/",
                "US-Production", false));

        /*
        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceCroatia/restfulservice.svc/", "*DEPRECATED* svcaz-Croatia ", false));
        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServiceLatvia/restfulservice.svc/","*DEPRECATED* svcaz-Latvia irrelevant", false));
        serverUrls.add(new ServerUrl("https://svcaz.puremonitor.supercom.com/PureMonitorWCFServicePolice/restfulservice.svc/","*DEPRECATED* svcaz-Police irrelevant", false));
        */

        serverUrls.add(new ServerUrl("", ServerUrls.CustomURL, false));
        return serverUrls;
    }

    public ServerUrl getSelectedServerUrl() {
        String j = get("ServerUrl", "");
        ServerUrl  result = new Gson().fromJson(j,ServerUrl.class);

        if (result == null) {
            if(config!= null && config.urls.size() > 0) {
                result = config.urls.get(0);
                for (ServerUrl url : config.urls) {
                    if (url.isDefault()) {
                        result = url;
                        break;
                    }
                }
            }
        }

        if (result==null) {
            result = new ServerUrl("","Unknow",false);
        }

        return result;
    }

    public void setSelectedServerUrl(ServerUrl serverUrl) {
        String j =new Gson().toJson(serverUrl);
        put("ServerUrl", j);
    }
}
