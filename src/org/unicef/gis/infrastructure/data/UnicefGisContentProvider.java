package org.unicef.gis.infrastructure.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class UnicefGisContentProvider extends ContentProvider {
	public final static String AUTHORITY = "org.unicef.gis.provider";
	
	private UnicefGisDbHelper dbHelper;
	private SQLiteDatabase db;
	
	@Override
	public boolean onCreate() {
		dbHelper = new UnicefGisDbHelper(getContext());		
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return new String();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
