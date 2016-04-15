package org.unicef.gis.model.couchdb;

import java.util.List;

import com.couchbase.lite.Reducer;

public class NullReduce implements Reducer {

	@Override
	public Object reduce(List<Object> keys, List<Object> values,
			boolean rereduce) {
		return null;
	}

}
