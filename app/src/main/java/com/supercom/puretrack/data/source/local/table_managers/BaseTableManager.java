package com.supercom.puretrack.data.source.local.table_managers;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.data.source.local.table.DatabaseTable;

public abstract class BaseTableManager {

    public int getIntValueByColumnName(String Col) {

        return getTable().getIntValueByColumnName(Col);
    }

    public float getFloatValueByColumnName(String Col) {

        return getTable().getFloatValueByColumnName(Col);
    }

    public long getLongValueByColumnName(String Col) {

        return getTable().getLongValueByColumnName(Col);
    }

    public String getStringValueByColumnName(String Col) {

        return getTable().getStringValueByColumnName(Col);
    }

    public long updateColumnLong(String Col, long longData) {

        return DatabaseAccess.getInstance().UpdateField(getEnumDBTable(), Col, longData);
    }

    public long updateColumnInt(String Col, int intData) {

        return DatabaseAccess.getInstance().UpdateField(getEnumDBTable(), Col, intData);
    }

    public long updateColumnFloat(String Col, float floatData) {

        return DatabaseAccess.getInstance().UpdateField(getEnumDBTable(), Col, floatData);
    }

    public long updateColumnString(String Col, String strData) {

        return DatabaseAccess.getInstance().UpdateField(getEnumDBTable(), Col, strData);
    }

    protected abstract DatabaseTable getTable();

    protected abstract EnumDatabaseTables getEnumDBTable();

}
