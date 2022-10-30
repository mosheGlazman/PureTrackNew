package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumCallContact;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityCallContacts;

import java.util.ArrayList;
import java.util.List;

public class TableCallContacts extends DatabaseTable {
    // Table: Call Contacts
    private static final String TABLE_CALL_CONTACTS = "CallContacts";
    // Column names
    private static final String COLUMN_CONTACT_TYPE = "ContactType";
    private static final String COLUMN_CONTACT_NUMBER = "ContactNumber";
    // Local copy
    private final List<EntityCallContacts> LocalCallContacts;

    /**
     * Constructor: update table's name, build columns
     */
    public TableCallContacts() {
        this.tableName = TABLE_CALL_CONTACTS;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_CONTACT_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_CONTACT_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();

        LocalCallContacts = new ArrayList<>();

    }

    /**
     * Add event Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityCallContacts ContactsRec = (EntityCallContacts) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTACT_TYPE, ContactsRec.Type);
        values.put(COLUMN_CONTACT_NUMBER, ContactsRec.Number);

        return database.insert(TABLE_CALL_CONTACTS, null, values);
    }

    /**
     * Add default data record/s
     */
    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        addRecord(db, new EntityCallContacts(EnumCallContact.CONTACT_OFFICER.getValue(), "0544244083"));
        addRecord(db, new EntityCallContacts(EnumCallContact.CONTACT_AGENCY.getValue(), "0544244083"));
        addRecord(db, new EntityCallContacts(EnumCallContact.CONTACT_EMERGENCY.getValue(), "0544244083"));
    }

    private EntityCallContacts GetRecFromQueryCursor(Cursor QueryCursor) {

        return new EntityCallContacts(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_CONTACT_TYPE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_CONTACT_NUMBER)));
    }

    /**
     * Load data to local copy
     */
    @Override
    public void LoadData(SQLiteDatabase db) {
        String LimitStr = String.format("%d", EnumCallContact.CONTACT_MAX_VALUE.getValue());
        Cursor QueryCursor = db.query(TABLE_CALL_CONTACTS,    // Table name
                columnNamesArray,        // Columns names
                null,                // Where
                null,                // Where Args - replaces '?'
                null,                // Group by...
                null,                // Having...
                null,                // Order by insert order
                LimitStr);    // Limit...
        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            LocalCallContacts.removeAll(null);
            return;
        } else {
            QueryCursor.moveToFirst();
        }
        int Rows = QueryCursor.getCount();
        while (Rows > 0) {
            // Add a record to local copy
            EntityCallContacts rec = GetRecFromQueryCursor(QueryCursor);
            LocalCallContacts.add(rec.Type, rec);
            Rows--;
        }
    }
}
