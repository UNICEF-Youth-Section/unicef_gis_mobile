package org.unicef.gis.infrastructure.data;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.Tag;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;

public class UnicefGisStore {
	private final static String PREF_TAGS_FETCHED = "unicef_gis_store_pref_tags_fetched";
	private final static String PREF_TAGS = "unicef_gis_store_pref_tags";
	
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
		CouchDbLiteStoreAdapter adapter = new CouchDbLiteStoreAdapter(context);
		adapter.saveReport(context, description, location, imageUri, tags);			
	}
	
	public List<Report> getReports() {
		CouchDbLiteStoreAdapter adapter = new CouchDbLiteStoreAdapter(context);
		return adapter.getReports();
	}

	public void saveTags(List<Tag> tags) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		
		for (Tag tag : tags) {
			if (!first) sb.append(","); 			
			first = false;
			
			sb.append(tag.getValue());			 				
		}
		
		writePref(PREF_TAGS, sb.toString());		
		setTagsHaveBeenFetched(true);		
	}
	
	public List<Tag> retrieveTags() {
		String tagsString = readPref(PREF_TAGS, "");
		String[] tagsArray = tagsString.split(",");
		
		List<Tag> tags = new ArrayList<Tag>();		
		
		for (int i = 0; i < tagsArray.length; i++) {
			tags.add(new Tag(tagsArray[i]));
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
	
	private String readPref(String property, String def) {
		return prefs().getString(property, def);
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
