package org.unicef.gis.model.couchdb.views;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.unicef.gis.model.Report;

import android.annotation.SuppressLint;
import com.couchbase.cblite.CBLDatabase;
import com.couchbase.cblite.CBLQueryOptions;
import com.couchbase.cblite.CBLStatus;
import com.couchbase.cblite.CBLViewMapBlock;
import com.couchbase.cblite.CBLViewMapEmitBlock;

public class PendingSyncReports extends UnicefGisView {
	private static final String VIEW_NAME = "pendingSyncReports";
	private static final String VIEW_VERSION = "2.0";
	
	public PendingSyncReports(CBLDatabase db, String designDoc) {
		super(db, designDoc);
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
			public void map(Map<String, Object> doc, CBLViewMapEmitBlock emitter) {
				if (isReport(doc)){
					Report r = Report.fromMap(doc);
					
					String viewKey = paddedAttempts(r) + "-" + r.getTimestamp(); 
					
					if (!r.getSyncedData() || !r.getSyncedImage())					
						emitter.emit(viewKey, doc);
				}	
			}
		};
	}

	public List<Map<String, Object>> query() {
		view.updateIndex();
		
		CBLQueryOptions options = new CBLQueryOptions();		
		CBLStatus status = new CBLStatus();
		
		return view.queryWithOptions(options, status);
	}

	@SuppressLint("DefaultLocale")
	private String paddedAttempts(Report r) {
		return String.format(Locale.ENGLISH, "%06d", r.getAttempts());
	}
}
