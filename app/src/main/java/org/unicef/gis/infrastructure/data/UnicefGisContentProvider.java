package org.unicef.gis.infrastructure.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/***
 * At the moment we only use the content provider because it's a requirement
 * of the sync adapter framework. This class is just a stub.
 * @author mverzilli
 *
 */
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
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
