package org.unicef.gis.infrastructure;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/***
 * Provides URLs to interact with UNICEF GIS' REST API
 * 
 * @author mverzilli
 *
 */
public class RoutesResolver {
	public static final String PREF_SERVER_URL = "server_url";
	
	private Context context;

	public RoutesResolver(Context context) {
		this.context = context;
	}
	
	public URL getUser() throws MalformedURLException {
		return new URL(getBaseUrl() + "user/me");
	}
	
	public String getBaseUrl() {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(PREF_SERVER_URL, "");
	}
}
