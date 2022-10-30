package com.supercom.puretrack.data.source.local;

import android.database.Cursor;
import android.util.Log;

public abstract class SQLLiteTable {

    protected abstract String getSQL_Insert();
    protected abstract String getSQL_Create();
    protected  String getSQL_SelectAll(){
        return "select * from " + tableName;
    }
    protected  String getSQL_Drop() {
        return "drop table if exists "+ tableName +";";
    }
    protected  String getSQL_Delete(){
        return "delete from " + tableName;
    }
    protected final String tableName;

    protected SQLLiteTable(String tableName){
        this.tableName=tableName;
    }
    protected void delete() {
         SQLLiteManager.getInstance().execSQL(getSQL_Delete());
    }
    protected void insert(String... params) {
        SQLLiteManager.getInstance().execSQL(String.format(getSQL_Insert(),params));
    }
    public Cursor selectAll() {
        Cursor c = null;

        try {
            c =  SQLLiteManager.getInstance().rawQuery(getSQL_SelectAll(), null);
            if (c == null || c.getCount() == 0) {
                return null;
            }
            return c;
        } catch (Exception e) {
            Log.e("SQLLiteTable", "getCallLog", e);
        }

        return null;
    }

    protected void execSQL(String sql){
        SQLLiteManager.getInstance().execSQL(sql);
    }
}
