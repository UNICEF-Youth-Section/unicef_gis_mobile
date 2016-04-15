package org.unicef.gis.model.couchdb.views;

import android.util.Log;

import java.util.List;
import java.util.Map;

import org.unicef.gis.model.Report;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;

public class AllReportsByTimestampDesc extends UnicefGisView {
	private static final String VIEW_NAME = "reports";
	private static final String VIEW_VERSION = "9.0";

	public AllReportsByTimestampDesc(Database db, String designDoc) {
		super(db, designDoc);
	}

	public List<QueryRow> query() throws CouchbaseLiteException {
		view.updateIndex();

		QueryOptions options = new QueryOptions();
		options.setDescending(true);

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
				if (isReport(document))
					emitter.emit(document.get(Report.TIMESTAMP_KEY), document);
			}
		};
	}
}
