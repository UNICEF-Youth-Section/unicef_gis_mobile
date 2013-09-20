package org.unicef.gis.infrastructure.data;

import java.io.IOException;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.views.AllReportsByTimestampDesc;
import org.unicef.gis.model.views.PendingSyncReports;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.couchbase.cblite.CBLDatabase;
import com.couchbase.cblite.CBLServer;
import com.couchbase.cblite.ektorp.CBLiteHttpClient;

public class CouchDbLiteStoreAdapter implements IUnicefGisStoreAdapter {
	public static final String REMOTE_DB_URL = "http://192.168.0.140:5984/unicef_gis";
	public static final String TOUCH_DB_NAME = "unicef_gis_touch_db";
	private static final String DESIGN_DOC_NAME = "design_doc";
	
	private static CBLServer couchDbServer = null;
	private static CBLiteHttpClient couchDbClient = null;	
	
	private static StdCouchDbInstance couchDb = null;
	private static CBLDatabase db = null;
	
	private static CouchDbConnector conn;
	
	private static AllReportsByTimestampDesc allReports;
	private static PendingSyncReports pendingSyncReports;
		
	public CouchDbLiteStoreAdapter(Context context) {
		if (couchDbServer == null) {
			String filesDir = context.getFilesDir().getAbsolutePath();
			try {
				couchDbServer = new CBLServer(filesDir);
				couchDbClient = new CBLiteHttpClient(couchDbServer);
				couchDb = new StdCouchDbInstance(couchDbClient);
				db = couchDbServer.getDatabaseNamed(TOUCH_DB_NAME, true);
				
				allReports = new AllReportsByTimestampDesc(db, DESIGN_DOC_NAME);
				pendingSyncReports = new PendingSyncReports(db, DESIGN_DOC_NAME);
			} catch (IOException e) {
				Log.e("UnicefGisStore", "Error starting TDServer", e);
			}
		}
	}

	@Override
	public void saveReport(Context context, String description,
			Location location, Uri imageUri, List<String> tags) {
		Report report = new Report(description, location, imageUri, tags);		
		ektorp().create(report);		
	}
	
	public void updateReport(Report report) {
		ektorp().update(report);
	}

	@Override
	public Cursor getReportsCursor(Context context) {
		return null;
	}
	
	@Override
	public List<Report> getReports() {			
		return Report.collectionFromMap(allReports.query());
	}
	
	public List<Report> getPendingSyncReports() {		
		return Report.collectionFromMap(pendingSyncReports.query());
	}	
	
	private CouchDbConnector ektorp() {
		if (conn == null)
			conn = couchDb.createConnector(TOUCH_DB_NAME, true);
		
		return conn;
	}
}