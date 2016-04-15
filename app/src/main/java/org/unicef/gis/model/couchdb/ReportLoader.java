package org.unicef.gis.model.couchdb;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.model.Report;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

public class ReportLoader extends AsyncTaskLoader<List<Report>> {
	
	private List<Report> reports = null;
	
	public ReportLoader(Context context) {
		super(context);
	}

	@Override
	public List<Report> loadInBackground() {
		try {
			UnicefGisStore store = new UnicefGisStore(getContext());

			return store.getReports();
		} catch (CouchbaseLiteException e) {
			Log.e("ReportLoader", "Query to load Reports failed.", e);
			e.printStackTrace();
		}
		return new ArrayList<Report>();
	}
	
	@Override
	public void deliverResult(List<Report> data) {
	    // Hold a reference to the old data so it doesn't get garbage collected.
	    // We must protect it until the new data has been delivered.
		@SuppressWarnings("unused")
		List<Report> oldReports = reports;
		
		reports = data;
		
		if (isStarted()) super.deliverResult(reports);
	}

	@Override
	protected void onStartLoading() {
		if (reports != null) deliverResult(reports);
		else forceLoad();
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		onStopLoading();		
		reports = null;
	}
	
	@Override
	public void onCanceled(List<Report> data) {
		super.onCanceled(data);
	}	
}
