package org.unicef.gis.infrastructure.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UnicefGisDbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "UnicefGis.db";

    public UnicefGisDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TagSqlScripts.SQL_CREATE_TABLE);
        db.execSQL(ReportSqlScripts.SQL_CREATE_TABLE);
    }    
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }    

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
