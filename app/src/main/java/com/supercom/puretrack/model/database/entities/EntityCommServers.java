package com.supercom.puretrack.model.database.entities;

public class EntityCommServers extends DatabaseEntity {
    public String HttpHeader;
    public String IpAddress;
    public String WebService;

    public EntityCommServers(String HttpHeader, String IpAddress, String WebService) {
        this.HttpHeader = HttpHeader;
        this.IpAddress = IpAddress;
        this.WebService = WebService;
    }
}
