package org.unicef.gis.infrastructure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.unicef.gis.infrastructure.data.UnicefGisDbContract;
import org.unicef.gis.infrastructure.data.UnicefGisDbHelper;
import org.unicef.gis.model.Tag;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;

public class UnicefGisStore {
	private final static String PREF_TAGS_FETCHED = "unicef_gis_store_pref_tags_fetched";
	
	private final Context context;
	
	public UnicefGisStore(Context context) {
		this.context = context;
	}

	public void saveAddress(String address) {
		writePref(RoutesResolver.PREF_SERVER_URL, address);
	}

	public boolean tagsHaveBeenFetched() {		
		return readPref(PREF_TAGS_FETCHED, Boolean.valueOf(false));
	}
	
	public void saveReport(String description, Location location, Uri imageUri,
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

	public void saveTags(List<Tag> tags) {
		UnicefGisDbHelper dbHelper = new UnicefGisDbHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		//Delete all tags before creating the new ones, we regard tags as value objects
		db.delete(UnicefGisDbContract.Tag.TABLE_NAME, "1=1", new String[0]);

		for (Tag tag : tags) {
			ContentValues values = new ContentValues();
			values.put(UnicefGisDbContract.Tag.COLUMN_NAME_NAME, tag.getValue());

			db.insert(UnicefGisDbContract.Tag.TABLE_NAME, "null", values);
		}

		setTagsHaveBeenFetched(true);		
	}
	
	public Cursor getReportsCursor() {
		UnicefGisDbHelper dbHelper = new UnicefGisDbHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = UnicefGisDbContract.Report.DEFAULT_PROJECTION;
		String sortOrder = UnicefGisDbContract.Report.COLUMN_NAME_TIMESTAMP + " DESC";
		String selection = "1=1";
		String[] selectionArgs = new String[0];
		
		return db.query(UnicefGisDbContract.Report.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}
	
	public List<Tag> retrieveTags() {
		UnicefGisDbHelper dbHelper = new UnicefGisDbHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String[] projection = { UnicefGisDbContract.Tag.COLUMN_NAME_NAME };
		String sortOrder = UnicefGisDbContract.Tag.COLUMN_NAME_NAME + " ASC";
		String selection = "1=1";
		String[] selectionArgs = new String[0];
		
		Cursor c = db.query(UnicefGisDbContract.Tag.TABLE_NAME, 
			    projection, selection, selectionArgs, null, null, sortOrder);
		
		List<Tag> tags = new ArrayList<Tag>();
		
		c.moveToFirst();
		while (!c.isAfterLast()) {
			tags.add(new Tag(c.getString(0)));
			c.moveToNext();
		}
		
		return tags;
	}	
	
	private void setTagsHaveBeenFetched(boolean value) {
		writePref(PREF_TAGS_FETCHED, Boolean.valueOf(value));
	}
	
	private SharedPreferences prefs() {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs;
	}
	
	private boolean readPref(String property, Boolean def) {		
		return prefs().getBoolean(property, def);
	}
	
	private void writePref(String key, Object value) {
		Editor prefsEditor = prefs().edit();
		
		if (value instanceof String)
			prefsEditor.putString(key, (String)value);
		else if (value instanceof Boolean)
			prefsEditor.putBoolean(key, (Boolean) value);

		prefsEditor.commit();
	}	
}
