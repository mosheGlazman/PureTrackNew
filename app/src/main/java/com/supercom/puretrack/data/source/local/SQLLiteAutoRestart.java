package com.supercom.puretrack.data.source.local;

import android.database.Cursor;
import android.util.Log;

public class SQLLiteAutoRestart extends SQLLiteTable {
    public SQLLiteAutoRestart() {
        super("autoRestart");
    }

    @Override
    protected String getSQL_Create() {
        return "CREATE TABLE IF NOT EXISTS autoRestart (jsonText TEXT)";
    }

    @Override
    protected String getSQL_Insert() {
        return "INSERT into autoRestart (jsonText) values('%s')";
    }

    public String getData() {
        Cursor c = selectAll();
        if (c == null) {
            return null;
        }

        try {
            c.moveToNext();
            String json = c.getString(c.getColumnIndex("jsonText"));
            return json;
        } catch (Exception e) {
            Log.e("AutoRestartSQLLite", "getCallLog", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return null;
    }

    public void saveJson(String json){
        delete();
        insert(json);
    }
}
