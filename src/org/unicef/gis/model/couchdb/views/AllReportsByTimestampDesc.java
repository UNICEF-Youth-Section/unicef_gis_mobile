package org.unicef.gis.model.couchdb.views;

import java.util.List;
import java.util.Map;

import org.unicef.gis.model.Report;

import com.couchbase.cblite.CBLDatabase;
import com.couchbase.cblite.CBLQueryOptions;
import com.couchbase.cblite.CBLStatus;
import com.couchbase.cblite.CBLViewMapBlock;
import com.couchbase.cblite.CBLViewMapEmitBlock;

public class AllReportsByTimestampDesc extends UnicefGisView {
	private static final String VIEW_NAME = "reports";
	private static final String VIEW_VERSION = "6.0";

	public AllReportsByTimestampDesc(CBLDatabase db, String designDoc) {
		super(db, designDoc);
	}

	public List<Map<String, Object>> query() {
		view.updateIndex();
		
		CBLQueryOptions options = new CBLQueryOptions();
		options.setDescending(true);
		
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
				
				if (isReport(document))				
					emitter.emit(document.get(Report.TIMESTAMP_KEY), document);
			}
		};
	}
}
