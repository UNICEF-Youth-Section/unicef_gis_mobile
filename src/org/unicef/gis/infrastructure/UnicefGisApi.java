package org.unicef.gis.infrastructure;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unicef.gis.ConfigureServerUrlActivity;
import org.unicef.gis.model.Tag;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Credentials;
import android.net.Uri;
import android.os.Bundle;
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
	private final Context context;
	
	/**
	 * Create a new NetworkClient, authenticating with the given account.
	 *
	 * @param context
	 * @param account
	 * @throws MalformedURLException 
	 */
	public UnicefGisApi(Context context, Account account) throws MalformedURLException {
		this(context);
		loadFromExistingAccount(account);
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
		this.context = context;
		this.routes = new RoutesResolver(context);
	}
	
	//TODO: refactor to reuse generic methods: get, post, etc.
	public Bundle authenticate(final String email, final String password) throws ServerUrlPreferenceNotSetException {
		HttpURLConnection conn = null;
		try {
			URL url = routes.getUser();
			conn = (HttpURLConnection) url.openConnection();
			
			//Disable the requirement of GZIP to the server (it'd be nice to eventually reenable it)
			conn.setRequestProperty("Accept-Encoding", "identity");
			
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email, password.toCharArray());
				}
			});
					
			String response = StreamUtils.inputStreamToString(new BufferedInputStream(conn.getInputStream()));
			
			boolean requestSucceeded = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
			
			if (!requestSucceeded) {
				handleRequestFailure(conn);
			} else {
				JSONObject jo = new JSONObject(response);
				
				final Bundle userData = jsonObjectToBundle(jo, true);
				return userData;
			}
			
			return null;
		} catch (IOException e) {
			//TODO: log whole stacktrace
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
		} catch (JSONException e) {
			Log.e(TAG, "authenticate: invalid JSON response");
			return null;
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	public List<Tag> getTags() {
		try {
			JSONArray tags = getArray(routes.getTags());
			return Tag.listFromJSON(tags);
		} catch (ServerUrlPreferenceNotSetException exception) {
			context.startActivity(new Intent(context, ConfigureServerUrlActivity.class));
			return null;
		}
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
	
	public static Bundle jsonObjectToBundle(JSONObject jsonObject, boolean allStrings) {
		final Bundle b = new Bundle();
		for (@SuppressWarnings("unchecked")
		final Iterator<String> i = jsonObject.keys(); i.hasNext();) {
			final String key = i.next();
			final Object value = jsonObject.opt(key);
			if (value == null) {
				b.putSerializable(key, null);

			} else if (allStrings) {
				b.putString(key, String.valueOf(value));

			} else if (value instanceof String) {
				b.putString(key, (String) value);

			} else if (value instanceof Integer) {
				b.putInt(key, (Integer) value);
			}
		}
		return b;
	}

	public synchronized Uri getFullUrl(String path) {
		Uri fullUri;
		if (path.startsWith("http")) {
			fullUri = Uri.parse(path);

		} else {
			fullUri = Uri.parse(baseUrl.resolve(path).normalize().toASCIIString());
			Log.d("NetworkClient", "path: " + path + ", baseUrl: " + baseUrl + ", fullUri: " + fullUri);
		}

		return fullUri;
	}

	public String getBaseUrl() {
		return baseUrl.toString();
	}

	public synchronized String getFullUrlAsString(String path) {
		String fullUrl;
		if (path.startsWith("http")) {
			fullUrl = path;

		} else {
			fullUrl = baseUrl.resolve(path).normalize().toASCIIString();
			Log.d("NetworkClient", "path: " + path + ", baseUrl: " + baseUrl + ", fullUrl: " + fullUrl);
		}

		return fullUrl;
	}

	/**
	 * Listener for use with InputStreamWatcher.
	 *
	 * @author steve
	 *
	 */
	public static interface TransferProgressListener {
		/**
		 * @param bytes
		 *            Total bytes transferred.
		 */
		public void publish(long bytes);
	}

	public static class InputStreamWatcher extends InputStream {
		private static final int GRANULARITY = 1024 * 100; // bytes; number needed to trigger a
															// publish()
		private final InputStream mInputStream;
		private final TransferProgressListener mProgressListener;
		private long mCount = 0;
		private long mIncrementalCount = 0;

		public InputStreamWatcher(InputStream wrappedStream,
				TransferProgressListener progressListener) {
			mInputStream = wrappedStream;
			mProgressListener = progressListener;
		}

		private void incrementAndNotify(long count) {
			mCount += count;
			mIncrementalCount += count;
			if (mIncrementalCount > GRANULARITY) {
				mProgressListener.publish(mCount);
				mIncrementalCount = 0;
			}
		}

		@Override
		public int read() throws IOException {
			return mInputStream.read();
		}

		private int rcount;

		@Override
		public int read(byte[] b) throws IOException {
			rcount = mInputStream.read(b);
			incrementAndNotify(rcount);
			return rcount;
		}

		@Override
		public int read(byte[] b, int offset, int length) throws IOException {
			rcount = mInputStream.read(b, offset, length);
			incrementAndNotify(rcount);
			return rcount;
		}

		@Override
		public int available() throws IOException {
			return mInputStream.available();
		}

		@Override
		public void close() throws IOException {
			mCount = 0;
			mInputStream.close();
		}

		@Override
		public boolean equals(Object o) {
			return mInputStream.equals(o);
		}

		@Override
		public int hashCode() {
			return mInputStream.hashCode();
		}

		@Override
		public void mark(int readlimit) {
			mInputStream.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return mInputStream.markSupported();
		}

		@Override
		public long skip(long n) throws IOException {
			final long count = mInputStream.skip(n);
			incrementAndNotify(count);
			return count;
		}

		@Override
		public synchronized void reset() throws IOException {
			mInputStream.reset();
		}
	}
	
	protected synchronized void loadFromExistingAccount(Account account) {
		/*if (account == null) {
			throw new IllegalArgumentException("must specify account");
		}

		String baseUrlString;

		final AccountManager am = AccountManager.get(context);
		
		baseUrlString = getBaseUrlFromPreferences(context);
		am.setUserData(account, AuthenticatorService.USERDATA_LOCAST_API_URL, baseUrlString);
		
		try {
			setBaseUrl(baseUrlString);
			setCredentialsFromAccount(account);
		} catch (final MalformedURLException e) {
			Log.e(TAG, e.getLocalizedMessage(), e);
		}*/
	}

	public static Date parseDate(String dateString) throws ParseException {
		/*
		 * if (dateString.endsWith("Z")){ dateString = dateString.substring(0,
		 * dateString.length()-2) + "GMT"; }
		 */
		return dateFormat.parse(dateString);
	}

	static {
		dateFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
	}
}
