package org.unicef.gis.infrastructure.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.couchdb.NullReduce;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDQueryOptions;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDStatus;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;

public class TouchDbStoreAdapter implements IUnicefGisStoreAdapter {
	private static final String TOUCH_DB_NAME = "unicef_gis_touch_db";
	private static final String DESIGN_DOC_NAME = "design_doc";
	private static final String REPORTS_VIEW = "reports";
	private static final String REPORTS_VIEW_VERSION = "6.0";
	
	private static TDServer touchDbServer = null;
	private static TouchDBHttpClient touchDbClient = null; 
	private static StdCouchDbInstance couchDb = null;
	private static TDDatabase db = null;
	private static TDView reportsView = null;
	
	public TouchDbStoreAdapter(Context context) {
		if (touchDbServer == null) {
			String filesDir = context.getFilesDir().getAbsolutePath();
			try {
				touchDbServer = new TDServer(filesDir);
				touchDbClient = new TouchDBHttpClient(touchDbServer);
				couchDb = new StdCouchDbInstance(touchDbClient);
				
				setupDb();
				setupReportsView();
			} catch (IOException e) {
				Log.e("UnicefGisStore", "Error starting TDServer", e);
			}
		}
	}

	@Override
	public void saveReport(Context context, String description,
			Location location, Uri imageUri, List<String> tags) {		
		connector().create(new Report(description, location, imageUri, tags));		
	}

	@Override
	public Cursor getReportsCursor(Context context) {
		return null;
	}
	
	@Override
	public List<Report> getReports() {	
		reportsView.updateIndex();
		
		TDQueryOptions options = new TDQueryOptions();
		options.setDescending(true);
		
		TDStatus status = new TDStatus();
					
		return Report.collectionFromMap(reportsView.queryWithOptions(options, status));
	}

	private CouchDbConnector connector() {
		CouchDbConnector conn = couchDb.createConnector(TOUCH_DB_NAME, true);
		return conn;
	}
	
	private void setupDb() {
		db = touchDbServer.getDatabaseNamed(TOUCH_DB_NAME, true);
	}
	
	private void setupReportsView() {	
		reportsView = db.getViewNamed(String.format("%s/%s", DESIGN_DOC_NAME, REPORTS_VIEW));
		
		reportsView.setMapReduceBlocks(new TDViewMapBlock() {
			@Override
			public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
				emitter.emit(document.get(Report.TIMESTAMP_KEY), document);
			}
		}, new NullReduce(), REPORTS_VIEW_VERSION);
	}	
}
