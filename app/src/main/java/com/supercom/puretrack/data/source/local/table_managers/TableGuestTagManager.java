package com.supercom.puretrack.data.source.local.table_managers;

import com.supercom.puretrack.data.source.local.table.DatabaseTable;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableGuestTag;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;

public class TableGuestTagManager extends BaseTableManager {

    private final TableGuestTag table = DatabaseAccess.getInstance().tableGuestTag;


    private static final TableGuestTagManager INSTANCE = new TableGuestTagManager();


    public static TableGuestTagManager sharedInstance() {
        return INSTANCE;
    }

    public void onGuestTagDetected(int tagId) {
        boolean guestTagEnabled = TableOffenderDetailsManager.sharedInstance().getIsGuestTagEnabled();
        if (!guestTagEnabled) return;
        boolean tagExist = table.wasTagReceived(tagId);
        if (tagExist) {
            table.updateGuestTagTime(tagId);
            return;
        }
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig
                        .EventTypes.eventGuestTagEntered, -1, -1, System.currentTimeMillis(),
                "{\"info\":\"TagId=" + tagId + "\"}");
        table.insertRecord(tagId);
    }


    @Override
    protected DatabaseTable getTable() {
        return DatabaseAccess.getInstance().tableGuestTag;
    }

    @Override
    protected EnumDatabaseTables getEnumDBTable() {
        return EnumDatabaseTables.TABLE_GUEST_TAG;
    }
}
