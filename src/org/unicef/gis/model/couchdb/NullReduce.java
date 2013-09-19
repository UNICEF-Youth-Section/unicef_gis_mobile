package org.unicef.gis.model.couchdb;

import java.util.List;

import com.couchbase.cblite.CBLViewReduceBlock;

public class NullReduce implements CBLViewReduceBlock {

	@Override
	public Object reduce(List<Object> keys, List<Object> values,
			boolean rereduce) {
		return null;
	}

}
