package org.unicef.gis.infrastructure.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class UnicefGisContentProvider extends ContentProvider {
	public final static String AUTHORITY = "org.unicef.gis.provider";
	
	@Override
	public boolean onCreate() {		
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
		if (uri.equals(UnicefGisDbContract.Report.CONTENT_URI))
			return queryReports();
					
		return null;
	}
	
	//TODO: the store should return a custom Cursor dettached from the original DB instance, so that
	//we don't risk leaking databases.
	private Cursor queryReports() {
		UnicefGisStore store = new UnicefGisStore(getContext());
		return store.getReportsCursor();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
