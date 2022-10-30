package com.supercom.puretrack.util.constants.network;

import com.supercom.puretrack.data.repositories.KnoxProfileConfig;
import com.supercom.puretrack.model.business_logic_models.build.ServerUrl;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO - Alon - This util class can be simplified using Kotlin's list manipulation methods.
 *  Refactor after adding Kotlin support to the project to only use 1 method.
 */
public class ServerUrls  {
public static String CustomURL="Custom URL";
    public static String SERVER_URL_AES_KEY_BYTES = "A31380F07B001DFAE38F76EF75B89528";
    private static ServerUrls instance;

    private ServerUrls() {

    }

    public static ServerUrls getInstance() {
        if (instance == null) {
            instance = new ServerUrls();
        }

        return instance;
    }

    public String getDefaultUrl() {
        ArrayList<ServerUrl> serverUrlsByBuild = getServerUrlModelList();
        if(serverUrlsByBuild.size()==0){
            return "";
        }
        for (ServerUrl server : serverUrlsByBuild) {
            if (server.isDefault()) return server.getServerName();
        }
        return serverUrlsByBuild.get(0).getServerName();
    }

    public String getSelectedServerUrlAddress() {
        ArrayList<ServerUrl> serverUrlModelList = getServerUrlModelList();
        for (ServerUrl serverUrl : serverUrlModelList) {
            if (serverUrl.getServerName().equals(PureTrackSharedPreferences.getSelectedServerUrlName())
                    && !serverUrl.getServerName().equals(CustomURL)) {
                return serverUrl.getUrl();
            }
        }

        ServerUrl serverUrl = selectDefaultUrl();
        if(serverUrl!= null){
            return serverUrl.getUrl();
        }

        return PureTrackSharedPreferences.getCustomUrl();
    }

    private ServerUrl selectDefaultUrl() {
        ArrayList<ServerUrl> serverUrlModelList = getServerUrlModelList();
        String server= PureTrackSharedPreferences.getSelectedServerUrlName();

       if(server!=null && server.length()>0){
           String customUrl= PureTrackSharedPreferences.getCustomUrl();
           if(!server.equals(CustomURL) || (customUrl.length()>0 && !customUrl.equals(CustomURL))) {
               return null;
           }
       }

        ServerUrl result=null;
        for (ServerUrl serverUrl : serverUrlModelList) {
            if (serverUrl.isDefault()
                    && !serverUrl.getServerName().equals(CustomURL)) {
                result=serverUrl;
               break;
            }
        }

        if(serverUrlModelList.size()>0){
            if(!serverUrlModelList.get(0).getServerName().equals(CustomURL)){
               result= serverUrlModelList.get(0);
            }
        }

        if(result != null){
            PureTrackSharedPreferences.setSelectedServerUrlName(result.getServerName());
        }

        return result;
    }

    public boolean isUrlExistsInServerUrlList(String url) {
        for (ServerUrl serverUrl : getServerUrlModelList()) {
            if (serverUrl.getUrl().equals(url)) return true;
        }
        return false;
    }

    public String[] getServerUrlNamesArray() {
        final List<String> serversList = new ArrayList<>();
        for (ServerUrl serverUrl : getServerUrlModelList()) {
            serversList.add(serverUrl.getServerName());
        }
        return serversList.toArray(new String[0]);
    }

    public ServerUrl getServerUrlModelByName(String serverName) {
        ArrayList<ServerUrl> serverUrlModelList = getServerUrlModelList();
        for (ServerUrl serverUrl : serverUrlModelList) {
            if (serverUrl.getServerName().equals(serverName)) return serverUrl;
        }
        return null;
    }

    public ServerUrl getServerUrlModelByUrl(String url) {
        ArrayList<ServerUrl> serverUrlModelList = getServerUrlModelList();
        for (ServerUrl serverUrl : serverUrlModelList) {
            if (serverUrl.getUrl().equals(url)) return serverUrl;
        }
        for (ServerUrl serverUrl : serverUrlModelList) {
            if (serverUrl.getServerName().equals(CustomURL)) return serverUrl;
        }
        return null;
    }

    public ArrayList<ServerUrl> getServerUrlModelList() {
        return KnoxProfileConfig.getInstance().getKnoxConfig().urls;
    }

}
