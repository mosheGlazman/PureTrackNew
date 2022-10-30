package com.supercom.puretrack.data.source.local.table_managers;

public class TableApnDetailsManager {

    private static final TableApnDetailsManager INSTANCE = new TableApnDetailsManager();


    private TableApnDetailsManager() {

    }

    public static TableApnDetailsManager sharedInstance() {
        return INSTANCE;
    }


    public void createApnIfNotExists() {
    }

}

