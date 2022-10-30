package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityApnDetails;

import java.util.ArrayList;
import java.util.List;

public class TableApnDetails extends DatabaseTable {

    public interface APN_DEFAULT_VALUES {
        String ICC_ID_PREFIX_1 = "894453";
        String DETAILS_1 = "eseye.com";
        String NAME_1 = "Eseye";
        String USER_1 = "user";
        String PASSWORD_1 = "pass";
        int AUTH_TYPE_1 = 3;

        String ICC_ID_PREFIX_2 = "894620";
        String DETAILS_2 = "internetm2m.mbb.com";
        String NAME_2 = "Telit";

        String ICC_ID_PREFIX_3 = "893510";
        String DETAILS_3 = "m2minternet";
        String NAME_3 = "SFR";

        String ICC_ID_PREFIX_4 = "882360";
        String DETAILS_4 = "data641003";
        String NAME_4 = "data641003";

    }

    //Table
    private static final String TABLE_APN_DETAILS = "APNDetails";

    // Columns names
    public static final String COLUMN_APN_ICC_ID_PREFIX = "apn_icc_id_prefix";
    public static final String COLUMN_APN_DETAILS = "apn_detals";
    public static final String COLUMN_APN_NAME = "apn_name";
    public static final String COLUMN_APN_USER = "apn_user";
    public static final String COLUMN_APN_PASSWORD = "apn_password";
    public static final String COLUMN_APN_AUTH_TYPE = "apn_auth_type";

    public TableApnDetails() {
        this.tableName = TABLE_APN_DETAILS;
        this.columnNumber = 0;
        this.AddColumn(new DatabaseColumn(COLUMN_APN_ICC_ID_PREFIX, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_APN_DETAILS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_APN_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_APN_USER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_APN_PASSWORD, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_APN_AUTH_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        BuildColumnNameArray();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityApnDetails recordAPNDetails = (EntityApnDetails) databaseEntity;
        ContentValues values = new ContentValues();
        values.put(COLUMN_APN_ICC_ID_PREFIX, recordAPNDetails.apn_icc_id_prefix);
        values.put(COLUMN_APN_DETAILS, recordAPNDetails.apn_details);
        values.put(COLUMN_APN_NAME, recordAPNDetails.apn_name);
        values.put(COLUMN_APN_USER, recordAPNDetails.apn_user);
        values.put(COLUMN_APN_PASSWORD, recordAPNDetails.apn_password);
        values.put(COLUMN_APN_AUTH_TYPE, recordAPNDetails.apn_auth_type);
        return database.insert(TABLE_APN_DETAILS, null, values);
    }

    @Override
    protected EntityApnDetails GetRecordFromQueryCursor(Cursor QueryCursor) {
        return new EntityApnDetails(
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_APN_ICC_ID_PREFIX)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_APN_DETAILS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_APN_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_APN_USER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_APN_PASSWORD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_APN_AUTH_TYPE))
        );
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        List<EntityApnDetails> configuredAPNDetailsList = getConfiguredAPNRecordsList();
        for (int i = 0; i < configuredAPNDetailsList.size(); i++) {
            addRecord(db, configuredAPNDetailsList.get(i));
        }
    }

    private List<EntityApnDetails> getConfiguredAPNRecordsList() {
        List<EntityApnDetails> recordAPNDetailsArr = new ArrayList<>();
        recordAPNDetailsArr.add(new EntityApnDetails(APN_DEFAULT_VALUES.ICC_ID_PREFIX_1, APN_DEFAULT_VALUES.DETAILS_1, APN_DEFAULT_VALUES.NAME_1, APN_DEFAULT_VALUES.USER_1, APN_DEFAULT_VALUES.PASSWORD_1, APN_DEFAULT_VALUES.AUTH_TYPE_1));
        recordAPNDetailsArr.add(new EntityApnDetails(APN_DEFAULT_VALUES.ICC_ID_PREFIX_2, APN_DEFAULT_VALUES.DETAILS_2, APN_DEFAULT_VALUES.NAME_2));
        recordAPNDetailsArr.add(new EntityApnDetails(APN_DEFAULT_VALUES.ICC_ID_PREFIX_3, APN_DEFAULT_VALUES.DETAILS_3, APN_DEFAULT_VALUES.NAME_3));
        recordAPNDetailsArr.add(new EntityApnDetails(APN_DEFAULT_VALUES.ICC_ID_PREFIX_4, APN_DEFAULT_VALUES.DETAILS_4, APN_DEFAULT_VALUES.NAME_4));

        return recordAPNDetailsArr;
    }


    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
    }
}
