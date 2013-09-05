package org.unicef.gis.infrastructure.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;
import org.ektorp.ReplicationCommand;
import org.ektorp.impl.StdCouchDbInstance;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.couchdb.NullReduce;
import org.unicef.gis.services.ReportAttachmentsService;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDQueryOptions;
import com.couchbase.touchdb.TDRevision;
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
		Report report = new Report(description, location, imageUri, tags);
		ektorp().create(report);		
			
		Intent addAttachmentIntent = new Intent(context, ReportAttachmentsService.class);
		addAttachmentIntent.putExtra(ReportAttachmentsService.REPORT_ID, report.getId());
		addAttachmentIntent.putExtra(ReportAttachmentsService.IMAGE_URI, imageUri.toString());
		context.startService(addAttachmentIntent);		
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
	
	public Report getReport(String reportId) {		
		TDRevision revision = getCurrentReportRevision(reportId);		
		return Report.fromMap(revision.getBody().getProperties());
	}

	private TDRevision getCurrentReportRevision(String reportId) {
		TDRevision revision = db.getDocumentWithIDAndRev(reportId, null, EnumSet.noneOf(TDDatabase.TDContentOptions.class));
		return revision;
	}
	
	public void addAttachment(String reportId, String imageUri) {	
		Report report = getReport(reportId);
		
		File pic = Camera.fileFromUri(Uri.parse(imageUri));
		
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		try {
			is = new BufferedInputStream(new FileInputStream(pic));
			bos = new ByteArrayOutputStream();
			
			while (is.available() > 0) {
				bos.write(is.read());
			}
		} catch (Exception e) {
			Log.e("TouchDbStoreAdapter", "Report picture not found, saving report without an image.");
			return;
		} finally {
			if (is != null)
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				
		byte[] attach = bos.toByteArray(); 
		AttachmentInputStream ais = new AttachmentInputStream(pic.getName(), new ByteArrayInputStream(attach), "image/jpeg");		
		ektorp().createAttachment(report.getId(), report.getRevision(), ais);
				
		replicateWithAttach(report.getId());
	}

	@SuppressWarnings("unchecked")
	private boolean reportHasAttachment(String docId) {
		TDRevision revision = getCurrentReportRevision(docId);
		
		HashMap<String, Object> attachs = (HashMap<String, Object>)revision.getProperties().get("_attachments");
		
		if (attachs == null) return false;
		
		return attachs.size() > 0;	
	}
	
	private void replicateWithAttach(String docId) {
		ArrayList<String> docIdFilter = new ArrayList<String>();
		docIdFilter.add(docId);
		
		int backoff = 100;
		
		while (!reportHasAttachment(docId)) {
			try {
				Thread.sleep(backoff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			backoff = backoff * 2;
			continue;
		}
						
		ReplicationCommand cmd = new ReplicationCommand.Builder()
			.source(TOUCH_DB_NAME)
			.target("http://192.168.0.120:5984/unicef_gis")
			.continuous(false)
			.docIds(docIdFilter)
			.build();

		couchDb.replicate(cmd);	
	}	

	private CouchDbConnector ektorp() {
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
