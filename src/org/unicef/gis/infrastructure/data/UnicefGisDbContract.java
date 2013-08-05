package org.unicef.gis.infrastructure.data;

import android.provider.BaseColumns;

public final class UnicefGisDbContract {
	public UnicefGisDbContract() {}
	
	public static abstract class Tag implements BaseColumns {
		public static final String TABLE_NAME = "tag";
		public static final String COLUMN_NAME_NAME = "name";
	}
}
