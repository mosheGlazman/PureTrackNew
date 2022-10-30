package com.supercom.puretrack.data.source.local;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.supercom.puretrack.util.application.App;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

public class SQLLiteManager extends SQLiteOpenHelper {

    private static SQLLiteManager mInstance;
    ArrayList<String> sql_drop;
    ArrayList<String> sql_create;

    public static SQLLiteManager getInstance() {
        if (mInstance == null) {
            mInstance = new SQLLiteManager(App.getContext());
        }

        return mInstance;
    }

    private final static int DATABASE_VERSION = 2;
    public String TAG = "SQLLiteManager";

    private static SQLiteDatabase readDB;
    private static SQLiteDatabase writaDB;

    public SQLLiteManager(Context context) {
        super(context, "SQLLiteDB", null, DATABASE_VERSION);
        Log.i(TAG, "init" );

        sql_drop = new ArrayList<>();
        sql_create = new ArrayList<>();

        sql_drop.add(new SQLLiteAutoRestart().getSQL_Drop());
        sql_create.add(new SQLLiteAutoRestart().getSQL_Create());

        onCreate(getWritaDB());
        getReadDB();
  }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate" );
        try {
            for(String create : sql_create) {
                execSQL(create);
            }
        } catch (Exception e) {
            // Log.e(TAG, "onCreate", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        Log.i(TAG, "onUpgrade " + arg1 + "," + arg2);

        try {
            for(String drop : sql_drop) {
                execSQL(drop);
            }
        } catch (Exception e) {
            Log.e(TAG, "onUpgrade", e);
        }

        try {
            for(String create : sql_create) {
                execSQL(create);
            }
        } catch (Exception e) {
            Log.e(TAG, "onUpgrade", e);
        }
    }

    private SQLiteDatabase getReadDB() {
        if (readDB == null || !readDB.isOpen())
            readDB = getReadableDatabase();

        return readDB;
    }

    private SQLiteDatabase getWritaDB() {
        try {
            if (writaDB == null || !writaDB.isOpen())
                writaDB = getWritableDatabase();
        }catch (Exception ex){}
        return writaDB;
    }


    public Cursor rawQuery(String sql, String[] selectionArgs) {
       return getReadDB().rawQuery(sql,selectionArgs);
    }

    private String getSQLDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
    }

    public void execSQL(final String sql){
        Log.i(TAG,"execSQL: "+sql);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getWritaDB().execSQL(sql);
                } catch (Exception e) {
                    Log.e(TAG, "execSQL failed " + sql, e);
                }
            }
        }).start();
    }
}


