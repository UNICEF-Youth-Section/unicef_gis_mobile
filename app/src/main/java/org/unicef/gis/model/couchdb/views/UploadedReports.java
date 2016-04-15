package org.unicef.gis.model.couchdb.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.unicef.gis.model.Report;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;

public class UploadedReports extends UnicefGisView {
	private static final String VIEW_NAME = "uploaded_reports";
	private static final String VIEW_VERSION = "1.0";

	public UploadedReports(Database db, String designDoc) {
		super(db, designDoc);
	}

	public List<QueryRow> query() throws CouchbaseLiteException {
		view.updateIndex();

		QueryOptions options = new QueryOptions();

		return view.query(options);
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
			public void map(Map<String, Object> document,
					Emitter emitter) {
				
				if (isReport(document) && 
						(Boolean)document.get(Report.SYNCED_DATA_KEY) && 
						(Boolean)document.get(Report.SYNCED_IMAGE_KEY)) {					
					
					emitter.emit(document.get(Report.TIMESTAMP_KEY), document);
				}				
			}
		};
	}
}