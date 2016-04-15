package org.unicef.gis.model.couchdb.views;

import android.util.Log;

import java.util.Map;

import org.unicef.gis.model.Report;

import com.couchbase.lite.Database;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;

public abstract class UnicefGisView {
	protected View view = null;
	
	public UnicefGisView(Database db, String designDoc) {
		if (view == null) {
			setupView(db, designDoc);
		}	
	}

	private void setupView(Database db, String designDoc) {
		view = db.getView(String.format("%s/%s", designDoc, getViewName()));
		view.setMap(getViewMapBlock(), getViewVersion());
	}

	protected abstract String getViewName();
	protected abstract String getViewVersion();
	protected abstract Mapper getViewMapBlock();
	
	protected final boolean isReport(Map<String, Object> doc) {
		String type = (String) doc.get(Report.TYPE_KEY);
		return type != null && type.equals(Report.TYPE);
	}
}
