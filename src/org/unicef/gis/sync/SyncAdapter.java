package org.unicef.gis.sync;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.unicef.gis.infrastructure.data.CouchDbLiteStoreAdapter;
import org.unicef.gis.model.Report;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.couchbase.cblite.router.CBLURLStreamHandlerFactory;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
	{
	    CBLURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	private static String charset = Charset.defaultCharset().displayName();
	
	private CouchDbLiteStoreAdapter store = null;
	
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        store = new CouchDbLiteStoreAdapter(getContext());
    }
    
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        store = new CouchDbLiteStoreAdapter(getContext());
    }
	
	@Override
	public void onPerformSync(Account account, Bundle bundle, String authority,
			ContentProviderClient provider, SyncResult syncResult) {								
		List<Report> reportsToSync = store.getPendingSyncReports();
		
		for (Report report : reportsToSync) {
			sync(report);
		}		
	}

	private void sync(Report report) {
		OutputStream out = null;
		
		int status = 0;
		try {
			HttpURLConnection conn = openConnection();						
			
			out = conn.getOutputStream();
			
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(report);
			
			Log.d("SyncAdapter", "Sending JSON string to server: " + json);
			
			out.write(json.getBytes(charset));			
			out.flush();		
			
			status = conn.getResponseCode();								
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
			
			if (status == HttpURLConnection.HTTP_OK)
				markAsSyncd(report);
			else
				markAsNotSyncd(report);
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
		
		store.updateReport(report);
	}

	private HttpURLConnection openConnection() throws IOException,
			MalformedURLException {
		HttpURLConnection conn = (HttpURLConnection) new URL("http://192.168.0.148:8000/api/sync_spike/").openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
		conn.connect();
		return conn;
	}
}
