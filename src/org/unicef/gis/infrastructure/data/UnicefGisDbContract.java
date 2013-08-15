package org.unicef.gis.infrastructure.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class UnicefGisDbContract {
	public UnicefGisDbContract() {}
	
	public static abstract class Selections {
		public static final String ALL = "1 = 1";
	}
	
	public static abstract class Tag implements BaseColumns {
		public static final String TABLE_NAME = "tag";
		public static final String COLUMN_NAME_NAME = "name";
	}
	
	public static abstract class Report implements BaseColumns {
		public static final String TABLE_NAME = "report";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_GUID = "guid";
		public static final String COLUMN_NAME_LAT = "lat";
		public static final String COLUMN_NAME_LONG = "lng";
		public static final String COLUMN_NAME_IMAGE = "image";
		public static final String COLUMN_NAME_TAGS = "tags";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		
		public static final String[] DEFAULT_PROJECTION = new String[] 
		{ 
			_ID, 
			COLUMN_NAME_TITLE,
			COLUMN_NAME_GUID,
			COLUMN_NAME_LAT,
			COLUMN_NAME_LONG,
			COLUMN_NAME_IMAGE,
			COLUMN_NAME_TAGS,
			COLUMN_NAME_TIMESTAMP
		};
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + UnicefGisContentProvider.AUTHORITY+ "/reports");				
	}
}
