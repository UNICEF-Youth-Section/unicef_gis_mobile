package org.unicef.gis.infrastructure.data;

import org.unicef.gis.infrastructure.data.UnicefGisDbContract.Report;

public class ReportSqlScripts {	
	public static final String SQL_CREATE_TABLE =
		    "CREATE TABLE " + Report.TABLE_NAME + " (" +
		    Report._ID + " INTEGER PRIMARY KEY," +
		    Report.COLUMN_NAME_TITLE + " TEXT)";

	public static final String SQL_DROP_TABLE =
		    "DROP TABLE IF EXISTS " + Report.TABLE_NAME;
}
