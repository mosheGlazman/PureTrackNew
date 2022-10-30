package com.supercom.puretrack.database;

import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.supercom.puretrack.data.source.local.table.TableAutoRestart;
import com.supercom.puretrack.data.source.local.table.TableCaseTamper;
import com.supercom.puretrack.data.source.local.table.TableDeviceJamming;
import com.supercom.puretrack.data.source.local.table.TableDeviceShielding;
import com.supercom.puretrack.data.source.local.table.TableScannerType;
import com.supercom.puretrack.data.source.local.table.TableSelfDiagnosticEvents;
import com.supercom.puretrack.data.source.local.table.TableTagMotion;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.data.source.local.table.DatabaseTable;
import com.supercom.puretrack.data.source.local.table.TableApnDetails;
import com.supercom.puretrack.data.source.local.table.TableCallContacts;
import com.supercom.puretrack.data.source.local.table.TableCallLog;
import com.supercom.puretrack.data.source.local.table.TableCommParam;
import com.supercom.puretrack.data.source.local.table.TableCommServers;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table.TableDeviceDetails;
import com.supercom.puretrack.data.source.local.table.TableDeviceInfoCellular;
import com.supercom.puretrack.data.source.local.table.TableDeviceInfoDetails;
import com.supercom.puretrack.data.source.local.table.TableDeviceInfoStatus;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventLog;
import com.supercom.puretrack.data.source.local.table.TableGpsPoints;
import com.supercom.puretrack.data.source.local.table.TableGuestTag;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.data.source.local.table.TableOffenderPhoto;
import com.supercom.puretrack.data.source.local.table.TableOffenderStatus;
import com.supercom.puretrack.data.source.local.table.TableOpenEventsLog;
import com.supercom.puretrack.data.source.local.table.TableSchedule;
import com.supercom.puretrack.data.source.local.table.TableScheduleOfZones;
import com.supercom.puretrack.data.source.local.table.TableTextMessages;
import com.supercom.puretrack.data.source.local.table.TableZones;
import com.supercom.puretrack.data.source.local.table.TableZonesDeleted;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.encryption.ScramblingTextUtils;
import com.supercom.puretrack.util.hardware.FilesManager;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DatabaseAccess extends SQLiteOpenHelper {

    private static DatabaseAccess databaseAccess;

    public static synchronized DatabaseAccess getInstance() {
        if (databaseAccess == null) {
            databaseAccess = new DatabaseAccess(App.getContext());
        }
        return databaseAccess;
    }

    private SQLiteDatabase sqliteDatabase;

    public static final String TAG = "DatabaseAccess";
    // General database definitions
    private static final int DATABASE_VERSION = 200;
    // Tables list - for common operations (addRecord, create table, delete etc.)
    private final List<DatabaseTable> databaseTables;
    // Individual tables - for custom operations
    public TableGpsPoints tableGpsPoint;
    public TableEventLog tableEventLog;
    public TableOffenderDetails tableOffenderDetails;
    public TableCallContacts tableCallContacts;
    public TableTextMessages tableMessages;
    public TableAutoRestart tableAutoRestart;
    public TableDeviceDetails tableDevDetails;
    public TableCommServers tableCommServers;
    public TableCommParam tableCommParam;
    public TableSchedule tableSchedule;
    public TableZones tableZones;
    public TableOffenderStatus tableOffStatus;
    public TableScheduleOfZones tableScheduleOfZones;
    public TableEventConfig tableEventConfig;
    public TableOpenEventsLog tableOpenEventsLog;
    public TableZonesDeleted tableZonesDeleted;
    public TableCallLog tableCallLog;
    public TableDebugInfo tableDebugInfo;
    public TableDeviceInfoDetails tableDeviceInfoDetails;
    public TableDeviceInfoStatus tableDeviceInfoStatus;
    public TableDeviceInfoCellular tableDeviceInfoCellular;
    public TableApnDetails tableApnDetails;
    public TableGuestTag tableGuestTag;
    public TableOffenderPhoto tableOffenderPhoto;
    public TableDeviceShielding tableDeviceShielding;
    public TableTagMotion tableTagMotion;
    public TableScannerType tableScannerType;
    public TableSelfDiagnosticEvents tableSelfDiagnosticEvents;
    public TableDeviceJamming tableDeviceJamming;
    public TableCaseTamper tableCaseTamper;
    private final StringBuilder dbStates = new StringBuilder();

    /**
     * Constructor: create/open DB, add table objects to List
     */
    private DatabaseAccess(Context context) {
        super(context, FilesManager.getInstance().DATABASE_NAME, null, DATABASE_VERSION);
        // Init list
        databaseTables = new ArrayList<>();
        // Create table objects, with order as 'EnumDbTables'
        tableGpsPoint = new TableGpsPoints();
        databaseTables.add(EnumDatabaseTables.TABLE_GPS_POINTS.getValue(), tableGpsPoint);
        tableEventLog = new TableEventLog();
        databaseTables.add(EnumDatabaseTables.TABLE_EVENT_LOG.getValue(), tableEventLog);
        tableOffenderDetails = new TableOffenderDetails();
        databaseTables.add(EnumDatabaseTables.TABLE_OFFENDER_DETAILS.getValue(), tableOffenderDetails);
        tableCallContacts = new TableCallContacts();
        databaseTables.add(EnumDatabaseTables.TABLE_CALL_CONTACTS.getValue(), tableCallContacts);

        tableMessages = new TableTextMessages();
        databaseTables.add(EnumDatabaseTables.TABLE_TEXT_MSG.getValue(), tableMessages);
        tableDevDetails = new TableDeviceDetails();
        databaseTables.add(EnumDatabaseTables.TABLE_DEVICE_DETAILS.getValue(), tableDevDetails);
        tableCommServers = new TableCommServers();
        databaseTables.add(EnumDatabaseTables.TABLE_COMM_SERVERS.getValue(), tableCommServers);
        tableCommParam = new TableCommParam();
        databaseTables.add(EnumDatabaseTables.TABLE_COMM_PARAMS.getValue(), tableCommParam);
        tableSchedule = new TableSchedule();
        databaseTables.add(EnumDatabaseTables.TABLE_SCHEDULE.getValue(), tableSchedule);
        tableZones = new TableZones();
        databaseTables.add(EnumDatabaseTables.TABLE_ZONES.getValue(), tableZones);
        tableScheduleOfZones = new TableScheduleOfZones();
        databaseTables.add(EnumDatabaseTables.TABLE_SCHEDULE_OF_ZONES.getValue(), tableScheduleOfZones);
        tableEventConfig = new TableEventConfig();
        databaseTables.add(EnumDatabaseTables.TABLE_EVENT_CONFIG.getValue(), tableEventConfig);
        tableOpenEventsLog = new TableOpenEventsLog();
        databaseTables.add(EnumDatabaseTables.TABLE_OPEN_EVENT_LOG.getValue(), tableOpenEventsLog);
        tableOffStatus = new TableOffenderStatus();
        databaseTables.add(EnumDatabaseTables.TABLE_OFFENDER_STATUS.getValue(), tableOffStatus);
        tableCallLog = new TableCallLog();
        databaseTables.add(EnumDatabaseTables.TABLE_CALL_LOG.getValue(), tableCallLog);
        tableZonesDeleted = new TableZonesDeleted();
        databaseTables.add(EnumDatabaseTables.TABLE_ZONES_DELETED.getValue(), tableZonesDeleted);
        tableDebugInfo = new TableDebugInfo();
        databaseTables.add(EnumDatabaseTables.TABLE_DEBUG_INFO.getValue(), tableDebugInfo);
        tableDeviceInfoDetails = new TableDeviceInfoDetails();
        databaseTables.add(EnumDatabaseTables.TABLE_DEVICE_INFO_DETAILS.getValue(), tableDeviceInfoDetails);
        tableDeviceInfoStatus = new TableDeviceInfoStatus();
        databaseTables.add(EnumDatabaseTables.TABLE_DEVICE_INFO_STATUS.getValue(), tableDeviceInfoStatus);
        tableDeviceInfoCellular = new TableDeviceInfoCellular();
        databaseTables.add(EnumDatabaseTables.TABLE_DEVICE_INFO_CELLULAR.getValue(), tableDeviceInfoCellular);
        tableApnDetails = new TableApnDetails();
        databaseTables.add(EnumDatabaseTables.TABLE_APN_DETAILS.getValue(), tableApnDetails);
        tableGuestTag = new TableGuestTag();
        databaseTables.add(EnumDatabaseTables.TABLE_GUEST_TAG.getValue(), tableGuestTag);
        tableOffenderPhoto = new TableOffenderPhoto();
        databaseTables.add(EnumDatabaseTables.TABLE_OFFENDER_PHOTO.getValue(), tableOffenderPhoto);
        tableDeviceShielding = new TableDeviceShielding();
        databaseTables.add(EnumDatabaseTables.TABLE_OFFENDER_PHOTO.getValue(), tableDeviceShielding);
        tableTagMotion = new TableTagMotion();
        databaseTables.add(EnumDatabaseTables.TABLE_TAG_MOTION.getValue(), tableTagMotion);
        tableScannerType = new TableScannerType();
        databaseTables.add(EnumDatabaseTables.TABLE_SCANNER_TYPE.getValue(), tableScannerType);
        tableDeviceJamming = new TableDeviceJamming();
        databaseTables.add(EnumDatabaseTables.TABLE_DEVICE_JAMMING.getValue(), tableDeviceJamming);
        tableSelfDiagnosticEvents = new TableSelfDiagnosticEvents();
        databaseTables.add(EnumDatabaseTables.TABLE_SELF_DIAGNOSTIC_EVENTS.getValue(), tableSelfDiagnosticEvents);
        tableCaseTamper = new TableCaseTamper();
        databaseTables.add(EnumDatabaseTables.TABLE_CASE_TAMPER.getValue(), tableCaseTamper);
        tableAutoRestart = new TableAutoRestart();
        databaseTables.add(EnumDatabaseTables.TABLE_AutoRestart.getValue(), tableAutoRestart);

        //delete database if user is running the application in the first time
        if (PureTrackSharedPreferences.getIsFirstTimeUsingApplication()) {
            FilesManager.getInstance().deleteDatabaseFiles();
            PureTrackSharedPreferences.setShouldDeleteDb(false);
            dbStates.append("DatabaseAccess - DatabaseAccess was deleted since it was the first time user installed puretrack application");
        }
        sqliteDatabase = getWritableDatabase();
    }

    /**
     * @return true if all operations succeeded, false if not
     */
    public boolean resetDatabase(String deviceSn, String url, String password) {

        close();

        boolean isDatabaseSucceesfullyDeleted = deleteDatabase();

        sqliteDatabase = getWritableDatabase();

        if (isDatabaseSucceesfullyDeleted) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Succeeded to delete database", DebugInfoModuleId.DB);
        } else {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Failed to delete database", DebugInfoModuleId.DB);
            return false;
        }

        if (UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS, TableDeviceDetails.COLUMN_DEV_SN, deviceSn) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update COLUMN_DEV_SN", DebugInfoModuleId.DB);
            return false;
        }

        // add encrypt decrypt server url
        String encURL = "";
        try {
            encURL = AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, url);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_URL, encURL) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DEVICE_CONFIG_SERVER_URL", DebugInfoModuleId.DB);
            return false;
        }
        // add scrambled/unscrambled encrypt decrypt server password
        String scrambledEncPass = "";
        try {
            scrambledEncPass = AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, ScramblingTextUtils.scramble(password));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_SERVER_PASS, scrambledEncPass) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DEVICE_CONFIG_SERVER_PASS", DebugInfoModuleId.DB);
            return false;
        }
        if (TableOffenderStatusManager.sharedInstance().updateColumnInt
                (OFFENDER_STATUS_CONS.OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME, 1) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME",
                    DebugInfoModuleId.DB);
            return false;
        }

        if (TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS, OffenderActivation.OFFENDER_STATUS_UNALLOCATED) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update OFF_IS_OFFENDER_ACTIVATED", DebugInfoModuleId.DB);
            return false;
        }

        return true;

    }

    public boolean resetDatabase(String deviceSn, String url, String password, String tagRfId, String tagEncryption, String tagId, String tagAddress,
                                 int lastOffenderRequestIdTreated, String appLanguage) {

        resetDatabase(deviceSn, url, password);

        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID, tagRfId) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DETAILS_OFF_TAG_RFID", DebugInfoModuleId.DB);
            return false;
        }


        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ENCRYPTION, tagEncryption) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DETAILS_OFF_TAG_ENCRYPTION", DebugInfoModuleId.DB);
            return false;
        }

        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_ID, tagId) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update DETAILS_OFF_TAG_ID", DebugInfoModuleId.DB);
            return false;
        }

        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.OFFENDER_TAG_ADDRESS, tagAddress) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update OFFENDER_TAG_ADDRESS", DebugInfoModuleId.DB);
            return false;
        }

        if (TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED, lastOffenderRequestIdTreated) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update OFF_LAST_OFFENDER_REQUEST_ID_TREATED", DebugInfoModuleId.DB);
            return false;
        }

        if (TableOffenderDetailsManager.sharedInstance().updateColumnString(OFFENDER_DETAILS_CONS.APP_LANGUAGE, appLanguage) <= 0) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "DatabaseAccess - Can't update APP_LANGUAGE", DebugInfoModuleId.DB);
            return false;
        }


        return true;
    }

    /**
     * DB Created, create tables from table objects
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createStatement;
        DatabaseTable databaseTable;
        Iterator<DatabaseTable> It = databaseTables.iterator();

        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + TAG + " onCreate ", false);
        dbStates.append("DatabaseAccess - onCreate\n");

        while (It.hasNext()) {
            databaseTable = It.next();
            // Create table (get the statement from table class)
            createStatement = databaseTable.getCreateStatement();
            db.execSQL(createStatement);
            // Add default data if needed (if implemented...)
            databaseTable.AddDefaultData(db);
        }
        // Load data
        onOpen(db);
    }

    /**
     * DB opened
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        DatabaseTable databaseTable;
        Iterator<DatabaseTable> It = databaseTables.iterator();
        // Load content to local copy - if defined
        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + TAG + " onOpen ", false);
        dbStates.append("DatabaseAccess - onOpen\n");
        while (It.hasNext()) {
            databaseTable = It.next();
            // Set database reference to each table
            databaseTable.SetDb(db);
            // Load data to local copy
            databaseTable.LoadData(db);
        }
    }

    /**
     * DB Upgrade
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + TAG + " onUpgrade ", false);
        dbStates.append("DatabaseAccess - onUpgrade\n");

        for (DatabaseTable table : databaseTables) {
            for (int databaseOldVersion = oldVersion; databaseOldVersion <= newVersion; databaseOldVersion++) {
                try {
                    table.alterTableAfterDatabaseUpgrade(database, databaseOldVersion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * DB Downgrade
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Iterator<DatabaseTable> It = databaseTables.iterator();

        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] " + TAG + " onDowngrade ", false);
        dbStates.append("DatabaseAccess - onDowngrade\n");

        while (It.hasNext()) {
            // Delete each table
            db.execSQL("DROP TABLE IF EXISTS " + It.next().GetName());
        }
        // create new tables
        onCreate(db);
    }


    /**
     * Table Record Count
     */
    public int TableRecordCount(EnumDatabaseTables Table) {
        SQLiteDatabase db = this.getReadableDatabase();
        return databaseTables.get(Table.getValue()).GetRecordCount(db);
    }


    /**
     * @return Row record affected. -1 If Error occurred.
     */
    public long insertNewRecord(EnumDatabaseTables Table, DatabaseEntity Rec) {
        long insertedRowId;
        SQLiteDatabase db = getWritableDatabase();
        insertedRowId = databaseTables.get(Table.getValue()).addRecord(db, Rec);

        return insertedRowId;
    }



    public long insertWithOnConflict(EnumDatabaseTables Table, DatabaseEntity record) {
        SQLiteDatabase db = getWritableDatabase();
        return databaseTables.get(Table.getValue()).insertWithOnConflict(db, record);
    }


    /**
     * API for update an INTEGER field with no WHERE condition
     */
    public int UpdateField(EnumDatabaseTables Table, String FieldName, int FieldValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FieldName, FieldValue);
        // Update the table
        int rowsAffected = db.update(databaseTables.get(Table.getValue()).GetName(),
                values,
                null,
                null);

        GetTable(Table).LoadData(sqliteDatabase);

        return rowsAffected;
    }

    public int UpdateField(EnumDatabaseTables Table, String FieldName, long FieldValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FieldName, FieldValue);
        // Update the table
        int rowsAffected = db.update(databaseTables.get(Table.getValue()).GetName(),
                values,
                null,
                null);

        GetTable(Table).LoadData(sqliteDatabase);

        return rowsAffected;
    }

    public int UpdateField(EnumDatabaseTables Table, String FieldName, float FieldValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FieldName, FieldValue);
        // Update the table
        int rowsAffected = db.update(databaseTables.get(Table.getValue()).GetName(),
                values,
                null,
                null);

        GetTable(Table).LoadData(sqliteDatabase);

        return rowsAffected;
    }

    /**
     * API for update an INTEGER field with WHERE condition
     */
    public void UpdateField(EnumDatabaseTables Table, String FieldName, int FieldValue, String WhereClause, String[] WhereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FieldName, FieldValue);
        // Update the table
        db.update(databaseTables.get(Table.getValue()).GetName(),
                values,
                WhereClause,
                WhereArgs);

        GetTable(Table).LoadData(sqliteDatabase);
    }

    /**
     * API for update an TEXT field with no WHERE condition
     */
    public int UpdateField(EnumDatabaseTables Table, String FieldName, String FieldValue) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FieldName, FieldValue);
        // Update the table
        int rowsAffected = db.update(databaseTables.get(Table.getValue()).GetName(),
                values,
                null,
                null);

        GetTable(Table).LoadData(sqliteDatabase);

        return rowsAffected;
    }

    /**
     * API for getting a table reference
     */
    public DatabaseTable GetTable(EnumDatabaseTables Table) {
        return databaseTables.get(Table.getValue());
    }


    public boolean deleteDatabase() {
        return SQLiteDatabase.deleteDatabase((new File(FilesManager.getInstance().DATABASE_NAME)));
    }

    public void deleteAllRecordsInTable(EnumDatabaseTables tableEnum) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + databaseTables.get(tableEnum.getValue()).GetName());
    }

    public boolean isTableEmpty(EnumDatabaseTables tableEnum) {
        boolean flag;
        String quString = "select exists(select 1 from " + databaseTables.get(tableEnum.getValue()).GetName() + ");";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(quString, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        flag = count != 1;
        cursor.close();
        return flag;
    }

    public StringBuilder getDbStates() {
        return dbStates;
    }

    public int getDatabaseSize() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.getPath().length();
    }

    public String getDatabaseVersion() {
        int version = sqliteDatabase.getVersion();
        return String.valueOf(version);
    }
}
