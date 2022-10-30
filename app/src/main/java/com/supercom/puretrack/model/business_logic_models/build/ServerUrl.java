package com.supercom.puretrack.model.business_logic_models.build;

public class ServerUrl {

    private String url;
    private String serverName;
    private boolean isDefault;

    public ServerUrl(String url, String serverName, boolean isDefault) {
        this.url = url;
        this.serverName = serverName;
        this.isDefault = isDefault;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
