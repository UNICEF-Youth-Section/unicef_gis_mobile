package org.unicef.gis.sync;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.unicef.gis.auth.Authenticator;
import org.unicef.gis.infrastructure.Notificator;
import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.UnicefGisApi;
import org.unicef.gis.infrastructure.data.CouchDbLiteStoreAdapter;
import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.Tag;
import org.unicef.gis.ui.AuthenticatorActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.couchbase.cblite.router.CBLURLStreamHandlerFactory;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
	{
		CBLURLStreamHandlerFactory.registerSelfIgnoreError();
	}

	private CouchDbLiteStoreAdapter couchDb = null;
	private Camera camera = null;
	private UnicefGisApi api = null;
	private UnicefGisStore store = null;
	private AccountManager accountManager = null;
	private Notificator notificator = null;
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		couchDb = new CouchDbLiteStoreAdapter(getContext());
		camera = new Camera(getContext());
		api = new UnicefGisApi(getContext());
		store = new UnicefGisStore(getContext());
		accountManager = AccountManager.get(context);
		notificator = new Notificator(getContext());
	}

	public SyncAdapter(Context context, boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		couchDb = new CouchDbLiteStoreAdapter(getContext());
	}

	@Override
	public void onPerformSync(Account account, Bundle bundle, String authority,
			ContentProviderClient provider, SyncResult syncResult) {

		String authtoken = null;
		try {
			authtoken = accountManager.blockingGetAuthToken(account, AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, true);
			
			if (authtoken != null)
				Log.e("SyncAdapter", "Got auth token from manager: " + authtoken);
			
			List<Report> reportsToSync = couchDb.getPendingSyncReports();

			for (Report report : reportsToSync) {
				sync(report, authtoken);
			}
		} catch (Exception e) {
			handleException(authtoken, e, syncResult);
		}

		// We don't require authentication for tags synchronization
		syncTags();
	}

	private void handleException(String authtoken, Exception e,
			SyncResult syncResult) {
		if (e instanceof AuthenticatorException) {
			syncResult.stats.numParseExceptions++;
			Log.e("SyncAdapter", "AuthenticatorException", e);
		} else if (e instanceof OperationCanceledException) {
			Log.e("SyncAdapter", "OperationCanceledExcepion", e);
		} else if (e instanceof IOException) {
			Log.e("SyncAdapter", "IOException", e);
			syncResult.stats.numIoExceptions++;
		} else if (e instanceof AuthenticationException) {
			accountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, authtoken);
			syncResult.stats.numIoExceptions++;
			if (authtoken != null)
				Log.e("SyncAdapter", "Auth failed, invalidating token: " + authtoken);
			Log.e("SyncAdapter", "AuthenticationException", e);
		} else if (e instanceof ParseException) {
			syncResult.stats.numParseExceptions++;
			Log.e("SyncAdapter", "ParseException", e);
		} else if (e instanceof JsonParseException) {
			syncResult.stats.numParseExceptions++;
			Log.e("SyncAdapter", "JSONException", e);
		} else if (e instanceof ServerUrlPreferenceNotSetException) {
			Log.e("SyncAdapter", "ServerUrlPreferenceNotSetException", e);
		}
	}

	private void syncTags() {
		try {
			List<Tag> downloadedTags = api.getTags(); 
			
			if (downloadedTags != null)
				store.saveTags(downloadedTags);
		} catch (ServerUrlPreferenceNotSetException e) {
			e.printStackTrace();
		}
	}

	private void sync(Report report, String authtoken)
			throws JsonGenerationException, JsonMappingException, IOException,
			ServerUrlPreferenceNotSetException, AuthenticationException {
		OutputStream out = null;
		HttpURLConnection conn = null;

		File image = camera.rotateImageIfNecessary(report.getImageUri());

		//For some reason, the image is not currently available (it maybe missing altogether). 
		//In this case, we increment the "attempts" counter to give it a chance to eventually upload 
		//provided the issue is circumstantial.
		//This policy would play nice if in the future we decide to mark reports as failed upon reaching a 
		//number of attempts.
		if (image == null) {			
			markAsNotSyncd(report);
			return;
		}
		
		int status = 0;
		try {
			String json = report.json();
			Log.d("SyncAdapter", "Sending JSON string to server: " + json);

			String boundary = Long.toHexString(System.currentTimeMillis());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			writeMultipart(boundary, bos, false, json, image);
			byte[] extra = bos.toByteArray();

			int contentLength = extra.length;
			contentLength += image.length();
			contentLength += json.getBytes(Charset.defaultCharset()).length;

			conn = openConnection(boundary, contentLength, authtoken);

			out = conn.getOutputStream();
			writeMultipart(boundary, out, true, json, image);

			status = conn.getResponseCode();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (conn != null) {
				conn.disconnect();
			}

			if (status == HttpURLConnection.HTTP_OK) {
				markAsSyncd(report);
				notificator.notifyReportUploaded(report);
			} else {
				markAsNotSyncd(report);
			}
			
			if (status == HttpURLConnection.HTTP_UNAUTHORIZED)
				throw new AuthenticationException();
		}
	}

	private void markAsNotSyncd(Report report) {
		updateSyncData(report, false);
	}

	private void markAsSyncd(Report report) {
		updateSyncData(report, true);
	}

	private void updateSyncData(Report report, boolean synced) {
		report.setAttempts(report.getAttempts() + 1);
		report.setSyncedData(synced);
		report.setSyncedImage(synced);

		couchDb.updateReport(report);
	}

	private HttpURLConnection openConnection(String boundary, int contentLength, String authtoken)
			throws IOException, MalformedURLException,
			ServerUrlPreferenceNotSetException {

		RoutesResolver r = new RoutesResolver(getContext());

		HttpURLConnection conn = (HttpURLConnection) r.syncReport()
				.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setDoOutput(true);
		
		setupBasicAuth(conn, authtoken);

		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		conn.setFixedLengthStreamingMode(contentLength);

		conn.connect();
		return conn;
	}

	private void setupBasicAuth(HttpURLConnection conn, String authtoken) {
		String encodedAuthorization = Base64.encodeToString(authtoken.getBytes(), Base64.NO_WRAP);
		conn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
	}

	private void writeMultipart(String boundary, OutputStream output,
			boolean writeContent, String jsonBody, File image)
			throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(output,
					Charset.defaultCharset()), 8192);
			if (jsonBody != null) {
				writer.write("--" + boundary);
				writer.write("\r\n");
				writer.write("Content-Disposition: form-data; name=\"parameters\"");
				writer.write("\r\n");
				writer.write("Content-Type: application/json; charset="
						+ Charset.defaultCharset().displayName());
				writer.write("\r\n");
				writer.write("\r\n");
				if (writeContent) {
					writer.write(jsonBody);
				}
				writer.write("\r\n");
				writer.flush();
			}

			// Send binary file.
			writer.write("--" + boundary);
			writer.write("\r\n");
			writer.write("Content-Disposition: form-data; name=\""
					+ image.getName() + "\"; filename=\"" + image.getName()
					+ "\"");
			writer.write("\r\n");
			writer.write("Content-Type: "
					+ URLConnection.guessContentTypeFromName(image.getName()));
			writer.write("\r\n");
			writer.write("Content-Transfer-Encoding: binary");
			writer.write("\r\n");
			writer.write("\r\n");
			writer.flush();

			if (writeContent) {
				InputStream input = null;
				try {
					input = new FileInputStream(image);
					byte[] buffer = new byte[1024];

					for (int length = 0; (length = input.read(buffer)) > 0;) {
						output.write(buffer, 0, length);
					}

					// Don't close the OutputStream yet
					output.flush();
				} catch (IOException e) {
					Log.w("SyncAdapter", e);
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
						}
					}
				}
			}

			// This CRLF signifies the end of the binary data chunk
			writer.write("\r\n");
			writer.flush();

			// End of multipart/form-data.
			writer.write("--" + boundary + "--");
			writer.write("\r\n");
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
