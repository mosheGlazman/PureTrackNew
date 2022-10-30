package com.supercom.puretrack.database;

import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

/**
 * DatabaseColumn class - Defines the database column name, type and value
 */
public class DatabaseColumn {
    private final String Name;
    private final EnumDatabaseColumnType Type;
    private String CreateTypeStr; // INTEGER PRIMARY KEY,

    /**
     * Constructor for string value
     */
    public DatabaseColumn(String Name, EnumDatabaseColumnType Type) {
        this.Name = Name;
        this.Type = Type;
        switch (Type) {
            case COLUMN_TYPE_INTEGER_PK:
                CreateTypeStr = "INTEGER PRIMARY KEY AUTOINCREMENT";
                break;
            case COLUMN_TYPE_INTEGER:
                CreateTypeStr = "INTEGER";
                break;
            case COLUMN_TYPE_STRING:
                CreateTypeStr = "TEXT";
                break;
            case COLUMN_TYPE_REAL:
                CreateTypeStr = "REAL";
                break;
            default:
                break;
        }
    }

    public String GetColumnName() {
        return this.Name;
    }

    /**
     * Get Column create Type string (INTEGER, TEXT etc.)
     */
    public String GetColumnCreateType() {
        return this.CreateTypeStr;
    }

}
