package org.unicef.gis.model.couchdb;

import java.util.List;

import com.couchbase.touchdb.TDViewReduceBlock;

public class NullReduce implements TDViewReduceBlock {

	@Override
	public Object reduce(List<Object> keys, List<Object> values,
			boolean rereduce) {
		return null;
	}

}
