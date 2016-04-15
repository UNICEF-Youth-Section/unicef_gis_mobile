package org.unicef.gis.ui;

import java.util.List;

import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Report;

import android.os.AsyncTask;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

public class DeleteUploadedReportsTask extends AsyncTask<Void, Integer, Void> {
	
	private MyReportsActivity activity;

	public DeleteUploadedReportsTask(MyReportsActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			//First we get all the reports that will be deleted
			UnicefGisStore store = new UnicefGisStore(activity);
			List<Report> uploadedReports = null;

			uploadedReports = store.getUploadedReports();

			//Then we report the total amount of reports so that the UI provides better feedback
			int amountOfReportsToDelete = uploadedReports.size();
			publishProgress(amountOfReportsToDelete);

			//Then we delete one report at a time
			for (Report report : uploadedReports) {
				store.deleteReport(report);
				publishProgress(amountOfReportsToDelete, 1);
			}

			//Ensure that the phone's media gallery reflects the changes immediately
			Camera camera = new Camera(activity);
			camera.rescanMedia();
		} catch (CouchbaseLiteException e) {
			Log.e("DeletedUploadedRprtsTsk", "Database error when fetching uploaded reports", e);
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values.length == 1)
			activity.informReportsToDelete(values[0]);
		else
			activity.reportProgress();
	};
	
	@Override
	protected void onPostExecute(Void result) {
		activity.reportsDeleted();
	}

}
