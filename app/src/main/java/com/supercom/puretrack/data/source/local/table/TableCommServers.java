package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.util.constants.database_defaults.DefaultDatabaseData;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityCommServers;


public class TableCommServers extends DatabaseTable {
    // Table name
    private static final String TABLE_COMM_SERVERS = "CommServers";
    // Column names
    private static final String COLUMN_SERVER_HTTP_HEADER = "CommServHttpHeader";
    private static final String COLUMN_SERVER_IP = "CommServIp";
    private static final String COLUMN_SERVER_WEB_SERVICE = "CommServWebService";
    private static final String COLUMN_SERVER_IS_ACTIVE = "CommServIsActive";
    // Record for quick access
    private EntityCommServers ActiveCommServer;

    /**
     * Constructor: update table's name, build columns
     */
    public TableCommServers() {
        this.tableName = TABLE_COMM_SERVERS;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_SERVER_HTTP_HEADER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_SERVER_IP, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_SERVER_WEB_SERVICE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_SERVER_IS_ACTIVE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add event Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityCommServers ServerRec = (EntityCommServers) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVER_HTTP_HEADER, ServerRec.HttpHeader);
        values.put(COLUMN_SERVER_IP, ServerRec.IpAddress);
        values.put(COLUMN_SERVER_WEB_SERVICE, ServerRec.WebService);
        values.put(COLUMN_SERVER_IS_ACTIVE, 1);

        return database.insert(TABLE_COMM_SERVERS, null, values);
    }

    /**
     * Add default data record/s
     */
    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // 	default data record
        addRecord(db, new EntityCommServers(DefaultDatabaseData.ServerHttpHeader,
                DefaultDatabaseData.ServerIpAddress,
                DefaultDatabaseData.ServerWebService));
    }

    /**
     * Get record from any query cursor
     */
    private EntityCommServers GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityCommServers ServerRec = new EntityCommServers(
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_SERVER_HTTP_HEADER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_SERVER_IP)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_SERVER_HTTP_HEADER)));

        ServerRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return ServerRec;
    }

    @Override
    public void LoadData(SQLiteDatabase db) {
        ActiveCommServer = this.GetActiveServer(db);
    }

    public EntityCommServers GetActiveServer(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = databaseReference.query(TABLE_COMM_SERVERS,
                columnNamesArray,
                COLUMN_SERVER_IS_ACTIVE + " = 1",
                null,
                null,
                null,
                "1");
        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        return GetRecFromQueryCursor(QueryCursor);
    }

}
