package com.supercom.puretrack.data.source.local.table_managers;

import com.supercom.puretrack.data.source.local.table.DatabaseTable;
import com.supercom.puretrack.data.source.local.table.TableSelfDiagnosticEvents;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;

public class TableSelfDiagnosticManager extends BaseTableManager {

    private static final TableSelfDiagnosticManager INSTANCE = new TableSelfDiagnosticManager();

    public static String ENABLED = TableSelfDiagnosticEvents.ENABLED;
    public static String GYROSCOPE_SENSITIVITY = TableSelfDiagnosticEvents.GYROSCOPE_SENSITIVITY;
    public static String MAGNETIC_SENSITIVITY = TableSelfDiagnosticEvents.MAGNETIC_SENSITIVITY;


    public static TableSelfDiagnosticManager sharedInstance() {
        return INSTANCE;
    }


    @Override
    protected DatabaseTable getTable() {
        return DatabaseAccess.getInstance().tableSelfDiagnosticEvents;
    }

    @Override
    protected EnumDatabaseTables getEnumDBTable() {
        return EnumDatabaseTables.TABLE_SELF_DIAGNOSTIC_EVENTS;
    }
}