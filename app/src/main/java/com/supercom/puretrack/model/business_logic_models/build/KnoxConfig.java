package com.supercom.puretrack.model.business_logic_models.build;

import java.util.ArrayList;
import java.util.List;

public class KnoxConfig {
    public ArrayList<ServerUrl> urls=new ArrayList<>();
    public String defaultLanguage;
    public boolean lbsLocationRequired;
    public boolean isRelevantBuildForPhotoTestFeature;
    public boolean defaultUseSSLCertificate;
    public Boolean useSSLCertificate;

    public String[] getServerUrlNamesArray() {
        final List<String> serversList = new ArrayList<>();
        for (ServerUrl serverUrl : urls) {
            serversList.add(serverUrl.getServerName());
        }
        return serversList.toArray(new String[0]);
    }

    public Integer getServerIndex(ServerUrl server) {
        if(server== null){
            return null;
        }

        for (int i=0;i< urls.size();i++) {
            ServerUrl serverUrl = urls.get(i);
            if (serverUrl.getServerName().equals(server.getServerName())) {
                return i;
            }
        }

        return null;
    }
}
