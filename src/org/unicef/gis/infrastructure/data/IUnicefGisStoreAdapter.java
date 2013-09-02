package org.unicef.gis.infrastructure.data;

import java.util.List;

import org.unicef.gis.model.Report;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

public interface IUnicefGisStoreAdapter {

	public void saveReport(Context context, String description,
			Location location, Uri imageUri, List<String> tags);

	public Cursor getReportsCursor(Context context);

	public List<Report> getReports();

}