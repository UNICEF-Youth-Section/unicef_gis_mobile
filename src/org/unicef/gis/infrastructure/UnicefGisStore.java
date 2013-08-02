package org.unicef.gis.infrastructure;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class UnicefGisStore {
	private final static String PREF_TAGS_FETCHED = "unicef_gis_store_pref_tags_fetched";
	
	private final Context context;
	
	public UnicefGisStore(Context context) {
		this.context = context;
	}

	public void saveAddress(String address) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		Editor prefsEditor = prefs.edit();
		prefsEditor.putString(RoutesResolver.PREF_SERVER_URL, address);
		prefsEditor.commit();
	}

	public boolean tagsHaveBeenFetched() {		
		return readFromPreferences(PREF_TAGS_FETCHED, false);
	}
	
	private boolean readFromPreferences(String property, boolean def) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(property, false);
	}
}
