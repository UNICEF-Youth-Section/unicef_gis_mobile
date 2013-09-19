package org.unicef.gis.model.views;

import java.util.Map;

import org.unicef.gis.model.Report;
import org.unicef.gis.model.couchdb.NullReduce;

import com.couchbase.cblite.CBLDatabase;
import com.couchbase.cblite.CBLView;
import com.couchbase.cblite.CBLViewMapBlock;
import com.couchbase.cblite.CBLViewReduceBlock;

public abstract class UnicefGisView {
	protected CBLView view = null;
	
	public UnicefGisView(CBLDatabase db, String designDoc) {
		if (view == null) {
			setupView(db, designDoc);
		}	
	}

	private void setupView(CBLDatabase db, String designDoc) {
		view = db.getViewNamed(String.format("%s/%s", designDoc, getViewName()));		
		view.setMapReduceBlocks(getViewMapBlock(), getReduceBlock(), getViewVersion());
	}

	protected abstract String getViewName();
	protected abstract String getViewVersion();
	protected abstract CBLViewMapBlock getViewMapBlock();
	
	protected CBLViewReduceBlock getReduceBlock() {
		return new NullReduce();
	}
	
	protected final boolean isReport(Map<String, Object> doc) {
		String type = (String) doc.get(Report.TYPE_KEY);
		return type != null && type.equals(Report.TYPE);
	}
}
