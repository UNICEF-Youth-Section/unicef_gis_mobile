package org.unicef.gis.infrastructure.data;

import org.unicef.gis.infrastructure.data.UnicefGisDbContract.Tag;

public final class TagSqlScripts {
	
	public TagSqlScripts() {}
	
	public static final String SQL_CREATE_TABLE =
		    "CREATE TABLE " + Tag.TABLE_NAME + " (" +
		    Tag._ID + " INTEGER PRIMARY KEY," +
		    Tag.COLUMN_NAME_NAME + " TEXT)";

	public static final String SQL_DROP_TABLE =
		    "DROP TABLE IF EXISTS " + Tag.TABLE_NAME;
	
}
