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
	
	public URL getUser() throws ServerUrlPreferenceNotSetException {
		try {
			return new URL(getBaseUrl() + "user/me");
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public URL getTags() throws ServerUrlPreferenceNotSetException {
		try {
			String baseUrl = getBaseUrl();
			URL tagsUrl = new URL(baseUrl + "tags/"); 
			return tagsUrl;
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public String getBaseUrl() throws ServerUrlPreferenceNotSetException {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String baseUrl = prefs.getString(PREF_SERVER_URL, "");
		
		if (baseUrl.isEmpty())
			throw new ServerUrlPreferenceNotSetException();
		
		if (!(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")))
			baseUrl = "http://" + baseUrl;
		
		if (!baseUrl.endsWith("/"))
			baseUrl = baseUrl + "/";
		
		return baseUrl + "api/";
	}
}
