package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityTextMessage;

public class TableTextMessages extends DatabaseTable {
    // Table: GPS points
    private static final String TABLE_MESSAGES = "Messages";
    // Column names
    private static final String COLUMN_MSG_TYPE = "MsgType";
    private static final String COLUMN_MSG_TIME = "MsgTime";
    private static final String COLUMN_MSG_UI_TIME = "MsgUiTime";
    private static final String COLUMN_MSG_SENDER = "MsgSender";
    private static final String COLUMN_MSG_TEXT = "MsgText";
    private static final String COLUMN_MSG_READ = "MsgRead";
    private static final String COLUMN_MSG_ID = "MsgID";
    // Keep last message RowId for scrolling next/prev
    private long LastMsgRowId;

    /**
     * Constructor: update table's name, build columns
     */
    public TableTextMessages() {
        this.tableName = TABLE_MESSAGES;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_UI_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_SENDER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_TEXT, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_READ, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_MSG_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();

        LastMsgRowId = -1;
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
    }

    /**
     * Add message Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityTextMessage MsgRec = (EntityTextMessage) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_MSG_TYPE, MsgRec.Type);
        values.put(COLUMN_MSG_TIME, MsgRec.Time);
        values.put(COLUMN_MSG_UI_TIME, MsgRec.Time);
        values.put(COLUMN_MSG_SENDER, MsgRec.Sender);
        values.put(COLUMN_MSG_TEXT, MsgRec.Text);
        values.put(COLUMN_MSG_READ, MsgRec.Read);
        values.put(COLUMN_MSG_ID, MsgRec.Read);

        return database.insert(TABLE_MESSAGES, null, values);
    }

    /**
     * Get record from any query cursor
     */
    private EntityTextMessage GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityTextMessage MsgRec = new EntityTextMessage(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_MSG_TYPE)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_MSG_TIME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_MSG_UI_TIME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_MSG_SENDER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_MSG_TEXT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_MSG_ID)));

        // Add fields which are not covered by the constructor
        MsgRec.Read = QueryCursor.getColumnIndex(COLUMN_MSG_READ);
        MsgRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return MsgRec;
    }

    /**
     * Get latest message (SELECT * FROM Messages ORDER BY MsgTime DESC LIMIT 1)
     */
    public EntityTextMessage GetLastMsg() {
        Cursor QueryCursor = databaseReference.query(TABLE_MESSAGES,    // Table name
                columnNamesArray,        // Columns names
                null,                // Where
                null,                // Where Args - replaces '?'
                null,                // Group by...
                null,                // Having...
                "rowid" + " DESC",    // Order by insert order
                "1");                // Limit...

        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }
        EntityTextMessage MsgRec = GetRecFromQueryCursor(QueryCursor);
        // Update last message RowId (used for scrolling)
        LastMsgRowId = MsgRec.RowId;

        return MsgRec;
    }

    public void SetAckMsgByEventID(int ID) {
        Cursor QueryCursor = databaseReference.query(TABLE_MESSAGES,    // Table name
                columnNamesArray,        // Columns names
                "MsgID = ?",                // Where
                new String[]{String.valueOf(ID)},                // Where Args - replaces '?'
                null,                // Group by...
                null,                // Having...
                null,    // Order by insert order
                "1");                // Limit...

        if (QueryCursor.getCount() != 0) {
            SetRead(GetRecFromQueryCursor(QueryCursor), 1);
        }
    }

    public EntityTextMessage GetMsg(int i) {
        Cursor QueryCursor = databaseReference.query(TABLE_MESSAGES,    // Table name
                columnNamesArray,        // Columns names
                null,      // Where
                null,                // Where Args - replaces '?'
                null,                // Group by...
                null,                // Having...
                null,    // Order by insert order
                null);                // Limit...

        if (QueryCursor.getCount() == 0) {
            return null;
        }
        if (!QueryCursor.move(i))
            return null;

        return GetRecFromQueryCursor(QueryCursor);

    }

    public int GetMsgCount() {
        Cursor QueryCursor = databaseReference.query(TABLE_MESSAGES,    // Table name
                columnNamesArray,        // Columns names
                null,                // Where
                null,                // Where Args - replaces '?'
                null,                // Group by...
                null,                // Having...
                null,    // Order by insert order
                null);                // Limit...


        return QueryCursor.getCount();

    }

    public void SetRead(EntityTextMessage Rec, int IsRead) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MSG_READ, IsRead);

        databaseReference.update(TABLE_MESSAGES,
                values,
                "rowid" + " = ?",
                new String[]{String.valueOf(Rec.RowId)});
    }


    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }

}

