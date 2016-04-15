package org.unicef.gis.model.couchdb.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.unicef.gis.model.Report;

import android.annotation.SuppressLint;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;

public class PendingSyncReports extends UnicefGisView {
	private static final String VIEW_NAME = "pendingSyncReports";
	private static final String VIEW_VERSION = "2.0";
	
	public PendingSyncReports(Database db, String designDoc) {
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
	protected Mapper getViewMapBlock() {
		return new Mapper() {
			@Override
			public void map(Map<String, Object> doc, Emitter emitter) {
				if (isReport(doc)){
					Report r = Report.fromMap(doc);
					
					String viewKey = paddedAttempts(r) + "-" + r.getTimestamp(); 
					
					if (!r.getSyncedData() || !r.getSyncedImage())					
						emitter.emit(viewKey, doc);
				}	
			}
		};
	}

	public List<QueryRow> query() throws CouchbaseLiteException {
		view.updateIndex();
		QueryOptions options = new QueryOptions();

		return view.query(options);
	}

	@SuppressLint("DefaultLocale")
	private String paddedAttempts(Report r) {
		return String.format(Locale.ENGLISH, "%06d", r.getAttempts());
	}
}
