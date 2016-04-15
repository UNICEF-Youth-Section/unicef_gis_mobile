package org.unicef.gis.infrastructure.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unicef.gis.model.Report;
import org.unicef.gis.model.couchdb.views.AllReportsByTimestampDesc;
import org.unicef.gis.model.couchdb.views.PendingSyncReports;
import org.unicef.gis.model.couchdb.views.UploadedReports;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;

public class CouchDbLiteStoreAdapter {
	public static final String TOUCH_DB_NAME = "unicef_gis_touch_db";
	private static final String DESIGN_DOC_NAME = "design_doc";
	public static final String TAG = "CouchDbLiteStoreAdapter";
	
	private static Database database;
	private static Manager manager;

	private static AllReportsByTimestampDesc allReports;
	private static PendingSyncReports pendingSyncReports;
	private static UploadedReports uploadedReports;

	private static CouchDbLiteStoreAdapter instance = null;
	private Context context = null;

	public static CouchDbLiteStoreAdapter get(Context context) {
		if (instance == null) {
			instance = new CouchDbLiteStoreAdapter(context.getApplicationContext());
		}
		return instance;
	}

	private CouchDbLiteStoreAdapter(Context context) {
		try {
			manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
			database = manager.getDatabase(TOUCH_DB_NAME);

			allReports = new AllReportsByTimestampDesc(database, DESIGN_DOC_NAME);
			pendingSyncReports = new PendingSyncReports(database, DESIGN_DOC_NAME);
			uploadedReports = new UploadedReports(database, DESIGN_DOC_NAME);
		} catch (Exception e) {
			Log.e(TAG, "Error getting database", e);
			return;
		}
	}

	public void saveReport(Context context, String description,
			Location location, Uri imageUri, List<String> tags, boolean postToTwitter, boolean postToFacebook) {

		Document document = database.createDocument();
		String documentId = document.getId();

		Report report = new Report(description, location, imageUri, tags);
		report.setPostToFacebook(postToFacebook);
		report.setPostToTwitter(postToTwitter);
		Map<String, Object> map = report.toMap();

		try {
			// Save the properties to the document
			document.putProperties(map);
		} catch (CouchbaseLiteException e) {
			Log.e(TAG, "Error putting", e);
		}
	}

	public void updateReportSyncStatus(String id, final boolean synced) {
		try {
			Document document = database.getDocument(id);
			document.update(new Document.DocumentUpdater() {
				@Override
				public boolean update(UnsavedRevision newRevision) {
					Map<String, Object> properties = newRevision.getUserProperties();
					properties.put(Report.ATTEMPTS_KEY, (Integer)properties.get(Report.ATTEMPTS_KEY) + 1);
					if (synced) {
						properties.put(Report.SYNCED_DATA_KEY, true);
						properties.put(Report.SYNCED_IMAGE_KEY, true);
					}
					newRevision.setUserProperties(properties);
					return true;
				}
			});
//			document.putProperties(report.toMap());
		} catch (CouchbaseLiteException e) {
			Log.e(TAG, "Error putting", e);
		}
	}
	
	public void updateReport(final Report report) {
		try {
			Document document = database.getDocument(report.getId());
			document.putProperties(report.toMap());
		} catch (CouchbaseLiteException e) {
			Log.e(TAG, "Error putting", e);
		}
	}
	
	public void deleteReport(Report report) {
		try {
			Document retrievedReport = database.getDocument(report.getId());
			retrievedReport.delete();
			Log.d(TAG, "Deleted document, deletion status = " + retrievedReport.isDeleted());
		} catch (CouchbaseLiteException e) {
			Log.e (TAG, "Cannot delete document", e);
		}
	}

	public List<Report> getReports() throws CouchbaseLiteException {
		List<Report> coll = Report.collectionFromMap(allReports.query());
		Log.e("getReports",coll.toString());
		return coll;
	}
	
	public List<Report> getPendingSyncReports() throws CouchbaseLiteException {
		List<Report> coll = Report.collectionFromMap(pendingSyncReports.query());
		Log.e("getPendingReports",coll.toString());
		return coll;
	}	
	
	public List<Report> getUploadedReports() throws CouchbaseLiteException {
		List<Report> coll = Report.collectionFromMap(uploadedReports.query());
		Log.e("getUploadedReports",coll.toString());
		return coll;
	}
}
