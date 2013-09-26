package org.unicef.gis.infrastructure;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.unicef.gis.model.Tag;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Credentials;
import android.util.Base64;
import android.util.Log;
import edu.mit.mobile.android.utils.StreamUtils;

public class UnicefGisApi {
	private static final String TAG = UnicefGisApi.class.getSimpleName();
	public final static String JSON_MIME_TYPE = "application/json";

	protected URI baseUrl;
	
	// one of the formats from ISO 8601
	@SuppressLint("SimpleDateFormat")
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

	//TODO: erased auth, accept-language and remove expectations interceptors, may need to implement an alternative
	public static final String PREF_LOCAST_SITE = "locast_site";

	private final RoutesResolver routes;
	
	/**
	 * Create a new NetworkClient, authenticating with the given account.
	 *
	 * @param context
	 * @param account
	 * @throws MalformedURLException 
	 */
	public UnicefGisApi(Context context, Account account) throws MalformedURLException {
		this(context);
	}

	/**
	 * Create a new NetworkClient using the baseUrl. You will need to call
	 * {@link #setCredentials(Credentials)} at some point if you want authentication.
	 *
	 * @param context
	 * @param baseUrl
	 */
	public UnicefGisApi(Context context) {
		super();
		this.routes = new RoutesResolver(context);
	}
	
	public String authenticate(final String email, final String password) {
		HttpURLConnection conn = null;
		try {
			URL url = routes.getUser();
			conn = (HttpURLConnection) url.openConnection();
			
			String token = email + ":" + password;
			String encodedAuthorization = Base64.encodeToString(token.getBytes(), Base64.NO_WRAP);
			conn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
			
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				return token;
			else
				return null;
		} catch (IOException e) {
			Log.e(TAG, "authenticate: " + e.getMessage());
			
			if (conn != null){
				try {
					String errors = StreamUtils.inputStreamToString(new BufferedInputStream(conn.getErrorStream()));
					Log.e(TAG, "authenticate: server response: " + errors);
				} catch (IOException e1) {
					Log.e(TAG, "authenticate: handle error stream: " + e1.getMessage());
					e1.printStackTrace();
				}
			}
			
			return null;		
		} catch (ServerUrlPreferenceNotSetException e) {
			e.printStackTrace();
			Log.e(TAG, "authenticate: failed because there's no Server URL set up");
			return null;
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	public List<Tag> getTags() throws ServerUrlPreferenceNotSetException {
		JSONArray tags = getArray(routes.getTags());
		
		if (tags == null) return null;
		
		return Tag.listFromJSON(tags);	
	}
	
	/**
	 * Makes a GET request to the given url
	 * @param url
	 * @return a JSONObject containing the response if the request succeeded, null otherwise
	 */
	private JSONArray getArray(final URL url) {
		HttpURLConnection conn = null;
		
		try {
			conn = (HttpURLConnection) url.openConnection();
			
			//Disable the requirement of GZIP to the server (it'd be nice to eventually reenable it)
			conn.setRequestProperty("Accept-Encoding", "identity");
			
			InputStream responseStream = conn.getInputStream();
			
			boolean requestSucceeded = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
			if (!requestSucceeded) {
				handleRequestFailure(conn);
			} else {
				String response = StreamUtils.inputStreamToString(new BufferedInputStream(responseStream));
				return new JSONArray(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) conn.disconnect();
		}
		
		return null;
	}

	private boolean handleRequestFailure(HttpURLConnection conn) throws IOException {
		int statusCode = conn.getResponseCode();
		String errorMessage = conn.getResponseMessage();
		
		if (conn.getContentType().equals("text/html") || conn.getContentLength() > 40)
			errorMessage = "Got long response body. Not showing.";				
		else if (errorMessage == null) errorMessage = "HTTP request failed";
		
		Log.d(TAG, "handleRequestFailure: " + errorMessage);
		throw new HttpResponseException(statusCode, errorMessage);
	}
}
