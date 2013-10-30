package org.unicef.gis.model.couchdb.views;

import java.util.List;
import java.util.Map;

import org.unicef.gis.model.Report;

import com.couchbase.cblite.CBLDatabase;
import com.couchbase.cblite.CBLQueryOptions;
import com.couchbase.cblite.CBLStatus;
import com.couchbase.cblite.CBLViewMapBlock;
import com.couchbase.cblite.CBLViewMapEmitBlock;

public class UploadedReports extends UnicefGisView {
	private static final String VIEW_NAME = "uploaded_reports";
	private static final String VIEW_VERSION = "1.0";

	public UploadedReports(CBLDatabase db, String designDoc) {
		super(db, designDoc);
	}

	public List<Map<String, Object>> query() {
		view.updateIndex();
		
		CBLQueryOptions options = new CBLQueryOptions();		
		CBLStatus status = new CBLStatus();
		
		return view.queryWithOptions(options, status);			
	}

	@Override
	protected String getViewName() {
		return VIEW_NAME;
	}

	@Override
	protected String getViewVersion() {
		return VIEW_VERSION;
	}

	@Override
	protected CBLViewMapBlock getViewMapBlock() {
		return new CBLViewMapBlock() {
			@Override
			public void map(Map<String, Object> document,
					CBLViewMapEmitBlock emitter) {
				
				if (isReport(document) && 
						(Boolean)document.get(Report.SYNCED_DATA_KEY) && 
						(Boolean)document.get(Report.SYNCED_IMAGE_KEY)) {					
					
					emitter.emit(document.get(Report.TIMESTAMP_KEY), document);
				}				
			}
		};
	}
}