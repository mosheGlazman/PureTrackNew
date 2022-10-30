package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DatabaseTable class - Defines the database table name, columns, statements etc.
 */
public class DatabaseTable {
    public static final String TAG = "DatabaseTable";
    protected String tableName;
    protected int columnNumber;
    protected String createStatement;
    protected List<DatabaseColumn> Columns;    // The column names
    protected DatabaseEntity databaseEntity;
    protected String[] columnNamesArray;
    protected SQLiteDatabase databaseReference;

    /**
     * Constructor
     */
    public DatabaseTable() {
        Columns = new ArrayList<>();
    }

    /**
     * Set DB reference in order to perform DB actions from table object directly (not from DatabaseAccess class)
     */
    public void SetDb(SQLiteDatabase Db) {
        databaseReference = Db;
    }

    protected void BuildColumnNameArray() {
        columnNamesArray = new String[GetColumnNum() + 1/*for 'rowid'*/];
        Iterator<DatabaseColumn> It = Columns.iterator();
        int index = 0;
        while (It.hasNext()) {
            columnNamesArray[index] = It.next().GetColumnName();
            index++;
        }
        // Add the default SQLite column: rowid
        columnNamesArray[index] = "rowid";
    }

    protected void buildColumnNameArrayWithoutRowId() {
        columnNamesArray = new String[GetColumnNum()];
        Iterator<DatabaseColumn> It = Columns.iterator();
        int index = 0;
        while (It.hasNext()) {
            columnNamesArray[index] = It.next().GetColumnName();
            index++;
        }
    }

    protected void AddColumn(DatabaseColumn Col) {
        this.Columns.add(Col);
        this.columnNumber++;
    }

    public String GetName() {
        return this.tableName;
    }


    /**
     * Generate a statement for create table SQL
     */
    public String getCreateStatement() {
        String createStart;
        StringBuilder createColumns = new StringBuilder();
        String currentColumn;
        DatabaseColumn databaseColumn;
        Iterator<DatabaseColumn> columnIterator = Columns.iterator();

        createStart = "CREATE TABLE " + tableName + "(";
        // Add columns to statement
        while (columnIterator.hasNext()) {
            databaseColumn = columnIterator.next();
            // Build column in CREATE statement:
            currentColumn = "";
            // Column name
            currentColumn += databaseColumn.GetColumnName();
            currentColumn += " ";
            // Column type string
            currentColumn += databaseColumn.GetColumnCreateType();
            if (columnIterator.hasNext()) {
                currentColumn += ", ";
            }
            createColumns.append(currentColumn);
        }
        createStart += createColumns;
        createStart += ");";

        createStatement = createStart;

        return createStatement;
    }

    public int GetColumnNum() {
        return this.columnNumber;
    }

    /**
     * Add Record template
     */
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        return 0;
    }

    public long insertWithOnConflict(SQLiteDatabase db, DatabaseEntity record) {
        return -1;
    }

    /**
     * Add default data record/s
     */
    public void AddDefaultData(SQLiteDatabase db) {
    }

    /**
     * Load data to local copy
     */
    public void LoadData(SQLiteDatabase db) {
    }

    /**
     * Get Record template
     */
    public DatabaseEntity GetRecord(SQLiteDatabase db) {
        return null;
    }

    public int GetRecordCount(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * @return The number of rows affected if a whereClause is passed in, 0 otherwise
     */
    public int deleteRowById(long RowId) {
        String Where = String.format("%s = %d", "rowid", RowId);
        return databaseReference.delete(tableName, Where, null);
    }

    public DatabaseEntity getRecordByRowId(long RowId) {

        DatabaseEntity rec = null;

        // Select query
        Cursor QueryCursor = databaseReference.query(tableName,    // Table name
                columnNamesArray,        // Columns names
                "rowid" + " = ?",    // Where
                new String[]{String.valueOf(RowId)}, // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            rec = GetRecordFromQueryCursor(QueryCursor);
        }

        QueryCursor.close();

        return rec;
    }

    public int getIntValueByColumnName(String columnName) {
        int value = 0;

        // Select query
        Cursor QueryCursor = databaseReference.query(tableName,    // Table name
                columnNamesArray,        // Columns names
                null,    // Where
                null, // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            value = QueryCursor.getInt(QueryCursor.getColumnIndex(columnName));
        }

        QueryCursor.close();

        return value;
    }

    public float getFloatValueByColumnName(String columnName) {
        float value = 0;

        // Select query
        Cursor QueryCursor = databaseReference.query(tableName,    // Table name
                columnNamesArray,        // Columns names
                null,    // Where
                null, // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            value = QueryCursor.getFloat(QueryCursor.getColumnIndex(columnName));
        }

        QueryCursor.close();

        return value;
    }

    public long getLongValueByColumnName(String columnName) {
        long value = 0;

        // Select query
        Cursor QueryCursor = databaseReference.query(tableName,    // Table name
                columnNamesArray,        // Columns names
                null,    // Where
                null, // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            value = QueryCursor.getLong(QueryCursor.getColumnIndex(columnName));
        }

        QueryCursor.close();

        return value;
    }

    public String getStringValueByColumnName(String columnName) {
        String value = "";

        // Select query
        Cursor QueryCursor = databaseReference.query(tableName,    // Table name
                columnNamesArray,        // Columns names
                null,    // Where
                null, // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() > 0) {

            QueryCursor.moveToFirst();
            value = QueryCursor.getString(QueryCursor.getColumnIndex(columnName));
        }

        QueryCursor.close();

        return value;
    }

    protected DatabaseEntity GetRecordFromQueryCursor(Cursor QueryCursor) {
        return null;
    }

    public void printContentValues(ContentValues vals) { }

    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }

}

