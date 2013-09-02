package org.unicef.gis.infrastructure.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.unicef.gis.model.Report;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;

public class SqlLiteStoreAdapter implements IUnicefGisStoreAdapter {
	@Override
	public void saveReport(Context context, String description, Location location, Uri imageUri,
			List<String> tags) {
		UnicefGisDbHelper dbHelper = new UnicefGisDbHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_GUID, generateGuid());
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_TITLE, description);
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_LAT, location.getLatitude());
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_LONG, location.getLongitude());
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_IMAGE, imageUri.toString());
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_TAGS, asCommaSeparated(tags));
		values.put(UnicefGisDbContract.Report.COLUMN_NAME_TIMESTAMP, generateTimestamp());
		
		db.insert(UnicefGisDbContract.Report.TABLE_NAME, "null", values);
		
		db.close();	
	}
	
	@Override
	public Cursor getReportsCursor(Context context) {
		UnicefGisDbHelper dbHelper = new UnicefGisDbHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = UnicefGisDbContract.Report.DEFAULT_PROJECTION;
		String sortOrder = UnicefGisDbContract.Report.COLUMN_NAME_TIMESTAMP + " DESC";
		String selection = "1=1";
		String[] selectionArgs = new String[0];
		
		return db.query(UnicefGisDbContract.Report.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}
	
	private String generateTimestamp() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(new Date());		
	}

	private String asCommaSeparated(List<String> tags) {
		StringBuffer sb = new StringBuffer("");
		for (String string : tags) {
			if (sb.length() != 0) 
				sb.append(",");
			
			sb.append(string);
		}
		return sb.toString();
	}

	private String generateGuid() {
		return UUID.randomUUID().toString();
	}

	@Override
	public List<Report> getReports() {
		return null;
	}
}
