package com.supercom.puretrack.data.source.local.table_managers;

import com.supercom.puretrack.data.source.local.table.DatabaseTable;
import com.supercom.puretrack.data.source.local.table.TableScannerType;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;

public class TableScannerTypeManager extends BaseTableManager {

    private static final TableScannerTypeManager INSTANCE = new TableScannerTypeManager();

    public static String NORMAL_SCAN_ENABLED = TableScannerType.NORMAL_SCAN_ENABLED;
    public static String MAC_SCAN_ENABLED = TableScannerType.MAC_SCAN_ENABLED;
    public static String MANUFACTURER_ID = TableScannerType.MANUFACTURER_ID;
    public static String TAG_MAC_ADDRESS = TableScannerType.TAG_MAC_ADDRESS;
    public static String BEACON_MAC_ADDRESS = TableScannerType.BEACON_MAC_ADDRESS;


    public static TableScannerTypeManager sharedInstance() {
        return INSTANCE;
    }


    @Override
    protected DatabaseTable getTable() {
        return DatabaseAccess.getInstance().tableScannerType;
    }

    @Override
    protected EnumDatabaseTables getEnumDBTable() {
        return EnumDatabaseTables.TABLE_SCANNER_TYPE;
    }
}
